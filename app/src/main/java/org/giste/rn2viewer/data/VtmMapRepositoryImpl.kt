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

package org.giste.rn2viewer.data

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.giste.rn2viewer.data.model.MapCategoryDto
import org.giste.rn2viewer.domain.model.MapCategory
import org.giste.rn2viewer.domain.model.MapFile
import org.giste.rn2viewer.domain.model.RemoteMapInfo
import org.giste.rn2viewer.domain.repositories.MapRepository
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VtmMapRepositoryImpl @Inject constructor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
    private val okHttpClient: OkHttpClient
) : MapRepository {

    companion object {
        private const val BASE_URL = "https://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/v5/"
    }

    private val mapsDir = File(context.filesDir, "maps")
    private val _downloadedMaps = MutableStateFlow<List<MapFile>>(emptyList())
    private val repositoryScope = CoroutineScope(ioDispatcher + SupervisorJob())
    
    private val json = Json {
        ignoreUnknownKeys = true
    }

    init {
        repositoryScope.launch {
            refreshDownloadedMaps()
        }
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

    override suspend fun downloadMap(
        mapInfo: RemoteMapInfo,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            val url = BASE_URL + mapInfo.relativeUrl
            val request = Request.Builder().url(url).build()
            
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to download map: ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))
            val totalBytes = body.contentLength()
            
            if (!mapsDir.exists()) {
                mapsDir.mkdirs()
            }
            
            val fileName = mapInfo.relativeUrl.substringAfterLast("/")
            val destinationFile = File(mapsDir, fileName)
            
            body.byteStream().use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            onProgress(totalRead.toFloat() / totalBytes)
                        }
                    }
                }
            }
            
            refreshDownloadedMaps()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error downloading map")
            Result.failure(e)
        }
    }
}
