package org.giste.rn2viewer.domain.usecases

import org.giste.rn2viewer.domain.JsonElement
import org.giste.rn2viewer.domain.JsonNotes
import org.giste.rn2viewer.domain.JsonPoint
import org.giste.rn2viewer.domain.JsonRoadIn
import org.giste.rn2viewer.domain.JsonRoadOut
import org.giste.rn2viewer.domain.JsonRouteData
import org.giste.rn2viewer.domain.JsonRouteResponse
import org.giste.rn2viewer.domain.JsonTulip
import org.giste.rn2viewer.domain.JsonWaypoint
import org.giste.rn2viewer.domain.model.Element
import org.giste.rn2viewer.domain.model.Road
import org.giste.rn2viewer.domain.model.Road.RoadType
import org.giste.rn2viewer.domain.model.Track
import org.giste.rn2viewer.domain.model.Waypoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OpenRouteUseCaseTest {

    private val openRouteUseCase = OpenRouteUseCase()
    private val resetId = "308c7365-bc3f-451b-9e98-531e9015024f"
    private val dangerLevel1 = "bffeadbd-116b-49a7-921e-20dff8deec4b"

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
                    show = true,
                ),
                JsonWaypoint(
                    tUuid = "uuid-2",
                    waypointId = 1,
                    lat = 40.001, // Approx 111.2m north
                    lon = -3.0,
                    show = false,
                ),
                JsonWaypoint(
                    tUuid = "uuid-3",
                    waypointId = 2,
                    lat = 40.002, // Another approx 111.2m north
                    lon = -3.0,
                    show = true,
                    notes = JsonNotes(
                        elements = listOf(
                            JsonElement(type = "Icon", eId = "reset-eid", id = resetId)
                        )
                    ),
                ),
                JsonWaypoint(
                    tUuid = "uuid-4",
                    waypointId = 3,
                    lat = 40.003, // Another approx 111.2m north
                    lon = -3.0,
                    show = true,
                ),
                JsonWaypoint(
                    tUuid = "uuid-5",
                    waypointId = 4,
                    lat = 40.004, // Another approx 111.2m north
                    lon = -3.0,
                    show = true,
                    tulip = JsonTulip(
                        listOf(
                            JsonElement(
                                type = "Road",
                                eId = "86084e6f-0551-4943-a884-51a6419756a0",
                                end = JsonPoint(-64.62391198310421, 7.914146695848885e-15),
                                typeId = 12,
                                z = 0,
                            ),
                            JsonElement(
                                type = "Road",
                                eId = "5fbffcf8-4472-49d3-a35d-bfdc8b6f9807",
                                end = JsonPoint(61.629944020743686, 0.0),
                                typeId = 4,
                                z = 1,
                            ),
                            JsonElement(
                                type = "Road",
                                eId = "7d68c683-7e12-4dc9-b8ad-1e5d43ab7328",
                                end = JsonPoint(38.316119323334405, -38.3161193233344),
                                typeId = 16,
                                z = 2,
                            ),
                            JsonElement(
                                type = "Road",
                                eId = "14c4bd20-f403-4494-bf5c-66ad02bffb1e",
                                end = JsonPoint(-50.88835819713581, -50.888358197135815),
                                typeId = 15,
                                z = 3,
                            ),
                            JsonElement(
                                type = "Track",
                                eId = "97136e41-80f4-4bd9-941e-da130b15fd26",
                                roadIn = JsonRoadIn(),
                                roadOut = JsonRoadOut(typeId = 18),
                                z = 0,
                            ),
                            JsonElement(
                                type = "Icon",
                                id = "1d752896-09fd-498d-b416-21f31a356be5",
                                eId = "3416854a-071b-48f4-83c4-847c24818c3a",
                                w = 50.0,
                                x = 173.00603015075376,
                                y = 108.99851851851851,
                                scaleX = 0.3472222222222222,
                                scaleY = 0.3472222222222222,
                            ),
                        )
                    ),
                    notes = JsonNotes(
                        listOf(
                            JsonElement(
                                type = "Icon",
                                eId = "danger-level-1",
                                id = dangerLevel1,
                                angle = 0.0,
                                w = 70.0,
                                x = 163.00904522613067,
                                y = 99.43333333333334,
                                scaleX = 0.4861111111111111,
                                scaleY = 0.4861111111111111,
                            ),
                            JsonElement(
                                type = "Text",
                                eId = "05fb5b85-2a0f-449e-8309-b6e22504ebb2",
                                text = "FIN",
                                fontSize = 18,
                                width = 180.0,
                                height = 20.339999999999996,
                                x = 99.5,
                                y = 54.5,
                            ),
                        ),
                    ),
                )
            )
        )

        // When: We invoke the use case
        val route = openRouteUseCase(jsonRouteData)

        // Then: Basic metadata should be mapped correctly
        assertEquals("Test Route", route.name)

        // And: 3 tulips should be created (waypoints 0, 2, 3 and 4 have show=true)
        assertEquals(4, route.waypoints.size)

        // Tulip 1 (Waypoint 0): Start point
        val tulip1 = route.waypoints[0]
        assertEquals(1, tulip1.number)
        assertEquals(0.0, tulip1.distance, 0.1)

        // Tulip 2 (Waypoint 3): Includes reset.
        // Its own distance should still be the sum of previous segments (approx 222.4m)
        val tulip2 = route.waypoints[1]
        assertEquals(2, tulip2.number)
        assertEquals(222.4, tulip2.distance, 0.5)
        assertEquals(111.2, tulip2.distanceFromPrevious, 0.5)
        assertTrue(tulip2.reset)

        // Tulip 3 (Waypoint 4): Segment AFTER reset.
        // Its accumulated distance should start from 0 + distance from waypoint 2 (approx 111.2m)
        val tulip3 = route.waypoints[2]
        assertEquals(3, tulip3.number)
        assertEquals(111.2, tulip3.distance, 0.5)
        assertEquals(111.2, tulip3.distanceFromPrevious, 0.5)
        assertEquals(Waypoint.DangerLevel.NONE, tulip3.dangerLevel)

        // Tulip 4 (Waypoint 5): Different track and road types.
        val tulip4 = route.waypoints[3]
        assertEquals(4, tulip4.number)
        assertEquals(Waypoint.DangerLevel.LOW, tulip4.dangerLevel)
        assertEquals(RoadType.DualCarriageway, (tulip4.tulipElements[0] as Road).roadType)
        assertEquals(RoadType.SmallTrack, (tulip4.tulipElements[1] as Road).roadType)
        assertEquals(RoadType.OffTrack, (tulip4.tulipElements[2] as Road).roadType)
        assertEquals(RoadType.LowVisibleTrack, (tulip4.tulipElements[3] as Road).roadType)
        assertEquals(RoadType.Track, (tulip4.tulipElements[4] as Track).roadIn.roadType)
        assertEquals(RoadType.TarmacRoad, (tulip4.tulipElements[4] as Track).roadOut.roadType)
        assertEquals(Element.ElementType.Icon, tulip4.tulipElements[5].elementType)
        assertEquals(Element.ElementType.Icon, tulip4.notesElements[0].elementType)
        assertEquals(Element.ElementType.Text, tulip4.notesElements[1].elementType)
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
