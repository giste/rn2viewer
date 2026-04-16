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

import android.location.Location
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.domain.model.Odometer
import org.giste.rn2viewer.domain.model.UserLocation
import org.giste.rn2viewer.domain.repositories.LocationRepository
import org.giste.rn2viewer.domain.repositories.OdometerRepository
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetOdometerUseCaseTest {

    private val odometerRepository: OdometerRepository = mockk()
    private val locationRepository: LocationRepository = mockk()
    private val gpsFlow = MutableSharedFlow<UserLocation>()
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var getOdometerUseCase: GetOdometerUseCase

    @Before
    fun setup() {
        every { locationRepository.getLocations() } returns gpsFlow
        every { odometerRepository.odometer } returns flowOf(Odometer(0.0, 0.0))
        coEvery { odometerRepository.updateDistance(any()) } returns Unit
        
        mockkStatic(Location::class)
        getOdometerUseCase = GetOdometerUseCase(odometerRepository, locationRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Location::class)
    }

    @Test
    fun `should ignore first location fix and not update repository`() = runTest(testDispatcher) {
        val job = backgroundScope.launch { getOdometerUseCase().collect {} }

        gpsFlow.emit(createLocation(40.0, -3.0))

        coVerify(exactly = 0) { odometerRepository.updateDistance(any()) }
        job.cancel()
    }

    @Test
    fun `should calculate distance between two valid fixes and update repository`() = runTest(testDispatcher) {
        val job = backgroundScope.launch { getOdometerUseCase().collect {} }

        every { 
            Location.distanceBetween(any(), any(), any(), any(), any()) 
        } answers {
            val results = lastArg<FloatArray>()
            results[0] = 100.0f
        }

        gpsFlow.emit(createLocation(40.0, -3.0))
        gpsFlow.emit(createLocation(40.1, -3.1))

        coVerify(exactly = 1) { odometerRepository.updateDistance(100.0) }
        job.cancel()
    }

    @Test
    fun `should ignore fixes with poor accuracy`() = runTest(testDispatcher) {
        val job = backgroundScope.launch { getOdometerUseCase().collect {} }

        gpsFlow.emit(createLocation(40.0, -3.0, accuracy = 10f))
        gpsFlow.emit(createLocation(40.1, -3.1, accuracy = 50f)) // Poor accuracy

        coVerify(exactly = 0) { odometerRepository.updateDistance(any()) }
        job.cancel()
    }

    @Test
    fun `should resume calculation after a poor accuracy fix`() = runTest(testDispatcher) {
        val job = backgroundScope.launch { getOdometerUseCase().collect {} }

        every { Location.distanceBetween(any(), any(), any(), any(), any()) } answers {
            lastArg<FloatArray>()[0] = 50.0f
        }

        gpsFlow.emit(createLocation(40.0, -3.0, accuracy = 10f)) // Valid 1
        gpsFlow.emit(createLocation(40.1, -3.1, accuracy = 50f)) // Ignored
        gpsFlow.emit(createLocation(40.2, -3.2, accuracy = 10f)) // Valid 2

        // Distance should be between Valid 1 and Valid 2
        coVerify(exactly = 1) { odometerRepository.updateDistance(50.0) }
        job.cancel()
    }

    private fun createLocation(lat: Double, lon: Double, accuracy: Float = 5f): UserLocation {
        return UserLocation(
            latitude = lat,
            longitude = lon,
            altitude = 0.0,
            accuracy = accuracy,
            speed = 0f,
            bearing = 0f,
            time = System.currentTimeMillis()
        )
    }
}
