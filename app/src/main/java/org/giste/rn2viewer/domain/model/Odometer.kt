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

package org.giste.rn2viewer.domain.model

/**
 * Represents the current state of the odometer.
 * Distances are in meters.
 * Values are automatically clamped to valid limits upon initialization.
 */
class Odometer(
    total: Double = 0.0,
    partial: Double = 0.0
) {
    companion object {
        const val MAX_TOTAL_METERS = 9999900.0 // 9999.9 km
        const val MAX_PARTIAL_METERS = 999990.0 // 999.99 km
        const val MIN_DISTANCE_METERS = 0.0
    }

    val total: Double = total.coerceIn(MIN_DISTANCE_METERS, MAX_TOTAL_METERS)
    val partial: Double = partial.coerceIn(MIN_DISTANCE_METERS, MAX_PARTIAL_METERS)

    /**
     * Returns a copy of the odometer with optionally updated values.
     * New values are automatically clamped.
     */
    fun copy(
        total: Double = this.total,
        partial: Double = this.partial
    ): Odometer = Odometer(total, partial)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Odometer) return false
        return total == other.total && partial == other.partial
    }

    override fun hashCode(): Int {
        var result = total.hashCode()
        result = 31 * result + partial.hashCode()
        return result
    }

    override fun toString(): String {
        return "Odometer(total=$total, partial=$partial)"
    }
}
