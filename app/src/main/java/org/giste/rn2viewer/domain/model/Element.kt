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

package org.giste.rn2viewer.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class Element {
    abstract val elementType: ElementType

    @Serializable
    enum class ElementType(val value: String) {
        Track("Track"),
        Road("Road"),
        Icon("Icon"),
        Text("Text"),
    }

}

@Serializable
data class Track(
    val roadIn: Road,
    val roadOut: Road,
    val z: Int = 0,
) : Element() {
    override val elementType: ElementType = ElementType.Track
}

@Serializable
data class Road(
    val start: Point?,
    val end: Point?,
    val roadType: RoadType = RoadType.Track,
    val handles: List<Point> = emptyList(),
    val z: Int = 0,
) : Element() {
    override val elementType: ElementType = ElementType.Road

    @Serializable
    enum class RoadType(val value: Int) {
        LowVisibleTrack(15),
        OffTrack(16),
        SmallTrack(4),
        Track(17),
        TarmacRoad(18),
        DualCarriageway(12),
    }
}

@Serializable
sealed class Icon : Element() {
    abstract val w: Int
    abstract val center: Point
    abstract val angle: Int
    abstract val scaleX: Double
    abstract val scaleY: Double
    override val elementType: ElementType = ElementType.Icon

    @Serializable
    data class Danger1(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class Danger2(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class Danger3(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class FuelZone(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class ResetDistance(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class AboveBridge(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class UnderBridge(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class FortCastle(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class House(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class TrafficLight(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class Tunnel(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class Alert(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class Roundabout(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class Stop(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class RiverWater(
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()

    @Serializable
    data class Unknown(
        val originalId: String,
        override val w: Int,
        override val center: Point,
        override val angle: Int = 0,
        override val scaleX: Double = 1.0,
        override val scaleY: Double = 1.0,
    ) : Icon()
}

@Serializable
data class Text(
    val text: String,
    val fontSize: Int = 18,
    val width: Double,
    val height: Double,
    val center: Point,
) : Element() {
    override val elementType: ElementType = ElementType.Text
}

@Serializable
data class Point(
    val x: Double,
    val y: Double,
)
