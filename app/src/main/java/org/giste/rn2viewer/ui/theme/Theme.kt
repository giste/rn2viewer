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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import org.giste.rn2viewer.domain.model.settings.AppTheme

private val LocalAppTheme = staticCompositionLocalOf { AppTheme.FOLLOW_SYSTEM }

private val DarkColorScheme = darkColorScheme(
    primary = BeerDarkPrimary,
    onPrimary = BeerDarkOnPrimary,
    primaryContainer = BeerDarkPrimaryContainer,
    onPrimaryContainer = BeerDarkOnPrimaryContainer,
    secondary = BeerDarkSecondary,
    onSecondary = BeerDarkOnSecondary,
    secondaryContainer = BeerDarkSecondaryContainer,
    onSecondaryContainer = BeerDarkOnSecondaryContainer,
    tertiary = BeerDarkTertiary,
    onTertiary = BeerDarkOnTertiary,
    surface = BeerDarkSurface,
    onSurface = BeerDarkOnSurface,
    surfaceVariant = BeerDarkSurfaceVariant,
    onSurfaceVariant = BeerDarkOnSurfaceVariant,
    outline = BeerDarkOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = BeerLightPrimary,
    onPrimary = BeerLightOnPrimary,
    primaryContainer = BeerLightPrimaryContainer,
    onPrimaryContainer = BeerLightOnPrimaryContainer,
    secondary = BeerLightSecondary,
    onSecondary = BeerLightOnSecondary,
    secondaryContainer = BeerLightSecondaryContainer,
    onSecondaryContainer = BeerLightOnSecondaryContainer,
    tertiary = BeerLightTertiary,
    onTertiary = BeerLightOnTertiary,
    surface = BeerLightSurface,
    onSurface = BeerLightOnSurface,
    surfaceVariant = BeerLightSurfaceVariant,
    onSurfaceVariant = BeerLightOnSurfaceVariant,
    outline = BeerLightOutline,
)

private val FiaColorScheme = lightColorScheme(
    primary = FiaLightPrimary,
    onPrimary = FiaLightOnPrimary,
    primaryContainer = FiaLightPrimaryContainer,
    onPrimaryContainer = FiaLightOnPrimaryContainer,
    secondary = FiaLightSecondary,
    onSecondary = FiaLightOnSecondary,
    secondaryContainer = FiaLightSecondaryContainer,
    onSecondaryContainer = FiaLightOnSecondaryContainer,
    tertiary = FiaLightTertiary,
    onTertiary = FiaLightOnTertiary,
    tertiaryContainer = FiaLightTertiaryContainer,
    onTertiaryContainer = FiaLightOnTertiaryContainer,
    error = FiaLightError,
    onError = FiaLightOnError,
    errorContainer = FiaLightErrorContainer,
    onErrorContainer = FiaLightOnSurface,
    surface = FiaLightSurface,
    onSurface = FiaLightOnSurface,
    surfaceVariant = FiaLightSurfaceVariant,
    onSurfaceVariant = FiaLightOnSurfaceVariant,
    outline = FiaLightOutline,
    inverseSurface = FiaBlack,
    inverseOnSurface = FiaWhite,
)

/**
 * Access object for the custom theme dimensions.
 */
object Rn2Theme {
    val dimensions: AppDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalAppDimensions.current

    val appTheme: AppTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTheme.current
}

@Composable
fun Rn2ViewerTheme(
    windowSizeClass: WindowSizeClass,
    appTheme: AppTheme = AppTheme.FOLLOW_SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        AppTheme.DYNAMIC -> isSystemInDarkTheme()
        AppTheme.FIA -> false
    }

    val colorScheme = when (appTheme) {
        AppTheme.FIA -> FiaColorScheme
        AppTheme.DYNAMIC -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    // Determine the scale based on the window size
    val useExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded &&
                     windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact

    val (dimensions, typography) = if (useExpanded) {
        expandedDimensions to expandedTypography
    } else {
        compactDimensions to compactTypography
    }

    CompositionLocalProvider(
        LocalAppDimensions provides dimensions,
        LocalAppTheme provides appTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
