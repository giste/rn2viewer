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

package org.giste.rn2viewer.domain.usecases

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.domain.mappers.Rn2Mapper
import org.giste.rn2viewer.domain.model.ResourceState
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.repositories.RouteRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetRouteUseCaseTest {

    private val repository = mockk<RouteRepository>()
    private val mapper = mockk<Rn2Mapper>()
    private val testDispatcher = UnconfinedTestDispatcher()
    // Initialize with a dummy value that will be dropped by .drop(1)
    private val routeFlow = MutableStateFlow<String?>("initial_dropped_value")

    private lateinit var getRouteUseCase: GetRouteUseCase

    @Before
    fun setup() {
        every { repository.loadRouteRaw() } returns routeFlow
        getRouteUseCase = GetRouteUseCase(repository, mapper, testDispatcher)
    }

    @Test
    fun `should skip initial null and emit Loading then Empty when repository is empty`() = runTest(testDispatcher) {
        val results = mutableListOf<ResourceState<Route>>()
        val job = launch {
            getRouteUseCase().collect { results.add(it) }
        }

        // Emit first real value after initial null
        routeFlow.value = null 

        // Current implementation: .drop(1) skips the initial null.
        // transform emits Loading then Empty because it's still null.
        assertEquals(2, results.size)
        assertTrue(results[0] is ResourceState.Loading)
        assertTrue(results[1] is ResourceState.Empty)
        
        job.cancel()
    }

    @Test
    fun `should emit Success when mapping is successful`() = runTest(testDispatcher) {
        val json = "{}"
        val route = Route(name = "Test", waypoints = emptyList())
        every { mapper.mapToDomain(json) } returns route

        val results = mutableListOf<ResourceState<Route>>()
        val job = launch {
            getRouteUseCase().collect { results.add(it) }
        }

        routeFlow.value = json

        assertTrue(results[0] is ResourceState.Loading)
        assertTrue(results[1] is ResourceState.Success)
        assertEquals(route, (results[1] as ResourceState.Success).data)

        job.cancel()
    }

    @Test
    fun `should emit Error when mapping fails`() = runTest(testDispatcher) {
        val json = "invalid"
        every { mapper.mapToDomain(json) } throws RuntimeException("Parse error")

        val results = mutableListOf<ResourceState<Route>>()
        val job = launch {
            getRouteUseCase().collect { results.add(it) }
        }

        routeFlow.value = json

        assertTrue(results[0] is ResourceState.Loading)
        assertTrue(results[1] is ResourceState.Error)
        assertEquals("Parse error", (results[1] as ResourceState.Error).message)

        job.cancel()
    }
}
