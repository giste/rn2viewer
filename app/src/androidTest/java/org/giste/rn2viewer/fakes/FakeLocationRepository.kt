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

package org.giste.rn2viewer.fakes

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.giste.rn2viewer.domain.model.UserLocation
import org.giste.rn2viewer.domain.repositories.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeLocationRepository @Inject constructor() : LocationRepository {
    private val _locations = MutableSharedFlow<UserLocation>(replay = 1)

    override fun getLocations(): Flow<UserLocation> = _locations

    suspend fun emit(location: UserLocation) {
        _locations.emit(location)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun reset() {
        _locations.resetReplayCache()
    }
}
