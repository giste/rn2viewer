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

package org.giste.rn2viewer.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.giste.rn2viewer.domain.model.MapCategory
import org.giste.rn2viewer.domain.model.MapFile
import org.giste.rn2viewer.domain.model.RemoteMapInfo

/**
 * Interface for managing local and remote Mapsforge map files.
 */
interface MapRepository {
    /**
     * Returns a flow of the list of maps currently downloaded on the device.
     */
    fun getDownloadedMaps(): Flow<List<MapFile>>

    /**
     * Returns a flow of the hierarchical list of maps available for download.
     */
    fun getAvailableMaps(): Flow<List<MapCategory>>

    /**
     * Refreshes the list of available maps from the remote server.
     */
    suspend fun refreshAvailableMaps(): Result<Unit>

    /**
     * Deletes a local map file.
     */
    suspend fun deleteMap(mapFile: MapFile)

    /**
     * Refreshes the list of downloaded maps from disk.
     */
    suspend fun refreshDownloadedMaps()

    /**
     * Downloads a map file from the remote server.
     * @param mapInfo Information about the remote map to download.
     * @param onProgress Callback to track download progress (0.0 to 1.0).
     */
    suspend fun downloadMap(
        mapInfo: RemoteMapInfo,
        onProgress: (Float) -> Unit
    ): Result<Unit>
}
