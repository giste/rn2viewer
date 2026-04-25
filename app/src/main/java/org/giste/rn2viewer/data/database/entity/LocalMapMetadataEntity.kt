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

package org.giste.rn2viewer.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.giste.rn2viewer.domain.model.LocalMapMetadata

@Entity(tableName = "local_map_metadata")
data class LocalMapMetadataEntity(
    @PrimaryKey val mapId: String,
    val fileName: String,
    val serverLastModified: Long,
    val size: Long
) {
    fun toDomain() = LocalMapMetadata(
        mapId = mapId,
        fileName = fileName,
        serverLastModified = serverLastModified,
        size = size
    )

    companion object {
        fun fromDomain(domain: LocalMapMetadata) = LocalMapMetadataEntity(
            mapId = domain.mapId,
            fileName = domain.fileName,
            serverLastModified = domain.serverLastModified,
            size = domain.size
        )
    }
}
