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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.giste.rn2viewer.domain.model.Odometer
import org.giste.rn2viewer.domain.repositories.OdometerRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure SSOT (Single Source of Truth) implementation of [OdometerRepository].
 * It only handles persistent storage using DataStore.
 */
@Singleton
class AndroidOdometerRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : OdometerRepository {

    companion object {
        private val TOTAL_DISTANCE_KEY = doublePreferencesKey("total_distance")
        private val PARTIAL_DISTANCE_KEY = doublePreferencesKey("partial_distance")
    }

    override val odometer: Flow<Odometer> = dataStore.data
        .map { prefs ->
            Odometer(
                total = prefs[TOTAL_DISTANCE_KEY] ?: 0.0,
                partial = prefs[PARTIAL_DISTANCE_KEY] ?: 0.0
            )
        }

    override suspend fun updateDistance(delta: Double) {
        dataStore.edit { prefs ->
            val currentTotal = prefs[TOTAL_DISTANCE_KEY] ?: 0.0
            val currentPartial = prefs[PARTIAL_DISTANCE_KEY] ?: 0.0
            prefs[TOTAL_DISTANCE_KEY] = currentTotal + delta
            prefs[PARTIAL_DISTANCE_KEY] = currentPartial + delta
        }
    }

    override suspend fun resetPartialDistance() {
        dataStore.edit { it[PARTIAL_DISTANCE_KEY] = 0.0 }
    }

    override suspend fun resetAllDistances() {
        dataStore.edit {
            it[TOTAL_DISTANCE_KEY] = 0.0
            it[PARTIAL_DISTANCE_KEY] = 0.0
        }
    }
}
