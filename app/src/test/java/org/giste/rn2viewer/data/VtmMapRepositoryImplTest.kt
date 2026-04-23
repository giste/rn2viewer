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
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        context = mockk()
        every { context.filesDir } returns tempFolder.root
        // Mock assets
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
        
        repository = VtmMapRepositoryImpl(context, testDispatcher)
    }

    @Test
    fun `getAvailableMaps should return categorized maps from manifest`() = runTest {
        // When
        val categories = repository.getAvailableMaps().first()

        // Then
        assertEquals(1, categories.size)
        assertEquals("Europe", categories.first().name)
        assertEquals(1, categories.first().maps.size)
        assertEquals("Spain", categories.first().maps.first().name)
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
}
