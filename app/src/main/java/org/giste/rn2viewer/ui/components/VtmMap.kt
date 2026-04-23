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

package org.giste.rn2viewer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.oscim.android.MapView

@Composable
fun VtmMap(
    modifier: Modifier = Modifier,
    onMapInitialized: (MapView) -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                onMapInitialized(this)
            }
        },
        update = { /* Update map state if needed */ },
        onRelease = { mapView ->
            mapView.onPause()
            mapView.onDestroy()
        }
    )
}
