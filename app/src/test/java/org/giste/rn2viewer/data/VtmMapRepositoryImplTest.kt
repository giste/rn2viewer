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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.giste.rn2viewer.domain.model.LocalMapMetadata
import org.giste.rn2viewer.domain.model.RemoteMapInfo
import org.giste.rn2viewer.domain.repositories.LocalMapMetadataRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class VtmMapRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var repository: VtmMapRepositoryImpl
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var metadataRepository: LocalMapMetadataRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        context = mockk()
        okHttpClient = mockk()
        metadataRepository = mockk()
        every { context.filesDir } returns tempFolder.root
        
        // Mock assets with a sample manifest
        val assets = mockk<android.content.res.AssetManager>()
        every { context.assets } returns assets
        val manifestJson = """
            [
              {
                "name": "Europe",
                "maps": [
                  {
                    "id": "europe_spain",
                    "name": "Spain",
                    "relativeUrl": "europe/spain.map",
                    "size": 524288000,
                    "continent": "Europe",
                    "lastModified": 1710000000000
                  }
                ]
              }
            ]
        """.trimIndent()
        every { assets.open("maps_manifest.json") } returns manifestJson.byteInputStream()
        
        repository = VtmMapRepositoryImpl(context, testDispatcher, okHttpClient, metadataRepository)
    }

    @Test
    fun `refreshDownloadedMaps should detect existing map files`() = runTest {
        // Given
        val mapsDir = File(tempFolder.root, "maps")
        mapsDir.mkdirs()
        File(mapsDir, "spain.map").writeText("dummy content")
        File(mapsDir, "france.map").writeText("dummy content")
        File(mapsDir, "readme.txt").writeText("not a map")

        // When
        repository.refreshDownloadedMaps()

        // Then
        val maps = repository.getDownloadedMaps().first()
        assertEquals(2, maps.size)
        assertTrue(maps.any { it.name == "spain.map" })
        assertTrue(maps.any { it.name == "france.map" })
    }

    @Test
    fun `getAvailableMaps should return categorized maps from manifest`() = runTest {
        // When
        val categories = repository.getAvailableMaps().first()

        // Then
        assertEquals(1, categories.size)
        assertEquals("Europe", categories.first().name)
        assertEquals("Spain", categories.first().maps.first().name)
        assertEquals(1710000000000L, categories.first().maps.first().lastModified)
    }

    @Test
    fun `deleteMap should remove file and its metadata`() = runTest {
        // Given
        val mapsDir = File(tempFolder.root, "maps")
        mapsDir.mkdirs()
        val spainFile = File(mapsDir, "spain.map")
        spainFile.writeText("dummy content")
        
        val metadata = LocalMapMetadata("europe_spain", "spain.map", 1710000000000L, spainFile.length())
        every { metadataRepository.getAllMetadata() } returns flowOf(listOf(metadata))
        coEvery { metadataRepository.deleteMetadata("europe_spain") } returns Unit
        
        repository.refreshDownloadedMaps()
        val initialMaps = repository.getDownloadedMaps().first()

        // When
        repository.deleteMap(initialMaps.first())

        // Then
        val finalMaps = repository.getDownloadedMaps().first()
        assertTrue(finalMaps.isEmpty())
        assertTrue(!spainFile.exists())
        coVerify { metadataRepository.deleteMetadata("europe_spain") }
    }

    @Test
    fun `downloadMap should save file and metadata`() = runTest {
        // Given
        val mapInfo = RemoteMapInfo("europe_spain", "Spain", "europe/spain.map", 100, "Europe", 1710000000000L)
        val content = "map data"
        val response = Response.Builder()
            .request(Request.Builder().url("https://example.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(content.toByteArray().toResponseBody())
            .build()
        
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.execute() } returns response
        coEvery { metadataRepository.saveMetadata(any()) } returns Unit

        // When
        val result = repository.downloadMap(mapInfo) {}

        // Then
        assertTrue(result.isSuccess)
        val mapsDir = File(tempFolder.root, "maps")
        val downloadedFile = File(mapsDir, "spain.map")
        assertTrue(downloadedFile.exists())
        coVerify { 
            metadataRepository.saveMetadata(match { 
                it.mapId == "europe_spain" && it.serverLastModified == 1710000000000L 
            }) 
        }
    }

    @Test
    fun `refreshAvailableMaps should fetch from network and save locally`() = runTest {
        // Given
        val newManifestJson = """
            [
              {
                "name": "Africa",
                "maps": [
                  {
                    "id": "africa_morocco",
                    "name": "Morocco",
                    "relativeUrl": "africa/morocco.map",
                    "size": 100,
                    "continent": "Africa",
                    "lastModified": 1720000000000
                  }
                ]
              }
            ]
        """.trimIndent()
        
        val response = Response.Builder()
            .request(Request.Builder().url("https://raw.githubusercontent.com/GISte/rn2viewer-maps/main/maps_v5_manifest.json").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(newManifestJson.toByteArray().toResponseBody())
            .build()
        
        val call = mockk<Call>()
        every { okHttpClient.newCall(any()) } returns call
        every { call.execute() } returns response

        // When
        val result = repository.refreshAvailableMaps()

        // Then
        assertTrue(result.isSuccess)
        val available = repository.getAvailableMaps().first()
        assertEquals(1, available.size)
        assertEquals("Africa", available.first().name)
        
        // Check local file persistence
        val manifestFile = File(tempFolder.root, "maps_manifest.json")
        assertTrue(manifestFile.exists())
        assertEquals(newManifestJson, manifestFile.readText())
    }
}
