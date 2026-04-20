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

package org.giste.rn2viewer

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.giste.rn2viewer.di.qualifiers.OdometerDataStore
import org.giste.rn2viewer.domain.model.UserLocation
import org.giste.rn2viewer.domain.repositories.OdometerRepository
import org.giste.rn2viewer.domain.usecases.DecrementPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.GetOdometerUseCase
import org.giste.rn2viewer.domain.usecases.IncrementPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.ResetPartialDistanceUseCase
import org.giste.rn2viewer.domain.usecases.SetPartialDistanceUseCase
import org.giste.rn2viewer.fakes.FakeLocationRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
class OdometerIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var getOdometerUseCase: GetOdometerUseCase

    @Inject
    lateinit var incrementPartialDistanceUseCase: IncrementPartialDistanceUseCase

    @Inject
    lateinit var decrementPartialDistanceUseCase: DecrementPartialDistanceUseCase

    @Inject
    lateinit var setPartialDistanceUseCase: SetPartialDistanceUseCase

    @Inject
    lateinit var resetPartialDistanceUseCase: ResetPartialDistanceUseCase

    @Inject
    lateinit var fakeLocationRepository: FakeLocationRepository

    @Inject
    lateinit var odometerRepository: OdometerRepository

    @Inject
    @OdometerDataStore
    lateinit var odometerDataStore: DataStore<Preferences>

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun testOdometerUpdateOnRoute() = runBlocking {
        // Reset odometer
        odometerRepository.resetAllDistances()

        // Initial location
        val loc1 = UserLocation(
            latitude = 40.0,
            longitude = -3.0,
            altitude = 0.0,
            accuracy = 5f,
            verticalAccuracy = 5f,
            speed = 0f,
            bearing = 0f,
            time = System.currentTimeMillis()
        )

        // Move ~111.19 meters North (0.001 degrees latitude)
        val loc2 = UserLocation(
            latitude = 40.001,
            longitude = -3.0,
            altitude = 0.0,
            accuracy = 5f,
            verticalAccuracy = 5f,
            speed = 0f,
            bearing = 0f,
            time = System.currentTimeMillis() + 1000
        )

        // Start collecting the odometer flow
        val odometerFlow = getOdometerUseCase()
        
        // Use a single collection job to drive the internal logic
        val collectionJob = launch {
            odometerFlow.collect { }
        }

        // Wait a bit for the flow to stabilize
        kotlinx.coroutines.delay(500)

        // Emit locations
        fakeLocationRepository.emit(loc1)
        kotlinx.coroutines.delay(100)
        fakeLocationRepository.emit(loc2)

        // Wait for the odometer to reach the expected distance
        try {
            withTimeout(15000) {
                val result = odometerFlow
                    .filter { it.total > 0 }
                    .first()

                assertEquals(111.19, result.total, 0.1)
                assertEquals(111.19, result.partial, 0.1)
            }
        } finally {
            collectionJob.cancel()
        }
    }

    @Test
    fun testOdometerPersistence() = runBlocking {
        // 1. Reset and set a specific value
        odometerRepository.resetAllDistances()
        val expectedDistance = 123.45
        odometerRepository.updateDistance(expectedDistance)

        // 2. Verify it's set in the current repository
        val currentVal = odometerRepository.odometer.first()
        assertEquals(expectedDistance, currentVal.total, 0.001)

        // 3. Since we are in the same process and DataStore is a singleton, 
        // we can't easily "restart" the app in a single test without breaking DataStore rules.
        // However, verifying that it's in the DataStore is enough to prove it will 
        // persist across restarts as long as the file is the same.
        val storedValue = odometerDataStore.data.first()
        val totalDistanceKey = doublePreferencesKey("total_distance")
        
        assertEquals("Distance should be persisted in DataStore", expectedDistance, storedValue[totalDistanceKey] ?: 0.0, 0.001)
    }

    @Test
    fun testManualAdjustments() = runBlocking {
        // Reset odometer
        odometerRepository.resetAllDistances()

        val odometerFlow = getOdometerUseCase()
        val collectionJob = launch {
            odometerFlow.collect { }
        }

        kotlinx.coroutines.delay(500)

        // 1. Initial check (0.0)
        assertEquals(0.0, odometerFlow.first().partial, 0.0)

        // 2. Increment (should add 10m = 0.01 km? No, repo says 10.0, which usually means meters in my domain logic, but let's check)
        // Actually, IncrementPartialDistanceUseCase calls repository.updatePartialDistance(10.0)
        // Let's assume the unit is meters based on the comment "fixed step (10 meters)".
        
        incrementPartialDistanceUseCase()
        withTimeout(5000) {
            val result = odometerFlow.filter { it.partial == 10.0 }.first()
            assertEquals(10.0, result.partial, 0.001)
            assertEquals(0.0, result.total, 0.001) // Only partial should change
        }

        // 3. Decrement
        decrementPartialDistanceUseCase()
        withTimeout(5000) {
            val result = odometerFlow.filter { it.partial == 0.0 }.first()
            assertEquals(0.0, result.partial, 0.001)
        }

        // 4. Set specific value
        setPartialDistanceUseCase(55.5)
        withTimeout(5000) {
            val result = odometerFlow.filter { it.partial == 55.5 }.first()
            assertEquals(55.5, result.partial, 0.001)
        }

        collectionJob.cancel()
    }

    @Test
    fun testPartialResetStability() = runBlocking {
        // 1. Setup
        odometerRepository.resetAllDistances()
        val odometerFlow = getOdometerUseCase()
        val collectionJob = launch { odometerFlow.collect { } }
        kotlinx.coroutines.delay(500)

        // 2. Initial Movement (100m)
        val loc1 = UserLocation(40.0, -3.0, 0.0, 5f, 5f, 0f, 0f, 1000)
        val loc2 = UserLocation(40.0009, -3.0, 0.0, 5f, 5f, 0f, 0f, 2000) // ~100.07m

        fakeLocationRepository.emit(loc1)
        kotlinx.coroutines.delay(100)
        fakeLocationRepository.emit(loc2)

        withTimeout(5000) {
            val res = odometerFlow.filter { it.total > 0 }.first()
            assertEquals(100.07, res.total, 0.1)
            assertEquals(100.07, res.partial, 0.1)
        }

        // 3. Reset Partial
        resetPartialDistanceUseCase()
        withTimeout(5000) {
            val res = odometerFlow.filter { it.partial == 0.0 }.first()
            assertEquals(100.07, res.total, 0.1) // Total should persist
            assertEquals(0.0, res.partial, 0.001)
        }

        // 4. More Movement (another ~100m)
        val loc3 = UserLocation(40.0018, -3.0, 0.0, 5f, 5f, 0f, 0f, 3000) // ~100.07m from loc2
        fakeLocationRepository.emit(loc3)

        withTimeout(5000) {
            val res = odometerFlow.filter { it.partial > 0 }.first()
            assertEquals(200.14, res.total, 0.2)
            assertEquals(100.07, res.partial, 0.1)
        }

        collectionJob.cancel()
    }

    @Test
    fun test3DFallbackOnPoorVerticalAccuracy() = runBlocking {
        // 1. Setup
        odometerRepository.resetAllDistances()
        val odometerFlow = getOdometerUseCase()
        val collectionJob = launch { odometerFlow.collect { } }
        kotlinx.coroutines.delay(500)

        // Point A: Good horizontal, good vertical accuracy (at 0m altitude)
        val loc1 = UserLocation(40.0, -3.0, 0.0, 5f, 5f, 0f, 0f, 1000)
        
        // Point B: Good horizontal, POOR vertical accuracy (at 100m altitude)
        // Movement is ~111.19m horizontal + 100m vertical
        val loc2PoorVertical = UserLocation(40.001, -3.0, 100.0, 5f, 15f, 0f, 0f, 2000)

        fakeLocationRepository.emit(loc1)
        kotlinx.coroutines.delay(100)
        fakeLocationRepository.emit(loc2PoorVertical)

        withTimeout(5000) {
            val res = odometerFlow.filter { it.total > 0 }.first()
            // Should ignore the 100m altitude change and only use 2D distance (~111.19m)
            // If it used 3D, it would be sqrt(111.19^2 + 100^2) = ~149.52m
            assertEquals("Should fallback to 2D due to poor vertical accuracy", 111.19, res.total, 0.5)
        }

        // Point C: Good horizontal, GOOD vertical accuracy (back at 0m altitude)
        // Movement from B is ~111.19m horizontal + 100m vertical drop
        val loc3GoodVertical = UserLocation(40.002, -3.0, 0.0, 5f, 5f, 0f, 0f, 3000)
        
        fakeLocationRepository.emit(loc3GoodVertical)
        
        withTimeout(5000) {
            val res = odometerFlow.filter { it.total > 112 }.first()
            // Between B and C: B has poor vertical (15m), so it still falls back to 2D
            // Total should be 111.19 + 111.19 = 222.38m
            assertEquals("Should still fallback to 2D if one point has poor vertical accuracy", 222.38, res.total, 0.5)
        }

        // Point D: Good horizontal, GOOD vertical accuracy (climb to 100m altitude)
        // Movement from C is ~111.19m horizontal + 100m vertical climb
        val loc4GoodVertical = UserLocation(40.003, -3.0, 100.0, 5f, 5f, 0f, 0f, 4000)
        
        fakeLocationRepository.emit(loc4GoodVertical)

        withTimeout(5000) {
            val res = odometerFlow.filter { it.total > 223 }.first()
            // Between C and D: Both have good vertical accuracy (5m), so it uses 3D
            // New leg: sqrt(111.19^2 + 100^2) = 149.53m
            // Total: 222.38 + 149.53 = 371.91m
            assertEquals("Should use 3D when both points have good vertical accuracy", 371.91, res.total, 0.5)
        }

        collectionJob.cancel()
    }

    @Test
    fun testOdometerIgnoresLowAccuracyPoints() = runBlocking {
        // Reset odometer
        odometerRepository.resetAllDistances()

        val loc1 = UserLocation(
            latitude = 40.0,
            longitude = -3.0,
            altitude = 0.0,
            accuracy = 5f,
            verticalAccuracy = 5f,
            speed = 0f,
            bearing = 0f,
            time = 1000
        )

        val locLowAcc = UserLocation(
            latitude = 40.001,
            longitude = -3.0,
            altitude = 0.0,
            accuracy = 50f, // > 20m, should be ignored
            verticalAccuracy = 5f,
            speed = 0f,
            bearing = 0f,
            time = 2000
        )

        val loc2 = UserLocation(
            latitude = 40.002,
            longitude = -3.0,
            altitude = 0.0,
            accuracy = 5f,
            verticalAccuracy = 5f,
            speed = 0f,
            bearing = 0f,
            time = 3000
        )

        val odometerFlow = getOdometerUseCase()
        val collectionJob = launch {
            odometerFlow.collect { }
        }

        kotlinx.coroutines.delay(500)

        // Emit locations
        fakeLocationRepository.emit(loc1)
        kotlinx.coroutines.delay(100)
        fakeLocationRepository.emit(locLowAcc)
        kotlinx.coroutines.delay(100)
        fakeLocationRepository.emit(loc2)

        try {
            withTimeout(15000) {
                val result = odometerFlow
                    .filter { it.total > 0 }
                    .first()

                // Distance should be directly between loc1 and loc2 (~222.38m)
                // If locLowAcc wasn't ignored, the total would still be ~222.38m 
                // but via two smaller increments.
                // The key is that locLowAcc should NOT update lastLocation.
                assertEquals(222.38, result.total, 0.5)
            }
        } finally {
            collectionJob.cancel()
        }
    }
}
