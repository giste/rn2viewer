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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme

@Composable
fun MainScreen() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { _ ->
        // TODO: Handle the selected .rn2 file URI
    }

    if (isLandscape) {
        MainScreenLandscape(onImportClick = { launcher.launch("*/*") })
    } else {
        MainScreenPortrait(onImportClick = { launcher.launch("*/*") })
    }
}

@Composable
fun MainScreenLandscape(onImportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(9f)) {
            DistanceSection(modifier = Modifier.weight(2f))
            RoadbookSection(modifier = Modifier.weight(5f))
        }
        BottomButtonBar(modifier = Modifier.weight(1f), onImportClick = onImportClick)
    }
}

@Composable
fun MainScreenPortrait(onImportClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        DistanceSection(modifier = Modifier.weight(6f))
        RoadbookSection(modifier = Modifier.weight(12.5f))
        BottomButtonBar(modifier = Modifier.weight(1.5f), onImportClick = onImportClick)
    }
}

@Composable
fun DistanceSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, MaterialTheme.colorScheme.outline),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Distance", style = MaterialTheme.typography.headlineLarge)
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
fun BottomButtonBar(modifier: Modifier = Modifier, onImportClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val buttonModifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(2.dp)

        OutlinedButton(onClick = { }, modifier = buttonModifier, shape = RectangleShape) {
            Icon(Icons.Default.KeyboardArrowDown, "Bajar", Modifier.size(48.dp))
        }
        OutlinedButton(onClick = { }, modifier = buttonModifier, shape = RectangleShape) {
            Icon(Icons.Default.Refresh, "Refrescar", Modifier.size(48.dp))
        }
        OutlinedButton(onClick = { }, modifier = buttonModifier, shape = RectangleShape) {
            Icon(Icons.Default.KeyboardArrowUp, "Subir", Modifier.size(48.dp))
        }
        OutlinedButton(onClick = { }, modifier = buttonModifier, shape = RectangleShape) {
            Icon(Icons.Default.Clear, "Limpiar", Modifier.size(48.dp))
        }
        OutlinedButton(onClick = onImportClick, modifier = buttonModifier, shape = RectangleShape) {
            Icon(Icons.Default.Search, "Importar", Modifier.size(48.dp))
        }
        OutlinedButton(onClick = { }, modifier = buttonModifier, shape = RectangleShape) {
            Icon(Icons.Default.Place, "Mapa", Modifier.size(48.dp))
        }
        OutlinedButton(onClick = { }, modifier = buttonModifier, shape = RectangleShape) {
            Icon(Icons.Default.Settings, "Ajustes", Modifier.size(48.dp))
        }
    }
}

// PREVIEWS

@Preview(name = "Tab - Land - Light", device = "spec:width=1920px,height=1200px,dpi=280,orientation=landscape", showBackground = true)
@Preview(name = "Tab - Land - Dark", device = "spec:width=1920px,height=1200px,dpi=280,orientation=landscape", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Tab - Port - Light", device = "spec:width=1200px,height=1920px,dpi=280,orientation=portrait", showBackground = true)
@Preview(name = "Tab - Port - Dark", device = "spec:width=1200px,height=1920px,dpi=280,orientation=portrait", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Phone - Port - Light", device = "spec:width=411dp,height=891dp,orientation=portrait", showBackground = true)
@Preview(name = "Phone - Port - Dark", device = "spec:width=411dp,height=891dp,orientation=portrait", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Phone - Land - Light", device = "spec:width=891dp,height=411dp,orientation=landscape", showBackground = true)
@Preview(name = "Phone - Land - Dark", device = "spec:width=891dp,height=411dp,orientation=landscape", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenPreview() {
    Rn2ViewerTheme {
        MainScreen()
    }
}
