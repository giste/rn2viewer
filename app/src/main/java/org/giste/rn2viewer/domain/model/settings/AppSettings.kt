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

package org.giste.rn2viewer.domain.model.settings

data class AppSettings(
    val theme: AppTheme = AppTheme.FOLLOW_SYSTEM,
    val orientation: AppOrientation = AppOrientation.FOLLOW_SYSTEM,
    val shortDistanceThreshold: Double = DEFAULT_SHORT_DISTANCE_THRESHOLD,
    val odometerSpeedThreshold: Float = DEFAULT_ODOMETER_SPEED_THRESHOLD,
    val odometerMinAccuracy: Float = DEFAULT_ODOMETER_MIN_ACCURACY,
    val odometerMinVerticalAccuracy: Float = DEFAULT_ODOMETER_MIN_VERTICAL_ACCURACY
) {
    companion object {
        const val DEFAULT_SHORT_DISTANCE_THRESHOLD = 300.0
        const val DEFAULT_ODOMETER_SPEED_THRESHOLD = 0.5f // m/s
        const val DEFAULT_ODOMETER_MIN_ACCURACY = 20.0f // m
        const val DEFAULT_ODOMETER_MIN_VERTICAL_ACCURACY = 10.0f // m
    }
}
