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

package org.giste.rn2viewer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.oscim.android.MapView
import org.oscim.core.MapPosition
import org.oscim.layers.tile.vector.VectorTileLayer
import org.oscim.layers.tile.vector.labeling.LabelLayer
import org.oscim.tiling.source.mapfile.MapFileTileSource
import timber.log.Timber
import java.io.File

@Composable
fun VtmMap(
    modifier: Modifier = Modifier,
    mapFilePath: String? = null,
    onMapInitialized: (MapView) -> Unit = {}
) {
    var currentLoadedPath by remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                onMapInitialized(this)
            }
        },
        update = { mapView ->
            if (mapFilePath != currentLoadedPath) {
                val map = mapView.map()
                
                if (mapFilePath != null && File(mapFilePath).exists()) {
                    try {
                        val tileSource = MapFileTileSource()
                        if (tileSource.setMapFile(mapFilePath)) {
                            val tileLayer = VectorTileLayer(map, tileSource)
                            val labelLayer = LabelLayer(map, tileLayer)
                            
                            map.layers().clear()
                            map.layers().add(tileLayer)
                            map.layers().add(labelLayer)
                            
                            // MapPosition setup
                            if (currentLoadedPath == null) {
                                val pos = MapPosition()
                                pos.setPosition(40.4168, -3.7038)
                                pos.zoomLevel = 6
                                map.setMapPosition(pos)
                            }
                            
                            // Default rendering - Usually requires a theme but let's see if it renders anything default
                            map.render()
                            currentLoadedPath = mapFilePath
                            Timber.d("VTM map loaded: $mapFilePath")
                        } else {
                            Timber.e("Failed to set map file: $mapFilePath")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error loading VTM map file")
                    }
                } else if (mapFilePath == null) {
                    map.layers().clear()
                    map.render()
                    currentLoadedPath = null
                }
            }
        },
        onRelease = { mapView ->
            mapView.onPause()
            mapView.onDestroy()
        }
    )
}
