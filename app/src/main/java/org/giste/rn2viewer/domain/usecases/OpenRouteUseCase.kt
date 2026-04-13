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

package org.giste.rn2viewer.domain.usecases

import org.giste.rn2viewer.domain.JsonRouteData
import org.giste.rn2viewer.domain.JsonWaypoint
import org.giste.rn2viewer.domain.JsonElement
import org.giste.rn2viewer.domain.model.*
import org.giste.rn2viewer.domain.model.Waypoint.DangerLevel
import kotlin.math.*

class OpenRouteUseCase {

    companion object {
        private const val DISTANCE_RESET_ID = JsonElement.JsonIcon.CROSS_RESET_DISTANCE_ID
        private const val DANGER_LEVEL_1 = JsonElement.JsonIcon.CROSS_DANGER_1_ID
        private const val DANGER_LEVEL_2 = JsonElement.JsonIcon.CROSS_DANGER_2_ID
        private const val DANGER_LEVEL_3 = JsonElement.JsonIcon.CROSS_DANGER_3_ID
    }

    /**
     * Internal state to track distance calculations during waypoint processing.
     */
    private data class WaypointProcessingState(
        val waypoint: JsonWaypoint,
        val distFromPrev: Double,
        val accumulatedDist: Double,
        val reset: Boolean,
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
            .scan(WaypointProcessingState(waypoints.first(), 0.0, 0.0, false)) { acc, current ->
                val distance = calculateDistance(acc.waypoint, current)

                val newAccumulatedDist = if (acc.reset) distance else acc.accumulatedDist + distance

                WaypointProcessingState(
                    waypoint = current,
                    distFromPrev = distance,
                    accumulatedDist = newAccumulatedDist,
                    reset = hasReset(current)
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
                    reset = state.reset,
                    dangerLevel = mapToDangerLevel(state.waypoint),
                    tulipElements = processTulipElements(state.waypoint),
                    notesElements = processNotesElements(state.waypoint),
                )
            }
            .toList()
    }

    private fun hasReset(waypoint: JsonWaypoint): Boolean {
        return waypoint.notes.elements.any {
            if (it is JsonElement.JsonIcon) {
                it.id == DISTANCE_RESET_ID
            } else
                false
        }
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
        return waypoint.tulip.elements.mapNotNull { jsonElement ->
            mapJsonElementToDomain(jsonElement)
        }
    }

    private fun processNotesElements(waypoint: JsonWaypoint): List<Element> {
        return waypoint.notes.elements.mapNotNull { jsonElement ->
            mapJsonElementToDomain(jsonElement)
        }
    }

    private fun mapJsonElementToDomain(jsonElement: JsonElement): Element? {
        return when (jsonElement) {
            is JsonElement.JsonIcon -> mapJsonIconToDomain(jsonElement)

            is JsonElement.JsonRoad -> {
                Road(
                    start = jsonElement.start?.let { Point(it.x, it.y) },
                    end = jsonElement.end?.let { Point(it.x, it.y) },
                    z = jsonElement.z ?: 0,
                    handles = jsonElement.handles.map { Point(it.x, it.y) },
                    roadType = mapToRoadType(jsonElement.typeId)
                )
            }

            is JsonElement.JsonTrack -> {
                Track(
                    roadIn = Road(
                        start = jsonElement.roadIn.start?.let { Point(it.x, it.y) },
                        end = jsonElement.roadIn.end?.let { Point(it.x, it.y) },
                        z = jsonElement.roadIn.z ?: 0,
                        roadType = mapToRoadType(jsonElement.roadIn.typeId),
                        handles = jsonElement.roadIn.handles.map { Point(it.x, it.y) },
                    ),
                    roadOut = Road(
                        start = jsonElement.roadOut.start?.let { Point(it.x, it.y) },
                        end = jsonElement.roadOut.end?.let { Point(it.x, it.y) },
                        z = jsonElement.roadOut.z ?: 0,
                        roadType = mapToRoadType(jsonElement.roadOut.typeId),
                        handles = jsonElement.roadOut.handles.map { Point(it.x, it.y) },
                    ),
                    z = 0
                )
            }

            is JsonElement.JsonText -> {
                Text(
                    text = jsonElement.text,
                    fontSize = jsonElement.fontSize,
                    width = jsonElement.width,
                    height = jsonElement.height,
                    center = Point(jsonElement.x, jsonElement.y),
                )
            }
        }
    }

    private fun mapJsonIconToDomain(jsonIcon: JsonElement.JsonIcon): Icon {
        val w = jsonIcon.w?.toInt() ?: 50
        val center = Point(jsonIcon.x ?: 0.0, jsonIcon.y ?: 0.0)
        val angle = jsonIcon.angle?.toInt() ?: 0
        val scaleX = jsonIcon.scaleX ?: 1.0
        val scaleY = jsonIcon.scaleY ?: 1.0

        return when (jsonIcon.id) {
            JsonElement.JsonIcon.CROSS_DANGER_1_ID -> Icon.Danger1(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.CROSS_DANGER_2_ID -> Icon.Danger2(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.CROSS_DANGER_3_ID -> Icon.Danger3(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.CROSS_FUEL_ZONE_ID -> Icon.FuelZone(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.CROSS_RESET_DISTANCE_ID -> Icon.ResetDistance(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.LANDMARK_ABOVE_BRIDGE_ID -> Icon.AboveBridge(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.LANDMARK_FORT_CASTLE_ID -> Icon.FortCastle(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.LANDMARK_HOUSE_ID -> Icon.House(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.LANDMARK_TRAFFIC_LIGHT_ID -> Icon.TrafficLight(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.LANDMARK_TUNNEL_ID -> Icon.Tunnel(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.LANDMARK_UNDER_BRIDGE_ID -> Icon.UnderBridge(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.SIGN_ALERT_ID -> Icon.Alert(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.SIGN_ROUNDABOUT_ID -> Icon.Roundabout(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.SIGN_STOP_ID -> Icon.Stop(w, center, angle, scaleX, scaleY)
            JsonElement.JsonIcon.TERRAIN_RIVER_WATER_ID -> Icon.RiverWater(w, center, angle, scaleX, scaleY)
            else -> Icon.Unknown(jsonIcon.id, w, center, angle, scaleX, scaleY)
        }
    }

    private fun mapToRoadType(typeId: Int?): Road.RoadType {
        return Road.RoadType.entries.find { it.value == typeId } ?: Road.RoadType.Track
    }

    private fun mapToDangerLevel(waypoint: JsonWaypoint): DangerLevel {
        waypoint.notes.elements.forEach {
            if (it is JsonElement.JsonIcon) {
                return when(it.id) {
                    DANGER_LEVEL_1 -> DangerLevel.LOW
                    DANGER_LEVEL_2 -> DangerLevel.MEDIUM
                    DANGER_LEVEL_3 -> DangerLevel.HIGH
                    else -> DangerLevel.NONE
                }
            }
        }
        return DangerLevel.NONE
    }
}
