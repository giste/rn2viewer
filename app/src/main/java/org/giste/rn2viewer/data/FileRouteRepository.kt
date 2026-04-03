/*
 * Rn2 Viewer
 * Copyright (C) 2024  Giste
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

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.repositories.RouteRepository
import java.io.File
import javax.inject.Inject

class FileRouteRepository @Inject constructor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RouteRepository {

    companion object {
        private const val ROUTE_FILE_NAME = "current_route.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    override suspend fun saveRoute(route: Route) {
        val jsonString = json.encodeToString(route)
        val file = File(context.filesDir, ROUTE_FILE_NAME)
        file.writeText(jsonString)
    }

    override fun loadRoute(): Flow<Route?> = flow {
        val file = File(context.filesDir, ROUTE_FILE_NAME)

        if (file.exists()) {
            val jsonString = file.readText()
            val route = json.decodeFromString<Route>(jsonString)
            emit(route)
        } else {
            emit(null)
        }
    }.catch { _ -> emit(null) }
        .flowOn(ioDispatcher)
}
