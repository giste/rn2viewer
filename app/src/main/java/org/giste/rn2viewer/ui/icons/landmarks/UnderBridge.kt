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

package org.giste.rn2viewer.ui.icons.landmarks

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.giste.rn2viewer.ui.icons.Rn2Icons

/**
 * Compose-native version of ic_landmark_under_bridge.xml
 * Supports dynamic theme colors for both fill and strokes.
 * Coordinates are flattened to 128x128 grid.
 */
fun Rn2Icons.underBridge(surface: Color, onSurface: Color): ImageVector {
    return ImageVector.Builder(
        name = "UnderBridge",
        defaultWidth = 128.dp,
        defaultHeight = 128.dp,
        viewportWidth = 128f,
        viewportHeight = 128f
    ).apply {
        // Background fill
        path(fill = SolidColor(surface)) {
            moveTo(109.38f, 45.38f)
            horizontalLineTo(18.04f)
            verticalLineTo(81.45f)
            horizontalLineTo(109.38f)
            verticalLineTo(45.38f)
            close()
        }
        // Top horizontal stroke
        path(
            stroke = SolidColor(onSurface),
            strokeLineWidth = 5.82f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(16.49f, 45.71f)
            lineTo(112.03f, 45.97f)
        }
        // Bottom horizontal stroke
        path(
            stroke = SolidColor(onSurface),
            strokeLineWidth = 5.82f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(16.49f, 81.45f)
            lineTo(112.03f, 81.71f)
        }
        // Diagonal top-left stroke
        path(
            stroke = SolidColor(onSurface),
            strokeLineWidth = 5.82f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(17.97f, 46.50f)
            lineTo(2.01f, 30.52f)
        }
        // Diagonal bottom-right stroke
        path(
            stroke = SolidColor(onSurface),
            strokeLineWidth = 5.82f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(125.46f, 97.43f)
            lineTo(109.51f, 81.45f)
        }
        // Diagonal bottom-left stroke
        path(
            stroke = SolidColor(onSurface),
            strokeLineWidth = 5.82f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(17.97f, 81.45f)
            lineTo(2.01f, 97.43f)
        }
        // Diagonal top-right stroke
        path(
            stroke = SolidColor(onSurface),
            strokeLineWidth = 5.82f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter
        ) {
            moveTo(125.47f, 30.87f)
            lineTo(109.51f, 46.85f)
        }
    }.build()
}
