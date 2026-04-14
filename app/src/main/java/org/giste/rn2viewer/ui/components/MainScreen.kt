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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.giste.rn2viewer.ui.theme.Rn2Theme
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme

@Composable
fun MainScreen(widthSizeClass: WindowWidthSizeClass) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { _ ->
        // TODO: Handle the selected .rn2 file URI
    }
    val onImportClick = { launcher.launch("*/*") }

    Rn2ViewerTheme(widthSizeClass = widthSizeClass) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                isLandscape && widthSizeClass == WindowWidthSizeClass.Compact -> {
                    CompactLandscapeLayout(onImportClick)
                }
                isLandscape -> {
                    ExpandedLandscapeLayout(onImportClick)
                }
                widthSizeClass == WindowWidthSizeClass.Compact -> {
                    CompactPortraitLayout(onImportClick)
                }
                else -> {
                    MediumPortraitLayout(onImportClick)
                }
            }
        }
    }
}

// --- LANDSCAPE LAYOUTS ---

@Composable
fun ExpandedLandscapeLayout(onImportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(9f)) {
            LandscapeDistanceSection(modifier = Modifier.weight(2f))
            RoadbookSection(modifier = Modifier.weight(5f))
        }
        BottomButtonBar(modifier = Modifier.weight(1f), onImportClick = onImportClick)
    }
}

@Composable
fun CompactLandscapeLayout(onImportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(8.5f)) {
            LandscapeDistanceSection(modifier = Modifier.weight(2f))
            RoadbookSection(modifier = Modifier.weight(5f))
        }
        BottomButtonBar(modifier = Modifier.weight(1.5f), onImportClick = onImportClick)
    }
}

// --- PORTRAIT LAYOUTS ---

@Composable
fun CompactPortraitLayout(onImportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        PortraitDistanceSection(modifier = Modifier.weight(6.5f))
        RoadbookSection(modifier = Modifier.weight(12f))
        BottomButtonBar(modifier = Modifier.weight(1.5f), onImportClick = onImportClick)
    }
}

@Composable
fun MediumPortraitLayout(onImportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        PortraitDistanceSection(modifier = Modifier.weight(6f))
        RoadbookSection(modifier = Modifier.weight(12.5f))
        BottomButtonBar(modifier = Modifier.weight(1.5f), onImportClick = onImportClick)
    }
}

// --- SHARED COMPONENTS ---

@Composable
fun LandscapeDistanceSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
    ) {
        TotalDistance(distance = "9.999,99")
        PartialDistance(distance = "999,99")

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
fun PortraitDistanceSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline)
    ) {
        TotalDistance(distance = "9.999,99", modifier = Modifier.weight(1f))
        PartialDistance(distance = "999,99", modifier = Modifier.weight(1.2f))
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
fun RoadbookSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(Rn2Theme.dimensions.sectionBorder, MaterialTheme.colorScheme.outline),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Roadbook Content", style = MaterialTheme.typography.titleLarge)
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

@Preview(name = "Tablet - Landscape - Light", device = "spec:width=1920px,height=1200px,dpi=280,orientation=landscape", showBackground = true)
@Preview(name = "Tablet - Landscape - Dark", device = "spec:width=1920px,height=1200px,dpi=280,orientation=landscape", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TabletLandPreview() {
    Rn2ViewerTheme {
        MainScreen(widthSizeClass = WindowWidthSizeClass.Expanded)
    }
}

@Preview(name = "Tablet - Portrait - Light", device = "spec:width=1200px,height=1920px,dpi=280,orientation=portrait", showBackground = true)
@Preview(name = "Tablet - Portrait - Dark", device = "spec:width=1200px,height=1920px,dpi=280,orientation=portrait", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TabletPortPreview() {
    Rn2ViewerTheme {
        MainScreen(widthSizeClass = WindowWidthSizeClass.Medium)
    }
}

@Preview(name = "Phone - Portrait - Light", device = "spec:width=411dp,height=891dp,orientation=portrait", showBackground = true)
@Preview(name = "Phone - Portrait - Dark", device = "spec:width=411dp,height=891dp,orientation=portrait", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PhonePortPreview() {
    Rn2ViewerTheme {
        MainScreen(widthSizeClass = WindowWidthSizeClass.Compact)
    }
}

@Preview(name = "Phone - Landscape - Light", device = "spec:width=891dp,height=411dp,orientation=landscape", showBackground = true)
@Preview(name = "Phone - Landscape - Dark", device = "spec:width=891dp,height=411dp,orientation=landscape", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PhoneLandPreview() {
    Rn2ViewerTheme {
        MainScreen(widthSizeClass = WindowWidthSizeClass.Compact)
    }
}
