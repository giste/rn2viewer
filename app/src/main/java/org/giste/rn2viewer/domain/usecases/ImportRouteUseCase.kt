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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.giste.rn2viewer.domain.repositories.RouteRepository
import timber.log.Timber
import javax.inject.Inject

class ImportRouteUseCase @Inject constructor(
    private val routeRepository: RouteRepository
) {
    suspend operator fun invoke(uriString: String): Result<Unit> = withContext(Dispatchers.IO) {
        Timber.d("Invoking import for: $uriString")
        routeRepository.getExternalRouteContent(uriString).fold(
            onSuccess = { jsonString ->
                try {
                    Timber.d("JSON received, length: ${jsonString.length}")
                    routeRepository.saveRouteRaw(jsonString)
                    Result.success(Unit)
                } catch (e: Exception) {
                    Timber.e(e, "Error saving raw route")
                    Result.failure(e)
                }
            },
            onFailure = { 
                Timber.e(it, "Error getting external content")
                Result.failure(it) 
            }
        )
    }
}
