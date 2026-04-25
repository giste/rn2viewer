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

package org.giste.rn2viewer.ui.icons.landmarks

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.giste.rn2viewer.ui.icons.Rn2Icons

/**
 * Compose-native version of ic_landmark_under_bridge.xml
 * Supports dynamic theme colors for both fill and strokes.
 */
@Suppress("UnusedReceiverParameter")
fun Rn2Icons.underBridge(surface: Color, onSurface: Color): ImageVector {
    return ImageVector.Builder(
        name = "UnderBridge",
        defaultWidth = 128.dp,
        defaultHeight = 128.dp,
        viewportWidth = 128f,
        viewportHeight = 128f
    ).apply {
        // <rect width="94" height="44" x="17" y="42" fill="#FFFFFFFF" />
        path(
            fill = SolidColor(surface),
        ) {
            // M 17 42
            moveTo(x = 17.0f, y = 42.0f)
            // h 94
            horizontalLineToRelative(dx = 94.0f)
            // v 44
            verticalLineToRelative(dy = 44.0f)
            // h -94z
            horizontalLineToRelative(dx = -94.0f)
            close()
        }
        // M3 26 L19 42 H109 L125 26
        path(
            stroke = SolidColor(onSurface),
            strokeLineWidth = 8.0f,
        ) {
            // M 3 26
            moveTo(x = 3.0f, y = 26.0f)
            // L 19 42
            lineTo(x = 19.0f, y = 42.0f)
            // H 109
            horizontalLineTo(x = 109.0f)
            // L 125 26
            lineTo(x = 125.0f, y = 26.0f)
        }
        // M3 102 L19 86 H109 L125 102
        path(
            stroke = SolidColor(onSurface),
            strokeLineWidth = 8.0f,
        ) {
            // M 3 102
            moveTo(x = 3.0f, y = 102.0f)
            // L 19 86
            lineTo(x = 19.0f, y = 86.0f)
            // H 109
            horizontalLineTo(x = 109.0f)
            // L 125 102
            lineTo(x = 125.0f, y = 102.0f)
        }
    }.build()
}
