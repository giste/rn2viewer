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

package org.giste.rn2viewer.data

import android.location.Location
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.giste.rn2viewer.domain.model.Odometer
import org.giste.rn2viewer.domain.model.UserLocation
import org.giste.rn2viewer.domain.repositories.GpsRepository
import org.giste.rn2viewer.domain.repositories.OdometerRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [OdometerRepository] that calculates distance by consuming [GpsRepository].
 */
@Singleton
class AndroidOdometerRepository @Inject constructor(
    gpsRepository: GpsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) : OdometerRepository {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _odometerState = MutableStateFlow(Odometer())
    
    private var lastLocation: UserLocation? = null

    /**
     * This flow drives the odometer calculation.
     * It only collects from GpsRepository when there's at least one subscriber.
     */
    private val odometerDriver = gpsRepository.getLocations()
        .onStart { lastLocation = null }
        .onEach { processLocation(it) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    override val odometer: StateFlow<Odometer> = _odometerState.asStateFlow()
        .combine(odometerDriver) { state, _ -> state }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), Odometer())

    private fun processLocation(location: UserLocation) {
        if (location.accuracy > 20f) return

        lastLocation?.let { last ->
            val results = FloatArray(1)
            Location.distanceBetween(
                last.latitude, last.longitude,
                location.latitude, location.longitude,
                results
            )
            val delta = results[0].toDouble()

            _odometerState.value = _odometerState.value.let { current ->
                current.copy(
                    total = current.total + delta,
                    partial = current.partial + delta
                )
            }
        }
        lastLocation = location
    }

    override fun resetPartialDistance() {
        _odometerState.value = _odometerState.value.copy(partial = 0.0)
    }

    override fun resetAllDistances() {
        _odometerState.value = Odometer()
    }
}
