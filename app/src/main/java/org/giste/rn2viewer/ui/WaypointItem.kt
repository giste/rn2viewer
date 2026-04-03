package org.giste.rn2viewer.ui

import android.content.res.Configuration
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.giste.rn2viewer.domain.model.Waypoint
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import java.util.Locale

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

@Composable
private fun TulipSection(waypoint: Waypoint, modifier: Modifier = Modifier) {
    // Aspect ratio height/width = 0.675
    Box(
        modifier = modifier
            .aspectRatio(1f / 0.675f)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.onSurface)
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

    Rn2ViewerTheme {
        Surface {
            Column(
                modifier = Modifier.padding(1.dp),
            ) {
                WaypointItem(waypoint = waypointWithReset)
                WaypointItem(waypoint = waypoint)
            }
        }
    }


}
