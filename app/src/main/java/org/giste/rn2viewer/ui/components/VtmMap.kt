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
import org.oscim.theme.internal.VtmThemes
import timber.log.Timber
import java.io.File

@Composable
fun VtmMap(
    modifier: Modifier = Modifier,
    mapFilePath: String? = null,
    onMapInitialized: (MapView) -> Unit = {}
) {
    var currentLoadedPath by remember { mutableStateOf<String?>(null) }

    Timber.d("VtmMap Composable: path=$mapFilePath")

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Timber.d("VtmMap Factory: initializing MapView")
            MapView(context).apply {
                onResume()
                onMapInitialized(this)
            }
        },
        update = { mapView ->
            Timber.d("VtmMap Update: path=$mapFilePath, current=$currentLoadedPath")
            if (mapFilePath != currentLoadedPath) {
                val map = mapView.map()
                
                if (mapFilePath != null && File(mapFilePath).exists()) {
                    try {
                        val tileSource = MapFileTileSource()
                        if (tileSource.setMapFile(mapFilePath)) {
                            val tileLayer = VectorTileLayer(map, tileSource)
                            val labelLayer = LabelLayer(map, tileLayer)
                            
                            // Remove existing data layers (index 1 and above)
                            val layers = map.layers()
                            while (layers.size > 1) {
                                layers.removeAt(1)
                            }
                            
                            // Add new layers
                            map.setBaseMap(tileLayer)
                            layers.add(labelLayer)
                            
                            // MapPosition setup
                            if (currentLoadedPath == null) {
                                val pos = MapPosition()
                                pos.setPosition(40.4168, -3.7038)
                                pos.setZoomLevel(6)
                                map.setMapPosition(pos)
                            }
                            
                            // Apply theme - This is critical for vector rendering
                            map.setTheme(VtmThemes.DEFAULT)
                            
                            map.updateMap(true)
                            map.render()
                            currentLoadedPath = mapFilePath
                            Timber.i("VTM map loaded successfully: $mapFilePath")
                        } else {
                            Timber.e("Failed to set map file: $mapFilePath")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error loading VTM map file")
                    }
                } else if (mapFilePath == null) {
                    val layers = map.layers()
                    while (layers.size > 1) {
                        layers.removeAt(1)
                    }
                    map.render()
                    currentLoadedPath = null
                } else {
                    Timber.w("Map file does not exist at path: $mapFilePath")
                }
            }
        },
        onRelease = { mapView ->
            mapView.onPause()
            mapView.onDestroy()
        }
    )
}
