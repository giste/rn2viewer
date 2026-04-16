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

package org.giste.rn2viewer.domain.usecases

import android.location.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import org.giste.rn2viewer.domain.model.Odometer
import org.giste.rn2viewer.domain.model.UserLocation
import org.giste.rn2viewer.domain.repositories.LocationRepository
import org.giste.rn2viewer.domain.repositories.OdometerRepository
import javax.inject.Inject

/**
 * Use case to observe and update the odometer.
 *
 * It acts as the business engine:
 * 1. Observes the GPS stream.
 * 2. Calculates distance deltas between high-accuracy fixes.
 * 3. Updates the persistent storage via the repository.
 * 4. Exposes the reactive odometer state.
 */
class GetOdometerUseCase @Inject constructor(
    private val odometerRepository: OdometerRepository,
    private val locationRepository: LocationRepository
) {
    private var lastLocation: UserLocation? = null

    operator fun invoke(): Flow<Odometer> = combine(
        odometerRepository.odometer,
        locationRepository.getLocations()
            .onStart { lastLocation = null }
            .onEach { location ->
                processLocation(location)
            }
            .onStart { emit(UserLocation(0.0, 0.0, 0.0, 999f, 0f, 0f, 0L)) }
    ) { odometer, _ -> odometer }

    private suspend fun processLocation(location: UserLocation) {
        if (location.accuracy > 20f) return

        val last = lastLocation
        lastLocation = location
        if (last == null) return

        val delta = calculateDistance(last, location)
        if (delta > 0) {
            odometerRepository.updateDistance(delta)
        }
    }

    private fun calculateDistance(start: UserLocation, end: UserLocation): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            results
        )
        return results[0].toDouble()
    }
}
