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

package org.giste.rn2viewer.ui.components

import android.content.res.Configuration
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.giste.rn2viewer.domain.model.Icon
import org.giste.rn2viewer.domain.model.Point
import org.giste.rn2viewer.domain.model.Road
import org.giste.rn2viewer.domain.model.Track
import org.giste.rn2viewer.domain.model.Waypoint
import org.giste.rn2viewer.ui.IconMapper
import org.giste.rn2viewer.ui.theme.Rn2Theme
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import org.giste.rn2viewer.domain.model.Text as TulipText

@Composable
fun WaypointItem(
    waypoint: Waypoint,
    onSetPartialClick: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val isHighDanger = waypoint.dangerLevel == Waypoint.DangerLevel.HIGH
    val borderColor = if (isHighDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val borderWidth = if (isHighDanger) Rn2Theme.dimensions.dangerHighThickness else Rn2Theme.dimensions.sectionBorder

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(535f / 135)
    ) {
        if (isHighDanger) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(width = borderWidth, color = borderColor)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = if (isHighDanger) 0.dp else borderWidth,
                    color = borderColor
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First part: Distance info and number
            DistanceInfo(
                waypoint = waypoint,
                onSetPartialClick = onSetPartialClick,
                modifier = Modifier
                    .weight(weight = 1f, fill = true)
                    .fillMaxHeight()
            )

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.onSurface,
                thickness = Rn2Theme.dimensions.sectionBorder
            )

            // Second part: Tulip elements
            TulipSection(
                waypoint = waypoint,
                modifier = Modifier.fillMaxHeight()
            )

            VerticalDivider(
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.onSurface,
                thickness = Rn2Theme.dimensions.sectionBorder
            )

            // Third part: Notes elements
            NotesSection(
                waypoint = waypoint,
                modifier = Modifier.fillMaxHeight()
            )
        }
    }
}

