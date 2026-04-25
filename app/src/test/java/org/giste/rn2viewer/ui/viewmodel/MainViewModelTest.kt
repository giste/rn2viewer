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

package org.giste.rn2viewer.ui.viewmodel

import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.giste.rn2viewer.domain.model.Odometer
import org.giste.rn2viewer.domain.model.ResourceState
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.model.ScrollPosition
import org.giste.rn2viewer.domain.repositories.RouteRepository
import org.giste.rn2viewer.domain.usecases.DecrementPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.GetOdometerUseCase
import org.giste.rn2viewer.domain.usecases.GetRouteUseCase
import org.giste.rn2viewer.domain.usecases.ImportRouteUseCase
import org.giste.rn2viewer.domain.usecases.IncrementPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.ResetAllDistancesUseCase
import org.giste.rn2viewer.domain.usecases.ResetPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.SetPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.maps.GetDownloadedMapsUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val getRouteUseCase: GetRouteUseCase = mockk()
    private val getOdometerUseCase: GetOdometerUseCase = mockk()
    private val getDownloadedMapsUseCase: GetDownloadedMapsUseCase = mockk()
    private val importRouteUseCase: ImportRouteUseCase = mockk()
    private val resetPartialDistanceUseCase: ResetPartialDistanceUseCase = mockk()
    private val resetAllDistancesUseCase: ResetAllDistancesUseCase = mockk()
    private val incrementPartialDistanceUseCase: IncrementPartialDistanceUseCase = mockk()
    private val decrementPartialDistanceUseCase: DecrementPartialDistanceUseCase = mockk()
    private val setPartialDistanceUseCase: SetPartialDistanceUseCase = mockk()
    private val routeRepository: RouteRepository = mockk(relaxed = true)
    
    private lateinit var viewModel: MainViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val routeFlow = MutableStateFlow<ResourceState<Route>>(ResourceState.Loading)
    private val odometerFlow = MutableStateFlow(Odometer())
    private val mapsFlow = MutableStateFlow<List<org.giste.rn2viewer.domain.model.MapFile>>(emptyList())
    private val scrollFlow = MutableStateFlow(ScrollPosition())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getRouteUseCase() } returns routeFlow
        every { getOdometerUseCase() } returns odometerFlow
        every { getDownloadedMapsUseCase() } returns mapsFlow
        every { routeRepository.getSavedScrollPosition() } returns scrollFlow
        
        viewModel = MainViewModel(
            getRouteUseCase = getRouteUseCase,
            getOdometerUseCase = getOdometerUseCase,
            getDownloadedMapsUseCase = getDownloadedMapsUseCase,
            importRouteUseCase = importRouteUseCase,
            resetPartialDistanceUseCase = resetPartialDistanceUseCase,
            resetAllDistancesUseCase = resetAllDistancesUseCase,
            incrementPartialDistanceUseCase = incrementPartialDistanceUseCase,
            decrementPartialDistanceUseCase = decrementPartialDistanceUseCase,
            setPartialDistanceUseCase = setPartialDistanceUseCase,
            routeRepository = routeRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Uri::class)
    }

    @Test
    fun `initial state should be Empty based on use case emission`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        
        routeFlow.value = ResourceState.Empty

        val state = viewModel.uiState.value
        assertTrue("Expected Empty state but was ${state.roadbook}", state.roadbook is RoadbookUiState.Empty)
        assertEquals(0.0, state.odometer.total, 0.0)
    }

    @Test
    fun `when route is emitted as Success, state should be Success`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        
        // Given
        val route = Route(name = "Test Route", waypoints = emptyList())
        routeFlow.value = ResourceState.Success(route)

        // Then
        val state = viewModel.uiState.value
        assertTrue("Expected Success state but was ${state.roadbook}", state.roadbook is RoadbookUiState.Success)
        assertEquals(route, (state.roadbook as RoadbookUiState.Success).route)
    }

    @Test
    fun `onWaypointVisible should save position in repository`() = runTest(testDispatcher) {
        viewModel.onWaypointVisible(index = 5, offset = 100)
        
        coVerify { routeRepository.saveScrollPosition(ScrollPosition(5, 100)) }
    }

    @Test
    fun `setPartialDistance should call the use case`() = runTest(testDispatcher) {
        coEvery { setPartialDistanceUseCase(any()) } returns Unit

        viewModel.setPartialDistance(1234.0)

        coVerify { setPartialDistanceUseCase(1234.0) }
    }

    @Test
    fun `showSetPartialDialog should update uiState`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.showSetPartialDialog()

        assertTrue(viewModel.uiState.value.showSetPartialDialog)
    }

    @Test
    fun `hideSetPartialDialog should update uiState`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }

        viewModel.showSetPartialDialog()
        viewModel.hideSetPartialDialog()

        assertTrue(!viewModel.uiState.value.showSetPartialDialog)
    }
}
