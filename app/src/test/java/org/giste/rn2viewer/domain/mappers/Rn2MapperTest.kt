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

package org.giste.rn2viewer.domain.mappers

import org.giste.rn2viewer.domain.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Rn2MapperTest {

    private lateinit var mapper: Rn2Mapper
    private val resetId = "308c7365-bc3f-451b-9e98-531e9015024f"

    @Before
    fun setup() {
        mapper = Rn2Mapper()
    }

    @Test
    fun `mapToDomain should parse JSON and calculate distances correctly including resets`() {
        // Given
        val jsonString = """
            {
                "route": {
                    "version": 4,
                    "name": "Test Route",
                    "waypoints": [
                        {
                            "t_uuid": "uuid-1",
                            "waypointid": 0,
                            "lat": 40.0,
                            "lon": -3.0,
                            "show": true,
                            "tulip": {"elements": []},
                            "notes": {"elements": []}
                        },
                        {
                            "t_uuid": "uuid-2",
                            "waypointid": 1,
                            "lat": 40.001,
                            "lon": -3.0,
                            "show": false,
                            "tulip": {"elements": []},
                            "notes": {"elements": []}
                        },
                        {
                            "t_uuid": "uuid-3",
                            "waypointid": 2,
                            "lat": 40.002,
                            "lon": -3.0,
                            "show": true,
                            "tulip": {"elements": []},
                            "notes": {
                                "elements": [
                                    {
                                        "id": "$resetId",
                                        "type": "Icon"
                                    }
                                ]
                            }
                        }
                    ]
                }
            }
        """.trimIndent()

        // When
        val route = mapper.mapToDomain(jsonString)

        // Then
        // Check that only shown waypoints are kept (uuid-2 is show:false)
        assertEquals(2, route.waypoints.size)
        assertEquals(1, route.waypoints[0].number)
        assertEquals(2, route.waypoints[1].number)

        // Check distance calculation (40.0, -3.0 to 40.002, -3.0 is ~222.4 meters)
        assertEquals(0.0, route.waypoints[0].distance, 0.1)
        assertEquals(222.4, route.waypoints[1].distance, 0.5)
        assertTrue(route.waypoints[1].reset)
        
        // Check partial distance: from uuid-1 to uuid-3 (uuid-2 is hidden but distance is accumulated)
        assertEquals(222.4, route.waypoints[1].distanceFromPrevious, 0.5)
    }

    @Test
    fun `mapToDomain should calculate roadOut end point when missing based on next waypoint`() {
        // Given
        val jsonString = """
            {
                "route": {
                    "name": "Test Route",
                    "waypoints": [
                        {
                            "lat": 40.0,
                            "lon": -3.0,
                            "show": true,
                            "tulip": {
                                "elements": [
                                    {
                                        "type": "Track",
                                        "roadIn": {"start": {"x": 0, "y": 40}, "end": {"x": 0, "y": 0}},
                                        "roadOut": {"start": {"x": 0, "y": 0}}
                                    }
                                ]
                            },
                            "notes": {"elements": []}
                        },
                        {
                            "lat": 40.001,
                            "lon": -3.0,
                            "show": true,
                            "tulip": {"elements": []},
                            "notes": {"elements": []}
                        }
                    ]
                }
            }
        """.trimIndent()

        // When
        val route = mapper.mapToDomain(jsonString)

        // Then
        val track = route.waypoints[0].tulipElements[0] as Track
        // Next waypoint is North (40.0 -> 40.001). Bearing 0. 
        // RN2 angle -PI/2 (up). Point should be on top boundary (y = -85)
        assertEquals(0.0, track.roadOut.end!!.x, 0.1)
        assertEquals(-85.0, track.roadOut.end.y, 0.1)
    }
}
