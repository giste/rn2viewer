package org.giste.rn2viewer.domain.model

sealed class TulipElement (
    val elementType: ElementType,
) {
    enum class ElementType(val value: String) {
        Track("Track"),
        Road("Road"),
        Icon("Icon"),
    }
}

data class Track(
    val roadIn: Road,
    val roadOut: Road,
    val z: Int = 0,
) : TulipElement(ElementType.Track)

data class Road(
    val start: Point?,
    val end: Point?,
    val roadType: RoadType = RoadType.Track,
    val handles: List<Point> = emptyList(),
    val z: Int = 0,
) : TulipElement(ElementType.Road) {
    enum class RoadType(val value: Int) {
        LowVisibleTrack(15),
        OffTrack(16),
        SmallTrack(4),
        Track(17),
        TarmacRoad(18),
        DualCarriageway(12),
    }
}

data class Icon(
    val id: String,
    val angle: Int = 0,
    val w: Int = 50,
    val center: Point,
    val scaleX: Double = 1.0,
    val scaleY: Double = 1.0,
) : TulipElement(ElementType.Icon)

data class Point(
    val x: Double,
    val y: Double,
)


