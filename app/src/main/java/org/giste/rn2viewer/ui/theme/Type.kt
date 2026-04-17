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

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Ratio used for the Major Third harmonic scale.
 */
private const val SCALE_RATIO = 1.25f

/**
 * Creates a complete Material 3 Typography set based on a harmonic scale
 * derived from the [baseSize] (DisplayLarge).
 */
private fun createHarmonicTypography(baseSize: Float): Typography {
    // Calculate sizes using the Major Third ratio (1.25)
    val dl = baseSize
    val dm = dl / SCALE_RATIO
    val ds = dm / SCALE_RATIO
    val hl = ds / SCALE_RATIO
    val hm = hl / SCALE_RATIO
    val hs = hm / SCALE_RATIO
    val tl = hs / SCALE_RATIO
    val tm = tl / SCALE_RATIO
    val ts = tm / SCALE_RATIO
    val bl = ts / SCALE_RATIO
    val bm = bl / SCALE_RATIO
    val bs = bm / SCALE_RATIO

    return Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = dl.sp,
            lineHeight = (dl * 1.1f).sp,
            letterSpacing = (-2).sp
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = dm.sp,
            lineHeight = (dm * 1.1f).sp,
            letterSpacing = (-1).sp
        ),
        displaySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = ds.sp,
            lineHeight = (ds * 1.1f).sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = hl.sp,
            lineHeight = (hl * 1.1f).sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = hm.sp,
            lineHeight = (hm * 1.1f).sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = hs.sp,
            lineHeight = (hs * 1.1f).sp,
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = tl.sp,
            lineHeight = (tl * 1.2f).sp,
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = tm.sp,
            lineHeight = (tm * 1.2f).sp,
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = ts.sp,
            lineHeight = (ts * 1.2f).sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = bl.coerceAtLeast(16f).sp,
            lineHeight = (bl.coerceAtLeast(16f) * 1.25f).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = bm.coerceAtLeast(14f).sp,
            lineHeight = (bm.coerceAtLeast(14f) * 1.25f).sp,
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = bs.coerceAtLeast(12f).sp,
            lineHeight = (bs.coerceAtLeast(12f) * 1.25f).sp,
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = bm.coerceAtLeast(14f).sp,
            lineHeight = (bm.coerceAtLeast(14f) * 1.25f).sp,
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = bs.coerceAtLeast(12f).sp,
            lineHeight = (bs.coerceAtLeast(12f) * 1.25f).sp,
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (bs / SCALE_RATIO).coerceAtLeast(10f).sp,
            lineHeight = ((bs / SCALE_RATIO).coerceAtLeast(10f) * 1.25f).sp,
        )
    )
}

/**
 * Typography for compact screens (phones), based on a 96sp DisplayLarge.
 */
val compactTypography = createHarmonicTypography(baseSize = 56f)

/**
 * Typography for expanded screens (tablets), based on a 112sp DisplayLarge.
 */
val expandedTypography = createHarmonicTypography(baseSize = 72f)
