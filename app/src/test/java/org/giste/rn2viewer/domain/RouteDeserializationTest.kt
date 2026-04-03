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

package org.giste.rn2viewer.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class RouteDeserializationTest {

    @Test
    fun `deserialization of route_example json works`() {
        // Read the file from test resources
        val jsonString = File("src/test/resources/route_example.json").readText()

        // Deserialize using the factory method in the companion object
        val response = JsonRouteResponse.fromJson(jsonString)

        // Basic assertions based on the current JSON content (12 waypoints)
        assertEquals(4, response.route.version)
        assertEquals(12, response.route.waypoints.size)
        assertEquals(
            "wpt_uuid_968b3bc3-0d3e-47ac-8b99-23a063dd3766",
            response.route.waypoints[0].tUuid
        )

        println("Route loaded: ${response.route.waypoints.size} waypoints found.")
    }
}
