/*
 * Rn2 Viewer
 * Copyright (C) 2024  Giste
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
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileRouteRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: FileRouteRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FileRouteRepository(context, testDispatcher)
    }

    @Test
    fun `saveRouteRaw should write to file and loadRouteRaw should reflect the change`() = runTest(testDispatcher) {
        // Given
        val jsonContent = "{\"name\": \"Test Route\"}"

        // When
        repository.saveRouteRaw(jsonContent)
        val loadedContent = repository.loadRouteRaw().first()

        // Then
        assertEquals(jsonContent, loadedContent)
    }

    @Test
    fun `loadRouteRaw should emit new values reactively when saveRouteRaw is called`() = runTest(testDispatcher) {
        // Given
        val json1 = "{\"name\": \"Route 1\"}"
        val json2 = "{\"name\": \"Route 2\"}"
        val emissions = mutableListOf<String?>()

        // Start collecting emissions
        val job = launch {
            repository.loadRouteRaw().toList(emissions)
        }

        // When
        repository.saveRouteRaw(json1)
        repository.saveRouteRaw(json2)

        // Then
        assertTrue("Should have at least 3 emissions", emissions.size >= 3)
        assertTrue("Emissions should contain json1", emissions.contains(json1))
        assertEquals(json2, emissions.last())

        job.cancel()
    }

    @Test
    fun `repository should load existing file on demand`() = runTest(testDispatcher) {
        // Given: a file already exists before repository is even used
        val jsonContent = "{\"name\": \"Pre-existing\"}"
        val file = java.io.File(context.filesDir, "roadbook.json")
        file.writeText(jsonContent)

        // When: loadRouteRaw is called for the first time
        val loadedContent = repository.loadRouteRaw().first()

        // Then
        assertEquals(jsonContent, loadedContent)
    }

    @Test
    fun `getExternalRouteContent should read text from content URI`() = runTest(testDispatcher) {
        // Robolectric provides a working ContentResolver.
        val fileName = "test_import.rn2"
        val expectedContent = "{\"route\": {\"name\": \"Imported\"}}"
        val file = java.io.File(context.cacheDir, fileName)
        file.writeText(expectedContent)

        val uriString = android.net.Uri.fromFile(file).toString()

        // When
        val result = repository.getExternalRouteContent(uriString)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedContent, result.getOrNull())
    }
}
