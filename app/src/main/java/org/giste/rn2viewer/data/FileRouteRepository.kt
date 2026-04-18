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
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.giste.rn2viewer.domain.repositories.RouteRepository
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FileRouteRepository @Inject constructor(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : RouteRepository {

    companion object {
        private const val ROUTE_FILE_NAME = "roadbook.json"
    }

    private val repositoryScope = CoroutineScope(ioDispatcher + SupervisorJob())

    // A SharedFlow to push manual updates (like after saving)
    private val updates = MutableSharedFlow<String?>(replay = 1)

    // The single source of truth, loaded on demand
    private val routeState: StateFlow<String?> = flow {
        // 1. Initial load from disk
        val file = File(context.filesDir, ROUTE_FILE_NAME)
        Timber.d("Loading initial route from ${file.absolutePath}")
        val initialRoute = if (file.exists()) {
            try {
                val jsonString = file.readText()
                Timber.d("Found file, size: ${jsonString.length}")
                jsonString
            } catch (e: Exception) {
                Timber.e(e, "Error reading initial route")
                null
            }
        } else {
            Timber.d("No route file found at startup")
            null
        }
        
        emit(initialRoute)
        
        // 2. Then emit all future manual updates
        Timber.d("Starting to listen for updates")
        emitAll(updates.onEach { Timber.d("New update received, size: ${it?.length ?: 0}") })
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000), // Keep alive for 5s after last subscriber leaves
        initialValue = null
    )

    override suspend fun saveRouteRaw(jsonContent: String) {
        withContext(ioDispatcher) {
            try {
                val file = File(context.filesDir, ROUTE_FILE_NAME)
                file.writeText(jsonContent)
                Timber.d("Saved route content, size: ${jsonContent.length}")
                updates.emit(jsonContent)
                Timber.d("Emitted update to SharedFlow")
            } catch (e: Exception) {
                Timber.e(e, "Error saving route")
            }
        }
    }

    override fun loadRouteRaw(): Flow<String?> = routeState

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
