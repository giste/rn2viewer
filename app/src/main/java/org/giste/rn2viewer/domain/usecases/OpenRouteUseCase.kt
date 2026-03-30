package org.giste.rn2viewer.domain.usecases

import org.giste.rn2viewer.domain.JsonRouteData
import org.giste.rn2viewer.domain.JsonWaypoint
import org.giste.rn2viewer.domain.model.*
import kotlin.math.*

class OpenRouteUseCase {

    companion object {
        private const val DISTANCE_RESET_ID = "308c7365-bc3f-451b-9e98-531e9015024f"
    }

    /**
     * Internal state to track distance calculations during waypoint processing.
     */
    private data class WaypointProcessingState(
        val waypoint: JsonWaypoint,
        val distFromPrev: Double,
        val accumulatedDist: Double
    )

    operator fun invoke(jsonRouteData: JsonRouteData): Route {
        return Route(
            name = jsonRouteData.name,
            description = jsonRouteData.description,
            startLocation = jsonRouteData.startLocation,
            endLocation = jsonRouteData.endLocation,
            waypoints = processWaypoints(jsonRouteData.waypoints)
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
                val previousHasResetNote = acc.waypoint.notes?.elements?.any { it.id == DISTANCE_RESET_ID } == true
                val newAccumulatedDist = if (previousHasResetNote) distance else acc.accumulatedDist + distance

                WaypointProcessingState(
                    waypoint = current,
                    distFromPrev = distance,
                    accumulatedDist = newAccumulatedDist
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
                    tulipElements = processTulipElements(state.waypoint)
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

    private fun processTulipElements(waypoint: JsonWaypoint): List<TulipElement> {
        return waypoint.tulip?.elements?.mapNotNull { jsonElement ->
            when (jsonElement.type) {
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
                        end = jsonElement.roadOut?.end?.let { Point(it.x, it.y) } ?: jsonElement.end?.let { Point(it.x, it.y) },
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
                else -> null
            }
        } ?: emptyList()
    }

    private fun mapToRoadType(typeId: Int?): Road.RoadType {
        return Road.RoadType.entries.find { it.value == typeId } ?: Road.RoadType.Track
    }
}
