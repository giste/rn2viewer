package org.giste.rn2viewer.domain.repositories

import kotlinx.coroutines.flow.Flow
import org.giste.rn2viewer.domain.model.Route

interface RouteRepository {
    suspend fun saveRoute(route: Route)
    fun loadRoute(): Flow<Route?>
}