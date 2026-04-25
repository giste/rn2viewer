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
import org.giste.rn2viewer.domain.repositories.LocalMapMetadataRepository
import org.giste.rn2viewer.domain.repositories.MapRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
        // Ensure we start with no maps for a clean test
        runBlocking {
            val maps = mapRepository.getDownloadedMaps().first()
            maps.forEach { mapRepository.deleteMap(it) }
            
            metadataRepository.getAllMetadata().first().forEach {
                metadataRepository.deleteMetadata(it.mapId)
            }
        }
    }

    @Test
    fun navigateToMapsTab_showsCatalog() {
        // 1. Wait for MainScreen to be ready and click Settings
        val settingsDesc = composeTestRule.activity.getString(R.string.action_settings)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithContentDescription(settingsDesc).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription(settingsDesc).performClick()

        // 2. Click on Maps tab
        composeTestRule.onNodeWithTag("SettingsTab_3").performClick()

        // 3. Verify catalog sections are visible
        composeTestRule.onNodeWithText("Spain").assertIsDisplayed()
        
        // Use resource for status label
        val availableStatus = composeTestRule.activity.getString(R.string.settings_maps_status_available)
        composeTestRule.onAllNodesWithText(availableStatus, substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun mapsTab_showsEmptyState_whenNoMapsDownloaded() {
        val settingsDesc = composeTestRule.activity.getString(R.string.action_settings)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithContentDescription(settingsDesc).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithContentDescription(settingsDesc).performClick()
        
        composeTestRule.onNodeWithTag("SettingsTab_3").performClick()

        val emptyMapsMsg = composeTestRule.activity.getString(R.string.settings_maps_no_installed)
        composeTestRule.onNodeWithText(emptyMapsMsg).assertIsDisplayed()
    }
}
