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

import org.giste.rn2viewer.domain.repositories.OdometerRepository
import javax.inject.Inject

/**
 * Use case to decrement the partial distance by a fixed step (10 meters).
 */
class DecrementPartialDistanceUseCase @Inject constructor(
    private val repository: OdometerRepository
) {
    suspend operator fun invoke() {
        repository.updatePartialDistance(-10.0)
    }
}
