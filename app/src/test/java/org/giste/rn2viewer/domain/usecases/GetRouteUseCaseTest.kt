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
import org.giste.rn2viewer.domain.model.settings.AppSettings
import org.giste.rn2viewer.domain.repositories.RouteRepository
import org.giste.rn2viewer.domain.usecases.settings.GetSettingsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetRouteUseCaseTest {

    private val repository = mockk<RouteRepository>()
    private val mapper = mockk<Rn2Mapper>()
    private val getSettingsUseCase = mockk<GetSettingsUseCase>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val routeFlow = MutableStateFlow<String?>(null)
    private val settingsFlow = MutableStateFlow(AppSettings())

    private lateinit var getRouteUseCase: GetRouteUseCase

    @Before
    fun setup() {
        every { repository.loadRouteRaw() } returns routeFlow
        every { getSettingsUseCase() } returns settingsFlow
        getRouteUseCase = GetRouteUseCase(repository, mapper, getSettingsUseCase, testDispatcher)
    }

    @Test
    fun `should emit Empty when repository is empty`() = runTest(testDispatcher) {
        val results = mutableListOf<ResourceState<Route>>()
        val job = launch {
            getRouteUseCase().collect { results.add(it) }
        }

        // Initial null from routeFlow triggers Empty
        assertTrue(results.any { it is ResourceState.Empty })
        
        job.cancel()
    }

    @Test
    fun `should emit Success when mapping is successful`() = runTest(testDispatcher) {
        val json = "{}"
        val route = Route(name = "Test", waypoints = emptyList())
        every { mapper.mapToDomain(json, any()) } returns route

        val results = mutableListOf<ResourceState<Route>>()
        val job = launch {
            getRouteUseCase().collect { results.add(it) }
        }

        routeFlow.value = json

        assertTrue(results.any { it is ResourceState.Loading })
        assertTrue(results.any { it is ResourceState.Success })
        assertEquals(route, (results.find { it is ResourceState.Success } as ResourceState.Success).data)

        job.cancel()
    }

    @Test
    fun `should emit Error when mapping fails`() = runTest(testDispatcher) {
        val json = "invalid"
        every { mapper.mapToDomain(json, any()) } throws RuntimeException("Parse error")

        val results = mutableListOf<ResourceState<Route>>()
        val job = launch {
            getRouteUseCase().collect { results.add(it) }
        }

        routeFlow.value = json

        assertTrue(results.any { it is ResourceState.Loading })
        assertTrue(results.any { it is ResourceState.Error })
        assertEquals("Parse error", (results.find { it is ResourceState.Error } as ResourceState.Error).message)

        job.cancel()
    }
}
