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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.domain.model.ScrollPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileRouteRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var repository: FileRouteRepository
    private lateinit var dataStore: DataStore<Preferences>

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dataStore = PreferenceDataStoreFactory.create(
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher),
            produceFile = { temporaryFolder.newFile("test.preferences_pb") }
        )
        repository = FileRouteRepository(context, dataStore, testDispatcher)
    }

    @Test
    fun `saveRouteRaw should write to file and loadRouteRaw should reflect the change`() = runTest(testDispatcher) {
        // Given
        val jsonContent = "{\"name\": \"Test Route\"}"

        // When
        repository.saveRouteRaw(jsonContent)
        val loadedContent = repository.loadRouteRaw().first { it != null }

        // Then
        assertEquals(jsonContent, loadedContent)
    }

    @Test
    fun `saveScrollPosition should persist index and offset`() = runTest(testDispatcher) {
        // Given
        val position = ScrollPosition(index = 5, offset = 100)

        // When
        repository.saveScrollPosition(position)
        val saved = repository.getSavedScrollPosition().first()

        // Then
        assertEquals(position, saved)
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
