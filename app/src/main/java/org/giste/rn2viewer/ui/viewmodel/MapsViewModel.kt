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
 * along with this program.  See <https://www.gnu.org/licenses/>.
 */

package org.giste.rn2viewer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.giste.rn2viewer.domain.model.MapCategory
import org.giste.rn2viewer.domain.model.MapFile
import org.giste.rn2viewer.domain.model.MapWithStatus
import org.giste.rn2viewer.domain.model.RemoteMapInfo
import org.giste.rn2viewer.domain.usecases.maps.*
import javax.inject.Inject

data class MapsUiState(
    val maps: List<MapWithStatus> = emptyList(),
    val downloadedMaps: List<MapFile> = emptyList(),
    val availableCategories: List<MapCategory> = emptyList(),
    val downloadingMaps: Map<String, Float> = emptyMap(), // id -> progress
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MapsViewModel @Inject constructor(
    getMapStatusListUseCase: GetMapStatusListUseCase,
    private val deleteMapUseCase: DeleteMapUseCase,
    private val refreshDownloadedMapsUseCase: RefreshDownloadedMapsUseCase,
    private val refreshAvailableMapsUseCase: RefreshAvailableMapsUseCase,
    private val downloadMapUseCase: DownloadMapUseCase
) : ViewModel() {

    private val _downloadingMaps = MutableStateFlow<Map<String, Float>>(emptyMap())
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MapsUiState> = combine(
        getMapStatusListUseCase(),
        _downloadingMaps,
        _errorMessage
    ) { maps, downloading, error ->
        MapsUiState(
            maps = maps,
            downloadedMaps = maps.filter { it.localFile != null }.mapNotNull { it.localFile },
            availableCategories = deriveCategories(maps),
            downloadingMaps = downloading,
            isLoading = false,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapsUiState(isLoading = true)
    )

    init {
        refreshMaps()
    }

    fun refreshMaps() {
        viewModelScope.launch {
            refreshAvailableMapsUseCase()
            refreshDownloadedMapsUseCase()
        }
    }

    fun deleteMap(mapFile: MapFile) {
        viewModelScope.launch {
            deleteMapUseCase(mapFile)
        }
    }

    fun downloadMap(mapInfo: RemoteMapInfo) {
        if (_downloadingMaps.value.containsKey(mapInfo.id)) return

        viewModelScope.launch {
            _downloadingMaps.update { it + (mapInfo.id to 0f) }
            val result = downloadMapUseCase(mapInfo) { progress ->
                _downloadingMaps.update { it + (mapInfo.id to progress) }
            }
            
            _downloadingMaps.update { it - mapInfo.id }
            
            result.onFailure { error ->
                _errorMessage.value = "Failed to download ${mapInfo.name}: ${error.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun deriveCategories(maps: List<MapWithStatus>): List<MapCategory> {
        return maps.mapNotNull { it.remoteInfo }
            .groupBy { it.continent }
            .map { (continent, mapsInfo) ->
                MapCategory(continent, mapsInfo)
            }
            .sortedBy { it.name }
    }
}
