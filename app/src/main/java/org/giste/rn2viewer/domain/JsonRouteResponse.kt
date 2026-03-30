package org.giste.rn2viewer.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
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
    val show: Boolean = true,
    val tulip: JsonTulip? = null,
    val notes: JsonNotes? = null,
    @SerialName("gravelLine") val gravelLine: JsonGravelLine? = null
)

@Serializable
data class JsonTulip(
    val elements: List<JsonTulipElement> = emptyList()
)

@Serializable
data class JsonTulipElement(
    val type: String,
    val eId: String,
    val id: String? = null,
    val name: String? = null,
    val src: String? = null,
    val x: Double? = null,
    val y: Double? = null,
    val w: Double? = null,
    val angle: Double? = null,
    @SerialName("isRoundabout") val isRoundabout: Boolean? = null,
    val roadIn: JsonRoadIn? = null,
    val roadOut: JsonRoadOut? = null,
    val handles: List<JsonHandle>? = null,
    val end: JsonPoint? = null
)

@Serializable
data class JsonNotes(
    val elements: List<JsonNoteElement> = emptyList()
)

@Serializable
data class JsonNoteElement(
    val type: String,
    val eId: String,
    val id: String? = null,
    val name: String? = null,
    val src: String? = null,
    val x: Double? = null,
    val y: Double? = null,
    val w: Double? = null,
    val angle: Double? = null
)

@Serializable
data class JsonRoadIn(
    val z: Int = 0
)

@Serializable
data class JsonRoadOut(
    val z: Int = 0,
    val end: JsonPoint? = null
)

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
data class JsonGravelLine(
    val top: Int,
    val bottom: Int
)

@Serializable
data class JsonRouteSettings(
    val units: String = "metric",
    @SerialName("coordFormat") val coordFormat: Int = 1,
    val showHighlight: Boolean = true
)
