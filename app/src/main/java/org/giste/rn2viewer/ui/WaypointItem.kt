package org.giste.rn2viewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.giste.rn2viewer.domain.model.Waypoint
import java.util.Locale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign

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
            .border(0.5.dp, Color.Black),
    ) {
        // Accumulated distance (large)
        Text(
            text = String.format(locale, "%.2f", waypoint.distance / 1000.0),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.headlineMedium
        )

        // Reset
        if (waypoint.reset) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 2.dp,
                color = Color.Black,
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
                color = Color.DarkGray,
                modifier = Modifier
                    .weight(0.5f)
                    .border(0.5.dp, Color.Black)
                    .padding(horizontal = 2.dp),
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
                    .background(color = Color.Black)
                    .weight(0.25f)
                    .padding(horizontal = 2.dp),
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
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
            .border(0.5.dp, Color.Black)
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
            .border(0.5.dp, Color.Black)
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
    val waypointWithReset = Waypoint(
        number = 1,
        latitude = 40.0,
        longitude = -3.0,
        distance = 999990.0,
        distanceFromPrevious = 123.0,
        reset = true,
    )
    val waypoint = Waypoint(
        number = 999,
        latitude = 40.0,
        longitude = -3.0,
        distance = 90.0,
        distanceFromPrevious = 450123.0,
        reset = false,
    )

    Column(
        modifier = Modifier.padding(8.dp),
    ) {
        WaypointItem(waypoint = waypointWithReset)
        WaypointItem(waypoint = waypoint)
    }

}
