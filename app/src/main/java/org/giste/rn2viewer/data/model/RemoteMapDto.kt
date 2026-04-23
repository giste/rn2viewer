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

package org.giste.rn2viewer.data.model

import kotlinx.serialization.Serializable
import org.giste.rn2viewer.domain.model.MapCategory
import org.giste.rn2viewer.domain.model.RemoteMapInfo

@Serializable
data class RemoteMapInfoDto(
    val id: String,
    val name: String,
    val relativeUrl: String,
    val size: Long,
    val continent: String
) {
    fun toDomain() = RemoteMapInfo(
        id = id,
        name = name,
        relativeUrl = relativeUrl,
        size = size,
        continent = continent
    )
}

@Serializable
data class MapCategoryDto(
    val name: String,
    val maps: List<RemoteMapInfoDto>
) {
    fun toDomain() = MapCategory(
        name = name,
        maps = maps.map { it.toDomain() }
    )
}
