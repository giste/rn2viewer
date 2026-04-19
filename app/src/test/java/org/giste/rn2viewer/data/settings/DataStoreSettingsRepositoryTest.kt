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

package org.giste.rn2viewer.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.domain.model.settings.AppOrientation
import org.giste.rn2viewer.domain.model.settings.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSettingsRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: DataStoreSettingsRepository
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { File(temporaryFolder.newFolder(), "test_settings.preferences_pb") }
        )
        repository = DataStoreSettingsRepository(dataStore)
    }

    @Test
    fun `initial settings should be default when no data exists`() = runTest {
        val settings = repository.getSettings().first()
        assertEquals(AppTheme.FOLLOW_SYSTEM, settings.theme)
        assertEquals(AppOrientation.FOLLOW_SYSTEM, settings.orientation)
    }

    @Test
    fun `setTheme should persist theme value`() = runTest {
        repository.setTheme(AppTheme.DARK)
        
        val settings = repository.getSettings().first()
        assertEquals(AppTheme.DARK, settings.theme)

        // Verify with new instance
        val newRepo = DataStoreSettingsRepository(dataStore)
        val persisted = newRepo.getSettings().first()
        assertEquals(AppTheme.DARK, persisted.theme)
    }

    @Test
    fun `setOrientation should persist orientation value`() = runTest {
        repository.setOrientation(AppOrientation.HORIZONTAL)
        
        val settings = repository.getSettings().first()
        assertEquals(AppOrientation.HORIZONTAL, settings.orientation)

        // Verify with new instance
        val newRepo = DataStoreSettingsRepository(dataStore)
        val persisted = newRepo.getSettings().first()
        assertEquals(AppOrientation.HORIZONTAL, persisted.orientation)
    }
}
