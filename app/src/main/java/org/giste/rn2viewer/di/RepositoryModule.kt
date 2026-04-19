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

package org.giste.rn2viewer.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.giste.rn2viewer.data.FileRouteRepository
import org.giste.rn2viewer.data.GpsLocationRepository
import org.giste.rn2viewer.data.AndroidOdometerRepository
import org.giste.rn2viewer.di.qualifiers.OdometerDataStore
import org.giste.rn2viewer.domain.repositories.LocationRepository
import org.giste.rn2viewer.domain.repositories.OdometerRepository
import org.giste.rn2viewer.domain.repositories.RouteRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRouteRepository(
        @ApplicationContext context: Context,
        @OdometerDataStore dataStore: DataStore<Preferences>,
        ioDispatcher: CoroutineDispatcher
    ): RouteRepository {
        return FileRouteRepository(context, dataStore, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context
    ): LocationRepository {
        return GpsLocationRepository(context)
    }

    @Provides
    @Singleton
    fun provideOdometerRepository(
        @OdometerDataStore dataStore: DataStore<Preferences>
    ): OdometerRepository {
        return AndroidOdometerRepository(dataStore)
    }

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
