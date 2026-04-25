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
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.giste.rn2viewer.domain.repositories.MapRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
class MapRepositoryIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: MapRepository

    private lateinit var context: Context

    @Before
    fun setup() {
        hiltRule.inject()
        context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Clean start: Remove cached manifest and maps
        val manifestFile = File(context.filesDir, "maps_manifest.json")
        if (manifestFile.exists()) manifestFile.delete()
        
        val mapsDir = File(context.filesDir, "maps")
        if (mapsDir.exists()) mapsDir.listFiles()?.forEach { it.delete() }
    }

    @Test
    fun testInitialLoad_UsesAssets_WhenNoCacheExists() = runBlocking {
        // Trigger available maps refresh
        repository.refreshAvailableMaps()
        
        val available = repository.getAvailableMaps().first()
        
        // Asset manifest has "Europe" and "South America"
        assertTrue("Available maps should not be empty (loaded from assets)", available.isNotEmpty())
        assertTrue("Should contain Europe from assets", available.any { it.name == "Europe" })
    }

    @Test
    fun testRefreshAvailableMaps_PersistsToInternalStorage() = runBlocking {
        // Trigger network refresh (Requires internet on device/emulator)
        val result = repository.refreshAvailableMaps()
        
        if (result.isSuccess) {
            val manifestFile = File(context.filesDir, "maps_manifest.json")
            assertTrue("Manifest file should be created in internal storage", manifestFile.exists())
            assertTrue("Manifest file should not be empty", manifestFile.length() > 0)
            
            // Check that the available flow is updated
            val available = repository.getAvailableMaps().first()
            assertTrue(available.isNotEmpty())
        }
    }

    @Test
    fun testMapFolderCreation_AndDetection() = runBlocking {
        // Manually create a dummy map file in the expected directory
        val mapsDir = File(context.filesDir, "maps")
        if (!mapsDir.exists()) mapsDir.mkdirs()
        val dummyFile = File(mapsDir, "integration_test.map")
        dummyFile.writeText("fake map data")

        // Trigger scan
        repository.refreshDownloadedMaps()

        val downloaded = repository.getDownloadedMaps().first()
        
        assertTrue("Repository should detect the manually added map file", 
            downloaded.any { it.name == "integration_test.map" })
        assertEquals(dummyFile.absolutePath, downloaded.find { it.name == "integration_test.map" }?.path)
    }
}
