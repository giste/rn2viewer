/*
 * Rn2 Viewer
 * Copyright (C) 2024  Giste
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

package org.giste.rn2viewer.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Waypoint (
    val number: Int,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double = 0.0,
    val distance: Double,
    val distanceFromPrevious: Double,
    val shortDistance: Boolean = false,
    val reset: Boolean = false,
    val dangerLevel: DangerLevel = DangerLevel.NONE,
    val tulipElements: List<Element> = emptyList(),
    val notesElements: List<Element> = emptyList(),
) {
    @Serializable
    enum class DangerLevel {
        NONE,
        LOW,
        MEDIUM,
        HIGH
    }
}
