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

package org.giste.rn2viewer.domain.usecases

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.repositories.RouteRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImportRouteUseCaseTest {

    private val routeRepository = mockk<RouteRepository>(relaxed = true)
    private lateinit var importRouteUseCase: ImportRouteUseCase

    private val resetId = "308c7365-bc3f-451b-9e98-531e9015024f"

    @Before
    fun setup() {
        importRouteUseCase = ImportRouteUseCase(routeRepository)
    }

    @Test
    fun `invoke should parse JSON and calculate distances correctly including resets`() = runTest {
        // Given
        val uriString = "content://path/to/file"
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

        coEvery { routeRepository.getExternalRouteContent(uriString) } returns Result.success(jsonString)
        val routeSlot = slot<Route>()
        coEvery { routeRepository.saveRoute(capture(routeSlot)) } returns Unit

        // When
        val result = importRouteUseCase(uriString)

        // Then
        assertTrue(result.isSuccess)
        val savedRoute = routeSlot.captured
        
        // Check that only shown waypoints are kept (uuid-2 is show:false)
        assertEquals(2, savedRoute.waypoints.size)
        // The code assigns consecutive numbers to visible waypoints
        assertEquals(1, savedRoute.waypoints[0].number)
        assertEquals(2, savedRoute.waypoints[1].number)

        // Check distance calculation (40.0, -3.0 to 40.002, -3.0 is ~222.4 meters)
        // uuid-1 is at 0.0m
        // uuid-2 (hidden) is at ~111.2m
        // uuid-3 is at ~222.4m
        assertEquals(0.0, savedRoute.waypoints[0].distance, 0.1)
        assertEquals(222.4, savedRoute.waypoints[1].distance, 0.5)
        assertTrue(savedRoute.waypoints[1].reset)
    }
}
