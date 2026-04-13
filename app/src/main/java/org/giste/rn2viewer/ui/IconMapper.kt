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
    private const val CROSS_FUEL_ZONE_ID = "e5167bd4-314b-47d3-ba23-708182be76a9"
    private const val CROSS_RESET_DISTANCE_ID = "308c7365-bc3f-451b-9e98-531e9015024f"
    private const val LANDMARK_ABOVE_BRIDGE_ID = "a49a0b2e-3be5-4659-8251-8205fd4e9571"
    private const val LANDMARK_FORT_CASTLE_ID = "da5ec2a7-612a-411f-aeb2-d1f9514d3dc7"
    private const val LANDMARK_HOUSE_ID = "3965bf45-97ee-4c6b-b087-0e128510c4e3"
    private const val LANDMARK_TRAFFIC_LIGHT_ID = "1d752896-09fd-498d-b416-21f31a356be5"
    private const val LANDMARK_TUNNEL_ID = "0539c8e3-393b-4416-8002-b30700cf68de"
    private const val LANDMARK_UNDER_BRIDGE_ID = "79f8c10f-d67b-4ba5-bf12-6a801ed79ed3"
    private const val SIGN_ALERT_ID = "2598a2c0-6a8b-4dc5-8211-8ad64d986bde"
    private const val SIGN_ROUNDABOUT_ID = "5d157992-6013-4bef-86cb-92fea891944c"
    private const val SIGN_STOP_ID = "5a4ced4c-68e2-41d3-a1b4-9c8b86ec2109"
    private const val TERRAIN_RIVER_WATER_ID = "aabe9acd-ab1b-467d-9bbb-877bb0d0da23"

    fun getDrawableId(iconId: String): Int {
        return when (iconId) {
            CROSS_DANGER_1_ID -> R.drawable.ic_cross_danger_1
            CROSS_DANGER_2_ID -> R.drawable.ic_cross_danger_2
            CROSS_DANGER_3_ID -> R.drawable.ic_cross_danger_3
            CROSS_FUEL_ZONE_ID -> R.drawable.ic_cross_fuel_zone
            CROSS_RESET_DISTANCE_ID -> R.drawable.ic_cross_reset_distance
            LANDMARK_ABOVE_BRIDGE_ID -> R.drawable.ic_landmark_above_bridge
            LANDMARK_FORT_CASTLE_ID -> R.drawable.ic_landmark_fort_castle_1
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
