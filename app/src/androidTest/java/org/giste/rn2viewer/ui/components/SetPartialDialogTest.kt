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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class SetPartialDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val portraitSize = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp))
    private val landscapeSize = WindowSizeClass.calculateFromSize(DpSize(891.dp, 411.dp))

    @Test
    fun shouldDisplayTitleAndButtons() {
        composeTestRule.setContent {
            Rn2ViewerTheme(windowSizeClass = portraitSize) {
                SetPartialPortrait(
                    input = "",
                    onInputChanged = {},
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("DialogHeader").assertIsDisplayed()
        composeTestRule.onNodeWithTag("CancelButton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ConfirmButton").assertIsDisplayed()
    }

    @Test
    fun numpadShouldUpdateDisplay() {
        var inputState by mutableStateOf("")
        composeTestRule.setContent {
            Rn2ViewerTheme(windowSizeClass = portraitSize) {
                SetPartialPortrait(
                    input = inputState,
                    onInputChanged = { inputState = it },
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        // Initially shows 0.00
        composeTestRule.onNode(hasTestTag("DisplayArea") and hasAnyChild(hasText("0.00"))).assertIsDisplayed()

        // Tap numbers via TestTag
        composeTestRule.onNodeWithTag("NumpadButton_1").performClick()
        composeTestRule.onNodeWithTag("NumpadButton_2").performClick()
        
        // Verify DisplayArea contains "12"
        composeTestRule.onNode(hasTestTag("DisplayArea") and hasAnyChild(hasText("12"))).assertIsDisplayed()
        
        composeTestRule.onNodeWithTag("NumpadButton_.").performClick()
        composeTestRule.onNodeWithTag("NumpadButton_3").performClick()

        composeTestRule.onNode(hasTestTag("DisplayArea") and hasAnyChild(hasText("12.3"))).assertIsDisplayed()
    }

    @Test
    fun delButtonShouldRemoveLastCharacter() {
        var inputState by mutableStateOf("12")
        composeTestRule.setContent {
            Rn2ViewerTheme(windowSizeClass = portraitSize) {
                SetPartialPortrait(
                    input = inputState,
                    onInputChanged = { inputState = it },
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNode(hasTestTag("DisplayArea") and hasAnyChild(hasText("12"))).assertIsDisplayed()

        composeTestRule.onNodeWithTag("NumpadButton_DEL").performClick()
        
        composeTestRule.onNode(hasTestTag("DisplayArea") and hasAnyChild(hasText("1"))).assertIsDisplayed()
    }

    @Test
    fun confirmButtonShouldReturnDistanceInMeters() {
        var confirmedValue: Double? = null
        var inputState by mutableStateOf("5.2")
        composeTestRule.setContent {
            Rn2ViewerTheme(windowSizeClass = portraitSize) {
                SetPartialPortrait(
                    input = inputState,
                    onInputChanged = { inputState = it },
                    onDismiss = {},
                    onConfirm = { confirmedValue = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("ConfirmButton").performClick()

        assert(confirmedValue == 5200.0)
    }

    @Test
    fun dismissButtonShouldTriggerOnDismiss() {
        var dismissed = false
        composeTestRule.setContent {
            Rn2ViewerTheme(windowSizeClass = portraitSize) {
                SetPartialPortrait(
                    input = "",
                    onInputChanged = {},
                    onDismiss = { dismissed = true },
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("CancelButton").performClick()

        assert(dismissed)
    }

    @Test
    fun landscapeLayoutShouldDisplayCorrectElements() {
        composeTestRule.setContent {
            Rn2ViewerTheme(windowSizeClass = landscapeSize) {
                SetPartialLandscape(
                    input = "1",
                    onInputChanged = {},
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("DialogHeader").assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("DisplayArea") and hasAnyChild(hasText("1"))).assertIsDisplayed()
        composeTestRule.onNodeWithTag("ConfirmButton").assertIsDisplayed()
    }
}
