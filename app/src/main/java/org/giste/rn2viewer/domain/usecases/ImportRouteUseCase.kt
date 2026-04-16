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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.giste.rn2viewer.domain.JsonElement
import org.giste.rn2viewer.domain.JsonRouteData
import org.giste.rn2viewer.domain.JsonRouteResponse
import org.giste.rn2viewer.domain.JsonWaypoint
import org.giste.rn2viewer.domain.model.*
import org.giste.rn2viewer.domain.model.Waypoint.DangerLevel
import org.giste.rn2viewer.domain.repositories.RouteRepository
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.*

class ImportRouteUseCase @Inject constructor(
    private val routeRepository: RouteRepository
) {
    /**
     * Internal state to track distance calculations during waypoint processing.
     */
    private data class WaypointProcessingState(
        val waypoint: JsonWaypoint,
        val distFromPrev: Double,
        val accumulatedDist: Double,
        val reset: Boolean,
    )

    suspend operator fun invoke(uriString: String): Result<Unit> = withContext(Dispatchers.IO) {
        Timber.d("Invoking import for: $uriString")
        routeRepository.getExternalRouteContent(uriString).fold(
            onSuccess = { jsonString ->
                try {
                    Timber.d("JSON received, length: ${jsonString.length}")
                    val jsonResponse = JsonRouteResponse.fromJson(jsonString)
                    Timber.d("JSON parsed, route name: ${jsonResponse.route.name}, waypoints: ${jsonResponse.route.waypoints.size}")
                    val route = mapToDomain(jsonResponse.route)
                    Timber.d("Mapped to domain: ${route.name} with ${route.waypoints.size} waypoints")
                    route.waypoints.forEach { wp ->
                        Timber.v("Waypoint #${wp.number}: Total Dist: ${wp.distance}m, Partial: ${wp.distanceFromPrevious}m, Reset: ${wp.reset}, Elements: ${wp.tulipElements.size} tulip / ${wp.notesElements.size} notes")
                    }
                    routeRepository.saveRoute(route)
                    Result.success(Unit)
                } catch (e: Exception) {
                    Timber.e(e, "Error during mapping")
                    Result.failure(e)
                }
            },
            onFailure = { 
                Timber.e(it, "Error getting external content")
                Result.failure(it) 
            }
        )
    }

    private fun mapToDomain(jsonRouteData: JsonRouteData): Route {
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
        return waypoint.notes.elements.any { it is JsonElement.JsonIcon.ResetDistance }
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
        return waypoint.tulip.elements.map { jsonElement ->
            mapJsonElementToDomain(jsonElement)
        }
    }

    private fun processNotesElements(waypoint: JsonWaypoint): List<Element> {
        return waypoint.notes.elements.map { jsonElement ->
            mapJsonElementToDomain(jsonElement)
        }
    }

    private fun mapJsonElementToDomain(jsonElement: JsonElement): Element {
        val element = when (jsonElement) {
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
                Track(
                    roadIn = Road(
                        start = jsonElement.roadIn.start?.let { Point(it.x, it.y) },
                        end = jsonElement.roadIn.end?.let { Point(it.x, it.y) } ?: Point(0.0, 35.0),
                        roadType = mapToRoadType(jsonElement.roadIn.typeId),
                        handles = jsonElement.roadIn.handles.map { Point(it.x, it.y) },
                    ),
                    roadOut = Road(
                        start = jsonElement.roadOut.start?.let { Point(it.x, it.y) } ?: Point(0.0, 0.0),
                        end = jsonElement.roadOut.end?.let { Point(it.x, it.y) } ?: Point(0.0, -55.0),
                        roadType = mapToRoadType(jsonElement.roadOut.typeId),
                        handles = jsonElement.roadOut.handles.map { Point(it.x, it.y) },
                    ),
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
        Timber.v("Mapped element: $element")
        return element
    }

    private fun mapJsonIconToDomain(jsonIcon: JsonElement.JsonIcon): Icon {
        val w = jsonIcon.w?.toInt() ?: 50
        val center = Point(jsonIcon.x ?: 0.0, jsonIcon.y ?: 0.0)
        val angle = jsonIcon.angle?.toInt() ?: 0
        val scaleX = jsonIcon.scaleX ?: 1.0
        val scaleY = jsonIcon.scaleY ?: 1.0

        return when (jsonIcon) {
            is JsonElement.JsonIcon.Danger1 -> Icon.Danger1(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Danger2 -> Icon.Danger2(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Danger3 -> Icon.Danger3(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.FuelZone -> Icon.FuelZone(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.ResetDistance -> Icon.ResetDistance(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.AboveBridge -> Icon.AboveBridge(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.FortCastle -> Icon.FortCastle(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.House -> Icon.House(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.TrafficLight -> Icon.TrafficLight(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Tunnel -> Icon.Tunnel(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.UnderBridge -> Icon.UnderBridge(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Alert -> Icon.Alert(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Roundabout -> Icon.Roundabout(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Stop -> Icon.Stop(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.RiverWater -> Icon.RiverWater(w, center, angle, scaleX, scaleY)
            is JsonElement.JsonIcon.Unknown -> Icon.Unknown(jsonIcon.id, w, center, angle, scaleX, scaleY)
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
