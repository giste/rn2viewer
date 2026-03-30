package org.giste.rn2viewer.domain.usecases

import org.giste.rn2viewer.domain.JsonNoteElement
import org.giste.rn2viewer.domain.JsonNotes
import org.giste.rn2viewer.domain.JsonRouteData
import org.giste.rn2viewer.domain.JsonRouteResponse
import org.giste.rn2viewer.domain.JsonWaypoint
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class OpenRouteUseCaseTest {

    private val openRouteUseCase = OpenRouteUseCase()
    private val resetId = "308c7365-bc3f-451b-9e98-531e9015024f"

    @Test
    fun `invoke should correctly map JsonRouteData to Route domain model`() {
        // Given: A mock JsonRouteData with a reset waypoint at index 2
        val jsonRouteData = JsonRouteData(
            version = 4,
            name = "Test Route",
            description = "A route for testing",
            startLocation = "Start",
            endLocation = "End",
            waypoints = listOf(
                JsonWaypoint(
                    tUuid = "uuid-1",
                    waypointId = 0,
                    lat = 40.0,
                    lon = -3.0,
                    show = true
                ),
                JsonWaypoint(
                    tUuid = "uuid-2",
                    waypointId = 1,
                    lat = 40.001, // Approx 111.2m north
                    lon = -3.0,
                    show = false
                ),
                JsonWaypoint(
                    tUuid = "uuid-3",
                    waypointId = 2,
                    lat = 40.002, // Another approx 111.2m north
                    lon = -3.0,
                    show = true,
                    notes = JsonNotes(
                        elements = listOf(
                            JsonNoteElement(type = "Icon", eId = "reset-eid", id = resetId)
                        )
                    )
                ),
                JsonWaypoint(
                    tUuid = "uuid-4",
                    waypointId = 3,
                    lat = 40.003, // Another approx 111.2m north
                    lon = -3.0,
                    show = true
                )
            )
        )

        // When: We invoke the use case
        val route = openRouteUseCase(jsonRouteData)

        // Then: Basic metadata should be mapped correctly
        assertEquals("Test Route", route.name)

        // And: 3 tulips should be created (waypoints 0, 2 and 3 have show=true)
        assertEquals(3, route.waypoints.size)

        // Tulip 1 (Waypoint 0): Start point
        val tulip1 = route.waypoints[0]
        assertEquals(1, tulip1.number)
        assertEquals(0.0, tulip1.distance, 0.1)

        // Tulip 2 (Waypoint 2): Includes reset. 
        // Its own distance should still be the sum of previous segments (approx 222.4m)
        val tulip2 = route.waypoints[1]
        assertEquals(2, tulip2.number)
        assertEquals(222.4, tulip2.distance, 0.5)
        assertEquals(111.2, tulip2.distanceFromPrevious, 0.5)

        // Tulip 3 (Waypoint 3): Segment AFTER reset.
        // Its accumulated distance should start from 0 + distance from waypoint 2 (approx 111.2m)
        val tulip3 = route.waypoints[2]
        assertEquals(3, tulip3.number)
        assertEquals(111.2, tulip3.distance, 0.5)
        assertEquals(111.2, tulip3.distanceFromPrevious, 0.5)
    }

    @Test
    fun `invoke with example resource should generate tulips with correct reset distances`() {
        // Given: The example JSON file from test resources
        val jsonString = File("src/test/resources/route_example.json").readText()
        val response = JsonRouteResponse.fromJson(jsonString)

        // When: We invoke the use case
        val route = openRouteUseCase(response.route)

        // Then: There should be exactly 4 tulips (waypointId 0, 5, 7 and 11 have show=true)
        assertEquals(4, route.waypoints.size)

        // Verify distances in kilometers (converting from meters)
        // Waypoint 0 (Start): 0.0 km
        assertEquals(0.0, route.waypoints[0].distance / 1000.0, 0.005)
        
        // Waypoint 5 (After ~113m): ~0.11 km
        assertEquals(0.11, route.waypoints[1].distance / 1000.0, 0.005)
        
        // Waypoint 7 (Contains Reset Note): Should be ~0.13 km (calculated from waypoint 0)
        assertEquals(0.13, route.waypoints[2].distance / 1000.0, 0.005)
        
        // Waypoint 11 (After ~156m from waypoint 7): Should be ~0.16 km since reset at 7
        assertEquals(0.16, route.waypoints[3].distance / 1000.0, 0.005)
    }

    @Test
    fun `invoke with empty waypoints should return empty tulips`() {
        val jsonRouteData = JsonRouteData(version = 1, waypoints = emptyList())
        val route = openRouteUseCase(jsonRouteData)
        assertEquals(0, route.waypoints.size)
    }
}
