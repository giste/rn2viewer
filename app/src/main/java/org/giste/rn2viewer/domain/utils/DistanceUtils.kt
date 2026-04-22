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

package org.giste.rn2viewer.domain.utils

import org.giste.rn2viewer.domain.model.UserLocation
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utility to calculate distances in a platform-independent way.
 */
object DistanceUtils {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates the horizontal 2D distance between two points.
     */
    fun calculateDistance2D(start: UserLocation, end: UserLocation): Double {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates the distance between two points.
     * Uses 3D distance if both points have good vertical accuracy,
     * otherwise falls back to 2D horizontal distance.
     */
    fun calculateDistance(
        start: UserLocation, 
        end: UserLocation, 
        verticalAccuracyThreshold: Float = 10.0f
    ): Double {
        val horizontalDistance = calculateDistance2D(start, end)

        val canUseAltitude = start.verticalAccuracy != null && end.verticalAccuracy != null &&
                start.verticalAccuracy <= verticalAccuracyThreshold && 
                end.verticalAccuracy <= verticalAccuracyThreshold

        return if (canUseAltitude) {
            val heightDistance = end.altitude - start.altitude
            sqrt(horizontalDistance * horizontalDistance + heightDistance * heightDistance)
        } else {
            horizontalDistance
        }
    }
}
