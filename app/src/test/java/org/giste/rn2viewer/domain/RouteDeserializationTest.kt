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
