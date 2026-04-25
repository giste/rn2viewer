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
fun Rn2Icons.aboveBridge(surface: Color, onSurface: Color): ImageVector {
    return ImageVector.Builder(
        name = "AboveBridge",
        defaultWidth = 128.dp,
        defaultHeight = 128.dp,
        viewportWidth = 128f,
        viewportHeight = 128f
    ).apply {
        // M86 17 H42 V111 H86 V17Z
        path(
            fill = SolidColor(surface),
        ) {
            // M 86 17
            moveTo(x = 86.0f, y = 17.0f)
            // H 42
            horizontalLineTo(x = 42.0f)
            // V 111
            verticalLineTo(y = 111.0f)
            // H 86
            horizontalLineTo(x = 86.0f)
            // V 17z
            verticalLineTo(y = 17.0f)
            close()
        }
        // M102 3 L86 19 V109 L102 125
        path(
            stroke = SolidColor(onSurface),
            strokeLineWidth = 8.0f,
        ) {
            // M 102 3
            moveTo(x = 102.0f, y = 3.0f)
            // L 86 19
            lineTo(x = 86.0f, y = 19.0f)
            // V 109
            verticalLineTo(y = 109.0f)
            // L 102 125
            lineTo(x = 102.0f, y = 125.0f)
        }
        // M26 3 L42 19 V109 L26 125
        path(
            stroke = SolidColor(onSurface),
            strokeLineWidth = 8.0f,
        ) {
            // M 26 3
            moveTo(x = 26.0f, y = 3.0f)
            // L 42 19
            lineTo(x = 42.0f, y = 19.0f)
            // V 109
            verticalLineTo(y = 109.0f)
            // L 26 125
            lineTo(x = 26.0f, y = 125.0f)
        }
    }.build()
}
