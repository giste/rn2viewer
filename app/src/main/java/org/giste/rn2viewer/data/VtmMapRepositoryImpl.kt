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

package org.giste.rn2viewer.data

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.giste.rn2viewer.data.model.MapCategoryDto
import org.giste.rn2viewer.domain.model.MapCategory
import org.giste.rn2viewer.domain.model.MapFile
import org.giste.rn2viewer.domain.repositories.MapRepository
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VtmMapRepositoryImpl @Inject constructor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) : MapRepository {

    private val mapsDir = File(context.filesDir, "maps")
    private val _downloadedMaps = MutableStateFlow<List<MapFile>>(emptyList())
    
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun getDownloadedMaps(): Flow<List<MapFile>> = _downloadedMaps.asStateFlow()

    override fun getAvailableMaps(): Flow<List<MapCategory>> {
        val categories = try {
            val jsonString = context.assets.open("maps_manifest.json").bufferedReader().use { it.readText() }
            val dtos = json.decodeFromString<List<MapCategoryDto>>(jsonString)
            dtos.map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Error loading maps manifest from assets")
            emptyList()
        }
        return MutableStateFlow(categories).asStateFlow()
    }

    override suspend fun refreshDownloadedMaps() {
        withContext(ioDispatcher) {
            if (!mapsDir.exists()) {
                mapsDir.mkdirs()
            }
            val files = mapsDir.listFiles { _, name -> name.endsWith(".map") }
            val mapFiles = files?.map {
                MapFile(
                    name = it.name,
                    path = it.absolutePath,
                    size = it.length()
                )
            } ?: emptyList()
            _downloadedMaps.value = mapFiles
        }
    }

    override suspend fun deleteMap(mapFile: MapFile) {
        withContext(ioDispatcher) {
            val file = File(mapFile.path)
            if (file.exists()) {
                file.delete()
                refreshDownloadedMaps()
            }
        }
    }
}
