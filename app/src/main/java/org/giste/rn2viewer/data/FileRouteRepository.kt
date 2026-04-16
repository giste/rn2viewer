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
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
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
        private const val ROUTE_FILE_NAME = "roadbook.json"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val repositoryScope = CoroutineScope(ioDispatcher + SupervisorJob())

    // A SharedFlow to push manual updates (like after saving)
    private val updates = MutableSharedFlow<Route?>(replay = 1)

    // The single source of truth, loaded on demand
    private val routeState: StateFlow<Route?> = flow {
        // 1. Initial load from disk
        val file = File(context.filesDir, ROUTE_FILE_NAME)
        Log.d("FileRouteRepository", "Loading initial route from ${file.absolutePath}")
        val initialRoute = if (file.exists()) {
            try {
                val jsonString = file.readText()
                Log.d("FileRouteRepository", "Found file, size: ${jsonString.length}")
                json.decodeFromString<Route>(jsonString)
            } catch (e: Exception) {
                Log.e("FileRouteRepository", "Error decoding initial route", e)
                null
            }
        } else {
            Log.d("FileRouteRepository", "No route file found at startup")
            null
        }
        
        emit(initialRoute)
        
        // 2. Then emit all future manual updates
        Log.d("FileRouteRepository", "Starting to listen for updates")
        emitAll(updates.onEach { Log.d("FileRouteRepository", "New update received: ${it?.name}") })
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000), // Keep alive for 5s after last subscriber leaves
        initialValue = null
    )

    override suspend fun saveRoute(route: Route) {
        withContext(ioDispatcher) {
            try {
                val jsonString = json.encodeToString(route)
                val file = File(context.filesDir, ROUTE_FILE_NAME)
                file.writeText(jsonString)
                Log.d("FileRouteRepository", "Saved route: ${route.name}, size: ${jsonString.length}")
                updates.emit(route)
                Log.d("FileRouteRepository", "Emitted update to SharedFlow")
            } catch (e: Exception) {
                Log.e("FileRouteRepository", "Error saving route", e)
            }
        }
    }

    override fun loadRoute(): Flow<Route?> = routeState

    override suspend fun getExternalRouteContent(uriString: String): Result<String> = withContext(ioDispatcher) {
        try {
            val uri = uriString.toUri()
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: return@withContext Result.failure(Exception("Could not open file"))
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
