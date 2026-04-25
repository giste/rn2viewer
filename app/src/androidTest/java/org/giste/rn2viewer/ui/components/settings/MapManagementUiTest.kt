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
 * along with this program.  See <https://www.gnu.org/licenses/>.
 */

package org.giste.rn2viewer.ui.components.settings

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.giste.rn2viewer.MainActivity
import org.giste.rn2viewer.R
import org.giste.rn2viewer.domain.model.LocalMapMetadata
import org.giste.rn2viewer.domain.repositories.LocalMapMetadataRepository
import org.giste.rn2viewer.domain.repositories.MapRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
class MapManagementUiTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var mapRepository: MapRepository

    @Inject
    lateinit var metadataRepository: LocalMapMetadataRepository

    @Before
    fun setup() {
        hiltRule.inject()
        
        // Ensure we start with a clean state by clearing files and metadata directly
        val context = composeTestRule.activity
        val mapsDir = File(context.filesDir, "maps")
        if (mapsDir.exists()) {
            mapsDir.listFiles()?.forEach { it.delete() }
        }
        
        runBlocking {
            metadataRepository.getAllMetadata().first().forEach {
                metadataRepository.deleteMetadata(it.mapId)
            }
            // Force repository to see the empty disk
            mapRepository.refreshDownloadedMaps()
        }
    }

    @Test
    fun navigateToMapsTab_showsCatalog() {
        openMapsTab()

        // Verify catalog sections are visible
        composeTestRule.onAllNodesWithText("Spain").onFirst().assertIsDisplayed()
        
        // Use resource for status label
        val availableStatus = composeTestRule.activity.getString(R.string.settings_maps_status_available)
        composeTestRule.onAllNodesWithText(availableStatus, substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun outdatedMap_showsWarning() {
        // Given a map file that is NOT in the manifest
        val context = composeTestRule.activity
        val mapsDir = File(context.filesDir, "maps")
        if (!mapsDir.exists()) mapsDir.mkdirs()
        val legacyFile = File(mapsDir, "legacy_map.map")
        legacyFile.writeText("dummy content")

        // Refresh repository to detect the new file
        runBlocking { mapRepository.refreshDownloadedMaps() }

        openMapsTab()

        // Verify "Legacy" status is shown
        val outdatedStatus = context.getString(R.string.settings_maps_status_outdated)
        composeTestRule.onNodeWithText("legacy_map.map").assertIsDisplayed()
        composeTestRule.onNodeWithText(outdatedStatus, substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Not on server").assertIsDisplayed()
    }

    @Test
    fun deleteMap_removesFileAndMetadata() {
        // Given an installed map (Spain) simulated manually
        val context = composeTestRule.activity
        val mapsDir = File(context.filesDir, "maps")
        if (!mapsDir.exists()) mapsDir.mkdirs()
        val spainFile = File(mapsDir, "spain.map")
        spainFile.writeText("dummy")
        
        runBlocking {
            mapRepository.refreshDownloadedMaps()
            metadataRepository.saveMetadata(
                LocalMapMetadata(
                    mapId = "europe_spain",
                    fileName = "spain.map",
                    serverLastModified = 1710000000000L,
                    size = 5
                )
            )
        }

        openMapsTab()

        // Verify it is in "Installed" section
        composeTestRule.onAllNodesWithText("Spain").onFirst().assertIsDisplayed()
        
        // Click Delete - We use a more specific matcher to avoid ambiguity with catalog items
        // We find the 'Spain' text first and then its sibling/parent button if possible
        // But since we have 2 'Spain', we take the first one (Installed)
        composeTestRule.onAllNodes(hasContentDescription("Delete")).onFirst().performClick()

        // Verify it's gone from "Installed" (shows "No maps installed" message)
        val emptyMsg = context.getString(R.string.settings_maps_no_installed)
        composeTestRule.onNodeWithText(emptyMsg).assertIsDisplayed()
        
        // Still in "Catalog" (Available)
        composeTestRule.onAllNodesWithText("Spain").onFirst().assertIsDisplayed()
        val availableStatus = context.getString(R.string.settings_maps_status_available)
        composeTestRule.onAllNodesWithText(availableStatus, substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun manualCopyMap_showsAsInstalled_withoutUpdateIcon() {
        // Given a file on disk but NO database entry
        val context = composeTestRule.activity
        val mapsDir = File(context.filesDir, "maps")
        if (!mapsDir.exists()) mapsDir.mkdirs()
        val spainFile = File(mapsDir, "spain.map")
        spainFile.writeText("dummy")
        
        runBlocking { mapRepository.refreshDownloadedMaps() }

        openMapsTab()

        // Verify it shows as Installed
        composeTestRule.onAllNodesWithText("Spain").onFirst().assertIsDisplayed()
        val installedStatus = context.getString(R.string.settings_maps_status_installed)
        composeTestRule.onAllNodesWithText(installedStatus, substring = true).onFirst().assertIsDisplayed()
        
        // Verify NO "Update available" icon is shown (because we don't have metadata to compare)
        composeTestRule.onNodeWithContentDescription("Update available").assertDoesNotExist()
    }

    private fun openMapsTab() {
        val settingsDesc = composeTestRule.activity.getString(R.string.action_settings)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithContentDescription(settingsDesc).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription(settingsDesc).performClick()
        
        composeTestRule.onNodeWithTag("SettingsTab_3").performClick()
    }
}
