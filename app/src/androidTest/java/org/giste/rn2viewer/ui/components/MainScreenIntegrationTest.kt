/*
 * Rn2 Viewer
 * Copyright (C) 2026  Giste
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.giste.rn2viewer.ui.components

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.swipeUp
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.giste.rn2viewer.MainActivity
import org.giste.rn2viewer.R
import org.giste.rn2viewer.domain.model.UserLocation
import org.giste.rn2viewer.domain.repositories.OdometerRepository
import org.giste.rn2viewer.domain.repositories.RouteRepository
import org.giste.rn2viewer.fakes.FakeLocationRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalTestApi::class)
@HiltAndroidTest
class MainScreenIntegrationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 3)
    val intentsRule = IntentsRule()

    @Inject
    lateinit var odometerRepository: OdometerRepository

    @Inject
    lateinit var routeRepository: RouteRepository

    @Inject
    lateinit var locationRepository: FakeLocationRepository

    @Before
    fun setup() {
        hiltRule.inject()
        runBlocking {
            odometerRepository.resetAllDistances()

            // Delete the roadbook file from disk to ensure a true "Empty" state
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val file = File(context.filesDir, "roadbook.json")
            if (file.exists()) file.delete()
        }
    }

    private fun setupDefaultRoute() = runBlocking {
        // Provide a raw JSON that the app can parse
        // Add more waypoints to test scrolling
        val waypointsJson = (1..10).joinToString(",") { i ->
            """
            {
              "t_uuid": "$i",
              "waypointid": $i,
              "lat": ${40.0 + i * 0.01},
              "lon": -3.0,
              "ele": 0.0,
              "show": true,
              "tulip": { "elements": [] },
              "notes": { "elements": [] }
            }
            """.trimIndent()
        }
        val rawJson = """
            {
              "route": {
                "version": 1,
                "name": "UI Test Route",
                "description": "",
                "startLocation": "",
                "endLocation": "",
                "currentStyle": "default",
                "waypoints": [$waypointsJson]
              }
            }
        """.trimIndent()
        routeRepository.saveRouteRaw(rawJson)
    }

    @Test
    fun longClickWaypoint_updatesPartialOdometer() {
        setupDefaultRoute()
        // 1. Wait for loading to finish and the route to load.
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("LoadingIndicator").fetchSemanticsNodes().isEmpty()
        }

        // 2. Wait for the waypoint 2 distance info to be displayed.
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("WaypointDistanceInfo_2", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Perform long click on the waypoint 2 distance info
        composeTestRule.onNodeWithTag("WaypointDistanceInfo_2", useUnmergedTree = true)
            .performScrollTo()
            .performTouchInput {
                longClick()
            }

        // 4. Verify the partial odometer shows 1.11
        val expectedValue = String.format(Locale.getDefault(), "%.2f", 1.11)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("PartialOdometerValue", useUnmergedTree = true)
                .fetchSemanticsNodes().any { node ->
                    node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == expectedValue } == true
                }
        }
        composeTestRule.onNodeWithTag("PartialOdometerValue", useUnmergedTree = true).assertTextEquals(expectedValue)
    }

    @Test
    fun longClickPartialOdometer_opensDialog_andUpdatesOnConfirm() {
        // 1. Wait for the initial screen to be ready
        val initialPartial = String.format(Locale.getDefault(), "%.2f", 0.0)
        composeTestRule.onNodeWithTag("PartialOdometerValue", useUnmergedTree = true).assertTextEquals(initialPartial)

        // 2. Long click on partial odometer to open dialog
        composeTestRule.onNodeWithTag("PartialOdometer").performTouchInput {
            longClick()
        }

        // 3. Verify dialog is displayed
        composeTestRule.onNodeWithTag("DialogHeader").assertIsDisplayed()

        // 4. Enter "5.2"
        composeTestRule.onNodeWithTag("NumpadButton_5").performClick()
        composeTestRule.onNodeWithTag("NumpadButton_.").performClick()
        composeTestRule.onNodeWithTag("NumpadButton_2").performClick()

        // Verify input is reflected in DisplayArea
        composeTestRule.onNodeWithTag("DisplayArea").onChild().assertTextEquals("5.2")

        // 5. Confirm
        composeTestRule.onNodeWithTag("ConfirmButton").performClick()

        // 6. Verify dialog is closed and odometer shows 5.20
        composeTestRule.onNodeWithTag("DialogHeader").assertDoesNotExist()
        val expectedPartial = String.format(Locale.getDefault(), "%.2f", 5.2)

        // Wait for the repository update to propagate back to the UI
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(
                hasTestTag("PartialOdometerValue") and hasText(
                    expectedPartial
                ), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun hardwareKey_resetsPartialOdometer() {
        // 1. Set some distance first manually in repository to see it reset
        runBlocking {
            odometerRepository.updatePartialDistance(5000.0)
        }

        // 2. Verify it's displayed (5.00 km)
        val distanceText = String.format(Locale.getDefault(), "%.2f", 5.0)
        composeTestRule.onNodeWithTag("PartialOdometerValue", useUnmergedTree = true).assertTextEquals(distanceText)

        // 3. Press F6 (Reset) key
        // We use performKeyInput on the focused node. MainScreen box is focusable and requests focus on start.
        composeTestRule.onNodeWithTag("MainScreen").performKeyInput {
            pressKey(Key.F6)
        }

        // 4. Verify it's reset to 0.00
        val zeroText = String.format(Locale.getDefault(), "%.2f", 0.0)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasTestTag("PartialOdometerValue") and hasText(zeroText), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun hardwareKey_incrementsPartialOdometer() {
        // 1. Initial state 0.00
        val zeroText = String.format(Locale.getDefault(), "%.2f", 0.0)
        composeTestRule.onNodeWithTag("PartialOdometerValue", useUnmergedTree = true).assertTextEquals(zeroText)

        // 2. Press VolumeUp (mapped to increment)
        composeTestRule.onNodeWithTag("MainScreen").performKeyInput {
            pressKey(Key.VolumeUp)
        }

        // 3. Verify it's incremented by 10m (0.01 km)
        val incrementedText = String.format(Locale.getDefault(), "%.2f", 0.01)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasTestTag("PartialOdometerValue") and hasText(incrementedText), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun hardwareKey_decrementsPartialOdometer() {
        // 1. Set some distance first
        runBlocking {
            odometerRepository.updatePartialDistance(100.0)
        }
        val initialText = String.format(Locale.getDefault(), "%.2f", 0.1)
        composeTestRule.onNodeWithTag("PartialOdometerValue", useUnmergedTree = true).assertTextEquals(initialText)

        // 2. Press VolumeDown (mapped to decrement)
        composeTestRule.onNodeWithTag("MainScreen").performKeyInput {
            pressKey(Key.VolumeDown)
        }

        // 3. Verify it's decremented by 10m
        val decrementedText = String.format(Locale.getDefault(), "%.2f", 0.09)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasTestTag("PartialOdometerValue") and hasText(decrementedText), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun hardwareKey_scrollsRoadbookDown() {
        setupDefaultRoute()
        // 1. Wait for loading to finish
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("LoadingIndicator").fetchSemanticsNodes().isEmpty()
        }

        // 2. Ensure Waypoint 1 is visible
        composeTestRule.onNodeWithTag("WaypointDistanceInfo_1", useUnmergedTree = true).assertIsDisplayed()

        // 3. Press DirectionUp (often mapped to "Next" in remotes)
        composeTestRule.onNodeWithTag("MainScreen").performKeyInput {
            pressKey(Key.DirectionUp)
        }

        // 4. Verify it doesn't crash and remains stable
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("MainScreen").assertIsDisplayed()
    }

    @Test
    fun settingsButton_navigatesToSettings() {
        // 1. Click settings icon (Search for content description if possible, or use tag if added)
        // MainScreen uses Icons.Default.Settings. I'll use the content description from strings.xml
        val settingsDesc = composeTestRule.activity.getString(R.string.action_settings)
        composeTestRule.onNodeWithContentDescription(settingsDesc).performClick()

        // 2. Verify Settings screen is displayed
        // SettingsScreen usually has a "Settings" title or similar.
        val settingsTitle = composeTestRule.activity.getString(R.string.settings_title)
        composeTestRule.onNodeWithText(settingsTitle).assertIsDisplayed()
    }

    @Test
    fun scrollPosition_isPersisted() {
        setupDefaultRoute()
        // 1. Wait for route to load
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("LoadingIndicator").fetchSemanticsNodes().isEmpty()
        }

        // 2. Initial position check (Waypoint 1 is visible)
        composeTestRule.onNodeWithTag("WaypointDistanceInfo_1", useUnmergedTree = true).assertIsDisplayed()

        // 3. Perform a manual swipe to ensure listState.isScrollInProgress becomes true and then false
        composeTestRule.onNodeWithTag("MainScreen").performTouchInput {
            swipeUp(durationMillis = 500)
        }

        // 4. Wait for the scroll to finish and the LaunchedEffect to trigger
        composeTestRule.waitForIdle()

        // 5. Verify the repository has the updated position.
        // We expect index to be >= 1 after a significant swipe up
        composeTestRule.waitUntil(5000) {
            runBlocking {
                val pos = routeRepository.getSavedScrollPosition().first()
                pos.index >= 1
            }
        }
    }

    @Test
    fun hardwareKey_scrollsRoadbookUp() {
        setupDefaultRoute()
        // 1. Wait for loading to finish
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("LoadingIndicator").fetchSemanticsNodes().isEmpty()
        }

        // 2. Scroll down first so we can scroll back up
        composeTestRule.onNodeWithTag("MainScreen").performTouchInput {
            swipeUp(durationMillis = 500)
        }
        composeTestRule.waitForIdle()

        // 3. Press DirectionDown (often mapped to "Previous/Up" in remotes)
        composeTestRule.onNodeWithTag("MainScreen").performKeyInput {
            pressKey(Key.DirectionDown)
        }

        // 4. Verify it doesn't crash
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("MainScreen").assertIsDisplayed()
    }

    @Test
    fun importWorkflow_updatesUiWithNewRoute() {
        // 1. Prepare a fake .rn2 file in the cache directory
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fileName = "test_import.rn2"
        // Ensure the JSON matches the schema exactly to avoid parsing errors
        val fileContent = """
            {
                "route": {
                    "version": 4,
                    "name": "Imported Route",
                    "waypoints": [
                        {
                            "t_uuid": "uuid-1",
                            "waypointid": 1,
                            "lat": 40.0,
                            "lon": -3.0,
                            "ele": 0.0,
                            "show": true,
                            "tulip": { "elements": [] },
                            "notes": { "elements": [] }
                        }
                    ]
                }
            }
        """.trimIndent()

        val file = File(context.cacheDir, fileName)
        file.writeText(fileContent)
        val uri = Uri.fromFile(file)

        // 2. Mock the Intent result for OpenDocument
        val resultData = Intent()
        resultData.data = uri
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(result)

        // 3. Click the Import button in the UI
        val importDesc = composeTestRule.activity.getString(R.string.action_import)
        composeTestRule.onNodeWithContentDescription(importDesc).performClick()

        // 4. Verify the new route data is displayed
        // We look for Waypoint 1 which is in our "Imported Route"
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithTag("WaypointDistanceInfo_1", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("WaypointDistanceInfo_1", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() {
        // 1. Save an invalid JSON to trigger a parsing error in the Mapper/UseCase
        runBlocking {
            routeRepository.saveRouteRaw("{ invalid json }")
        }

        // 2. Wait for the error message to appear
        // The UI should show "Error: ..."
        val errorPrefix = composeTestRule.activity.getString(R.string.main_error_prefix, "")
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText(errorPrefix, substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText(errorPrefix, substring = true).assertIsDisplayed()
    }

    @Test
    fun gpsLocationUpdate_updatesOdometerValues() {
        // 1. Initial state: 0.00
        val zeroText = String.format(Locale.getDefault(), "%.2f", 0.0)
        composeTestRule.onNodeWithTag("PartialOdometerValue", useUnmergedTree = true).assertTextEquals(zeroText)

        // 2. Emit first location (Reference point)
        runBlocking {
            locationRepository.emit(
                UserLocation(
                    latitude = 40.0,
                    longitude = -3.0,
                    altitude = 0.0,
                    accuracy = 5f,
                    verticalAccuracy = 5f,
                    speed = 0f,
                    bearing = 0f,
                    time = System.currentTimeMillis()
                )
            )
        }

        // 3. Emit second location (~111 meters away: 0.001 degree lat)
        runBlocking {
            locationRepository.emit(
                UserLocation(
                    latitude = 40.001,
                    longitude = -3.0,
                    altitude = 0.0,
                    accuracy = 5f,
                    verticalAccuracy = 5f,
                    speed = 0f,
                    bearing = 0f,
                    time = System.currentTimeMillis()
                )
            )
        }

        // 4. Verify odometer updates (~0.11 km)
        // Distance is ~111.19m -> 0.11km
        val expectedText = String.format(Locale.getDefault(), "%.2f", 0.11)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodes(hasTestTag("PartialOdometerValue") and hasText(expectedText), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}