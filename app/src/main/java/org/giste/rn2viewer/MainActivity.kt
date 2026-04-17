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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import dagger.hilt.android.AndroidEntryPoint
import org.giste.rn2viewer.ui.components.MainScreen
import org.giste.rn2viewer.ui.permissions.LocationPermissionProvider
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            Rn2ViewerTheme(windowSizeClass = windowSizeClass) {
                LocationPermissionProvider {
                    LaunchedEffect(windowSizeClass) {
                        Timber.i("Detected WindowSizeClass: ${windowSizeClass.widthSizeClass} x ${windowSizeClass.heightSizeClass}")
                    }

                    MainScreen(windowSizeClass = windowSizeClass)
                }
            }
        }
    }
}
