package org.giste.rn2viewer.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Waypoint (
    val number: Int,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double = 0.0,
    val distance: Double,
    val distanceFromPrevious: Double,
    val shortDistance: Boolean = false,
    val reset: Boolean = false,
    val alertLevel: AlertLevel = AlertLevel.NONE,
    val tulipElements: List<Element> = emptyList()
) {
    @Serializable
    enum class AlertLevel {
        NONE,
        LOW,
        MEDIUM,
        HIGH
    }
}
