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
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.usecases.GetRouteUseCase
import org.giste.rn2viewer.domain.usecases.ImportRouteUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val getRouteUseCase: GetRouteUseCase = mockk()
    private val importRouteUseCase: ImportRouteUseCase = mockk()
    private lateinit var viewModel: MainViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val routeFlow = MutableStateFlow<Route?>(null)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getRouteUseCase() } returns routeFlow
        viewModel = MainViewModel(getRouteUseCase, importRouteUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Uri::class)
    }

    @Test
    fun `initial state should be Empty based on use case emission`() = runTest(testDispatcher) {
        // We need to collect the flow to trigger SharingStarted.WhileSubscribed
        backgroundScope.launch { viewModel.uiState.collect() }
        
        // Given: Use case emits null
        routeFlow.value = null

        // Then
        val state = viewModel.uiState.value
        assertTrue("Expected Empty state but was ${state.roadbook}", state.roadbook is RoadbookUiState.Empty)
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
        
        // 1. Create a Deferred to control when the use case finishes
        val deferredResult = CompletableDeferred<Result<Unit>>()
        
        coEvery { importRouteUseCase(any()) } coAnswers {
            deferredResult.await() // Suspension point
        }

        // 2. Start the import
        viewModel.importRoute(uri)

        // 3. Verify that it is currently in Loading state
        assertTrue(
            "Expected Loading state during import, but was ${viewModel.uiState.value.roadbook}",
            viewModel.uiState.value.roadbook is RoadbookUiState.Loading
        )

        // 4. Simulate the successful completion of the repository
        routeFlow.value = Route(name = "Imported", waypoints = emptyList())
        deferredResult.complete(Result.success(Unit))

        // 5. Verify the final Success state
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
}
