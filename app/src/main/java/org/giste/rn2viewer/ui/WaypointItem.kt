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

package org.giste.rn2viewer.ui

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.giste.rn2viewer.R
import org.giste.rn2viewer.domain.model.Icon
import org.giste.rn2viewer.domain.model.Point
import org.giste.rn2viewer.domain.model.Road
import org.giste.rn2viewer.domain.model.Track
import org.giste.rn2viewer.domain.model.Waypoint
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import org.giste.rn2viewer.domain.model.Text as TulipText

@Composable
fun WaypointItem(waypoint: Waypoint, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(535f / 135),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // First part: Distance info and number
        DistanceInfo(
            waypoint = waypoint,
            modifier = Modifier
                .weight(weight = 1f, fill = true)
                .fillMaxHeight()
        )

        // Second part: Tulip elements
        TulipSection(
            waypoint = waypoint,
            modifier = Modifier.fillMaxHeight()
        )

        // Third part: Notes elements
        NotesSection(
            waypoint = waypoint,
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
private fun DistanceInfo(waypoint: Waypoint, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()

    Column(
        modifier = modifier
            .fillMaxSize()
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.onSurface),
    ) {
        // Accumulated distance (large)
        Text(
            text = String.format(locale, "%.2f", waypoint.distance / 1000.0),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )

        // Reset
        if (waypoint.reset) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = String.format(locale, "%.2f", 0.0),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Partial distance (small)
            Text(
                text = String.format(locale, "%.2f", waypoint.distanceFromPrevious / 1000.0),
                modifier = Modifier
                    .weight(0.5f)
                    .border(width = 0.5.dp, color = MaterialTheme.colorScheme.onSurface)
                    .padding(horizontal = 1.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
            )

            VerticalDivider(
                modifier = Modifier
                    .weight(0.25f)
                    .height(IntrinsicSize.Min)
            )

            // Waypoint number
            Text(
                text = waypoint.number.toString(),
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.inverseSurface)
                    .weight(0.25f)
                    .padding(horizontal = 1.dp),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private const val TULIP_LOGICAL_WIDTH = 200f
private const val TULIP_LOGICAL_HEIGHT = 135f
private val TULIP_CENTER_POINT = Offset(100f, 85f)

private enum class RoadTermination {
    NONE,
    ARROW,
    PERPENDICULAR
}

@Composable
private fun TulipSection(waypoint: Waypoint, modifier: Modifier = Modifier) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val textMeasurer = rememberTextMeasurer()

    // Preload painters for icons to use them inside Canvas
    val iconPainters = waypoint.tulipElements
        .filterIsInstance<Icon>()
        .associate { it.id to painterResource(id = R.drawable.ic_cross_danger_1) }

    Box(
        modifier = modifier
            .aspectRatio(TULIP_LOGICAL_WIDTH / TULIP_LOGICAL_HEIGHT)
            .border(width = 0.5.dp, color = onSurfaceColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            clipRect {
                val scale = size.width / TULIP_LOGICAL_WIDTH
                withTransform({
                    scale(scale, scale, pivot = Offset.Zero)
                }) {
                    waypoint.tulipElements.forEach { element ->
                        when (element) {
                            is Road -> drawRoad(element, onSurfaceColor, secondaryColor, RoadTermination.PERPENDICULAR)
                            is Track -> {
                                drawRoad(element.roadIn, tertiaryColor, secondaryColor, RoadTermination.NONE)
                                drawRoad(element.roadOut, tertiaryColor, secondaryColor, RoadTermination.ARROW)
                            }

                            is Icon -> {
                                iconPainters[element.id]?.let { painter ->
                                    drawTulipIcon(element, painter)
                                }
                            }

                            is TulipText -> {
                                drawTulipText(element, textMeasurer, onSurfaceColor)
                            }

                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawTulipText(
    textElement: TulipText,
    textMeasurer: TextMeasurer,
    color: Color
) {
    val center = Offset(textElement.center.x.toFloat(), textElement.center.y.toFloat())
    val textLayoutResult = textMeasurer.measure(
        text = textElement.text,
        style = TextStyle(
            color = color,
            fontSize = textElement.fontSize.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    )

    val topLeft = Offset(
        center.x - textLayoutResult.size.width / 2f,
        center.y - textLayoutResult.size.height / 2f
    )

    drawText(textLayoutResult, topLeft = topLeft)
}

private fun DrawScope.drawTulipIcon(icon: Icon, painter: Painter) {
    val w = icon.w.toFloat()
    val center = Offset(icon.center.x.toFloat(), icon.center.y.toFloat())

    val intrinsicSize = painter.intrinsicSize
    val drawSize = if (intrinsicSize.isSpecified && intrinsicSize.width > 0f && intrinsicSize.height > 0f) {
        val scale = kotlin.math.min(w / intrinsicSize.width, w / intrinsicSize.height)
        Size(intrinsicSize.width * scale, intrinsicSize.height * scale)
    } else {
        Size(w, w)
    }

    withTransform({
        translate(center.x, center.y)
        rotate(icon.angle.toFloat(), pivot = Offset.Zero)
        scale(icon.scaleX.toFloat(), icon.scaleY.toFloat(), pivot = Offset.Zero)
        translate(-drawSize.width / 2f, -drawSize.height / 2f)
    }) {
        with(painter) {
            draw(
                size = drawSize,
                alpha = 1f,
                colorFilter = null
            )
        }
    }
}

private fun DrawScope.drawRoad(
    road: Road,
    color: Color,
    secondaryColor: Color,
    termination: RoadTermination
) {
    val endRelative = road.end ?: return
    val start = TULIP_CENTER_POINT
    val end = TULIP_CENTER_POINT + Offset(endRelative.x.toFloat(), endRelative.y.toFloat())

    val lastPointBeforeEnd = if (road.handles.isNotEmpty()) {
        val lastHandle = road.handles.last()
        TULIP_CENTER_POINT + Offset(lastHandle.x.toFloat(), lastHandle.y.toFloat())
    } else {
        start
    }

    val angle = atan2(end.y - lastPointBeforeEnd.y, end.x - lastPointBeforeEnd.x)
    val arrowSize = 20f

    val allPoints = mutableListOf<Offset>()
    allPoints.add(start)
    road.handles.forEach {
        allPoints.add(TULIP_CENTER_POINT + Offset(it.x.toFloat(), it.y.toFloat()))
    }
    allPoints.add(end)

    val path = Path().apply {
        if (allPoints.size >= 2) {
            moveTo(allPoints[0].x, allPoints[0].y)
            if (allPoints.size == 2) {
                lineTo(allPoints[1].x, allPoints[1].y)
            } else {
                val n = allPoints.size
                for (i in 0 until n - 1) {
                    val p1 = allPoints[i]
                    val p2 = allPoints[i + 1]
                    
                    // Cubic Spline (Cardinal Spline with tension 0.5)
                    val p0 = if (i > 0) allPoints[i - 1] else p1
                    val p3 = if (i + 2 < n) allPoints[i + 2] else p2
                    
                    val cp1 = p1 + (p2 - p0) / 6f
                    val cp2 = p2 - (p3 - p1) / 6f
                    
                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
                }
            }
        }
    }

    when (road.roadType) {
        Road.RoadType.Track -> {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 6f, join = StrokeJoin.Miter, cap = StrokeCap.Butt)
            )
        }

        Road.RoadType.SmallTrack -> {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2f, join = StrokeJoin.Miter, cap = StrokeCap.Butt)
            )
        }

        Road.RoadType.LowVisibleTrack -> {
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = 2f,
                    join = StrokeJoin.Miter,
                    cap = StrokeCap.Butt,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 5f, 5f, 5f), 0f)
                )
            )
        }

        Road.RoadType.OffTrack -> {
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = 2f,
                    join = StrokeJoin.Miter,
                    cap = StrokeCap.Butt,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )
            )
        }

        Road.RoadType.TarmacRoad -> {
            val nativePath = path.asAndroidPath()
            val outlinePath = android.graphics.Path()
            val outlinePaint = android.graphics.Paint().apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 6f
                strokeCap = android.graphics.Paint.Cap.BUTT
                strokeJoin = android.graphics.Paint.Join.MITER
            }
            outlinePaint.getFillPath(nativePath, outlinePath)

            drawPath(
                path = outlinePath.asComposePath(),
                color = color,
                style = Stroke(width = 1f, join = StrokeJoin.Miter, cap = StrokeCap.Butt)
            )
        }

        Road.RoadType.DualCarriageway -> {
            drawPath(
                path = path,
                color = secondaryColor,
                style = Stroke(width = 2f, join = StrokeJoin.Miter, cap = StrokeCap.Butt)
            )

            val nativePath = path.asAndroidPath()
            val outlinePath = android.graphics.Path()
            val outlinePaint = android.graphics.Paint().apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 6f
                strokeCap = android.graphics.Paint.Cap.BUTT
                strokeJoin = android.graphics.Paint.Join.MITER
            }
            outlinePaint.getFillPath(nativePath, outlinePath)

            drawPath(
                path = outlinePath.asComposePath(),
                color = color,
                style = Stroke(width = 1f, join = StrokeJoin.Miter, cap = StrokeCap.Butt)
            )
        }
    }

    when (termination) {
        RoadTermination.ARROW -> drawArrowHead(angle, end, color, arrowSize)
        RoadTermination.PERPENDICULAR -> drawPerpendicularEnd(angle, end, color)
        RoadTermination.NONE -> {}
    }
}

