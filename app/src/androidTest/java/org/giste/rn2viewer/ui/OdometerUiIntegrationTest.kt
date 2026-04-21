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

package org.giste.rn2viewer.ui

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.giste.rn2viewer.MainActivity
import org.giste.rn2viewer.domain.repositories.OdometerRepository
import org.giste.rn2viewer.domain.repositories.RouteRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalTestApi::class)
@HiltAndroidTest
class OdometerUiIntegrationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var odometerRepository: OdometerRepository

    @Inject
    lateinit var routeRepository: RouteRepository

    @Before
    fun setup() {
        hiltRule.inject()
        runBlocking {
            odometerRepository.resetAllDistances()
            
            // Provide a raw JSON that the app can parse
            // Waypoint 1 to Waypoint 2 is ~1.11km
            val rawJson = """
                {
                  "route": {
                    "version": 1,
                    "name": "UI Test Route",
                    "description": "",
                    "startLocation": "",
                    "endLocation": "",
                    "currentStyle": "default",
                    "waypoints": [
                      {
                        "t_uuid": "1",
                        "waypointid": 1,
                        "lat": 40.0,
                        "lon": -3.0,
                        "ele": 0.0,
                        "show": true,
                        "tulip": { "elements": [] },
                        "notes": { "elements": [] }
                      },
                      {
                        "t_uuid": "2",
                        "waypointid": 2,
                        "lat": 40.01,
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
            routeRepository.saveRouteRaw(rawJson)
        }
    }

    @Test
    fun longClickWaypoint_updatesPartialOdometer() {
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
        
        // 5. Confirm
        composeTestRule.onNodeWithTag("ConfirmButton").performClick()

        // 6. Verify dialog is closed and odometer shows 5.20
        composeTestRule.onNodeWithTag("DialogHeader").assertDoesNotExist()
        val expectedPartial = String.format(Locale.getDefault(), "%.2f", 5.2)
        composeTestRule.onNodeWithTag("PartialOdometerValue", useUnmergedTree = true).assertTextEquals(expectedPartial)
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
        composeTestRule.onNodeWithTag("PartialOdometerValue", useUnmergedTree = true).assertTextEquals(zeroText)
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
        composeTestRule.onNodeWithTag("PartialOdometerValue", useUnmergedTree = true).assertTextEquals(incrementedText)
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
        composeTestRule.onNodeWithTag("PartialOdometerValue", useUnmergedTree = true).assertTextEquals(decrementedText)
    }

    @Test
    fun hardwareKey_scrollsRoadbookDown() {
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
        val settingsDesc = composeTestRule.activity.getString(org.giste.rn2viewer.R.string.action_settings)
        composeTestRule.onNodeWithContentDescription(settingsDesc).performClick()

        // 2. Verify Settings screen is displayed
        // SettingsScreen usually has a "Settings" title or similar.
        val settingsTitle = composeTestRule.activity.getString(org.giste.rn2viewer.R.string.settings_title)
        composeTestRule.onNodeWithText(settingsTitle).assertIsDisplayed()
    }
}
