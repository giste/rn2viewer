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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.giste.rn2viewer.di.IoDispatcher
import org.giste.rn2viewer.domain.JsonRouteResponse
import org.giste.rn2viewer.domain.model.ScrollPosition
import org.giste.rn2viewer.domain.repositories.RouteRepository
import timber.log.Timber
import javax.inject.Inject

class ImportRouteUseCase @Inject constructor(
    private val routeRepository: RouteRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(uriString: String): Result<Unit> = withContext(ioDispatcher) {
        Timber.d("Invoking import for: $uriString")
        
        // 1. Strict extension check
        if (!uriString.lowercase().endsWith(".rn2")) {
            Timber.e("Invalid file extension: $uriString")
            return@withContext Result.failure(IllegalArgumentException("Only .rn2 files are supported"))
        }

        routeRepository.getExternalRouteContent(uriString).fold(
            onSuccess = { jsonString ->
                try {
                    Timber.d("Validating JSON structure before saving...")
                    // 2. Validation step: try to parse it. 
                    // This assures the file is only stored if deserialization succeeds.
                    JsonRouteResponse.fromJson(jsonString)
                    
                    Timber.d("JSON validated, saving raw content and resetting position...")
                    routeRepository.saveRouteRaw(jsonString)
                    routeRepository.saveScrollPosition(ScrollPosition(0, 0))
                    Result.success(Unit)
                } catch (e: Exception) {
                    Timber.e(e, "Invalid route content or error saving")
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
