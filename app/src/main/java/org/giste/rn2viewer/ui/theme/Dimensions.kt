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
    val paddingTiny: Dp,
    val paddingSmall: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp
)

val compactDimensions = AppDimensions(
    actionIconSize = 36.dp,
    buttonPadding = 1.dp,
    sectionBorder = 1.dp,
    paddingTiny = 2.dp,
    paddingSmall = 4.dp,
    paddingMedium = 8.dp,
    paddingLarge = 16.dp
)

val expandedDimensions = AppDimensions(
    actionIconSize = 48.dp,
    buttonPadding = 1.dp,
    sectionBorder = 1.dp,
    paddingTiny = 2.dp,
    paddingSmall = 4.dp,
    paddingMedium = 12.dp,
    paddingLarge = 24.dp
)

val LocalAppDimensions = staticCompositionLocalOf { compactDimensions }
