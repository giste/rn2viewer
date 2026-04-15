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
import org.giste.rn2viewer.domain.model.Route
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
    fun `saveRoute should write to file and loadRoute should reflect the change`() = runTest(testDispatcher) {
        // Given
        val route = Route(
            name = "Test Route",
            description = "Description",
            waypoints = emptyList()
        )

        // When
        repository.saveRoute(route)
        val loadedRoute = repository.loadRoute().first()

        // Then
        assertEquals(route.name, loadedRoute?.name)
        assertEquals(route.description, loadedRoute?.description)
    }

    @Test
    fun `loadRoute should emit new values reactively when saveRoute is called`() = runTest(testDispatcher) {
        // Given
        val route1 = Route(name = "Route 1", description = "D1", waypoints = emptyList())
        val route2 = Route(name = "Route 2", description = "D2", waypoints = emptyList())
        val emissions = mutableListOf<Route?>()

        // Start collecting emissions
        val job = launch {
            repository.loadRoute().toList(emissions)
        }

        // When
        repository.saveRoute(route1)
        repository.saveRoute(route2)

        // Then
        // emissions[0] is null (initial state)
        // emissions[1] is the initial load from file (null if file doesn't exist)
        // emissions[2] is route1
        // emissions[3] is route2
        // Since we are using UnconfinedTestDispatcher and SharedFlow with replay=1, we can see the flow.
        assertTrue("Should have at least 3 emissions", emissions.size >= 3)
        assertEquals("Route 1", emissions.find { it?.name == "Route 1" }?.name)
        assertEquals("Route 2", emissions.last()?.name)

        job.cancel()
    }

    @Test
    fun `repository should load existing file on demand`() = runTest(testDispatcher) {
        // Given: a file already exists before repository is even used
        val route = Route(name = "Pre-existing", description = "D", waypoints = emptyList())
        val json = kotlinx.serialization.json.Json { prettyPrint = true }
        val file = java.io.File(context.filesDir, "roadbook.json")
        file.writeText(json.encodeToString(Route.serializer(), route))

        // When: loadRoute is called for the first time
        val loadedRoute = repository.loadRoute().first()

        // Then
        assertEquals("Pre-existing", loadedRoute?.name)
    }

    @Test
    fun `getExternalRouteContent should read text from content URI`() = runTest(testDispatcher) {
        // Robolectric provides a working ContentResolver.
        // We can simulate a file by writing to the internal storage and getting its URI.
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
