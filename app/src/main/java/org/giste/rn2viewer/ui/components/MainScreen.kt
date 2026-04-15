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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.giste.rn2viewer.domain.model.Waypoint
import org.giste.rn2viewer.ui.WaypointItem
import org.giste.rn2viewer.ui.theme.Rn2Theme
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import org.giste.rn2viewer.ui.viewmodel.MainUiState
import org.giste.rn2viewer.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    widthSizeClass: WindowWidthSizeClass,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // TODO: viewModel.importRoute(it) 
        }
    }

    MainContent(
        widthSizeClass = widthSizeClass,
        uiState = uiState,
        onImportClick = { launcher.launch("*/*") }
    )
}

@Composable
fun MainContent(
    widthSizeClass: WindowWidthSizeClass,
    uiState: MainUiState,
    onImportClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val locale = configuration.locales[0]

    val totalDistanceStr = String.format(locale, "%.1f", uiState.totalDistance / 1000.0)
    val partialDistanceStr = String.format(locale, "%.2f", uiState.partialDistance / 1000.0)

    Rn2ViewerTheme(widthSizeClass = widthSizeClass) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val waypoints = uiState.route?.waypoints ?: emptyList()

            when {
                isLandscape && widthSizeClass == WindowWidthSizeClass.Compact -> {
                    CompactLandscapeLayout(
                        waypoints = waypoints,
                        totalDistance = totalDistanceStr,
                        partialDistance = partialDistanceStr,
                        onImportClick = onImportClick
                    )
                }

                isLandscape -> {
                    ExpandedLandscapeLayout(
                        waypoints = waypoints,
                        totalDistance = totalDistanceStr,
                        partialDistance = partialDistanceStr,
                        onImportClick = onImportClick
                    )
                }

                widthSizeClass == WindowWidthSizeClass.Compact -> {
                    CompactPortraitLayout(
                        waypoints = waypoints,
                        totalDistance = totalDistanceStr,
                        partialDistance = partialDistanceStr,
                        onImportClick = onImportClick
                    )
                }

                else -> {
                    MediumPortraitLayout(
                        waypoints = waypoints,
                        totalDistance = totalDistanceStr,
                        partialDistance = partialDistanceStr,
                        onImportClick = onImportClick
                    )
                }
            }
        }
    }
}

// --- LANDSCAPE LAYOUTS ---

@Composable
fun ExpandedLandscapeLayout(
    waypoints: List<Waypoint>,
    totalDistance: String,
    partialDistance: String,
    onImportClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(9f)) {
            LandscapeDistanceSection(
                totalDistance = totalDistance,
                partialDistance = partialDistance,
                modifier = Modifier.weight(2f)
            )
            RoadbookSection(
                waypoints = waypoints,
                modifier = Modifier.weight(5f)
            )
        }
        BottomButtonBar(modifier = Modifier.weight(1f), onImportClick = onImportClick)
    }
}

@Composable
fun CompactLandscapeLayout(
    waypoints: List<Waypoint>,
    totalDistance: String,
    partialDistance: String,
    onImportClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        SideButtonBar(modifier = Modifier.weight(1f), onImportClick = onImportClick)
        Row(modifier = Modifier.weight(9f)) {
            LandscapeDistanceSection(
                totalDistance = totalDistance,
                partialDistance = partialDistance,
                modifier = Modifier.weight(2f)
            )
            RoadbookSection(
                waypoints = waypoints,
                modifier = Modifier.weight(5f)
            )
        }
    }
}

// --- PORTRAIT LAYOUTS ---

@Composable
fun CompactPortraitLayout(
    waypoints: List<Waypoint>,
    totalDistance: String,
    partialDistance: String,
    onImportClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CompactPortraitDistanceSection(
            totalDistance = totalDistance,
            partialDistance = partialDistance,
            modifier = Modifier.fillMaxWidth()
        )
        RoadbookSection(
            waypoints = waypoints,
            modifier = Modifier.weight(1f)
        )
        BottomButtonBar(modifier = Modifier.fillMaxWidth(), onImportClick = onImportClick)
    }
}

