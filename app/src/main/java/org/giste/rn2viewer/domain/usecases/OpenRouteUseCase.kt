package org.giste.rn2viewer.domain.usecases

import org.giste.rn2viewer.domain.JsonRouteData
import org.giste.rn2viewer.domain.JsonWaypoint
import org.giste.rn2viewer.domain.JsonElement
import org.giste.rn2viewer.domain.model.*
import org.giste.rn2viewer.domain.model.Waypoint.DangerLevel
import kotlin.math.*

class OpenRouteUseCase {

    companion object {
        private const val DISTANCE_RESET_ID = "308c7365-bc3f-451b-9e98-531e9015024f"
        private const val DANGER_LEVEL_1 = "bffeadbd-116b-49a7-921e-20dff8deec4b"
        private const val DANGER_LEVEL_2 = "a6c80c12-49b1-4e68-a21f-a6d48ef0a0ed"
        private const val DANGER_LEVEL_3 = "fab72ac2-f809-4ddc-9a7a-c9a24768bb4e"
    }

    /**
     * Internal state to track distance calculations during waypoint processing.
     */
    private data class WaypointProcessingState(
        val waypoint: JsonWaypoint,
        val distFromPrev: Double,
        val accumulatedDist: Double,
    )

    operator fun invoke(jsonRouteData: JsonRouteData): Route {
        return Route(
            name = jsonRouteData.name,
            description = jsonRouteData.description,
            startLocation = jsonRouteData.startLocation,
            endLocation = jsonRouteData.endLocation,
            waypoints = processWaypoints(jsonRouteData.waypoints),
        )
    }

    /**
     * Processes the list of JSON waypoints using functional operations.
     * It calculates distances, filters visible waypoints, and maps them to the domain model.
     */
    private fun processWaypoints(waypoints: List<JsonWaypoint>): List<Waypoint> {
        if (waypoints.isEmpty()) return emptyList()

        return waypoints.asSequence()
            // Using scan to propagate accumulated distance and distance from previous.
            // We start with the first waypoint and drop it from the sequence to avoid self-comparison.
            .drop(1)
            .scan(WaypointProcessingState(waypoints.first(), 0.0, 0.0)) { acc, current ->
                val distance = calculateDistance(acc.waypoint, current)

                // Check if this waypoint contains a note that resets the distance
                val previousHasResetNote =
                    acc.waypoint.notes?.elements?.any { it.id == DISTANCE_RESET_ID } == true
                val newAccumulatedDist =
                    if (previousHasResetNote) distance else acc.accumulatedDist + distance

                WaypointProcessingState(
                    waypoint = current,
                    distFromPrev = distance,
                    accumulatedDist = newAccumulatedDist,
                )
            }
            // Only waypoints marked as 'show' are converted to Tulips
            .filter { it.waypoint.show }
            // mapIndexed provides the 0-based index of the filtered list
            .mapIndexed { index, state ->
                Waypoint(
                    number = index + 1,
                    latitude = state.waypoint.lat,
                    longitude = state.waypoint.lon,
                    elevation = state.waypoint.ele,
                    distance = state.accumulatedDist,
                    distanceFromPrevious = state.distFromPrev,
                    reset = state.waypoint.notes?.elements?.any { it.id == DISTANCE_RESET_ID } == true,
                    dangerLevel = mapToDangerLevel(state.waypoint),
                    tulipElements = processTulipElements(state.waypoint),
                    notesElements = processNotesElements(state.waypoint),
                )
            }
            .toList()
    }

    /**
     * Calculates the distance between two waypoints in meters using the Haversine formula.
     */
    private fun calculateDistance(waypoint1: JsonWaypoint, waypoint2: JsonWaypoint): Double {
        val earthRadius = 6371000.0 // Radius of the Earth in meters
        val lat1 = Math.toRadians(waypoint1.lat)
        val lon1 = Math.toRadians(waypoint1.lon)
        val lat2 = Math.toRadians(waypoint2.lat)
        val lon2 = Math.toRadians(waypoint2.lon)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val chordLengthSquared = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val angularDistance = 2 * atan2(sqrt(chordLengthSquared), sqrt(1 - chordLengthSquared))

        return earthRadius * angularDistance
    }

    private fun processTulipElements(waypoint: JsonWaypoint): List<Element> {
        return (waypoint.tulip?.elements ?: emptyList()).mapNotNull { jsonElement ->
            mapJsonElementToDomain(jsonElement)
        }
    }

    private fun processNotesElements(waypoint: JsonWaypoint): List<Element> {
        return (waypoint.notes?.elements ?: emptyList()).mapNotNull { jsonElement ->
            mapJsonElementToDomain(jsonElement)
        }
    }

    private fun mapJsonElementToDomain(jsonElement: JsonElement): Element? {
        return when (jsonElement.type) {
            "Icon" -> {
                jsonElement.id?.let { id ->
                    Icon(
                        id = id,
                        angle = jsonElement.angle?.toInt() ?: 0,
                        w = jsonElement.w?.toInt() ?: 50,
                        center = Point(jsonElement.x ?: 0.0, jsonElement.y ?: 0.0)
                    )
                }
            }

            "Road" -> {
                Road(
                    start = Point(jsonElement.x ?: 0.0, jsonElement.y ?: 0.0),
                    end = jsonElement.roadOut?.end?.let { Point(it.x, it.y) }
                        ?: jsonElement.end?.let { Point(it.x, it.y) },
                    z = jsonElement.roadOut?.z ?: jsonElement.z ?: 0,
                    handles = jsonElement.handles?.map { Point(it.x, it.y) } ?: emptyList(),
                    roadType = mapToRoadType(jsonElement.typeId)
                )
            }

            "Track" -> {
                Track(
                    roadIn = Road(
                        start = null,
                        end = Point(jsonElement.x ?: 0.0, jsonElement.y ?: 0.0),
                        z = jsonElement.roadIn?.z ?: 0,
                        roadType = mapToRoadType(jsonElement.roadIn?.typeId)
                    ),
                    roadOut = Road(
                        start = Point(jsonElement.x ?: 0.0, jsonElement.y ?: 0.0),
                        end = jsonElement.roadOut?.end?.let { Point(it.x, it.y) },
                        z = jsonElement.roadOut?.z ?: 0,
                        handles = jsonElement.handles?.map { Point(it.x, it.y) } ?: emptyList(),
                        roadType = mapToRoadType(jsonElement.roadOut?.typeId)
                    ),
                    z = 0
                )
            }

            "Text" -> {
                Text(
                    text = jsonElement.text!!,
                    fontSize = jsonElement.fontSize ?: 18,
                    width = jsonElement.width!!,
                    height = jsonElement.height!!,
                    center = Point(jsonElement.x!!, jsonElement.y!!),
                )
            }

            else -> null
        }
    }

    private fun mapToRoadType(typeId: Int?): Road.RoadType {
        return Road.RoadType.entries.find { it.value == typeId } ?: Road.RoadType.Track
    }

    private fun mapToDangerLevel(waypoint: JsonWaypoint): DangerLevel {
        return if(waypoint.notes?.elements?.any { it.id == DANGER_LEVEL_1 } == true)
            DangerLevel.LOW
        else if(waypoint.notes?.elements?.any { it.id == DANGER_LEVEL_2 } == true)
            DangerLevel.MEDIUM
        else if(waypoint.notes?.elements?.any { it.id == DANGER_LEVEL_3 } == true)
            DangerLevel.HIGH
        else
            DangerLevel.NONE
    }
}
