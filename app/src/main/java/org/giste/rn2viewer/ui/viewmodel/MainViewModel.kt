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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.usecases.ImportRouteUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getRouteUseCase: org.giste.rn2viewer.domain.usecases.GetRouteUseCase,
    private val importRouteUseCase: ImportRouteUseCase
) : ViewModel() {

    private val _isImporting = MutableStateFlow(false)
    private val _importError = MutableStateFlow<String?>(null)

    // In the future, these will come from their own Repositories (GPS, Maps)
    private val _totalDistance = MutableStateFlow(0.0)
    private val _partialDistance = MutableStateFlow(0.0)

    val uiState: StateFlow<MainUiState> = combine(
        getRouteUseCase(),
        _isImporting,
        _importError,
        _totalDistance,
        _partialDistance
    ) { route, isImporting, error, total, partial ->
        
        val roadbookState = when {
            isImporting -> RoadbookUiState.Loading
            error != null -> RoadbookUiState.Error(error)
            route != null -> RoadbookUiState.Success(route)
            else -> RoadbookUiState.Empty
        }

        MainUiState(
            roadbook = roadbookState,
            totalDistance = total,
            partialDistance = partial
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun importRoute(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            _importError.value = null
            importRouteUseCase(uri.toString()).onFailure { e ->
                _importError.value = e.message
            }
            _isImporting.value = false
        }
    }
}

/**
 * Represents the full screen state, composed of independent modules.
 */
data class MainUiState(
    val roadbook: RoadbookUiState = RoadbookUiState.Empty,
    val totalDistance: Double = 0.0,
    val partialDistance: Double = 0.0
)

/**
 * Specifically handles the Roadbook (Route) lifecycle.
 */
sealed interface RoadbookUiState {
    data object Loading : RoadbookUiState
    data object Empty : RoadbookUiState
    data class Error(val message: String) : RoadbookUiState
    data class Success(val route: Route) : RoadbookUiState
}