@Composable
fun MediumPortraitLayout(
    waypoints: List<Waypoint>,
    totalDistance: String,
    partialDistance: String,
    onImportClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MediumPortraitDistanceSection(
            totalDistance = totalDistance,
            partialDistance = partialDistance,
            modifier = Modifier.weight(6f)
        )
        RoadbookSection(
            waypoints = waypoints,
            modifier = Modifier.weight(12.5f)
        )
        BottomButtonBar(modifier = Modifier.weight(1.5f), onImportClick = onImportClick)
    }
}

// --- SHARED COMPONENTS ---

@Composable
fun LandscapeDistanceSection(
    totalDistance: String,
    partialDistance: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
    ) {
        TotalDistance(distance = totalDistance)
        PartialDistance(distance = partialDistance)

        // Map Area (Bottom) - Fills ALL remaining space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Map Placeholder",
                modifier = Modifier.size(Rn2Theme.dimensions.actionIconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MediumPortraitDistanceSection(
    totalDistance: String,
    partialDistance: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
    ) {
        // Left Column: Distances (stacked) - Fixed ratio for Tablet Portrait
        Column(modifier = Modifier.weight(0.35f)) {
            TotalDistance(distance = totalDistance, modifier = Modifier.weight(1f))
            PartialDistance(distance = partialDistance, modifier = Modifier.weight(1.2f))
        }

        // Right Column: Map Area - Fills the rest (0.65f)
        Box(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Map Placeholder",
                modifier = Modifier.size(Rn2Theme.dimensions.actionIconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CompactPortraitDistanceSection(
    totalDistance: String,
    partialDistance: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
    ) {
        // Total | Partial side-by-side
        TotalDistance(distance = totalDistance, modifier = Modifier
            .weight(4f)
            .fillMaxHeight())
        PartialDistance(distance = partialDistance, modifier = Modifier
            .weight(6f)
            .fillMaxHeight())
    }
}

@Composable
fun TotalDistance(distance: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
            .padding(vertical = Rn2Theme.dimensions.paddingTiny)
            .padding(horizontal = Rn2Theme.dimensions.paddingSmall),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = distance,
            style = MaterialTheme.typography.displayMedium,
            maxLines = 1
        )
    }
}

@Composable
fun PartialDistance(distance: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
            .padding(vertical = Rn2Theme.dimensions.paddingTiny)
            .padding(horizontal = Rn2Theme.dimensions.paddingSmall),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = distance,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.2f,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1
        )
    }
}

@Composable
fun RoadbookSection(
    waypoints: List<Waypoint>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(
                items = waypoints,
                key = { _, waypoint -> waypoint.number }
            ) { _, waypoint ->
                WaypointItem(waypoint = waypoint)
            }
        }

        if (waypoints.isEmpty()) {
            Text(
                text = "No route loaded",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun SideButtonBar(
    modifier: Modifier = Modifier,
    onImportClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val buttonModifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(Rn2Theme.dimensions.buttonPadding)

        val actions = listOf(
            Icons.Default.KeyboardArrowDown to "Down",
            Icons.Default.Refresh to "Refresh",
            Icons.Default.KeyboardArrowUp to "Up",
            Icons.Default.Clear to "Clear",
            Icons.Default.Search to "Import",
            Icons.Default.Place to "Map",
            Icons.Default.Settings to "Settings"
        )

        actions.forEach { (icon, label) ->
            OutlinedButton(
                onClick = { if (label == "Import") onImportClick() },
                modifier = buttonModifier,
                shape = RectangleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(Rn2Theme.dimensions.actionIconSize)
                )
            }
        }
    }
}

