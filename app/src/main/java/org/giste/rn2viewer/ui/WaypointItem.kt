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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.giste.rn2viewer.domain.model.Point
import org.giste.rn2viewer.domain.model.Road
import org.giste.rn2viewer.domain.model.Track
import org.giste.rn2viewer.domain.model.Waypoint
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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

@Composable
private fun TulipSection(waypoint: Waypoint, modifier: Modifier = Modifier) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .aspectRatio(TULIP_LOGICAL_WIDTH / TULIP_LOGICAL_HEIGHT)
            .border(width = 0.5.dp, color = onSurfaceColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scale = size.width / TULIP_LOGICAL_WIDTH
            withTransform({
                scale(scale, scale, pivot = Offset.Zero)
            }) {
                waypoint.tulipElements.forEach { element ->
                    when (element) {
                        is Road -> drawRoad(element, onSurfaceColor)
                        is Track -> {
                            drawRoad(element.roadIn, onSurfaceColor)
                            drawRoad(element.roadOut, onSurfaceColor)
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawRoad(road: Road, color: Color) {
    val endRelative = road.end ?: return
    val start = TULIP_CENTER_POINT
    val end = TULIP_CENTER_POINT + Offset(endRelative.x.toFloat(), endRelative.y.toFloat())

    val strokeWidth = when (road.roadType) {
        Road.RoadType.TarmacRoad, Road.RoadType.DualCarriageway -> 6f
        Road.RoadType.Track -> 4f
        else -> 2.5f
    }

    val path = Path().apply {
        moveTo(start.x, start.y)
        road.handles.forEach { handle ->
            lineTo(
                TULIP_CENTER_POINT.x + handle.x.toFloat(),
                TULIP_CENTER_POINT.y + handle.y.toFloat()
            )
        }
        lineTo(end.x, end.y)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
    )

    val lastPointBeforeEnd = if (road.handles.isNotEmpty()) {
        val lastHandle = road.handles.last()
        TULIP_CENTER_POINT + Offset(lastHandle.x.toFloat(), lastHandle.y.toFloat())
    } else {
        start
    }
    drawArrowHead(lastPointBeforeEnd, end, color, strokeWidth)
}

private fun DrawScope.drawArrowHead(start: Offset, end: Offset, color: Color, strokeWidth: Float) {
    val angle = atan2(end.y - start.y, end.x - start.x)
    val arrowSize = 12f
    val arrowAngle = Math.toRadians(30.0).toFloat()

    val path = Path().apply {
        moveTo(end.x, end.y)
        lineTo(
            end.x - arrowSize * cos(angle - arrowAngle),
            end.y - arrowSize * sin(angle - arrowAngle)
        )
        moveTo(end.x, end.y)
        lineTo(
            end.x - arrowSize * cos(angle + arrowAngle),
            end.y - arrowSize * sin(angle + arrowAngle)
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

@Composable
private fun NotesSection(waypoint: Waypoint, modifier: Modifier = Modifier) {
    // Aspect ratio height/width = 0.675
    Box(
        modifier = modifier
            .aspectRatio(1f / 0.675f)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.onSurface)
    ) {
        Text(
            text = "Notes",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 10.sp,
            color = Color.LightGray
        )
    }
}

@Preview(
    name = "Spanish Light",
    locale = "es",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Spanish Dark",
    locale = "es",
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

    val waypointWithReset = Waypoint(
        number = 2,
        latitude = 40.0,
        longitude = -3.0,
        distance = 2500.0,
        distanceFromPrevious = 1300.0,
        reset = true,
    )

    Rn2ViewerTheme {
        Surface {
            Column(
                modifier = Modifier.padding(1.dp),
            ) {
                WaypointItem(waypoint = waypointWithTulip)
                WaypointItem(waypoint = waypointWithReset)
            }
        }
    }
}