@Composable
private fun DistanceInfo(
    waypoint: Waypoint,
    onSetPartialClick: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()

    Column(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(
                onLongClick = { onSetPartialClick(if (waypoint.reset) 0.0 else waypoint.distance) },
                onClick = {}
            ),
    ) {
        // Accumulated distance (large)
        Text(
            text = String.format(locale, "%.2f", waypoint.distance / 1000.0),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displaySmall
        )

        if (waypoint.reset) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Rn2Theme.dimensions.paddingLarge),
                thickness = Rn2Theme.dimensions.resetDividerThickness,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (waypoint.dangerLevel == Waypoint.DangerLevel.MEDIUM) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = Rn2Theme.dimensions.paddingMedium),
                thickness = Rn2Theme.dimensions.dangerMediumThickness,
                color = MaterialTheme.colorScheme.error,
            )
        }

        // Reset
        if (waypoint.reset) {
            Text(
                text = String.format(locale, "%.2f", 0.0),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge,
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
                    .border(width = Rn2Theme.dimensions.sectionBorder, color = MaterialTheme.colorScheme.onSurface)
                    .padding(horizontal = Rn2Theme.dimensions.paddingMinimal),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )

            VerticalDivider(
                modifier = Modifier
                    .weight(0.25f)
                    .height(IntrinsicSize.Min),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Waypoint number
            Text(
                text = waypoint.number.toString(),
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.inverseSurface)
                    .weight(0.25f)
                    .padding(horizontal = Rn2Theme.dimensions.paddingMinimal),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private const val TULIP_LOGICAL_WIDTH = 200f
private const val TULIP_LOGICAL_HEIGHT = 135f
private val TULIP_CENTER_POINT = Offset(100f, 85f)
private const val SMOOTHNESS = 0.2f

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
        .associateWith { painterResource(id = IconMapper.getDrawableId(it)) }

    Box(
        modifier = modifier
            .aspectRatio(TULIP_LOGICAL_WIDTH / TULIP_LOGICAL_HEIGHT)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            clipRect {
                val scale = size.width / TULIP_LOGICAL_WIDTH
                withTransform({
                    scale(scale, scale, pivot = Offset.Zero)
                }) {
                    drawWaypointStart(onSurfaceColor)
                    waypoint.tulipElements.forEach { element ->
                        when (element) {
                            is Road -> drawRoad(element, onSurfaceColor, secondaryColor, RoadTermination.PERPENDICULAR)
                            is Track -> {
                                drawRoad(element.roadIn, tertiaryColor, secondaryColor, RoadTermination.NONE)
                                drawRoad(element.roadOut, tertiaryColor, secondaryColor, RoadTermination.ARROW)
                            }

                            is Icon -> {
                                iconPainters[element]?.let { painter ->
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

private fun DrawScope.drawWaypointStart(color: Color) {
    val start = TULIP_CENTER_POINT
    val end = start + Offset(15f, 15f)

    val path = Path().apply {
        moveTo(start.x, start.y)
        lineTo(end.x, end.y)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2f, join = StrokeJoin.Miter, cap = StrokeCap.Butt)
    )
    drawCircle(
        radius = 3f,
        color = color,
        center = end,
    )
}

private fun DrawScope.drawTulipText(
    textElement: TulipText,
    textMeasurer: TextMeasurer,
    color: Color
) {
    val center = Offset(textElement.center.x.toFloat(), textElement.center.y.toFloat())
    val maxWidth = textElement.maxWidth.toFloat()
    val maxHeight = textElement.maxHeight.toFloat()

    // fontSize from RN2 is the height of the font relative to the tulip height (135 logical units).
    // We want the text height to be exactly textElement.fontSize in logical units.
    // To achieve this regardless of system font settings, we divide by density and fontScale.
    val logicalFontSize = textElement.fontSize.toFloat()
    val fontSize = if (logicalFontSize > 0) {
        (logicalFontSize / (density * fontScale)).sp
    } else {
        12.sp // Fallback
    }

    val style = TextStyle(
        color = color,
        fontSize = fontSize,
        lineHeight = fontSize * (textElement.lineHeight.toFloat() * 1.1f),
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    val textLayoutResult = textMeasurer.measure(
        text = textElement.text,
        style = style,
        constraints = Constraints(
            maxWidth = maxWidth.toInt(),
            maxHeight = maxHeight.toInt()
        )
    )

    // Center the text block within its defined maxWidth/maxHeight box
    val boxTopLeft = Offset(
        center.x - maxWidth / 2f,
        center.y - maxHeight / 2f
    )

    val topLeft = Offset(
        boxTopLeft.x + (maxWidth - textLayoutResult.size.width) / 2f,
        boxTopLeft.y + (maxHeight - textLayoutResult.size.height) / 2f
    )

    clipRect(
        left = boxTopLeft.x,
        top = boxTopLeft.y,
        right = boxTopLeft.x + maxWidth,
        bottom = boxTopLeft.y + maxHeight
    ) {
        drawText(textLayoutResult, topLeft = topLeft)
    }
}

private fun DrawScope.drawTulipIcon(icon: Icon, painter: Painter) {
    val width = icon.width.toFloat()
    val height = icon.height.toFloat()
    val center = Offset(icon.center.x.toFloat(), icon.center.y.toFloat())

    val drawSize = Size(width, height)

    withTransform({
        translate(center.x, center.y)
        rotate(icon.angle.toFloat(), pivot = Offset.Zero)
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
    val start = if (road.start != null) {
        TULIP_CENTER_POINT + Offset(road.start.x.toFloat(), road.start.y.toFloat())
    } else {
        TULIP_CENTER_POINT
    }
    val end = TULIP_CENTER_POINT + Offset(endRelative.x.toFloat(), endRelative.y.toFloat())

    val allPoints = mutableListOf<Offset>().let {
        it.add(start)
        road.handles.forEach { handle ->
            it.add(TULIP_CENTER_POINT + Offset(handle.x.toFloat(), handle.y.toFloat()))
        }
        it.add(end)
        it.toList()
    }
    val angle = getAngle(allPoints, end, start)
    val path = getPath(allPoints)

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
                style = Stroke(width = 4f, join = StrokeJoin.Miter, cap = StrokeCap.Butt)
            )
        }

        Road.RoadType.LowVisibleTrack -> {
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = 4f,
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
                    width = 4f,
                    join = StrokeJoin.Miter,
                    cap = StrokeCap.Butt,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                )
            )
        }

        Road.RoadType.TarmacRoad -> {
            val nativePath = path.asAndroidPath()
            val outlinePath = android.graphics.Path()
            val outlinePaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 6f
                strokeCap = Paint.Cap.BUTT
                strokeJoin = Paint.Join.MITER
            }
            outlinePaint.getFillPath(nativePath, outlinePath)

            drawPath(
                path = outlinePath.asComposePath(),
                color = color,
                style = Stroke(width = 2f, join = StrokeJoin.Miter, cap = StrokeCap.Butt)
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
            val outlinePaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 8f
                strokeCap = Paint.Cap.BUTT
                strokeJoin = Paint.Join.MITER
            }
            outlinePaint.getFillPath(nativePath, outlinePath)

            drawPath(
                path = outlinePath.asComposePath(),
                color = color,
                style = Stroke(width = 2f, join = StrokeJoin.Miter, cap = StrokeCap.Butt)
            )
        }
    }

    when (termination) {
        RoadTermination.ARROW -> drawArrowHead(angle, end, color)
        RoadTermination.PERPENDICULAR -> drawPerpendicularEnd(angle, end, color)
        RoadTermination.NONE -> {}
    }
}

private fun getPath(
    allPoints: List<Offset>
): Path = Path().apply {
    if (allPoints.size >= 2) {
        moveTo(allPoints[0].x, allPoints[0].y)
        if (allPoints.size == 2) {
            lineTo(allPoints[1].x, allPoints[1].y)
        } else {
            for (i in 0 until allPoints.size - 1) {
                val current = allPoints[i]
                val next = allPoints[i + 1]

                when (i) {
                    0 -> {
                        val post = allPoints[2]
                        val cpX = next.x - (post.x - current.x) * SMOOTHNESS
                        val cpY = next.y - (post.y - current.y) * SMOOTHNESS
                        quadraticTo(cpX, cpY, next.x, next.y)
                    }

                    allPoints.size - 2 -> {
                        val prev = allPoints[i - 1]
                        val cpX = current.x + (next.x - prev.x) * SMOOTHNESS
                        val cpY = current.y + (next.y - prev.y) * SMOOTHNESS
                        quadraticTo(cpX, cpY, next.x, next.y)
                    }

                    else -> {
                        val prev = allPoints[i - 1]
                        val post = allPoints[i + 2]

                        val cp1 = current + (next - prev) * SMOOTHNESS
                        val cp2 = next - (post - current) * SMOOTHNESS

                        cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, next.x, next.y)
                    }
                }
            }
        }
    }
}

private fun getAngle(
    allPoints: List<Offset>,
    end: Offset,
    start: Offset
): Float = if (allPoints.size > 2) {
    val next = allPoints.last()
    val current = allPoints[allPoints.size - 2]
    val prev = allPoints[allPoints.size - 3]
    val cpX = current.x + (next.x - prev.x) * SMOOTHNESS
    val cpY = current.y + (next.y - prev.y) * SMOOTHNESS
    atan2(next.y - cpY, next.x - cpX)
} else {
    atan2(end.y - start.y, end.x - start.x)
}

private fun DrawScope.drawArrowHead(angle: Float, lineEnd: Offset, color: Color) {
    val arrowSize = 20f
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
        strokeWidth = 2f,
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
        .associateWith { painterResource(id = IconMapper.getDrawableId(it)) }

    Box(
        modifier = modifier
            .aspectRatio(TULIP_LOGICAL_WIDTH / TULIP_LOGICAL_HEIGHT)
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
                                iconPainters[element]?.let { painter ->
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    name = "Light Mode",
    device = "spec:width=1920px,height=1200px,dpi=280,orientation=portrait",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Dark Mode",
    device = "spec:width=1920px,height=1200px,dpi=280,orientation=portrait",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
fun WaypointItemPreview() {
    val waypointWithLowDangerAndRoadTypes = Waypoint(
        number = 1,
        latitude = 40.0,
        longitude = -3.0,
        distance = 1200.0,
        distanceFromPrevious = 1200.0,
        reset = false,
        dangerLevel = Waypoint.DangerLevel.LOW,
        tulipElements = listOf(
            Road(null, Point(-60.0, 0.0), Road.RoadType.Track),
            Road(null, Point(-40.0, -40.0), Road.RoadType.SmallTrack),
            Road(null, Point(-40.0, 40.0), Road.RoadType.LowVisibleTrack),
            Road(null, Point(40.0, -40.0), Road.RoadType.OffTrack),
            Road(null, Point(60.0, 0.0), Road.RoadType.TarmacRoad),
            Road(null, Point(40.0, 40.0), Road.RoadType.DualCarriageway),
            Track(
                roadIn = Road(null, Point(0.0, 35.0)),
                roadOut = Road(null, Point(0.0, -55.0))
            ),
        ),
        notesElements = listOf(
            Icon.Danger1(
                center = Point(100.0, 67.5),
                width = 80,
                height = 80,
                angle = 0
            )
        )
    )

    val waypointWithMediumDangerAndText = Waypoint(
        number = 3,
        latitude = 40.0,
        longitude = -3.0,
        distance = 3500.0,
        distanceFromPrevious = 1000.0,
        dangerLevel = Waypoint.DangerLevel.MEDIUM,
        reset = true,
        tulipElements = listOf(
            Track(
                roadIn = Road(null, Point(0.0, 40.0)),
                roadOut = Road(null, Point(0.0, -40.0))
            ),
            TulipText(
                text = "KM 1.2",
                center = Point(150.0, 100.0),
                fontSize = 12,
                lineHeight = 1.0,
                width = 40.0,
                height = 20.0,
                maxWidth = 180.0,
                maxHeight = 100.0,
            )
        ),
        notesElements = listOf(
            TulipText(
                text = "Attention!",
                center = Point(100.0, 40.0),
                fontSize = 14,
                lineHeight = 1.0,
                width = 100.0,
                height = 20.0,
                maxWidth = 180.0,
                maxHeight = 100.0,
            ),
            Icon.Danger2(
                center = Point(100.0, 90.0),
                width = 40,
                height = 40,
                angle = 0
            )
        )
    )

    val waypointWithHighDangerAndHandles = Waypoint(
        number = 5,
        latitude = 40.0,
        longitude = -3.0,
        distance = 5500.0,
        distanceFromPrevious = 2000.0,
        reset = true,
        dangerLevel = Waypoint.DangerLevel.HIGH,
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
        ),
        notesElements = listOf(
            Icon.Danger3(
                center = Point(100.0, 67.5),
                width = 80,
                height = 80,
                angle = 0
            )
        )
    )

    val waypointWithIcons = Waypoint(
        number = 6,
        latitude = 40.0,
        longitude = -3.0,
        distance = 9500.0,
        distanceFromPrevious = 2000.0,
        tulipElements = listOf(
            Icon.Danger1(center = Point(40.0, 40.0), width = 30, height = 30),
            Icon.Danger2(center = Point(80.0, 40.0), width = 30, height = 30),
            Icon.Danger3(center = Point(120.0, 40.0), width = 30, height = 30),
            Icon.FuelZone(center = Point(160.0, 40.0), width = 30, height = 30),
            Icon.ResetDistance(center = Point(40.0, 80.0), width = 30, height = 30),
            Icon.AboveBridge(center = Point(80.0, 80.0), width = 30, height = 30),
            Icon.FortCastle(center = Point(120.0, 80.0), width = 30, height = 30),
            Icon.House(center = Point(160.0, 80.0), width = 30, height = 30),
        ),
        notesElements = listOf(
            Icon.TrafficLight(center = Point(40.0, 40.0), width = 30, height = 30),
            Icon.Tunnel(center = Point(80.0, 40.0), width = 30, height = 30),
            Icon.UnderBridge(center = Point(120.0, 40.0), width = 30, height = 30),
            Icon.Alert(center = Point(160.0, 40.0), width = 30, height = 30),
            Icon.Roundabout(center = Point(40.0, 80.0), width = 30, height = 30),
            Icon.Stop(center = Point(80.0, 80.0), width = 30, height = 30),
            Icon.RiverWater(center = Point(120.0, 80.0), width = 30, height = 30),
        ),
    )

    Rn2ViewerTheme(
        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(1200.dp, 1920.dp))
    ) {
        Surface {
            Column(
                modifier = Modifier.padding(Rn2Theme.dimensions.paddingMinimal),
            ) {
                WaypointItem(waypoint = waypointWithLowDangerAndRoadTypes, onSetPartialClick = {})
                WaypointItem(waypoint = waypointWithMediumDangerAndText, onSetPartialClick = {})
                WaypointItem(waypoint = waypointWithHighDangerAndHandles, onSetPartialClick = {})
                WaypointItem(waypoint = waypointWithIcons, onSetPartialClick = {})
            }
        }
    }
}
