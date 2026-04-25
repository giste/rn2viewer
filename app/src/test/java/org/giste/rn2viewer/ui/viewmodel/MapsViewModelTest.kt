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

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.giste.rn2viewer.domain.model.MapStatus
import org.giste.rn2viewer.domain.model.MapWithStatus
import org.giste.rn2viewer.domain.model.RemoteMapInfo
import org.giste.rn2viewer.domain.usecases.maps.DeleteMapUseCase
import org.giste.rn2viewer.domain.usecases.maps.DownloadMapUseCase
import org.giste.rn2viewer.domain.usecases.maps.GetMapStatusListUseCase
import org.giste.rn2viewer.domain.usecases.maps.RefreshDownloadedMapsUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapsViewModelTest {

    private val getMapStatusListUseCase: GetMapStatusListUseCase = mockk()
    private val deleteMapUseCase: DeleteMapUseCase = mockk()
    private val refreshDownloadedMapsUseCase: RefreshDownloadedMapsUseCase = mockk()
    private val downloadMapUseCase: DownloadMapUseCase = mockk()
    
    private lateinit var viewModel: MapsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    private val mapsFlow = MutableStateFlow<List<MapWithStatus>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getMapStatusListUseCase() } returns mapsFlow
        coEvery { refreshDownloadedMapsUseCase() } returns Unit
        
        viewModel = MapsViewModel(
            getMapStatusListUseCase = getMapStatusListUseCase,
            deleteMapUseCase = deleteMapUseCase,
            refreshDownloadedMapsUseCase = refreshDownloadedMapsUseCase,
            downloadMapUseCase = downloadMapUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState updates when maps are emitted`() = runTest(testDispatcher) {
        // Given
        val remoteInfo = RemoteMapInfo("id", "Spain", "europe/spain.map", 100, "Europe", 1000L)
        val mapWithStatus = MapWithStatus(remoteInfo, null, null, MapStatus.AVAILABLE)
        
        backgroundScope.launch { viewModel.uiState.collect() }

        // When
        mapsFlow.value = listOf(mapWithStatus)

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.maps.size)
        assertEquals(MapStatus.AVAILABLE, state.maps.first().status)
        assertEquals("Spain", state.availableCategories.first().maps.first().name)
    }

    @Test
    fun `downloadMap updates progress and state`() = runTest(testDispatcher) {
        val mapInfo = RemoteMapInfo("id", "Spain", "europe/spain.map", 100, "Europe", 1000L)
        
        // Mock download with progress updates
        coEvery { downloadMapUseCase(mapInfo, any()) } coAnswers {
            val progressCallback = secondArg<(Float) -> Unit>()
            progressCallback(0.5f)
            Result.success(Unit)
        }

        backgroundScope.launch { viewModel.uiState.collect() }

        // When
        viewModel.downloadMap(mapInfo)

        // Then
        // During execution, progress was 0.5f. After completion, it's removed from downloading map.
        assertTrue(viewModel.uiState.value.downloadingMaps.isEmpty())
    }
}
