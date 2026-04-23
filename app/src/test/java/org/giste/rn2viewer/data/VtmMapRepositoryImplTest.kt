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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.giste.rn2viewer.domain.model.RemoteMapInfo
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
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        context = mockk()
        okHttpClient = mockk()
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
                    "continent": "Europe"
                  }
                ]
              }
            ]
        """.trimIndent()
        every { assets.open("maps_manifest.json") } returns manifestJson.byteInputStream()
        
        repository = VtmMapRepositoryImpl(context, testDispatcher, okHttpClient)
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
    }

    @Test
    fun `deleteMap should remove file and update list`() = runTest {
        // Given
        val mapsDir = File(tempFolder.root, "maps")
        mapsDir.mkdirs()
        val spainFile = File(mapsDir, "spain.map")
        spainFile.writeText("dummy content")
        repository.refreshDownloadedMaps()
        
        val initialMaps = repository.getDownloadedMaps().first()
        assertEquals(1, initialMaps.size)

        // When
        repository.deleteMap(initialMaps.first())

        // Then
        val finalMaps = repository.getDownloadedMaps().first()
        assertTrue(finalMaps.isEmpty())
        assertTrue(!spainFile.exists())
    }

    @Test
    fun `downloadMap should save file to maps directory`() = runTest {
        // Given
        val mapInfo = RemoteMapInfo("id", "Spain", "europe/spain.map", 100, "Europe")
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

        // When
        val result = repository.downloadMap(mapInfo) {}

        // Then
        assertTrue(result.isSuccess)
        val mapsDir = File(tempFolder.root, "maps")
        val downloadedFile = File(mapsDir, "spain.map")
        assertTrue(downloadedFile.exists())
        assertEquals(content, downloadedFile.readText())
    }
}
