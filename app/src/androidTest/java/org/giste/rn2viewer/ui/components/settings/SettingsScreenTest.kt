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

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.giste.rn2viewer.R
import org.giste.rn2viewer.domain.model.settings.AppOrientation
import org.giste.rn2viewer.domain.model.settings.AppSettings
import org.giste.rn2viewer.domain.model.settings.AppTheme
import org.giste.rn2viewer.ui.viewmodel.MapsUiState
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun themeSelection_updatesCallback() {
        var selectedTheme: AppTheme? = null
        val settings = AppSettings(AppTheme.FOLLOW_SYSTEM, AppOrientation.FOLLOW_SYSTEM)

        composeTestRule.setContent {
            SettingsScreenContent(
                settings = settings,
                mapsUiState = MapsUiState(),
                snackbarHostState = SnackbarHostState(),
                onBackClick = {},
                onThemeSelected = { selectedTheme = it },
                onOrientationSelected = {},
                onShortDistanceThresholdChanged = {},
                onRestoreRoadbookDefaults = {},
                onOdometerSpeedThresholdChanged = {},
                onOdometerMinAccuracyChanged = {},
                onOdometerMinVerticalAccuracyChanged = {},
                onRestoreOdometerDefaults = {},
                onDownloadMap = {},
                onDeleteMap = {}
            )
        }

        val themeSystem = context.getString(R.string.settings_theme_system)
        val themeDark = context.getString(R.string.settings_theme_dark)
        
        composeTestRule.onNode(hasText(themeSystem).and(hasAnyAncestor(hasTestTag("SettingsSectionTheme"))))
            .assertIsSelected()
        
        composeTestRule.onNode(hasText(themeDark).and(hasAnyAncestor(hasTestTag("SettingsSectionTheme"))))
            .performClick()
        
        assert(selectedTheme == AppTheme.DARK)
    }

    @Test
    fun orientationSelection_updatesCallback() {
        var selectedOrientation: AppOrientation? = null
        val settings = AppSettings(AppTheme.FOLLOW_SYSTEM, AppOrientation.FOLLOW_SYSTEM)

        composeTestRule.setContent {
            SettingsScreenContent(
                settings = settings,
                mapsUiState = MapsUiState(),
                snackbarHostState = SnackbarHostState(),
                onBackClick = {},
                onThemeSelected = {},
                onOrientationSelected = { selectedOrientation = it },
                onShortDistanceThresholdChanged = {},
                onRestoreRoadbookDefaults = {},
                onOdometerSpeedThresholdChanged = {},
                onOdometerMinAccuracyChanged = {},
                onOdometerMinVerticalAccuracyChanged = {},
                onRestoreOdometerDefaults = {},
                onDownloadMap = {},
                onDeleteMap = {}
            )
        }

        val orientSystem = context.getString(R.string.settings_orientation_system)
        val orientVertical = context.getString(R.string.settings_orientation_vertical)

        composeTestRule.onNode(hasText(orientSystem).and(hasAnyAncestor(hasTestTag("SettingsSectionOrientation"))))
            .assertIsSelected()
        
        composeTestRule.onNode(hasText(orientVertical).and(hasAnyAncestor(hasTestTag("SettingsSectionOrientation"))))
            .performClick()

        assert(selectedOrientation == AppOrientation.VERTICAL)
    }
}
