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

package org.giste.rn2viewer.data.location

import android.location.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.data.AndroidOdometerRepository
import org.giste.rn2viewer.domain.model.UserLocation
import org.giste.rn2viewer.domain.repositories.GpsRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidOdometerRepositoryTest {

    private val gpsRepository: GpsRepository = mockk()
    private lateinit var gpsFlow: MutableSharedFlow<UserLocation>
    private lateinit var odometerRepository: AndroidOdometerRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        gpsFlow = MutableSharedFlow(replay = 1)
        every { gpsRepository.getLocations() } returns gpsFlow
        mockkStatic(Location::class)
        odometerRepository = AndroidOdometerRepository(gpsRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        unmockkStatic(Location::class)
    }

    @Test
    fun `initial distances should be zero`() = runTest(testDispatcher) {
        assertEquals(0.0, odometerRepository.odometer.value.total, 0.0)
        assertEquals(0.0, odometerRepository.odometer.value.partial, 0.0)
    }

    @Test
    fun `should accumulate distance from GPS updates`() = runTest(testDispatcher) {
        // Start collecting to activate the odometer logic
        val job = backgroundScope.launch { odometerRepository.odometer.collect { } }

        // Mock distanceBetween for any coordinates
        every { 
            Location.distanceBetween(any(), any(), any(), any(), any()) 
        } answers {
            val results = lastArg<FloatArray>()
            results[0] = 111.0f
        }

        // Send first location (sets lastLocation)
        gpsFlow.emit(createLocation(40.0, -3.0))
        runCurrent()
        
        // Send second location (calculates delta)
        gpsFlow.emit(createLocation(40.001, -3.0))
        runCurrent()

        assertEquals(111.0, odometerRepository.odometer.value.total, 0.001)
        assertEquals(111.0, odometerRepository.odometer.value.partial, 0.001)
        job.cancel()
    }

    @Test
    fun `should ignore points with poor accuracy`() = runTest(testDispatcher) {
        val job = backgroundScope.launch { odometerRepository.odometer.collect { } }

        val loc1 = createLocation(40.0, -3.0, accuracy = 10f)
        val loc2 = createLocation(40.1, -3.1, accuracy = 50f) // Poor accuracy

        gpsFlow.emit(loc1)
        gpsFlow.emit(loc2)

        assertEquals(0.0, odometerRepository.odometer.value.total, 0.0)
        job.cancel()
    }

    @Test
    fun `resetPartialDistance should only reset partial distance`() = runTest(testDispatcher) {
        val job = backgroundScope.launch { odometerRepository.odometer.collect { } }

        val loc1 = createLocation(40.0, -3.0)
        val loc2 = createLocation(40.001, -3.0)
        
        every { Location.distanceBetween(any(), any(), any(), any(), any()) } answers {
            lastArg<FloatArray>()[0] = 100.0f
        }

        gpsFlow.emit(loc1)
        gpsFlow.emit(loc2)

        odometerRepository.resetPartialDistance()

        assertEquals(100.0, odometerRepository.odometer.value.total, 0.0)
        assertEquals(0.0, odometerRepository.odometer.value.partial, 0.0)
        job.cancel()
    }

    @Test
    fun `resetAllDistances should reset everything`() = runTest(testDispatcher) {
        val job = backgroundScope.launch { odometerRepository.odometer.collect { } }
        
        every { Location.distanceBetween(any(), any(), any(), any(), any()) } answers {
            lastArg<FloatArray>()[0] = 100.0f
        }

        gpsFlow.emit(createLocation(40.0, -3.0))
        gpsFlow.emit(createLocation(40.001, -3.0))

        odometerRepository.resetAllDistances()

        assertEquals(0.0, odometerRepository.odometer.value.total, 0.0)
        assertEquals(0.0, odometerRepository.odometer.value.partial, 0.0)
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