private fun DrawScope.drawArrowHead(angle: Float, lineEnd: Offset, color: Color, arrowSize: Float) {
    val arrowHalfAngle = Math.toRadians(30.0).toFloat()
    val height = arrowSize * cos(arrowHalfAngle)

    val tip = Offset(
        lineEnd.x + height * cos(angle),
        lineEnd.y + height * sin(angle)
    )

    val path = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(
            tip.x - arrowSize * cos(angle - arrowHalfAngle),
            tip.y - arrowSize * sin(angle - arrowHalfAngle)
        )
        lineTo(
            tip.x - arrowSize * cos(angle + arrowHalfAngle),
            tip.y - arrowSize * sin(angle + arrowHalfAngle)
        )
        close()
    }
    drawPath(
        path = path,
        color = color
    )
}

private fun DrawScope.drawPerpendicularEnd(angle: Float, end: Offset, color: Color) {
    val segmentLength = 20f
    val dx = cos(angle)
    val dy = sin(angle)

    val p1 = Offset(end.x - dy * segmentLength / 2, end.y + dx * segmentLength / 2)
    val p2 = Offset(end.x + dy * segmentLength / 2, end.y - dx * segmentLength / 2)

    drawLine(
        color = color,
        start = p1,
        end = p2,
        strokeWidth = 1.5f,
        cap = StrokeCap.Butt
    )
}

