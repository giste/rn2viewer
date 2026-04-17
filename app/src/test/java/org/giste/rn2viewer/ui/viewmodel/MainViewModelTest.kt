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
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.CompletableDeferred
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
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.usecases.DecrementPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.GetOdometerUseCase
import org.giste.rn2viewer.domain.usecases.GetRouteUseCase
import org.giste.rn2viewer.domain.usecases.ImportRouteUseCase
import org.giste.rn2viewer.domain.usecases.IncrementPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.ResetAllDistancesUseCase
import org.giste.rn2viewer.domain.usecases.ResetPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.SetPartialDistanceUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val getRouteUseCase: GetRouteUseCase = mockk()
    private val getOdometerUseCase: GetOdometerUseCase = mockk()
    private val importRouteUseCase: ImportRouteUseCase = mockk()
    private val resetPartialDistanceUseCase: ResetPartialDistanceUseCase = mockk()
    private val resetAllDistancesUseCase: ResetAllDistancesUseCase = mockk()
    private val incrementPartialDistanceUseCase: IncrementPartialDistanceUseCase = mockk()
    private val decrementPartialDistanceUseCase: DecrementPartialDistanceUseCase = mockk()
    private val setPartialDistanceUseCase: SetPartialDistanceUseCase = mockk()
    
    private lateinit var viewModel: MainViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val routeFlow = MutableStateFlow<Route?>(null)
    private val odometerFlow = MutableStateFlow(Odometer())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getRouteUseCase() } returns routeFlow
        every { getOdometerUseCase() } returns odometerFlow
        
        viewModel = MainViewModel(
            getRouteUseCase = getRouteUseCase,
            getOdometerUseCase = getOdometerUseCase,
            importRouteUseCase = importRouteUseCase,
            resetPartialDistanceUseCase = resetPartialDistanceUseCase,
            resetAllDistancesUseCase = resetAllDistancesUseCase,
            incrementPartialDistanceUseCase = incrementPartialDistanceUseCase,
            decrementPartialDistanceUseCase = decrementPartialDistanceUseCase,
            setPartialDistanceUseCase = setPartialDistanceUseCase
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
        
        routeFlow.value = null

        val state = viewModel.uiState.value
        assertTrue("Expected Empty state but was ${state.roadbook}", state.roadbook is RoadbookUiState.Empty)
        assertEquals(0.0, state.odometer.total, 0.0)
    }

    @Test
    fun `when odometer emits new values, uiState should be updated`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }

        // Given
        val newOdometer = Odometer(total = 1000.0, partial = 500.0)
        odometerFlow.value = newOdometer

        // Then
        assertEquals(newOdometer, viewModel.uiState.value.odometer)
    }

    @Test
    fun `resetPartialDistance should call the use case`() = runTest(testDispatcher) {
        coEvery { resetPartialDistanceUseCase() } returns Unit

        viewModel.resetPartialDistance()

        coVerify { resetPartialDistanceUseCase() }
    }

    @Test
    fun `resetAllDistances should call the use case`() = runTest(testDispatcher) {
        coEvery { resetAllDistancesUseCase() } returns Unit

        viewModel.resetAllDistances()

        coVerify { resetAllDistancesUseCase() }
    }

    @Test
    fun `incrementPartialDistance should call the use case`() = runTest(testDispatcher) {
        coEvery { incrementPartialDistanceUseCase() } returns Unit

        viewModel.incrementPartialDistance()

        coVerify { incrementPartialDistanceUseCase() }
    }

    @Test
    fun `decrementPartialDistance should call the use case`() = runTest(testDispatcher) {
        coEvery { decrementPartialDistanceUseCase() } returns Unit

        viewModel.decrementPartialDistance()

        coVerify { decrementPartialDistanceUseCase() }
    }

    @Test
    fun `when route is emitted, state should be Success`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        
        // Given
        val route = Route(name = "Test Route", waypoints = emptyList())
        routeFlow.value = route

        // Then
        val state = viewModel.uiState.value
        assertTrue("Expected Success state but was ${state.roadbook}", state.roadbook is RoadbookUiState.Success)
        assertEquals(route, (state.roadbook as RoadbookUiState.Success).route)
    }

    @Test
    fun `importRoute should update state through loading and success`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        
        val uri = mockk<Uri>()
        mockkStatic(Uri::class)
        every { uri.toString() } returns "content://test"
        
        val deferredResult = CompletableDeferred<Result<Unit>>()
        
        coEvery { importRouteUseCase(any()) } coAnswers {
            deferredResult.await()
        }

        viewModel.importRoute(uri)

        assertTrue(
            "Expected Loading state during import, but was ${viewModel.uiState.value.roadbook}",
            viewModel.uiState.value.roadbook is RoadbookUiState.Loading
        )

        routeFlow.value = Route(name = "Imported", waypoints = emptyList())
        deferredResult.complete(Result.success(Unit))

        val finalState = viewModel.uiState.value
        assertTrue(finalState.roadbook is RoadbookUiState.Success)
        assertEquals("Imported", (finalState.roadbook as RoadbookUiState.Success).route.name)
    }

    @Test
    fun `importRoute should show error when it fails and no previous route exists`() = runTest(testDispatcher) {
        backgroundScope.launch { viewModel.uiState.collect() }
        
        val uri = mockk<Uri>()
        mockkStatic(Uri::class)
        every { uri.toString() } returns "content://test"
        routeFlow.value = null
        coEvery { importRouteUseCase(any()) } returns Result.failure(Exception("Error message"))

        // When
        viewModel.importRoute(uri)

        // Then
        val state = viewModel.uiState.value
        assertTrue("Expected Error state but was ${state.roadbook}", state.roadbook is RoadbookUiState.Error)
        assertEquals("Error message", (state.roadbook as RoadbookUiState.Error).message)
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
