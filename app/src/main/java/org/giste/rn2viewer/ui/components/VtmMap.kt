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
    mapFilePaths: List<String> = emptyList(),
    onMapInitialized: (MapView) -> Unit = {}
) {
    var loadedPaths by remember { mutableStateOf<Set<String>>(emptySet()) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                onResume()
                onMapInitialized(this)
            }
        },
        update = { mapView ->
            val currentPaths = mapFilePaths.toSet()
            if (currentPaths != loadedPaths) {
                val map = mapView.map()
                val layers = map.layers()
                
                // Clear all data layers (preserving index 0 EventLayer)
                while (layers.size > 1) {
                    layers.removeAt(1)
                }

                if (currentPaths.isNotEmpty()) {
                    try {
                        var firstLayer: VectorTileLayer? = null
                        
                        currentPaths.forEach { path ->
                            if (File(path).exists()) {
                                val tileSource = MapFileTileSource()
                                if (tileSource.setMapFile(path)) {
                                    val tileLayer = VectorTileLayer(map, tileSource)
                                    layers.add(tileLayer)
                                    if (firstLayer == null) firstLayer = tileLayer
                                }
                            }
                        }

                        if (firstLayer != null) {
                            // Add one LabelLayer on top of all vector layers
                            layers.add(LabelLayer(map, firstLayer))
                            
                            // Set base map for theme purposes (uses the first one)
                            map.setBaseMap(firstLayer)
                            map.setTheme(VtmThemes.DEFAULT)

                            // Initial viewport if first load
                            if (loadedPaths.isEmpty()) {
                                val pos = MapPosition()
                                pos.setPosition(40.4168, -3.7038)
                                pos.setZoomLevel(6)
                                map.setMapPosition(pos)
                            }
                            
                            map.updateMap(true)
                            map.render()
                            Timber.i("VTM loaded ${currentPaths.size} map(s)")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error loading multiple VTM map files")
                    }
                } else {
                    map.render()
                }
                loadedPaths = currentPaths
            }
        },
        onRelease = { mapView ->
            mapView.onPause()
            mapView.onDestroy()
        }
    )
}
