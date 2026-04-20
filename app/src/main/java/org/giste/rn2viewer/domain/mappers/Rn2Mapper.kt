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

import org.giste.rn2viewer.domain.JsonElement
import org.giste.rn2viewer.domain.JsonRouteData
import org.giste.rn2viewer.domain.JsonRouteResponse
import org.giste.rn2viewer.domain.JsonWaypoint
import org.giste.rn2viewer.domain.model.Element
import org.giste.rn2viewer.domain.model.Icon
import org.giste.rn2viewer.domain.model.Point
import org.giste.rn2viewer.domain.model.Road
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.model.Text
import org.giste.rn2viewer.domain.model.Track
import org.giste.rn2viewer.domain.model.Waypoint
import org.giste.rn2viewer.domain.model.Waypoint.DangerLevel
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Rn2Mapper @Inject constructor() {

    companion object {
        private const val SHORT_DISTANCE_THRESHOLD = 300.0
    }

    /**
     * Internal state to track distance calculations during waypoint processing.
     */
    private data class WaypointProcessingState(
        val waypoint: JsonWaypoint,
        val accumulatedDist: Double,
        val lastVisibleDist: Double,
        val reset: Boolean,
    )

    fun mapToDomain(jsonString: String, threshold: Double? = null): Route {
        Timber.d("Starting mapping from JSON string, length: ${jsonString.length}")
        val jsonResponse = JsonRouteResponse.fromJson(jsonString)
        return mapToDomain(jsonResponse.route, threshold)
    }

    private fun mapToDomain(jsonRouteData: JsonRouteData, threshold: Double? = null): Route {
        Timber.i("Mapping route: ${jsonRouteData.name} with ${jsonRouteData.waypoints.size} waypoints")
        return Route(
            name = jsonRouteData.name,
            description = jsonRouteData.description,
            startLocation = jsonRouteData.startLocation,
            endLocation = jsonRouteData.endLocation,
            waypoints = processWaypoints(jsonRouteData.waypoints, threshold),
        )
    }

    private fun processWaypoints(waypoints: List<JsonWaypoint>, threshold: Double? = null): List<Waypoint> {
        if (waypoints.isEmpty()) {
            Timber.w("Waypoint list is empty")
            return emptyList()
        }

        val states = waypoints.asSequence()
            .drop(1)
            .scan(
                WaypointProcessingState(
                    waypoint = waypoints.first(),
                    accumulatedDist = 0.0,
                    lastVisibleDist = 0.0,
                    reset = hasReset(waypoints.first())
                )
            ) { acc, current ->
                val distance = calculateDistance(acc.waypoint, current)
                val newAccumulatedDist = if (acc.reset) distance else acc.accumulatedDist + distance
                val newLastVisibleDist = if (acc.waypoint.show) acc.accumulatedDist else acc.lastVisibleDist

                WaypointProcessingState(
                    waypoint = current,
                    accumulatedDist = newAccumulatedDist,
                    lastVisibleDist = newLastVisibleDist,
                    reset = hasReset(current)
                )
            }
            .toList()

        var visibleCount = 0
        val mappedWaypoints = states.mapIndexedNotNull { index, state ->
            if (!state.waypoint.show) return@mapIndexedNotNull null

            visibleCount++
            val distFromPrev = if (visibleCount == 1) state.accumulatedDist else state.accumulatedDist - state.lastVisibleDist

            val prevWaypoint = if (index > 0) states[index - 1].waypoint else null
            val nextWaypoint = if (index < states.size - 1) states[index + 1].waypoint else null

            val waypoint = Waypoint(
                number = visibleCount,
                latitude = state.waypoint.lat,
                longitude = state.waypoint.lon,
                elevation = state.waypoint.ele,
                distance = state.accumulatedDist,
                distanceFromPrevious = distFromPrev,
                shortDistance = distFromPrev > 0 && distFromPrev < (threshold ?: SHORT_DISTANCE_THRESHOLD),
                reset = state.reset,
                dangerLevel = mapToDangerLevel(state.waypoint),
                tulipElements = processTulipElements(prevWaypoint, state.waypoint, nextWaypoint),
                notesElements = processNotesElements(state.waypoint),
            )
            Timber.d("Processed waypoint ${state.waypoint.waypointId}: $waypoint")

            waypoint
        }

        Timber.d("Processed ${states.size} waypoints, ${mappedWaypoints.size} are visible")
        return mappedWaypoints
    }

    private fun hasReset(waypoint: JsonWaypoint): Boolean {
        return waypoint.notes.elements.any { it is JsonElement.JsonIcon.ResetDistance }
    }

    private fun calculateDistance(waypoint1: JsonWaypoint, waypoint2: JsonWaypoint): Double {
        val earthRadius = 6371000.0
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

    private fun processTulipElements(
        prevWaypoint: JsonWaypoint?,
        currentWaypoint: JsonWaypoint,
        nextWaypoint: JsonWaypoint?
    ): List<Element> {
        return currentWaypoint.tulip.elements.map { mapJsonElementToDomain(it, prevWaypoint, currentWaypoint, nextWaypoint) }
    }

    private fun processNotesElements(waypoint: JsonWaypoint): List<Element> {
        return waypoint.notes.elements.map { mapJsonElementToDomain(it, null, waypoint, null) }
    }

    private fun mapJsonElementToDomain(
        jsonElement: JsonElement,
        prevWaypoint: JsonWaypoint?,
        currentWaypoint: JsonWaypoint,
        nextWaypoint: JsonWaypoint?
    ): Element {
        return when (jsonElement) {
            is JsonElement.JsonIcon -> mapJsonIconToDomain(jsonElement)
            is JsonElement.JsonRoad -> {
                Road(
                    start = jsonElement.start?.let { Point(it.x, it.y) },
                    end = jsonElement.end?.let { Point(it.x, it.y) },
                    handles = jsonElement.handles.map { Point(it.x, it.y) },
                    roadType = mapToRoadType(jsonElement.typeId)
                )
            }

            is JsonElement.JsonTrack -> {
                val roadOutEnd = if (jsonElement.roadOut.end != null) {
                    Point(jsonElement.roadOut.end.x, jsonElement.roadOut.end.y)
                } else if (nextWaypoint != null) {
                    calculateExitPoint(prevWaypoint, currentWaypoint, nextWaypoint)
                } else {
                    Timber.v("No roadOut end and no next waypoint for waypoint ${currentWaypoint.waypointId}, using default")
                    Point(0.0, -55.0) // Default if no next waypoint
                }

                Track(
                    roadIn = Road(
                        start = jsonElement.roadIn.start?.let { Point(it.x, it.y) },
                        end = jsonElement.roadIn.end?.let { Point(it.x, it.y) } ?: Point(0.0, 35.0),
                        roadType = mapToRoadType(jsonElement.roadIn.typeId),
                        handles = jsonElement.roadIn.handles.map { Point(it.x, it.y) },
                    ),
                    roadOut = Road(
                        start = jsonElement.roadOut.start?.let { Point(it.x, it.y) } ?: Point(0.0, 0.0),
                        end = roadOutEnd,
                        roadType = mapToRoadType(jsonElement.roadOut.typeId),
                        handles = jsonElement.roadOut.handles.map { Point(it.x, it.y) },
                    ),
                )
            }

            is JsonElement.JsonText -> {
                Text(
                    text = jsonElement.text,
                    fontSize = jsonElement.fontSize,
                    lineHeight = jsonElement.lineHeight ?: 1.0,
                    width = jsonElement.width,
                    height = jsonElement.height,
                    maxWidth = jsonElement.maxWidth ?: 180.0,
                    maxHeight = jsonElement.maxHeight ?: 100.0,
                    center = Point(jsonElement.x, jsonElement.y),
                )
            }
        }
    }

    private fun calculateExitPoint(
        prev: JsonWaypoint?,
        current: JsonWaypoint,
        next: JsonWaypoint
    ): Point {
        val bearingOut = calculateBearing(current, next)
        val bearingIn = if (prev != null) calculateBearing(prev, current) else bearingOut

        // Relative bearing: shortest angular difference
        val relativeBearing = atan2(sin(bearingOut - bearingIn), cos(bearingOut - bearingIn))

        // RN2 coordinates: Y increases downwards, X increases rightwards.
        // "Up" in RN2 corresponds to the direction of arrival (relative angle 0).
        // North (0 rad) -> RN2 angle -PI/2 (up)
        val rn2Angle = relativeBearing - Math.PI / 2.0

        // Tulip boundaries relative to center (100, 85)
        // Leave 25 units of space from the actual limits
        val margin = 25.0
        val left = -100.0 + margin
        val right = 100.0 - margin
        val top = -85.0 + margin
        val bottom = 50.0 - margin

        val dx = cos(rn2Angle)
        val dy = sin(rn2Angle)

        var t = Double.MAX_VALUE

        if (dx > 0) t = minOf(t, right / dx)
        else if (dx < 0) t = minOf(t, left / dx)

        if (dy > 0) t = minOf(t, bottom / dy)
        else if (dy < 0) t = minOf(t, top / dy)

        val exitPoint = Point(dx * t, dy * t)
        Timber.v(
            "%snull", "Calculated relative exit point for wp ${current.waypointId}: " +
                "in=${Math.toDegrees(bearingIn)}°, out=${Math.toDegrees(bearingOut)}°, "
        )
        return exitPoint
    }

    private fun calculateBearing(from: JsonWaypoint, to: JsonWaypoint): Double {
        val lat1 = Math.toRadians(from.lat)
        val lon1 = Math.toRadians(from.lon)
        val lat2 = Math.toRadians(to.lat)
        val lon2 = Math.toRadians(to.lon)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        return atan2(y, x)
    }

    private fun mapJsonIconToDomain(jsonIcon: JsonElement.JsonIcon): Icon {
        val baseWidth = jsonIcon.width ?: 50.0
        val baseHeight = jsonIcon.height ?: 50.0
        val scaleX = jsonIcon.scaleX ?: 1.0
        val scaleY = jsonIcon.scaleY ?: 1.0
        val width = (baseWidth * scaleX).toInt()
        val height = (baseHeight * scaleY).toInt()
        val center = Point(jsonIcon.x ?: 0.0, jsonIcon.y ?: 0.0)
        val angle = jsonIcon.angle?.toInt() ?: 0

        return when (jsonIcon) {
            is JsonElement.JsonIcon.Danger1 -> Icon.Danger1(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Danger2 -> Icon.Danger2(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Danger3 -> Icon.Danger3(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.FuelZone -> Icon.FuelZone(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.ResetDistance -> Icon.ResetDistance(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.AboveBridge -> Icon.AboveBridge(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.FortCastle -> Icon.FortCastle(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.House -> Icon.House(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.TrafficLight -> Icon.TrafficLight(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Tunnel -> Icon.Tunnel(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.UnderBridge -> Icon.UnderBridge(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Alert -> Icon.Alert(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Roundabout -> Icon.Roundabout(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Stop -> Icon.Stop(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.RiverWater -> Icon.RiverWater(width, height, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Unknown -> Icon.Unknown(jsonIcon.id, width, height, center, angle, scaleX, scaleY)
        }
    }

    private fun mapToRoadType(typeId: Int?): Road.RoadType {
        return Road.RoadType.entries.find { it.value == typeId } ?: Road.RoadType.Track
    }

    private fun mapToDangerLevel(waypoint: JsonWaypoint): DangerLevel {
        waypoint.notes.elements.filterIsInstance<JsonElement.JsonIcon>().forEach {
            return when (it) {
                is JsonElement.JsonIcon.Danger1 -> DangerLevel.LOW
                is JsonElement.JsonIcon.Danger2 -> DangerLevel.MEDIUM
                is JsonElement.JsonIcon.Danger3 -> DangerLevel.HIGH
                else -> DangerLevel.NONE
            }
        }
        return DangerLevel.NONE
    }
}
