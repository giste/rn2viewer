package org.giste.rn2viewer.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.giste.rn2viewer.domain.model.Waypoint
import java.util.Locale
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun WaypointItem(waypoint: Waypoint, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First part: Distance info and number
            DistanceInfo(
                waypoint = waypoint,
                modifier = Modifier
                    .weight(1f)
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
        HorizontalDivider(thickness = 1.dp, color = Color.Black)
    }
}

@Composable
private fun DistanceInfo(waypoint: Waypoint, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    
    Box(
        modifier = modifier
            .border(0.5.dp, Color.Gray)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Accumulated distance (large)
            Text(
                text = String.format(locale, "%.2f", waypoint.distance / 1000.0),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.End)
            )

            // Waypoint number
            Text(
                text = waypoint.number.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Partial distance (small)
            Text(
                text = String.format(locale, "%.2f", waypoint.distanceFromPrevious / 1000.0),
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun TulipSection(waypoint: Waypoint, modifier: Modifier = Modifier) {
    // Aspect ratio height/width = 0.675
    Box(
        modifier = modifier
            .aspectRatio(1f / 0.675f)
            .border(0.5.dp, Color.Gray)
    ) {
        Text(
            text = "Tulip",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 10.sp,
            color = Color.LightGray
        )
    }
}

@Composable
private fun NotesSection(waypoint: Waypoint, modifier: Modifier = Modifier) {
    // Aspect ratio height/width = 0.675
    Box(
        modifier = modifier
            .aspectRatio(1f / 0.675f)
            .border(0.5.dp, Color.Gray)
    ) {
        Text(
            text = "Notes",
            modifier = Modifier.align(Alignment.Center),
            fontSize = 10.sp,
            color = Color.LightGray
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun WaypointItemPreview() {
    val sampleWaypoint = Waypoint(
        number = 1,
        latitude = 40.0,
        longitude = -3.0,
        distance = 1250.0,
        distanceFromPrevious = 450.0
    )
    WaypointItem(waypoint = sampleWaypoint)
}