@Composable
private fun NotesSection(waypoint: Waypoint, modifier: Modifier = Modifier) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()

    // Preload painters for icons in notes to use them inside Canvas
    val iconPainters = waypoint.notesElements
        .filterIsInstance<Icon>()
        .associate { it.id to painterResource(id = R.drawable.ic_cross_danger_1) }

    Box(
        modifier = modifier
            .aspectRatio(TULIP_LOGICAL_WIDTH / TULIP_LOGICAL_HEIGHT)
            .border(width = 0.5.dp, color = onSurfaceColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            clipRect {
                val scale = size.width / TULIP_LOGICAL_WIDTH
                withTransform({
                    scale(scale, scale, pivot = Offset.Zero)
                }) {
                    waypoint.notesElements.forEach { element ->
                        when (element) {
                            is Icon -> {
                                iconPainters[element.id]?.let { painter ->
                                    drawTulipIcon(element, painter)
                                }
                            }

                            is TulipText -> {
                                drawTulipText(element, textMeasurer, onSurfaceColor)
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Light Mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
fun WaypointItemPreview() {
    val waypointWithTulip = Waypoint(
        number = 1,
        latitude = 40.0,
        longitude = -3.0,
        distance = 1200.0,
        distanceFromPrevious = 1200.0,
        reset = false,
        tulipElements = listOf(
            Track(
                roadIn = Road(
                    start = null,
                    end = Point(0.0, 40.0),
                    roadType = Road.RoadType.TarmacRoad
                ),
                roadOut = Road(
                    start = null,
                    end = Point(50.0, -30.0),
                    roadType = Road.RoadType.Track,
                    handles = listOf(Point(30.0, 0.0))
                )
            )
        )
    )

    val waypointWith5Handles = Waypoint(
        number = 2,
        latitude = 40.0,
        longitude = -3.0,
        distance = 2500.0,
        distanceFromPrevious = 1300.0,
        tulipElements = listOf(
            Road(
                start = null,
                end = Point(80.0, -40.0),
                roadType = Road.RoadType.TarmacRoad,
                handles = listOf(
                    Point(10.0, 10.0),
                    Point(30.0, -20.0),
                    Point(50.0, 20.0),
                    Point(70.0, -10.0),
                    Point(75.0, -30.0)
                )
            ),
            Road(
                start = null,
                end = Point(-40.0, -80.0),
                roadType = Road.RoadType.DualCarriageway,
                handles = listOf(
                    Point(-10.0, -20.0),
                    Point(-30.0, -40.0)
                )
            )
        )
    )

    val waypointWithIcon = Waypoint(
        number = 3,
        latitude = 40.0,
        longitude = -3.0,
        distance = 3500.0,
        distanceFromPrevious = 1000.0,
        tulipElements = listOf(
            Track(
                roadIn = Road(null, Point(0.0, 40.0)),
                roadOut = Road(null, Point(0.0, -40.0))
            ),
            Icon(
                id = "danger",
                center = Point(130.0, 50.0),
                w = 50,
                angle = 0
            ),
            TulipText(
                text = "KM 1.2",
                center = Point(130.0, 100.0),
                fontSize = 12,
                width = 40.0,
                height = 20.0
            )
        ),
        notesElements = listOf(
            TulipText(
                text = "Attention!",
                center = Point(100.0, 40.0),
                fontSize = 14,
                width = 100.0,
                height = 20.0
            ),
            Icon(
                id = "danger",
                center = Point(100.0, 90.0),
                w = 40,
                angle = 0
            )
        )
    )

    val waypointWithAllRoads = Waypoint(
        number = 4,
        latitude = 40.0,
        longitude = -3.0,
        distance = 7500.0,
        distanceFromPrevious = 2000.0,
        tulipElements = listOf(
            Road(null, Point(-60.0, 0.0), Road.RoadType.Track),
            Road(null, Point(-40.0, -40.0), Road.RoadType.SmallTrack),
            Road(null, Point(0.0, -60.0), Road.RoadType.LowVisibleTrack),
            Road(null, Point(40.0, -40.0), Road.RoadType.OffTrack),
            Road(null, Point(60.0, 0.0), Road.RoadType.TarmacRoad),
            Road(null, Point(40.0, 40.0), Road.RoadType.DualCarriageway)
        )
    )

    val waypointWithReset = Waypoint(
        number = 5,
        latitude = 40.0,
        longitude = -3.0,
        distance = 5500.0,
        distanceFromPrevious = 2000.0,
        reset = true,
        tulipElements = listOf(
            Track(
                roadIn = Road(null, Point(0.0, 40.0)),
                roadOut = Road(
                    start = null,
                    end = Point(83.5, 35.0),
                )
            ),
            Road(
                start = null,
                end = Point(-91.5, -76.0),
                roadType = Road.RoadType.TarmacRoad,
            ),
            Road(
                start = null,
                end = Point(91.5, -76.0),
                handles = listOf(
                    Point(17.75703517587941, -51.797777777777775),
                    Point(76.69773869346733, -54.79333333333334),
                ),
            ),
            Road(
                start = null,
                end = Point(-91.5, 43.0),
                roadType = Road.RoadType.SmallTrack,
                handles = listOf(Point(-20.204773869346738, 25.088148148148164)),
            ),
        )
    )

    Rn2ViewerTheme {
        Surface {
            Column(
                modifier = Modifier.padding(1.dp),
            ) {
                WaypointItem(waypoint = waypointWithTulip)
                WaypointItem(waypoint = waypointWith5Handles)
                WaypointItem(waypoint = waypointWithIcon)
                WaypointItem(waypoint = waypointWithAllRoads)
                WaypointItem(waypoint = waypointWithReset)
            }
        }
    }
}
