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
data class Icon(
    val id: String,
    val angle: Int = 0,
    val w: Int = 50,
    val center: Point,
    val scaleX: Double = 1.0,
    val scaleY: Double = 1.0,
) : Element() {
    override val elementType: ElementType = ElementType.Icon
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