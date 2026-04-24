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

package org.giste.rn2viewer.ui.icons.cross

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.giste.rn2viewer.ui.icons.Rn2Icons

/**
 * Compose-native version of ic_cross_reset_distance.xml
 * Supports dynamic theme colors.
 */
fun Rn2Icons.resetDistance(surface: Color, onSurface: Color): ImageVector {
    return ImageVector.Builder(
        name = "ResetDistance",
        defaultWidth = 128.dp,
        defaultHeight = 128.dp,
        viewportWidth = 128f,
        viewportHeight = 128f
    ).apply {
        val contentBrush = SolidColor(onSurface)

        // Background rectangle
        path(fill = SolidColor(surface)) {
            moveTo(0f, 0f)
            horizontalLineToRelative(128f)
            verticalLineToRelative(128f)
            horizontalLineToRelative(-128f)
            close()
        }

        // Path 1 (Box)
        path(fill = contentBrush) {
            moveTo(0f, 28f)
            horizontalLineTo(29f)
            verticalLineTo(68f)
            horizontalLineTo(0f)
            verticalLineTo(28f)
            close()
        }

        // Path 2 (Large O shape and hole)
        path(fill = SolidColor(surface)) {
            moveTo(14.4957f, 60.5114f)
            curveTo(12.5412f, 60.5038f, 10.8594f, 60.0227f, 9.45028f, 59.0682f)
            curveTo(8.04877f, 58.1136f, 6.96922f, 56.7311f, 6.21165f, 54.9205f)
            curveTo(5.46165f, 53.1098f, 5.09044f, 50.9318f, 5.09801f, 48.3864f)
            curveTo(5.09801f, 45.8485f, 5.47301f, 43.6856f, 6.22301f, 41.8977f)
            curveTo(6.98059f, 40.1098f, 8.06013f, 38.75f, 9.46165f, 37.8182f)
            curveTo(10.8707f, 36.8788f, 12.5488f, 36.4091f, 14.4957f, 36.4091f)
            curveTo(16.4427f, 36.4091f, 18.117f, 36.8788f, 19.5185f, 37.8182f)
            curveTo(20.9276f, 38.7576f, 22.0109f, 40.1212f, 22.7685f, 41.9091f)
            curveTo(23.526f, 43.6894f, 23.901f, 45.8485f, 23.8935f, 48.3864f)
            curveTo(23.8935f, 50.9394f, 23.5147f, 53.1212f, 22.7571f, 54.9318f)
            curveTo(22.0071f, 56.7424f, 20.9313f, 58.125f, 19.5298f, 59.0795f)
            curveTo(18.1283f, 60.0341f, 16.4503f, 60.5114f, 14.4957f, 60.5114f)
            close()
            moveTo(14.4957f, 56.4318f)
            curveTo(15.8291f, 56.4318f, 16.8935f, 55.7614f, 17.6889f, 54.4205f)
            curveTo(18.4844f, 53.0795f, 18.8783f, 51.0682f, 18.8707f, 48.3864f)
            curveTo(18.8707f, 46.6212f, 18.6889f, 45.1515f, 18.3253f, 43.9773f)
            curveTo(17.9692f, 42.803f, 17.4616f, 41.9205f, 16.8026f, 41.3295f)
            curveTo(16.151f, 40.7386f, 15.3821f, 40.4432f, 14.4957f, 40.4432f)
            curveTo(13.17f, 40.4432f, 12.1094f, 41.1061f, 11.3139f, 42.4318f)
            curveTo(10.5185f, 43.7576f, 10.117f, 45.7424f, 10.1094f, 48.3864f)
            curveTo(10.1094f, 50.1742f, 10.2874f, 51.6667f, 10.6435f, 52.8636f)
            curveTo(11.0071f, 54.053f, 11.5185f, 54.947f, 12.1776f, 55.5455f)
            curveTo(12.8366f, 56.1364f, 13.6094f, 56.4318f, 14.4957f, 56.4318f)
            close()
        }

        // Path 3 (Right box)
        path(fill = contentBrush) {
            moveTo(99f, 28f)
            horizontalLineTo(128f)
            verticalLineTo(68f)
            horizontalLineTo(99f)
            verticalLineTo(28f)
            close()
        }

        // Path 4 (Another O shape)
        path(fill = SolidColor(surface)) {
            moveTo(113.496f, 60.5114f)
            curveTo(111.541f, 60.5038f, 109.859f, 60.0227f, 108.45f, 59.0682f)
            curveTo(107.049f, 58.1136f, 105.969f, 56.7311f, 105.212f, 54.9205f)
            curveTo(104.462f, 53.1098f, 104.09f, 50.9318f, 104.098f, 48.3864f)
            curveTo(104.098f, 45.8485f, 104.473f, 43.6856f, 105.223f, 41.8977f)
            curveTo(105.981f, 40.1098f, 107.06f, 38.75f, 108.462f, 37.8182f)
            curveTo(109.871f, 36.8788f, 111.549f, 36.4091f, 113.496f, 36.4091f)
            curveTo(115.443f, 36.4091f, 117.117f, 36.8788f, 118.518f, 37.8182f)
            curveTo(119.928f, 38.7576f, 121.011f, 40.1212f, 121.768f, 41.9091f)
            curveTo(122.526f, 43.6894f, 122.901f, 45.8485f, 122.893f, 48.3864f)
            curveTo(122.893f, 50.9394f, 122.515f, 53.1212f, 121.757f, 54.9318f)
            curveTo(121.007f, 56.7424f, 119.931f, 58.125f, 118.53f, 59.0795f)
            curveTo(117.128f, 60.0341f, 115.45f, 60.5114f, 113.496f, 60.5114f)
            close()
            moveTo(113.496f, 56.4318f)
            curveTo(114.829f, 56.4318f, 115.893f, 55.7614f, 116.689f, 54.4205f)
            curveTo(117.484f, 53.0795f, 117.878f, 51.0682f, 117.871f, 48.3864f)
            curveTo(117.871f, 46.6212f, 117.689f, 45.1515f, 117.325f, 43.9773f)
            curveTo(116.969f, 42.803f, 116.462f, 41.9205f, 115.803f, 41.3295f)
            curveTo(115.151f, 40.7386f, 114.382f, 40.4432f, 113.496f, 40.4432f)
            curveTo(112.17f, 40.4432f, 111.109f, 41.1061f, 110.314f, 42.4318f)
            curveTo(109.518f, 43.7576f, 109.117f, 45.7424f, 109.109f, 48.3864f)
            curveTo(109.109f, 50.1742f, 109.287f, 51.6667f, 109.643f, 52.8636f)
            curveTo(110.007f, 54.053f, 110.518f, 54.947f, 111.178f, 55.5455f)
            curveTo(111.837f, 56.1364f, 112.609f, 56.4318f, 113.496f, 56.4318f)
            close()
        }

        // Path 5 (Middle box)
        path(fill = contentBrush) {
            moveTo(66f, 28f)
            horizontalLineTo(95f)
            verticalLineTo(68f)
            horizontalLineTo(66f)
            verticalLineTo(28f)
            close()
        }

        // Path 6 (Another O shape)
        path(fill = SolidColor(surface)) {
            moveTo(80.4957f, 60.5114f)
            curveTo(78.5412f, 60.5038f, 76.8594f, 60.0227f, 75.4503f, 59.0682f)
            curveTo(74.0488f, 58.1136f, 72.9692f, 56.7311f, 72.2116f, 54.9205f)
            curveTo(71.4616f, 53.1098f, 71.0904f, 50.9318f, 71.098f, 48.3864f)
            curveTo(71.098f, 45.8485f, 71.473f, 43.6856f, 72.223f, 41.8977f)
            curveTo(72.9806f, 40.1098f, 74.0601f, 38.75f, 75.4616f, 37.8182f)
            curveTo(76.8707f, 36.8788f, 78.5488f, 36.4091f, 80.4957f, 36.4091f)
            curveTo(82.4427f, 36.4091f, 84.117f, 36.8788f, 85.5185f, 37.8182f)
            curveTo(86.9276f, 38.7576f, 88.0109f, 40.1212f, 88.7685f, 41.9091f)
            curveTo(89.526f, 43.6894f, 89.901f, 45.8485f, 89.8935f, 48.3864f)
            curveTo(89.8935f, 50.9394f, 89.5147f, 53.1212f, 88.7571f, 54.9318f)
            curveTo(88.0071f, 56.7424f, 86.9313f, 58.125f, 85.5298f, 59.0795f)
            curveTo(84.1283f, 60.0341f, 82.4503f, 60.5114f, 80.4957f, 60.5114f)
            close()
            moveTo(80.4957f, 56.4318f)
            curveTo(81.8291f, 56.4318f, 82.8935f, 55.7614f, 83.6889f, 54.4205f)
            curveTo(84.4844f, 53.0795f, 84.8783f, 51.0682f, 84.8707f, 48.3864f)
            curveTo(84.8707f, 46.6212f, 84.6889f, 45.1515f, 84.3253f, 43.9773f)
            curveTo(83.9692f, 42.803f, 83.4616f, 41.9205f, 82.8026f, 41.3295f)
            curveTo(82.151f, 40.7386f, 81.3821f, 40.4432f, 80.4957f, 40.4432f)
            curveTo(79.17f, 40.4432f, 78.1094f, 41.1061f, 77.3139f, 42.4318f)
            curveTo(76.5185f, 43.7576f, 76.117f, 45.7424f, 76.1094f, 48.3864f)
            curveTo(76.1094f, 50.1742f, 76.2874f, 51.6667f, 76.6435f, 52.8636f)
            curveTo(77.0071f, 54.053f, 77.5185f, 54.947f, 78.1776f, 55.5455f)
            curveTo(78.8366f, 56.1364f, 79.6094f, 56.4318f, 80.4957f, 56.4318f)
            close()
        }

        // Path 7 (Left box)
        path(fill = contentBrush) {
            moveTo(33f, 28f)
            horizontalLineTo(62f)
            verticalLineTo(68f)
            horizontalLineTo(33f)
            verticalLineTo(28f)
            close()
        }

        // Path 8 (Another O shape)
        path(fill = SolidColor(surface)) {
            moveTo(47.4957f, 60.5114f)
            curveTo(45.5412f, 60.5038f, 43.8594f, 60.0227f, 42.4503f, 59.0682f)
            curveTo(41.0488f, 58.1136f, 39.9692f, 56.7311f, 39.2116f, 54.9205f)
            curveTo(38.4616f, 53.1098f, 38.0904f, 50.9318f, 38.098f, 48.3864f)
            curveTo(38.098f, 45.8485f, 38.473f, 43.6856f, 39.223f, 41.8977f)
            curveTo(39.9806f, 40.1098f, 41.0601f, 38.75f, 42.4616f, 37.8182f)
            curveTo(43.8707f, 36.8788f, 45.5488f, 36.4091f, 47.4957f, 36.4091f)
            curveTo(49.4427f, 36.4091f, 51.117f, 36.8788f, 52.5185f, 37.8182f)
            curveTo(53.9276f, 38.7576f, 55.0109f, 40.1212f, 55.7685f, 41.9091f)
            curveTo(56.526f, 43.6894f, 56.901f, 45.8485f, 56.8935f, 48.3864f)
            curveTo(56.8935f, 50.9394f, 56.5147f, 53.1212f, 55.7571f, 54.9318f)
            curveTo(55.0071f, 56.7424f, 53.9313f, 58.125f, 52.5298f, 59.0795f)
            curveTo(51.1283f, 60.0341f, 49.4503f, 60.5114f, 47.4957f, 60.5114f)
            close()
            moveTo(47.4957f, 56.4318f)
            curveTo(48.8291f, 56.4318f, 49.8935f, 55.7614f, 50.6889f, 54.4205f)
            curveTo(51.4844f, 53.0795f, 51.8783f, 51.0682f, 51.8707f, 48.3864f)
            curveTo(51.8707f, 46.6212f, 51.6889f, 45.1515f, 51.3253f, 43.9773f)
            curveTo(50.9692f, 42.803f, 50.4616f, 41.9205f, 49.8026f, 41.3295f)
            curveTo(49.151f, 40.7386f, 48.3821f, 40.4432f, 47.4957f, 40.4432f)
            curveTo(46.17f, 40.4432f, 45.1094f, 41.1061f, 44.3139f, 42.4318f)
            curveTo(43.5185f, 43.7576f, 43.117f, 45.7424f, 43.1094f, 48.3864f)
            curveTo(43.1094f, 50.1742f, 43.2874f, 51.6667f, 43.6435f, 52.8636f)
            curveTo(44.0071f, 54.053f, 44.5185f, 54.947f, 45.1776f, 55.5455f)
            curveTo(45.8366f, 56.1364f, 46.6094f, 56.4318f, 47.4957f, 56.4318f)
            close()
        }

        // Path 9 (Letters)
        path(fill = SolidColor(onSurface)) { // Original used black
            moveTo(107.466f, 81.5455f)
            verticalLineTo(85.1818f)
            horizontalLineTo(96.9545f)
            verticalLineTo(81.5455f)
            horizontalLineTo(107.466f)
            close()
            moveTo(99.3409f, 77.3636f)
            horizontalLineTo(104.182f)
            verticalLineTo(93.6364f)
            curveTo(104.182f, 94.0833f, 104.25f, 94.4318f, 104.386f, 94.6818f)
            curveTo(104.523f, 94.9242f, 104.712f, 95.0947f, 104.955f, 95.1932f)
            curveTo(105.205f, 95.2917f, 105.492f, 95.3409f, 105.818f, 95.3409f)
            curveTo(106.045f, 95.3409f, 106.273f, 95.322f, 106.5f, 95.2841f)
            curveTo(106.727f, 95.2386f, 106.902f, 95.2045f, 107.023f, 95.1818f)
            lineTo(107.784f, 98.7841f)
            curveTo(107.542f, 98.8598f, 107.201f, 98.947f, 106.761f, 99.0455f)
            curveTo(106.322f, 99.1515f, 105.788f, 99.2159f, 105.159f, 99.2386f)
            curveTo(103.992f, 99.2841f, 102.97f, 99.1288f, 102.091f, 98.7727f)
            curveTo(101.22f, 98.4167f, 100.542f, 97.8636f, 100.057f, 97.1136f)
            curveTo(99.572f, 96.3636f, 99.3333f, 95.4167f, 99.3409f, 94.2727f)
            verticalLineTo(77.3636f)
            close()
        }
        
        // Path 10
        path(fill = SolidColor(onSurface)) {
            moveTo(87.0114f, 99.3409f)
            curveTo(85.2159f, 99.3409f, 83.6705f, 98.9773f, 82.375f, 98.25f)
            curveTo(81.0871f, 97.5152f, 80.0947f, 96.4773f, 79.3977f, 95.1364f)
            curveTo(78.7008f, 93.7879f, 78.3523f, 92.1932f, 78.3523f, 90.3523f)
            curveTo(78.3523f, 88.5568f, 78.7008f, 86.9811f, 79.3977f, 85.625f)
            curveTo(80.0947f, 84.2689f, 81.0758f, 83.2121f, 82.3409f, 82.4545f)
            curveTo(83.6136f, 81.697f, 85.1061f, 81.3182f, 86.8182f, 81.3182f)
            curveTo(87.9697f, 81.3182f, 89.0417f, 81.5038f, 90.0341f, 81.875f)
            curveTo(91.0341f, 82.2386f, 91.9053f, 82.7879f, 92.6477f, 83.5227f)
            curveTo(93.3977f, 84.2576f, 93.9811f, 85.1818f, 94.3977f, 86.2955f)
            curveTo(94.8144f, 87.4015f, 95.0227f, 88.697f, 95.0227f, 90.1818f)
            verticalLineTo(91.5114f)
            horizontalLineTo(80.2841f)
            verticalLineTo(88.5114f)
            horizontalLineTo(90.4659f)
            curveTo(90.4659f, 87.8144f, 90.3144f, 87.197f, 90.0114f, 86.6591f)
            curveTo(89.7083f, 86.1212f, 89.2879f, 85.7008f, 88.75f, 85.3977f)
            curveTo(88.2197f, 85.0871f, 87.6023f, 84.9318f, 86.8977f, 84.9318f)
            curveTo(86.1629f, 84.9318f, 85.5114f, 85.1023f, 84.9432f, 85.4432f)
            curveTo(84.3826f, 85.7765f, 83.9432f, 86.2273f, 83.625f, 86.7955f)
            curveTo(83.3068f, 87.3561f, 83.1439f, 87.9811f, 83.1364f, 88.6705f)
            verticalLineTo(91.5227f)
            curveTo(83.1364f, 92.3864f, 83.2955f, 93.1326f, 83.6136f, 93.7614f)
            curveTo(83.9394f, 94.3902f, 84.3977f, 94.875f, 84.9886f, 95.2159f)
            curveTo(85.5795f, 95.5568f, 86.2803f, 95.7273f, 87.0909f, 95.7273f)
            curveTo(87.6288f, 95.7273f, 88.1212f, 95.6515f, 88.5682f, 95.5f)
            curveTo(89.0152f, 95.3485f, 89.3977f, 95.1212f, 89.7159f, 94.8182f)
            curveTo(90.0341f, 94.5152f, 90.2765f, 94.1439f, 90.4432f, 93.7045f)
            lineTo(94.9205f, 94f)
            curveTo(94.6932f, 95.0758f, 94.2273f, 96.0152f, 93.5227f, 96.8182f)
            curveTo(92.8258f, 97.6136f, 91.9242f, 98.2349f, 90.8182f, 98.6818f)
            curveTo(89.7197f, 99.1212f, 88.4508f, 99.3409f, 87.0114f, 99.3409f)
            close()
        }

        // Path 11
        path(fill = SolidColor(onSurface)) {
            moveTo(75.6108f, 86.5227f)
            lineTo(71.179f, 86.7955f)
            curveTo(71.1032f, 86.4167f, 70.9403f, 86.0758f, 70.6903f, 85.7727f)
            curveTo(70.4403f, 85.4621f, 70.1108f, 85.2159f, 69.7017f, 85.0341f)
            curveTo(69.3002f, 84.8447f, 68.8191f, 84.75f, 68.2585f, 84.75f)
            curveTo(67.5085f, 84.75f, 66.8759f, 84.9091f, 66.3608f, 85.2273f)
            curveTo(65.8456f, 85.5379f, 65.5881f, 85.9545f, 65.5881f, 86.4773f)
            curveTo(65.5881f, 86.8939f, 65.7547f, 87.2462f, 66.0881f, 87.5341f)
            curveTo(66.4214f, 87.822f, 66.9934f, 88.053f, 67.804f, 88.2273f)
            lineTo(70.9631f, 88.8636f)
            curveTo(72.66f, 89.2121f, 73.9252f, 89.7727f, 74.7585f, 90.5455f)
            curveTo(75.5919f, 91.3182f, 76.0085f, 92.3333f, 76.0085f, 93.5909f)
            curveTo(76.0085f, 94.7348f, 75.6714f, 95.7386f, 74.9972f, 96.6023f)
            curveTo(74.3305f, 97.4659f, 73.4138f, 98.1402f, 72.2472f, 98.625f)
            curveTo(71.0881f, 99.1023f, 69.7509f, 99.3409f, 68.2358f, 99.3409f)
            curveTo(65.9252f, 99.3409f, 64.0843f, 98.8599f, 62.7131f, 97.8977f)
            curveTo(61.3494f, 96.928f, 60.5502f, 95.6099f, 60.3153f, 93.9432f)
            lineTo(65.0767f, 93.6932f)
            curveTo(65.2206f, 94.3977f, 65.5691f, 94.9356f, 66.1222f, 95.3068f)
            curveTo(66.6752f, 95.6705f, 67.3835f, 95.8523f, 68.2472f, 95.8523f)
            curveTo(69.0956f, 95.8523f, 69.7775f, 95.6894f, 70.2926f, 95.3636f)
            curveTo(70.8153f, 95.0303f, 71.0805f, 94.6023f, 71.0881f, 94.0795f)
            curveTo(71.0805f, 93.6402f, 70.8949f, 93.2803f, 70.5312f, 93f)
            curveTo(70.1676f, 92.7121f, 69.607f, 92.4924f, 68.8494f, 92.3409f)
            lineTo(65.8267f, 91.7386f)
            curveTo(64.1222f, 91.3977f, 62.8532f, 90.8068f, 62.0199f, 89.9659f)
            curveTo(61.1941f, 89.125f, 60.7812f, 88.053f, 60.7812f, 86.75f)
            curveTo(60.7812f, 85.6288f, 61.0843f, 84.6629f, 61.6903f, 83.8523f)
            curveTo(62.304f, 83.0417f, 63.1638f, 82.4167f, 64.2699f, 81.9773f)
            curveTo(65.3835f, 81.5379f, 66.6866f, 81.3182f, 68.179f, 81.3182f)
            curveTo(70.3835f, 81.3182f, 72.1184f, 81.7841f, 73.3835f, 82.7159f)
            curveTo(74.6562f, 83.6477f, 75.3987f, 84.9167f, 75.6108f, 86.5227f)
            close()
        }

        // Path 12
        path(fill = SolidColor(onSurface)) {
            moveTo(49.9176f, 99.3409f)
            curveTo(48.1222f, 99.3409f, 46.5767f, 98.9773f, 45.2812f, 98.25f)
            curveTo(43.9934f, 97.5152f, 43.0009f, 96.4773f, 42.304f, 95.1364f)
            curveTo(41.607f, 93.7879f, 41.2585f, 92.1932f, 41.2585f, 90.3523f)
            curveTo(41.2585f, 88.5568f, 41.607f, 86.9811f, 42.304f, 85.625f)
            curveTo(43.0009f, 84.2689f, 43.982f, 83.2121f, 45.2472f, 82.4545f)
            curveTo(46.5199f, 81.697f, 48.0123f, 81.3182f, 49.7244f, 81.3182f)
            curveTo(50.8759f, 81.3182f, 51.9479f, 81.5038f, 52.9403f, 81.875f)
            curveTo(53.9403f, 82.2386f, 54.8116f, 82.7879f, 55.554f, 83.5227f)
            curveTo(56.304f, 84.2576f, 56.8873f, 85.1818f, 57.304f, 86.2955f)
            curveTo(57.7206f, 87.4015f, 57.929f, 88.697f, 57.929f, 90.1818f)
            verticalLineTo(91.5114f)
            horizontalLineTo(43.1903f)
            verticalLineTo(88.5114f)
            horizontalLineTo(53.3722f)
            curveTo(53.3722f, 87.8144f, 53.2206f, 87.197f, 52.9176f, 86.6591f)
            curveTo(52.6146f, 86.1212f, 52.1941f, 85.7008f, 51.6562f, 85.3977f)
            curveTo(51.1259f, 85.0871f, 50.5085f, 84.9318f, 49.804f, 84.9318f)
            curveTo(49.0691f, 84.9318f, 48.4176f, 85.1023f, 47.8494f, 85.4432f)
            curveTo(47.2888f, 85.7765f, 46.8494f, 86.2273f, 46.5312f, 86.7955f)
            curveTo(46.2131f, 87.3561f, 46.0502f, 87.9811f, 46.0426f, 88.6705f)
            verticalLineTo(91.5227f)
            curveTo(46.0426f, 92.3864f, 46.2017f, 93.1326f, 46.5199f, 93.7614f)
            curveTo(46.8456f, 94.3902f, 47.304f, 94.875f, 47.8949f, 95.2159f)
            curveTo(48.4858f, 95.5568f, 49.1866f, 95.7273f, 49.9972f, 95.7273f)
            curveTo(50.535f, 95.7273f, 51.0275f, 95.6515f, 51.4744f, 95.5f)
            curveTo(51.9214f, 95.3485f, 52.304f, 95.1212f, 52.6222f, 94.8182f)
            curveTo(52.9403f, 94.5152f, 53.1828f, 94.1439f, 53.3494f, 93.7045f)
            lineTo(57.8267f, 94f)
            curveTo(57.5994f, 95.0758f, 57.1335f, 96.0152f, 56.429f, 96.8182f)
            curveTo(55.732f, 97.6136f, 54.8305f, 98.2349f, 53.7244f, 98.6818f)
            curveTo(52.6259f, 99.1212f, 51.357f, 99.3409f, 49.9176f, 99.3409f)
            close()
        }

        // Path 13
        path(fill = SolidColor(onSurface)) {
            moveTo(21.3352f, 99f)
            verticalLineTo(75.7273f)
            horizontalLineTo(30.517f)
            curveTo(32.2746f, 75.7273f, 33.7746f, 76.0417f, 35.017f, 76.6705f)
            curveTo(36.267f, 77.2917f, 37.2178f, 78.1742f, 37.8693f, 79.3182f)
            curveTo(38.5284f, 80.4545f, 38.858f, 81.7917f, 38.858f, 83.3295f)
            curveTo(38.858f, 84.875f, 38.5246f, 86.2045f, 37.858f, 87.3182f)
            curveTo(37.1913f, 88.4242f, 36.2254f, 89.2727f, 34.9602f, 89.8636f)
            curveTo(33.7027f, 90.4545f, 32.1799f, 90.75f, 30.392f, 90.75f)
            horizontalLineTo(24.2443f)
            verticalLineTo(86.7955f)
            horizontalLineTo(29.5966f)
            curveTo(30.536f, 86.7955f, 31.3163f, 86.6667f, 31.9375f, 86.4091f)
            curveTo(32.5587f, 86.1515f, 33.0208f, 85.7652f, 33.3239f, 85.25f)
            curveTo(33.6345f, 84.7348f, 33.7898f, 84.0947f, 33.7898f, 83.3295f)
            curveTo(33.7898f, 82.5568f, 33.6345f, 81.9053f, 33.3239f, 81.375f)
            curveTo(33.0208f, 80.8447f, 32.5549f, 80.4432f, 31.9261f, 80.1705f)
            curveTo(31.3049f, 79.8902f, 30.5208f, 79.75f, 29.5739f, 79.75f)
            horizontalLineTo(26.2557f)
            verticalLineTo(99f)
            horizontalLineTo(21.3352f)
            close()
            moveTo(33.9034f, 88.4091f)
            lineTo(39.6875f, 99f)
            horizontalLineTo(34.2557f)
            lineTo(28.5966f, 88.4091f)
            horizontalLineTo(33.9034f)
            close()
        }
    }.build()
}
