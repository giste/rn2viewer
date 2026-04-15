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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.repositories.RouteRepository
import org.giste.rn2viewer.domain.usecases.ImportRouteUseCase
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    private val importRouteUseCase: ImportRouteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            routeRepository.loadRoute().collect { route ->
                _uiState.value = _uiState.value.copy(
                    route = route,
                    error = null
                )
            }
        }
    }

    fun importRoute(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            importRouteUseCase(uri.toString()).onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onWaypointSelected(index: Int) {
        _uiState.value = _uiState.value.copy(currentWaypointIndex = index)
    }
}

data class MainUiState(
    val route: Route? = null,
    val currentWaypointIndex: Int = 0,
    val totalDistance: Double = 0.0,
    val partialDistance: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)
