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

package org.giste.rn2viewer.ui

import org.giste.rn2viewer.R

object IconMapper {
    private const val CROSS_DANGER_1_ID = "bffeadbd-116b-49a7-921e-20dff8deec4b"
    private const val CROSS_DANGER_2_ID = "a6c80c12-49b1-4e68-a21f-a6d48ef0a0ed"
    private const val CROSS_DANGER_3_ID = "fab72ac2-f809-4ddc-9a7a-c9a24768bb4e"
    private const val CROSS_FUEL_ZONE_ID = "Unknown_03"
    private const val CROSS_RESET_DISTANCE_ID = "308c7365-bc3f-451b-9e98-531e9015024f"
    private const val LANDMARK_ABOVE_BRIDGE_ID = "Unknown_07"
    private const val LANDMARK_FORT_CASTLE_1_ID = "Unknown_09"
    private const val LANDMARK_HOUSE_ID = "Unknown_02"
    private const val LANDMARK_TRAFFIC_LIGHT_ID = "Unknown_10"
    private const val LANDMARK_TUNNEL_ID = "Unknown_04"
    private const val LANDMARK_UNDER_BRIDGE_ID = "Unknown_08"
    private const val SIGN_ALERT_ID = "Unknown_11"
    private const val SIGN_ROUNDABOUT_ID = "Unknown_05"
    private const val SIGN_STOP_ID = "Unknown_01"
    private const val TERRAIN_RIVER_WATER_ID = "Unknown_06"

    fun getDrawableId(iconId: String): Int {
        return when (iconId) {
            CROSS_DANGER_1_ID -> R.drawable.ic_cross_danger_1
            CROSS_DANGER_2_ID -> R.drawable.ic_cross_danger_2
            CROSS_DANGER_3_ID -> R.drawable.ic_cross_danger_3
            CROSS_FUEL_ZONE_ID -> R.drawable.ic_cross_fuel_zone
            CROSS_RESET_DISTANCE_ID -> R.drawable.ic_cross_reset_distance
            LANDMARK_ABOVE_BRIDGE_ID -> R.drawable.ic_landmark_above_bridge
            LANDMARK_FORT_CASTLE_1_ID -> R.drawable.ic_landmark_fort_castle_1
            LANDMARK_HOUSE_ID -> R.drawable.ic_landmark_house
            LANDMARK_TRAFFIC_LIGHT_ID -> R.drawable.ic_landmark_traffic_light
            LANDMARK_TUNNEL_ID -> R.drawable.ic_landmark_tunnel
            LANDMARK_UNDER_BRIDGE_ID -> R.drawable.ic_landmark_under_bridge
            SIGN_ALERT_ID -> R.drawable.ic_sign_alert
            SIGN_ROUNDABOUT_ID -> R.drawable.ic_sign_roundabout
            SIGN_STOP_ID -> R.drawable.ic_sign_stop
            TERRAIN_RIVER_WATER_ID -> R.drawable.ic_terrain_river_water

            else -> R.drawable.ic_sign_alert
        }
    }
}
