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
import org.giste.rn2viewer.domain.usecases.GetOdometerUseCase
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
}
