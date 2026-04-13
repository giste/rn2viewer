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
import org.giste.rn2viewer.domain.model.Icon

object IconMapper {
    fun getDrawableId(icon: Icon): Int {
        return when (icon) {
            is Icon.Danger1 -> R.drawable.ic_cross_danger_1
            is Icon.Danger2 -> R.drawable.ic_cross_danger_2
            is Icon.Danger3 -> R.drawable.ic_cross_danger_3
            is Icon.FuelZone -> R.drawable.ic_cross_fuel_zone
            is Icon.ResetDistance -> R.drawable.ic_cross_reset_distance
            is Icon.AboveBridge -> R.drawable.ic_landmark_above_bridge
            is Icon.FortCastle -> R.drawable.ic_landmark_fort_castle_1
            is Icon.House -> R.drawable.ic_landmark_house
            is Icon.TrafficLight -> R.drawable.ic_landmark_traffic_light
            is Icon.Tunnel -> R.drawable.ic_landmark_tunnel
            is Icon.UnderBridge -> R.drawable.ic_landmark_under_bridge
            is Icon.Alert -> R.drawable.ic_sign_alert
            is Icon.Roundabout -> R.drawable.ic_sign_roundabout
            is Icon.Stop -> R.drawable.ic_sign_stop
            is Icon.RiverWater -> R.drawable.ic_terrain_river_water
            is Icon.Unknown -> R.drawable.ic_sign_alert
        }
    }
}
