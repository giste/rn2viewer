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

package org.giste.rn2viewer.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adaptive dimensions for the application.
 */
@Immutable
data class AppDimensions(
    val actionIconSize: Dp,
    val buttonPadding: Dp,
    val sectionBorder: Dp,
    val dangerMediumThickness: Dp,
    val dangerHighThickness: Dp,
    val resetDividerThickness: Dp,
    val paddingMinimal: Dp,
    val paddingTiny: Dp,
    val paddingSmall: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp,
    val dialogButtonHeight: Dp,
    val numpadButtonHeight: Dp,
    val numpadButtonHeightLandscape: Dp,
    val cornerRadius: Dp
)

val compactDimensions = AppDimensions(
    actionIconSize = 36.dp,
    buttonPadding = 1.dp,
    sectionBorder = 1.dp,
    dangerMediumThickness = 6.dp,
    dangerHighThickness = 4.dp,
    resetDividerThickness = 2.dp,
    paddingMinimal = 1.dp,
    paddingTiny = 2.dp,
    paddingSmall = 4.dp,
    paddingMedium = 8.dp,
    paddingLarge = 16.dp,
    dialogButtonHeight = 56.dp,
    numpadButtonHeight = 64.dp,
    numpadButtonHeightLandscape = 48.dp,
    cornerRadius = 12.dp
)

val expandedDimensions = AppDimensions(
    actionIconSize = 48.dp,
    buttonPadding = 1.dp,
    sectionBorder = 1.dp,
    dangerMediumThickness = 9.dp,
    dangerHighThickness = 6.dp,
    resetDividerThickness = 3.dp,
    paddingMinimal = 1.dp,
    paddingTiny = 2.dp,
    paddingSmall = 4.dp,
    paddingMedium = 12.dp,
    paddingLarge = 24.dp,
    dialogButtonHeight = 72.dp,
    numpadButtonHeight = 80.dp,
    numpadButtonHeightLandscape = 64.dp,
    cornerRadius = 16.dp
)

val LocalAppDimensions = staticCompositionLocalOf { compactDimensions }
