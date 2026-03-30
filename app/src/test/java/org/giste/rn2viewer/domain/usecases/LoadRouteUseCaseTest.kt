package org.giste.rn2viewer.domain.usecases

import org.giste.rn2viewer.domain.JsonRouteData
import org.giste.rn2viewer.domain.JsonRouteResponse
import org.giste.rn2viewer.domain.JsonWaypoint
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class LoadRouteUseCaseTest {

    private val loadRouteUseCase = LoadRouteUseCase()

    @Test
    fun `invoke should correctly map JsonRouteData to Route domain model`() {
        // Given: A mock JsonRouteData with some waypoints
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
                    ele = 100.0,
                    show = true
                ),
                JsonWaypoint(
                    tUuid = "uuid-2",
                    waypointId = 1,
                    lat = 40.001, // Approx 111.19m north
                    lon = -3.0,
                    ele = 110.0,
                    show = false
                ),
                JsonWaypoint(
                    tUuid = "uuid-3",
                    waypointId = 2,
                    lat = 40.002, // Another approx 111.19m north
                    lon = -3.0,
                    ele = 120.0,
                    show = true
                )
            )
        )

        // When: We invoke the use case
        val route = loadRouteUseCase(jsonRouteData)

        // Then: Basic metadata should be mapped correctly
        assertEquals("Test Route", route.name)
        assertEquals("A route for testing", route.description)

        // And: Only 2 tulips should be created (waypoints 0 and 2 have show=true)
        assertEquals(2, route.tulips.size)

        // Verify the first tulip
        val firstTulip = route.tulips[0]
        assertEquals(1, firstTulip.number)
        assertEquals(40.0, firstTulip.latitude, 0.0001)
        assertEquals(0.0, firstTulip.distance, 0.1)
        assertEquals(0.0, firstTulip.distanceFromPrevious, 0.1)

        // Verify the second tulip (from the third waypoint)
        val secondTulip = route.tulips[1]
        assertEquals(2, secondTulip.number)
        assertEquals(40.002, secondTulip.latitude, 0.0001)
        
        // Total distance should include the hidden waypoint (approx 222.4m total)
        assertEquals(222.4, secondTulip.distance, 0.5)
        assertEquals(111.2, secondTulip.distanceFromPrevious, 0.5)
    }

    @Test
    fun `invoke with example resource should generate tulips with correct reset distances`() {
        // Given: The example JSON file from test resources
        val jsonString = File("src/test/resources/route_example.json").readText()
        val response = JsonRouteResponse.fromJson(jsonString)

        // When: We invoke the use case
        val route = loadRouteUseCase(response.route)

        // Then: There should be exactly 4 tulips (waypointId 0, 5, 7 and 11 have show=true)
        assertEquals(4, route.tulips.size)

        // Verify distances in kilometers (converting from meters)
        // Waypoint 0 (Start): 0.0 km
        assertEquals(0.0, route.tulips[0].distance / 1000.0, 0.005)
        
        // Waypoint 5 (After ~113m): ~0.11 km
        assertEquals(0.11, route.tulips[1].distance / 1000.0, 0.005)
        
        // Waypoint 7 (Contains Reset Note): Should be 0.0 km instead of 0.13 km
        assertEquals(0.13, route.tulips[2].distance / 1000.0, 0.001)
        
        // Waypoint 11 (After ~238m from waypoint 7): Should be ~0.24 km since reset
        assertEquals(0.16, route.tulips[3].distance / 1000.0, 0.005)
    }

    @Test
    fun `invoke with empty waypoints should return empty tulips`() {
        val jsonRouteData = JsonRouteData(version = 1, waypoints = emptyList())
        val route = loadRouteUseCase(jsonRouteData)
        assertEquals(0, route.tulips.size)
    }
}
