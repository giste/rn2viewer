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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

// --- LANDSCAPE LAYOUTS ---

@Composable
fun ExpandedLandscapeLayout(onImportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(9f)) {
            DistanceSection(modifier = Modifier.weight(2f), textStyle = MaterialTheme.typography.displayMedium)
            RoadbookSection(modifier = Modifier.weight(5f))
        }
        BottomButtonBar(modifier = Modifier.weight(1f), iconSize = 48.dp, onImportClick = onImportClick)
    }
}

@Composable
fun CompactLandscapeLayout(onImportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(8f)) {
            DistanceSection(modifier = Modifier.weight(2f), textStyle = MaterialTheme.typography.headlineLarge)
            RoadbookSection(modifier = Modifier.weight(5f))
        }
        BottomButtonBar(modifier = Modifier.weight(2f), iconSize = 36.dp, onImportClick = onImportClick)
    }
}

// --- PORTRAIT LAYOUTS ---

@Composable
fun CompactPortraitLayout(onImportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        DistanceSection(modifier = Modifier.weight(6f), textStyle = MaterialTheme.typography.headlineMedium)
        RoadbookSection(modifier = Modifier.weight(11f))
        BottomButtonBar(modifier = Modifier.weight(3f), iconSize = 36.dp, onImportClick = onImportClick)
    }
}

@Composable
fun MediumPortraitLayout(onImportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        DistanceSection(modifier = Modifier.weight(4f), textStyle = MaterialTheme.typography.displayMedium)
        RoadbookSection(modifier = Modifier.weight(14f))
        BottomButtonBar(modifier = Modifier.weight(2f), iconSize = 48.dp, onImportClick = onImportClick)
    }
}

// --- SHARED COMPONENTS ---

@Composable
fun DistanceSection(modifier: Modifier = Modifier, textStyle: androidx.compose.ui.text.TextStyle) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, MaterialTheme.colorScheme.outline),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "0.00", style = textStyle)
    }
}

@Composable
fun RoadbookSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, MaterialTheme.colorScheme.outline),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Roadbook Content", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun BottomButtonBar(
    modifier: Modifier = Modifier, 
    iconSize: Dp,
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
            .padding(1.dp)

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
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(name = "Tablet - Landscape", device = "spec:width=1920px,height=1200px,dpi=280,orientation=landscape", showBackground = true)
@Composable
fun TabletLandPreview() {
    Rn2ViewerTheme {
        MainScreen(widthSizeClass = WindowWidthSizeClass.Expanded)
    }
}

@Preview(name = "Tablet - Portrait", device = "spec:width=1200px,height=1920px,dpi=280,orientation=portrait", showBackground = true)
@Composable
fun TabletPortPreview() {
    Rn2ViewerTheme {
        MainScreen(widthSizeClass = WindowWidthSizeClass.Medium)
    }
}

@Preview(name = "Phone - Portrait", device = "spec:width=411dp,height=891dp,orientation=portrait", showBackground = true)
@Composable
fun PhonePortPreview() {
    Rn2ViewerTheme {
        MainScreen(widthSizeClass = WindowWidthSizeClass.Compact)
    }
}

@Preview(name = "Phone - Landscape", device = "spec:width=891dp,height=411dp,orientation=landscape", showBackground = true)
@Composable
fun PhoneLandPreview() {
    Rn2ViewerTheme {
        MainScreen(widthSizeClass = WindowWidthSizeClass.Compact)
    }
}
