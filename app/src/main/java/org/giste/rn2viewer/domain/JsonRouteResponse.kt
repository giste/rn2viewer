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

package org.giste.rn2viewer.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class JsonRouteResponse(
    val route: JsonRouteData
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

        /**
         * Factory method to create a JsonRouteResponse from a JSON string
         */
        fun fromJson(jsonString: String): JsonRouteResponse {
            return json.decodeFromString<JsonRouteResponse>(jsonString)
        }
    }
}

@Serializable
data class JsonRouteData(
    val version: Int,
    val name: String = "",
    val description: String = "",
    @SerialName("startlocation") val startLocation: String = "",
    @SerialName("endlocation") val endLocation: String = "",
    @SerialName("current_style") val currentStyle: String = "",
    val waypoints: List<JsonWaypoint> = emptyList(),
    val settings: JsonRouteSettings? = null
)

@Serializable
data class JsonWaypoint(
    @SerialName("t_uuid") val tUuid: String,
    @SerialName("waypointid") val waypointId: Int,
    val lat: Double,
    val lon: Double,
    val ele: Double = 0.0,
    val show: Boolean,
    val tulip: JsonTulip,
    val notes: JsonNotes,
)

@Serializable
data class JsonTulip(
    val elements: List<JsonElement> = emptyList()
)

@Serializable
data class JsonNotes(
    val elements: List<JsonElement> = emptyList()
)

@Serializable
sealed class JsonElement {
    @Serializable
    @SerialName("Road")
    data class JsonRoad(
        val start: JsonPoint? = null,
        val end: JsonPoint? = null,
        val typeId: Int? = null,
        val z: Int? = null,
        val handles: List<JsonHandle> = emptyList(),
    ) : JsonElement()

    @Serializable
    @SerialName("Track")
    data class JsonTrack(
        val roadIn: JsonRoad,
        val roadOut: JsonRoad,
    ) : JsonElement()

    @Serializable
    @SerialName("Icon")
    data class JsonIcon(
        val id: String,
        val angle: Double? = null,
        val w: Double? = null,
        val x: Double? = null,
        val y: Double? = null,
        val scaleX: Double? = null,
        val scaleY: Double? = null,
    ) : JsonElement()

    @Serializable
    @SerialName("Text")
    data class JsonText(
        val id: String? = null,
        val text: String,
        val fontSize: Int,
        val width: Double,
        val height: Double,
        val x: Double,
        val y: Double,
    ) : JsonElement()
}

@Serializable
data class JsonPoint(
    val x: Double,
    val y: Double
)

@Serializable
data class JsonHandle(
    val x: Double,
    val y: Double
)

@Serializable
data class JsonRouteSettings(
    val units: String = "metric",
    @SerialName("coordFormat") val coordFormat: Int = 1,
    val showHighlight: Boolean = true
)
