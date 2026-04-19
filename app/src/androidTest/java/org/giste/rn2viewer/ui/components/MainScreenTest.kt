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

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.giste.rn2viewer.domain.model.Odometer
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.ui.viewmodel.MainUiState
import org.giste.rn2viewer.ui.viewmodel.RoadbookUiState
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Test
    fun roadbookEmpty_displaysNoRouteMessage() {
        val uiState = MainUiState(
            roadbook = RoadbookUiState.Empty,
            odometer = Odometer(0.0, 0.0)
        )

        composeTestRule.setContent {
            MainContent(
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp)),
                uiState = uiState,
                listState = androidx.compose.foundation.lazy.rememberLazyListState(),
                onImportClick = {},
                onSetPartialClick = {},
                onLongClickPartial = {},
                onWaypointVisible = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("No route loaded").assertIsDisplayed()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Test
    fun roadbookSuccess_displaysOdometerValues() {
        val uiState = MainUiState(
            roadbook = RoadbookUiState.Success(
                route = Route(name = "Test Route", waypoints = emptyList())
            ),
            odometer = Odometer(1200.0, 500.0)
        )

        composeTestRule.setContent {
            MainContent(
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp)),
                uiState = uiState,
                listState = androidx.compose.foundation.lazy.rememberLazyListState(),
                onImportClick = {},
                onSetPartialClick = {},
                onLongClickPartial = {},
                onWaypointVisible = { _, _ -> }
            )
        }

        // Format expectations using default locale to match production behavior
        val expectedTotal = String.format(Locale.getDefault(), "%.1f", 1200.0 / 1000.0)
        val expectedPartial = String.format(Locale.getDefault(), "%.2f", 500.0 / 1000.0)

        composeTestRule.onNodeWithText(expectedTotal).assertIsDisplayed()
        composeTestRule.onNodeWithText(expectedPartial).assertIsDisplayed()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Test
    fun longClickOnPartialDistance_triggersOnLongClickPartial() {
        var longClickTriggered = false
        val uiState = MainUiState(
            roadbook = RoadbookUiState.Empty,
            odometer = Odometer(0.0, 500.0)
        )
        val expectedPartial = String.format(Locale.getDefault(), "%.2f", 500.0 / 1000.0)

        composeTestRule.setContent {
            MainContent(
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp)),
                uiState = uiState,
                listState = androidx.compose.foundation.lazy.rememberLazyListState(),
                onImportClick = {},
                onSetPartialClick = {},
                onLongClickPartial = { longClickTriggered = true },
                onWaypointVisible = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText(expectedPartial).performTouchInput {
            longClick()
        }

        assert(longClickTriggered)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Test
    fun clickOnSearchIcon_triggersOnImportClick() {
        var importClicked = false
        val uiState = MainUiState(
            roadbook = RoadbookUiState.Empty,
            odometer = Odometer(0.0, 0.0)
        )

        composeTestRule.setContent {
            MainContent(
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp)),
                uiState = uiState,
                listState = androidx.compose.foundation.lazy.rememberLazyListState(),
                onImportClick = { importClicked = true },
                onSetPartialClick = {},
                onLongClickPartial = {},
                onWaypointVisible = { _, _ -> }
            )
        }

        // We use content description to find the icon
        composeTestRule.onNodeWithContentDescription("Import").performClick()

        assert(importClicked)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Test
    fun roadbookLoading_displaysLoadingIndicator() {
        val uiState = MainUiState(
            roadbook = RoadbookUiState.Loading,
            odometer = Odometer(0.0, 0.0)
        )

        composeTestRule.setContent {
            MainContent(
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp)),
                uiState = uiState,
                listState = androidx.compose.foundation.lazy.rememberLazyListState(),
                onImportClick = {},
                onSetPartialClick = {},
                onLongClickPartial = {},
                onWaypointVisible = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Test
    fun roadbookError_displaysErrorMessage() {
        val errorMessage = "Failed to load route"
        val uiState = MainUiState(
            roadbook = RoadbookUiState.Error(errorMessage),
            odometer = Odometer(0.0, 0.0)
        )

        composeTestRule.setContent {
            MainContent(
                windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp)),
                uiState = uiState,
                listState = androidx.compose.foundation.lazy.rememberLazyListState(),
                onImportClick = {},
                onSetPartialClick = {},
                onLongClickPartial = {},
                onWaypointVisible = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Error: $errorMessage").assertIsDisplayed()
    }
}
