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

package org.giste.rn2viewer.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.giste.rn2viewer.data.database.entity.LocalMapMetadataEntity

@Dao
interface LocalMapMetadataDao {
    @Query("SELECT * FROM local_map_metadata")
    fun getAllMetadata(): Flow<List<LocalMapMetadataEntity>>

    @Query("SELECT * FROM local_map_metadata WHERE mapId = :mapId")
    suspend fun getMetadataById(mapId: String): LocalMapMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: LocalMapMetadataEntity)

    @Query("DELETE FROM local_map_metadata WHERE mapId = :mapId")
    suspend fun deleteMetadata(mapId: String)
}
