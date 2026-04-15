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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.giste.rn2viewer.data.FileRouteRepository
import org.giste.rn2viewer.data.AndroidGpsRepository
import org.giste.rn2viewer.data.AndroidOdometerRepository
import org.giste.rn2viewer.domain.repositories.GpsRepository
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
        ioDispatcher: CoroutineDispatcher
    ): RouteRepository {
        return FileRouteRepository(context, ioDispatcher)
    }

    @Provides
    @Singleton
    fun provideGpsRepository(
        @ApplicationContext context: Context
    ): GpsRepository {
        return AndroidGpsRepository(context)
    }

    @Provides
    @Singleton
    fun provideOdometerRepository(
        gpsRepository: GpsRepository
    ): OdometerRepository {
        return AndroidOdometerRepository(gpsRepository)
    }

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
