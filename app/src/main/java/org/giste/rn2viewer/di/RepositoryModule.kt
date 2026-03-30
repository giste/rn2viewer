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
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
