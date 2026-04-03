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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.domain.model.Route
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FileRouteRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: FileRouteRepository
    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FileRouteRepository(context, testDispatcher)
    }

    @Test
    fun `saveRoute should write to file and loadRoute should read it back`() = runTest(testDispatcher) {
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
    fun `loadRoute should return null if file does not exist`() = runTest(testDispatcher) {
        // When
        val loadedRoute = repository.loadRoute().first()

        // Then
        assertNull(loadedRoute)
    }
}
