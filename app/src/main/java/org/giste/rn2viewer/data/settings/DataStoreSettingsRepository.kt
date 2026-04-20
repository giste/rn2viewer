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
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.giste.rn2viewer.domain.model.settings.AppOrientation
import org.giste.rn2viewer.domain.model.settings.AppSettings
import org.giste.rn2viewer.domain.model.settings.AppTheme
import org.giste.rn2viewer.domain.repositories.SettingsRepository
import org.giste.rn2viewer.di.qualifiers.SettingsDataStore
import javax.inject.Inject

class DataStoreSettingsRepository @Inject constructor(
    @param:SettingsDataStore private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val THEME = stringPreferencesKey("app_theme")
        val ORIENTATION = stringPreferencesKey("app_orientation")
        val SHORT_DISTANCE_THRESHOLD = doublePreferencesKey("short_distance_threshold")
    }

    override fun getSettings(): Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            theme = preferences[Keys.THEME]?.let { AppTheme.valueOf(it) } ?: AppTheme.FOLLOW_SYSTEM,
            orientation = preferences[Keys.ORIENTATION]?.let { AppOrientation.valueOf(it) } ?: AppOrientation.FOLLOW_SYSTEM,
            shortDistanceThreshold = preferences[Keys.SHORT_DISTANCE_THRESHOLD] ?: 300.0
        )
    }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }

    override suspend fun setOrientation(orientation: AppOrientation) {
        dataStore.edit { preferences ->
            preferences[Keys.ORIENTATION] = orientation.name
        }
    }

    override suspend fun setShortDistanceThreshold(threshold: Double) {
        dataStore.edit { preferences ->
            preferences[Keys.SHORT_DISTANCE_THRESHOLD] = threshold
        }
    }
}
