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

package org.giste.rn2viewer.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.giste.rn2viewer.domain.model.Odometer

/**
 * Interface to provide distance persistence (Single Source of Truth).
 */
interface OdometerRepository {
    /**
     * Flow of the current odometer state (total and partial distances).
     */
    val odometer: Flow<Odometer>

    /**
     * Updates the persistent odometer by adding a delta distance to both total and partial.
     */
    suspend fun updateDistance(delta: Double)

    /**
     * Updates only the partial distance by adding a delta.
     */
    suspend fun updatePartialDistance(delta: Double)

    /**
     * Resets the partial distance to zero in persistent storage.
     */
    suspend fun resetPartialDistance()

    /**
     * Resets both total and partial distances to zero in persistent storage.
     */
    suspend fun resetAllDistances()

    /**
     * Sets the partial distance to a specific value.
     */
    suspend fun setPartialDistance(distance: Double)
}
