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

package org.giste.rn2viewer.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidOdometerRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var odometerRepository: AndroidOdometerRepository
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(temporaryFolder.newFolder(), "test.preferences_pb") }
        )
        odometerRepository = AndroidOdometerRepository(dataStore)
    }

    @Test
    fun `initial distances should be zero when no data exists`() = runTest {
        val initial = odometerRepository.odometer.first()
        assertEquals(0.0, initial.total, 0.0)
        assertEquals(0.0, initial.partial, 0.0)
    }

    @Test
    fun `updateDistance should persist new values`() = runTest {
        odometerRepository.updateDistance(10.5)
        
        val updated = odometerRepository.odometer.first()
        assertEquals(10.5, updated.total, 0.0)
        assertEquals(10.5, updated.partial, 0.0)

        // Persistence check with new instance
        val newRepo = AndroidOdometerRepository(dataStore)
        val persisted = newRepo.odometer.first()
        assertEquals(10.5, persisted.total, 0.0)
    }

    @Test
    fun `resetPartialDistance should only reset partial distance`() = runTest {
        odometerRepository.updateDistance(100.0)
        odometerRepository.resetPartialDistance()

        val state = odometerRepository.odometer.first()
        assertEquals(100.0, state.total, 0.0)
        assertEquals(0.0, state.partial, 0.0)
    }

    @Test
    fun `resetAllDistances should reset everything`() = runTest {
        odometerRepository.updateDistance(100.0)
        odometerRepository.resetAllDistances()

        val state = odometerRepository.odometer.first()
        assertEquals(0.0, state.total, 0.0)
        assertEquals(0.0, state.partial, 0.0)
    }
}