@Composable
fun BottomButtonBar(
    modifier: Modifier = Modifier,
    onImportClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val buttonModifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(Rn2Theme.dimensions.buttonPadding)

        val actions = listOf(
            Icons.Default.KeyboardArrowDown to "Down",
            Icons.Default.Refresh to "Refresh",
            Icons.Default.KeyboardArrowUp to "Up",
            Icons.Default.Clear to "Clear",
            Icons.Default.Search to "Import",
            Icons.Default.Place to "Map",
            Icons.Default.Settings to "Settings"
        )

        actions.forEach { (icon, label) ->
            OutlinedButton(
                onClick = { if (label == "Import") onImportClick() },
                modifier = buttonModifier,
                shape = RectangleShape,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(Rn2Theme.dimensions.actionIconSize)
                )
            }
        }
    }
}

// --- PREVIEWS ---

private val sampleWaypoints = listOf(
    Waypoint(
        number = 1,
        latitude = 40.0,
        longitude = -3.0,
        distance = 0.0,
        distanceFromPrevious = 0.0,
    ),
    Waypoint(
        number = 2,
        latitude = 40.01,
        longitude = -3.01,
        distance = 1250.0,
        distanceFromPrevious = 1250.0,
    ),
    Waypoint(
        number = 3,
        latitude = 40.02,
        longitude = -3.02,
        distance = 2400.0,
        distanceFromPrevious = 1150.0,
        reset = true
    ),
    Waypoint(
        number = 4,
        latitude = 40.03,
        longitude = -3.03,
        distance = 3800.0,
        distanceFromPrevious = 1400.0,
    ),
    Waypoint(
        number = 5,
        latitude = 40.04,
        longitude = -3.04,
        distance = 5100.0,
        distanceFromPrevious = 1300.0,
    )
)

private val sampleUiState = MainUiState(
    route = org.giste.rn2viewer.domain.model.Route(
        name = "Test Route",
        waypoints = sampleWaypoints
    ),
    totalDistance = 2400.0,
    partialDistance = 1150.0
)

@Preview(
    name = "Tab Active 3 - Landscape - Light",
    device = "spec:width=1920px,height=1200px,dpi=280,orientation=landscape",
    showBackground = true,
    locale = "es",
)
@Preview(
    name = "Tab Active 3 - Landscape - Dark",
    device = "spec:width=1920px,height=1200px,dpi=280,orientation=landscape",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TabletLandPreview() {
    MainContent(
        widthSizeClass = WindowWidthSizeClass.Expanded,
        uiState = sampleUiState,
        onImportClick = {}
    )
}

@Preview(
    name = "Tab Active 3 - Portrait - Light",
    device = "spec:width=1200px,height=1920px,dpi=280,orientation=portrait",
    showBackground = true
)
@Preview(
    name = "Tab Active 3 - Portrait - Dark",
    device = "spec:width=1200px,height=1920px,dpi=280,orientation=portrait",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TabletPortPreview() {
    MainContent(
        widthSizeClass = WindowWidthSizeClass.Medium,
        uiState = sampleUiState,
        onImportClick = {}
    )
}

@Preview(
    name = "Phone - Portrait - Light",
    device = "spec:width=411dp,height=891dp,orientation=portrait",
    showBackground = true
)
@Preview(
    name = "Phone - Portrait - Dark",
    device = "spec:width=411dp,height=891dp,orientation=portrait",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PhonePortPreview() {
    MainContent(
        widthSizeClass = WindowWidthSizeClass.Compact,
        uiState = sampleUiState,
        onImportClick = {}
    )
}

@Preview(
    name = "Phone - Landscape - Light",
    device = "spec:width=891dp,height=411dp,orientation=landscape",
    showBackground = true
)
@Preview(
    name = "Phone - Landscape - Dark",
    device = "spec:width=891dp,height=411dp,orientation=landscape",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PhoneLandPreview() {
    MainContent(
        widthSizeClass = WindowWidthSizeClass.Compact,
        uiState = sampleUiState,
        onImportClick = {}
    )
}
