/*
 * Rn2 Viewer
 * Copyright (C) 2024  Giste
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

package org.giste.rn2viewer

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.giste.rn2viewer.domain.model.settings.AppOrientation
import org.giste.rn2viewer.domain.model.settings.AppTheme
import org.giste.rn2viewer.ui.components.MainScreen
import org.giste.rn2viewer.ui.components.settings.SettingsScreen
import org.giste.rn2viewer.ui.navigation.MainRoute
import org.giste.rn2viewer.ui.navigation.SettingsRoute
import org.giste.rn2viewer.ui.permissions.LocationPermissionProvider
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import org.giste.rn2viewer.ui.viewmodel.SettingsViewModel
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsViewModel.settings.collectAsState()

            LaunchedEffect(settings.orientation) {
                requestedOrientation = when (settings.orientation) {
                    AppOrientation.VERTICAL -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    AppOrientation.HORIZONTAL -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    AppOrientation.FOLLOW_SYSTEM -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            val darkTheme = when (settings.theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            }

            val windowSizeClass = calculateWindowSizeClass(this)
            Rn2ViewerTheme(
                windowSizeClass = windowSizeClass,
                darkTheme = darkTheme
            ) {
                LocationPermissionProvider {
                    LaunchedEffect(windowSizeClass) {
                        Timber.i("Detected WindowSizeClass: ${windowSizeClass.widthSizeClass} x ${windowSizeClass.heightSizeClass}")
                    }

                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = MainRoute
                    ) {
                        composable<MainRoute> {
                            MainScreen(
                                windowSizeClass = windowSizeClass,
                                onSettingsClick = { navController.navigate(SettingsRoute) }
                            )
                        }
                        composable<SettingsRoute> {
                            SettingsScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
