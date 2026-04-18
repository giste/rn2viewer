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
import org.giste.rn2viewer.domain.model.Odometer
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.usecases.DecrementPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.GetOdometerUseCase
import org.giste.rn2viewer.domain.usecases.GetRouteUseCase
import org.giste.rn2viewer.domain.usecases.ImportRouteUseCase
import org.giste.rn2viewer.domain.usecases.IncrementPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.ResetAllDistancesUseCase
import org.giste.rn2viewer.domain.usecases.ResetPartialDistanceUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getRouteUseCase: GetRouteUseCase,
    getOdometerUseCase: GetOdometerUseCase,
    private val importRouteUseCase: ImportRouteUseCase,
    private val resetPartialDistanceUseCase: ResetPartialDistanceUseCase,
    private val resetAllDistancesUseCase: ResetAllDistancesUseCase,
    private val incrementPartialDistanceUseCase: IncrementPartialDistanceUseCase,
    private val decrementPartialDistanceUseCase: DecrementPartialDistanceUseCase,
    private val setPartialDistanceUseCase: org.giste.rn2viewer.domain.usecases.SetPartialDistanceUseCase,
    private val routeRepository: org.giste.rn2viewer.domain.repositories.RouteRepository
) : ViewModel() {

    private val _isImporting = MutableStateFlow(false)
    private val _importError = MutableStateFlow<String?>(null)
    private val _showSetPartialDialog = MutableStateFlow(false)

    val uiState: StateFlow<MainUiState> = combine(
        getRouteUseCase().onStart { emit(null) },
        _isImporting,
        _importError,
        getOdometerUseCase().onStart { emit(Odometer()) },
        _showSetPartialDialog,
        routeRepository.getSavedWaypointIndex(),
        routeRepository.getSavedWaypointOffset()
    ) { args: Array<Any?> ->
        val route = args[0] as Route?
        val isImporting = args[1] as Boolean
        val error = args[2] as String?
        val odometer = args[3] as Odometer
        val showSetPartialDialog = args[4] as Boolean
        val savedIndex = args[5] as Int
        val savedOffset = args[6] as Int

        Timber.d("UI State update: showDialog=$showSetPartialDialog, savedIndex=$savedIndex, savedOffset=$savedOffset")
        val roadbookState = when {
            isImporting -> RoadbookUiState.Loading
            error != null -> RoadbookUiState.Error(error)
            route != null -> RoadbookUiState.Success(route)
            else -> RoadbookUiState.Empty
        }

        MainUiState(
            roadbook = roadbookState,
            odometer = odometer,
            showSetPartialDialog = showSetPartialDialog,
            initialWaypointIndex = savedIndex,
            initialWaypointOffset = savedOffset
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun showSetPartialDialog() {
        _showSetPartialDialog.value = true
    }

    fun hideSetPartialDialog() {
        _showSetPartialDialog.value = false
    }

    fun importRoute(uri: Uri) {
        Timber.d("Importing route from URI: $uri")
        viewModelScope.launch {
            _isImporting.value = true
            _importError.value = null
            importRouteUseCase(uri.toString()).onFailure { e ->
                Timber.e(e, "Import failed")
                _importError.value = e.message
            }.onSuccess {
                Timber.d("Import successful")
            }
            _isImporting.value = false
        }
    }

    fun resetPartialDistance() {
        viewModelScope.launch {
            resetPartialDistanceUseCase()
        }
    }

    fun resetAllDistances() {
        viewModelScope.launch {
            resetAllDistancesUseCase()
        }
    }

    fun incrementPartialDistance() {
        viewModelScope.launch {
            incrementPartialDistanceUseCase()
        }
    }

    fun decrementPartialDistance() {
        viewModelScope.launch {
            decrementPartialDistanceUseCase()
        }
    }

    fun setPartialDistance(distance: Double) {
        viewModelScope.launch {
            setPartialDistanceUseCase(distance)
        }
    }

    fun onWaypointVisible(index: Int, offset: Int) {
        viewModelScope.launch {
            routeRepository.saveWaypointPosition(index, offset)
        }
    }
}

/**
 * Represents the full screen state, composed of independent modules.
 */
data class MainUiState(
    val roadbook: RoadbookUiState = RoadbookUiState.Empty,
    val odometer: Odometer = Odometer(),
    val showSetPartialDialog: Boolean = false,
    val initialWaypointIndex: Int = 0,
    val initialWaypointOffset: Int = 0
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
