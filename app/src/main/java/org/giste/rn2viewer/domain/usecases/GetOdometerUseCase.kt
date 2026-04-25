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

package org.giste.rn2viewer.domain.usecases

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.giste.rn2viewer.domain.model.Odometer
import org.giste.rn2viewer.domain.model.UserLocation
import org.giste.rn2viewer.domain.model.settings.AppSettings
import org.giste.rn2viewer.domain.repositories.LocationRepository
import org.giste.rn2viewer.domain.repositories.OdometerRepository
import org.giste.rn2viewer.domain.repositories.SettingsRepository
import org.giste.rn2viewer.domain.utils.DistanceUtils
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
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository
) {
    private var lastLocation: UserLocation? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Odometer> = settingsRepository.getSettings()
        .onStart { lastLocation = null } // Reset when a NEW collector starts
        .flatMapLatest { settings ->
            combine(
                odometerRepository.odometer,
                locationRepository.getLocations()
                    .onEach { location ->
                        processLocation(location, settings)
                    }
                    .onStart { emit(UserLocation(0.0, 0.0, 0.0, 999f, null, 0f, 0f, 0L)) }
            ) { odometer, _ -> odometer }
        }

    private suspend fun processLocation(location: UserLocation, settings: AppSettings) {
        if (location.accuracy > settings.odometerMinAccuracy) return

        val last = lastLocation
        if (last == null) {
            lastLocation = location
            return
        }

        // Ignore updates if the user is effectively stopped to avoid GPS jitter "drifting" the odometer
        if (location.speed < settings.odometerSpeedThreshold) return

        lastLocation = location
        val delta = DistanceUtils.calculateDistance(
            start = last,
            end = location,
            verticalAccuracyThreshold = settings.odometerMinVerticalAccuracy
        )

        if (delta > 0) {
            odometerRepository.updateDistance(delta)
        }
    }
}
