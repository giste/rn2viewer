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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.giste.rn2viewer.domain.model.Route
import org.giste.rn2viewer.domain.model.Waypoint
import org.giste.rn2viewer.ui.components.roadbook.WaypointItem
import org.giste.rn2viewer.ui.theme.Rn2Theme
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import org.giste.rn2viewer.ui.viewmodel.MainUiState
import org.giste.rn2viewer.ui.viewmodel.MainViewModel
import org.giste.rn2viewer.ui.viewmodel.RoadbookUiState

@Composable
fun MainScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importRoute(it)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.MediaNext, Key.DirectionUp -> {
                            coroutineScope.launch {
                                listState.animateScrollToItem(listState.firstVisibleItemIndex + 1)
                            }
                            true
                        }
                        Key.MediaPrevious, Key.DirectionDown -> {
                            coroutineScope.launch {
                                listState.animateScrollToItem(maxOf(0, listState.firstVisibleItemIndex - 1))
                            }
                            true
                        }
                        Key.VolumeUp, Key.DirectionRight -> {
                            viewModel.incrementPartialDistance()
                            true
                        }
                        Key.VolumeDown, Key.DirectionLeft -> {
                            viewModel.decrementPartialDistance()
                            true
                        }
                        Key.MediaPlayPause, Key.MediaPlay, Key.MediaPause, Key.F6 -> {
                            viewModel.resetPartialDistance()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        MainContent(
            windowSizeClass = windowSizeClass,
            uiState = uiState,
            listState = listState,
            onImportClick = { launcher.launch("*/*") },
            onSetPartialClick = { viewModel.setPartialDistance(it) },
            onLongClickPartial = { viewModel.showSetPartialDialog() }
        )

        if (uiState.showSetPartialDialog) {
            SetPartialDialog(
                windowSizeClass = windowSizeClass,
                onDismiss = { viewModel.hideSetPartialDialog() },
                onConfirm = {
                    viewModel.setPartialDistance(it)
                    viewModel.hideSetPartialDialog()
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun MainContent(
    windowSizeClass: WindowSizeClass,
    uiState: MainUiState,
    listState: LazyListState,
    onImportClick: () -> Unit,
    onSetPartialClick: (Double) -> Unit,
    onLongClickPartial: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]

    val totalDistanceStr = String.format(locale, "%.1f", uiState.odometer.total / 1000.0)
    val partialDistanceStr = String.format(locale, "%.2f", uiState.odometer.partial / 1000.0)

    val widthSizeClass = windowSizeClass.widthSizeClass
    val heightSizeClass = windowSizeClass.heightSizeClass

    // Pure Size-Based Logic:
    // 1. If width is Compact, use Portrait (vertical stacking).
    // 2. If height is Compact, it's a "wide but short" window (like a phone in landscape).
    // 3. Otherwise, it's a large window (Tablet/Expanded).
    
    val isWide = widthSizeClass > WindowWidthSizeClass.Compact
    val isShort = heightSizeClass == WindowHeightSizeClass.Compact

    Rn2ViewerTheme(windowSizeClass = windowSizeClass) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            val roadbookState = uiState.roadbook

            when {
                isShort && isWide -> {
                    CompactLandscapeLayout(
                        roadbookState = roadbookState,
                        listState = listState,
                        totalDistance = totalDistanceStr,
                        partialDistance = partialDistanceStr,
                        onImportClick = onImportClick,
                        onSetPartialClick = onSetPartialClick,
                        onLongClickPartial = onLongClickPartial
                    )
                }

                isWide -> {
                    ExpandedLandscapeLayout(
                        roadbookState = roadbookState,
                        listState = listState,
                        totalDistance = totalDistanceStr,
                        partialDistance = partialDistanceStr,
                        onImportClick = onImportClick,
                        onSetPartialClick = onSetPartialClick,
                        onLongClickPartial = onLongClickPartial
                    )
                }

                else -> {
                    PortraitLayout(
                        roadbookState = roadbookState,
                        listState = listState,
                        totalDistance = totalDistanceStr,
                        partialDistance = partialDistanceStr,
                        onImportClick = onImportClick,
                        onSetPartialClick = onSetPartialClick,
                        onLongClickPartial = onLongClickPartial
                    )
                }
            }
        }
    }
}

// --- LANDSCAPE LAYOUTS ---

@Composable
fun ExpandedLandscapeLayout(
    roadbookState: RoadbookUiState,
    listState: LazyListState,
    totalDistance: String,
    partialDistance: String,
    onImportClick: () -> Unit,
    onSetPartialClick: (Double) -> Unit,
    onLongClickPartial: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        LandscapeDistanceSection(
            totalDistance = totalDistance,
            partialDistance = partialDistance,
            onLongClickPartial = onLongClickPartial,
            onImportClick = onImportClick,
            modifier = Modifier.weight(2f)
        )
        RoadbookSection(
            state = roadbookState,
            listState = listState,
            modifier = Modifier.weight(5f),
            onSetPartialClick = onSetPartialClick
        )
    }
}

