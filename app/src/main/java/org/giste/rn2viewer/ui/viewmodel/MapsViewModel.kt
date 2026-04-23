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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.giste.rn2viewer.domain.model.MapCategory
import org.giste.rn2viewer.domain.model.MapFile
import org.giste.rn2viewer.domain.model.RemoteMapInfo
import org.giste.rn2viewer.domain.usecases.maps.DeleteMapUseCase
import org.giste.rn2viewer.domain.usecases.maps.DownloadMapUseCase
import org.giste.rn2viewer.domain.usecases.maps.GetAvailableMapsUseCase
import org.giste.rn2viewer.domain.usecases.maps.GetDownloadedMapsUseCase
import org.giste.rn2viewer.domain.usecases.maps.RefreshDownloadedMapsUseCase
import org.giste.rn2viewer.domain.usecases.maps.SelectMapUseCase
import org.giste.rn2viewer.domain.usecases.settings.GetSettingsUseCase
import javax.inject.Inject

data class MapsUiState(
    val downloadedMaps: List<MapFile> = emptyList(),
    val availableCategories: List<MapCategory> = emptyList(),
    val downloadingMaps: Map<String, Float> = emptyMap(), // id -> progress
    val selectedMapPath: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MapsViewModel @Inject constructor(
    getDownloadedMapsUseCase: GetDownloadedMapsUseCase,
    getAvailableMapsUseCase: GetAvailableMapsUseCase,
    getSettingsUseCase: GetSettingsUseCase,
    private val deleteMapUseCase: DeleteMapUseCase,
    private val refreshDownloadedMapsUseCase: RefreshDownloadedMapsUseCase,
    private val downloadMapUseCase: DownloadMapUseCase,
    private val selectMapUseCase: SelectMapUseCase
) : ViewModel() {

    private val _downloadingMaps = MutableStateFlow<Map<String, Float>>(emptyMap())
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MapsUiState> = combine(
        getDownloadedMapsUseCase(),
        getAvailableMapsUseCase(),
        getSettingsUseCase(),
        _downloadingMaps,
        _errorMessage
    ) { downloaded, available, settings, downloading, error ->
        MapsUiState(
            downloadedMaps = downloaded,
            availableCategories = available,
            downloadingMaps = downloading,
            selectedMapPath = settings.selectedMapPath,
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
            refreshDownloadedMapsUseCase()
        }
    }

    fun deleteMap(mapFile: MapFile) {
        viewModelScope.launch {
            if (uiState.value.selectedMapPath == mapFile.path) {
                selectMapUseCase(null)
            }
            deleteMapUseCase(mapFile)
        }
    }

    fun selectMap(mapFile: MapFile?) {
        viewModelScope.launch {
            selectMapUseCase(mapFile?.path)
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
}
