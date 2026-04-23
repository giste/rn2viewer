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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.giste.rn2viewer.domain.model.MapCategory
import org.giste.rn2viewer.domain.model.MapFile
import org.giste.rn2viewer.domain.usecases.maps.DeleteMapUseCase
import org.giste.rn2viewer.domain.usecases.maps.GetAvailableMapsUseCase
import org.giste.rn2viewer.domain.usecases.maps.GetDownloadedMapsUseCase
import org.giste.rn2viewer.domain.usecases.maps.RefreshDownloadedMapsUseCase
import javax.inject.Inject

data class MapsUiState(
    val downloadedMaps: List<MapFile> = emptyList(),
    val availableCategories: List<MapCategory> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class MapsViewModel @Inject constructor(
    getDownloadedMapsUseCase: GetDownloadedMapsUseCase,
    getAvailableMapsUseCase: GetAvailableMapsUseCase,
    private val deleteMapUseCase: DeleteMapUseCase,
    private val refreshDownloadedMapsUseCase: RefreshDownloadedMapsUseCase
) : ViewModel() {

    val uiState: StateFlow<MapsUiState> = combine(
        getDownloadedMapsUseCase(),
        getAvailableMapsUseCase()
    ) { downloaded, available ->
        MapsUiState(
            downloadedMaps = downloaded,
            availableCategories = available,
            isLoading = false
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
            deleteMapUseCase(mapFile)
        }
    }
}