@Composable
fun CompactLandscapeLayout(
    roadbookState: RoadbookUiState,
    listState: LazyListState,
    totalDistance: String,
    partialDistance: String,
    onImportClick: () -> Unit,
    onSetPartialClick: (Double) -> Unit,
    onLongClickPartial: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        LandscapeDistanceSection(
            totalDistance = totalDistance,
            partialDistance = partialDistance,
            onLongClickPartial = onLongClickPartial,
            onImportClick = onImportClick,
            modifier = Modifier.weight(2f)
        )
        RoadbookSection(
            state = roadbookState,
            listState = listState,
            modifier = Modifier.weight(5f),
            onSetPartialClick = onSetPartialClick
        )
    }
}

// --- PORTRAIT LAYOUT ---

@Composable
fun PortraitLayout(
    roadbookState: RoadbookUiState,
    listState: LazyListState,
    totalDistance: String,
    partialDistance: String,
    onImportClick: () -> Unit,
    onSetPartialClick: (Double) -> Unit,
    onLongClickPartial: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PortraitDistanceSection(
            totalDistance = totalDistance,
            partialDistance = partialDistance,
            onLongClickPartial = onLongClickPartial,
            onImportClick = onImportClick,
            modifier = Modifier.fillMaxWidth()
        )
        RoadbookSection(
            state = roadbookState,
            listState = listState,
            modifier = Modifier.weight(1f),
            onSetPartialClick = onSetPartialClick
        )
    }
}

// --- SHARED COMPONENTS ---

@Composable
fun LandscapeDistanceSection(
    totalDistance: String,
    partialDistance: String,
    onLongClickPartial: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
    ) {
        TotalDistance(
            distance = totalDistance,
            onImportClick = onImportClick,
            onSettingsClick = { /* TODO */ }
        )
        PartialDistance(
            distance = partialDistance,
            onLongClick = onLongClickPartial
        )

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
fun PortraitDistanceSection(
    totalDistance: String,
    partialDistance: String,
    onLongClickPartial: () -> Unit,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
    ) {
        // Total | Partial side-by-side
        TotalDistance(
            distance = totalDistance, 
            onImportClick = onImportClick,
            onSettingsClick = { /* TODO */ },
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
        )
        PartialDistance(
            distance = partialDistance,
            onLongClick = onLongClickPartial,
            modifier = Modifier
                .weight(0.45f)
                .fillMaxHeight()
        )
    }
}

@Composable
fun TotalDistance(
    distance: String, 
    onImportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = Rn2Theme.dimensions.paddingSmall),
            horizontalArrangement = Arrangement.spacedBy(Rn2Theme.dimensions.paddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Import",
                modifier = Modifier
                    .size(Rn2Theme.dimensions.actionIconSize)
                    .combinedClickable(onClick = onImportClick),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier
                    .size(Rn2Theme.dimensions.actionIconSize)
                    .combinedClickable(onClick = onSettingsClick),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = Rn2Theme.dimensions.paddingTiny)
                .padding(horizontal = Rn2Theme.dimensions.paddingSmall),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = distance,
                style = MaterialTheme.typography.displaySmall,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun PartialDistance(
    distance: String,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
            .combinedClickable(
                onClick = { },
                onLongClick = onLongClick
            )
            .padding(vertical = Rn2Theme.dimensions.paddingTiny)
            .padding(horizontal = Rn2Theme.dimensions.paddingSmall),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = distance,
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun RoadbookSection(
    state: RoadbookUiState,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onSetPartialClick: (Double) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
    ) {
        when (state) {
            is RoadbookUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is RoadbookUiState.Empty -> {
                Text(
                    text = "No route loaded",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            is RoadbookUiState.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            is RoadbookUiState.Success -> {
                RoadbookList(
                    waypoints = state.route.waypoints,
                    listState = listState,
                    onSetPartialClick = onSetPartialClick
                )
            }
        }
    }
}

@Composable
fun RoadbookList(
    waypoints: List<Waypoint>,
    listState: LazyListState,
    onSetPartialClick: (Double) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(
            items = waypoints,
            key = { _, waypoint -> waypoint.number }
        ) { _, waypoint ->
            WaypointItem(
                waypoint = waypoint,
                onSetPartialClick = onSetPartialClick
            )
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
        number = 999,
        latitude = 40.02,
        longitude = -3.02,
        distance = 240000.0,
        distanceFromPrevious = 11500.0,
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
    roadbook = RoadbookUiState.Success(
        route = Route(
            name = "Test Route",
            waypoints = sampleWaypoints
        )
    ),
    odometer = org.giste.rn2viewer.domain.model.Odometer(
        total = 2400.0,
        partial = 1150.0
    )
)

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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
    val listState = rememberLazyListState()
    MainContent(
        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(1920.dp, 1200.dp)),
        uiState = sampleUiState,
        listState = listState,
        onImportClick = {},
        onSetPartialClick = {},
        onLongClickPartial = {}
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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
    val listState = rememberLazyListState()
    MainContent(
        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(1200.dp, 1920.dp)),
        uiState = sampleUiState,
        listState = listState,
        onImportClick = {},
        onSetPartialClick = {},
        onLongClickPartial = {}
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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
    val listState = rememberLazyListState()
    MainContent(
        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp)),
        uiState = sampleUiState,
        listState = listState,
        onImportClick = {},
        onSetPartialClick = {},
        onLongClickPartial = {}
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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
    val listState = rememberLazyListState()
    MainContent(
        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(891.dp, 411.dp)),
        uiState = sampleUiState,
        listState = listState,
        onImportClick = {},
        onSetPartialClick = {},
        onLongClickPartial = {}
    )
}
