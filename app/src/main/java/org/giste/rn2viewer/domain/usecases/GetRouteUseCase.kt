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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.giste.rn2viewer.domain.mappers.Rn2Mapper
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.repositories.RouteRepository
import javax.inject.Inject

/**
 * Use case to retrieve the current route from the repository.
 * It observes the raw data and maps it to the domain model using Rn2Mapper.
 */
class GetRouteUseCase @Inject constructor(
    private val repository: RouteRepository,
    private val mapper: Rn2Mapper
) {
    operator fun invoke(): Flow<Route?> = repository.loadRouteRaw().map { jsonString ->
        jsonString?.let { mapper.mapToDomain(it) }
    }
}
