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

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.serialization.json.JsonElement as KJsonElement

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

    @Serializable(with = JsonIconSerializer::class)
    @SerialName("Icon")
    sealed class JsonIcon : JsonElement() {
        abstract val id: String
        abstract val angle: Double?
        abstract val w: Double?
        abstract val x: Double?
        abstract val y: Double?
        abstract val scaleX: Double?
        abstract val scaleY: Double?

        companion object {
            const val CROSS_DANGER_1_ID = "bffeadbd-116b-49a7-921e-20dff8deec4b"
            const val CROSS_DANGER_2_ID = "a6c80c12-49b1-4e68-a21f-a6d48ef0a0ed"
            const val CROSS_DANGER_3_ID = "fab72ac2-f809-4ddc-9a7a-c9a24768bb4e"
            const val CROSS_FUEL_ZONE_ID = "e5167bd4-314b-47d3-ba23-708182be76a9"
            const val CROSS_RESET_DISTANCE_ID = "308c7365-bc3f-451b-9e98-531e9015024f"
            const val LANDMARK_ABOVE_BRIDGE_ID = "a49a0b2e-3be5-4659-8251-8205fd4e9571"
            const val LANDMARK_FORT_CASTLE_ID = "da5ec2a7-612a-411f-aeb2-d1f9514d3dc7"
            const val LANDMARK_HOUSE_ID = "3965bf45-97ee-4c6b-b087-0e128510c4e3"
            const val LANDMARK_TRAFFIC_LIGHT_ID = "1d752896-09fd-498d-b416-21f31a356be5"
            const val LANDMARK_TUNNEL_ID = "0539c8e3-393b-4416-8002-b30700cf68de"
            const val LANDMARK_UNDER_BRIDGE_ID = "79f8c10f-d67b-4ba5-bf12-6a801ed79ed3"
            const val SIGN_ALERT_ID = "2598a2c0-6a8b-4dc5-8211-8ad64d986bde"
            const val SIGN_ROUNDABOUT_ID = "5d157992-6013-4bef-86cb-92fea891944c"
            const val SIGN_STOP_ID = "5a4ced4c-68e2-41d3-a1b4-9c8b86ec2109"
            const val TERRAIN_RIVER_WATER_ID = "aabe9acd-ab1b-467d-9bbb-877bb0d0da23"
        }

        @Serializable data class Danger1(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class Danger2(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class Danger3(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class FuelZone(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class ResetDistance(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class AboveBridge(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class FortCastle(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class House(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class TrafficLight(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class Tunnel(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class UnderBridge(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class Alert(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class Roundabout(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class Stop(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class RiverWater(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
        @Serializable data class Unknown(override val id: String, override val angle: Double? = null, override val w: Double? = null, override val x: Double? = null, override val y: Double? = null, override val scaleX: Double? = null, override val scaleY: Double? = null) : JsonIcon()
    }

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

object JsonIconSerializer : JsonContentPolymorphicSerializer<JsonElement.JsonIcon>(JsonElement.JsonIcon::class) {
    override fun selectDeserializer(element: KJsonElement): DeserializationStrategy<JsonElement.JsonIcon> {
        val id = element.jsonObject["id"]?.jsonPrimitive?.content
        return when (id) {
            JsonElement.JsonIcon.CROSS_DANGER_1_ID -> JsonElement.JsonIcon.Danger1.serializer()
            JsonElement.JsonIcon.CROSS_DANGER_2_ID -> JsonElement.JsonIcon.Danger2.serializer()
            JsonElement.JsonIcon.CROSS_DANGER_3_ID -> JsonElement.JsonIcon.Danger3.serializer()
            JsonElement.JsonIcon.CROSS_FUEL_ZONE_ID -> JsonElement.JsonIcon.FuelZone.serializer()
            JsonElement.JsonIcon.CROSS_RESET_DISTANCE_ID -> JsonElement.JsonIcon.ResetDistance.serializer()
            JsonElement.JsonIcon.LANDMARK_ABOVE_BRIDGE_ID -> JsonElement.JsonIcon.AboveBridge.serializer()
            JsonElement.JsonIcon.LANDMARK_FORT_CASTLE_ID -> JsonElement.JsonIcon.FortCastle.serializer()
            JsonElement.JsonIcon.LANDMARK_HOUSE_ID -> JsonElement.JsonIcon.House.serializer()
            JsonElement.JsonIcon.LANDMARK_TRAFFIC_LIGHT_ID -> JsonElement.JsonIcon.TrafficLight.serializer()
            JsonElement.JsonIcon.LANDMARK_TUNNEL_ID -> JsonElement.JsonIcon.Tunnel.serializer()
            JsonElement.JsonIcon.LANDMARK_UNDER_BRIDGE_ID -> JsonElement.JsonIcon.UnderBridge.serializer()
            JsonElement.JsonIcon.SIGN_ALERT_ID -> JsonElement.JsonIcon.Alert.serializer()
            JsonElement.JsonIcon.SIGN_ROUNDABOUT_ID -> JsonElement.JsonIcon.Roundabout.serializer()
            JsonElement.JsonIcon.SIGN_STOP_ID -> JsonElement.JsonIcon.Stop.serializer()
            JsonElement.JsonIcon.TERRAIN_RIVER_WATER_ID -> JsonElement.JsonIcon.RiverWater.serializer()
            else -> JsonElement.JsonIcon.Unknown.serializer()
        }
    }
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
