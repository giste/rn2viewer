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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme

@Composable
fun MainScreen() {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { _ ->
        // TODO: Handle the selected .rn2 file URI
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Main Content Area (Two Columns)
        Row(modifier = Modifier.weight(9f)) {
            // Left Column (2f)
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .border(1.dp, MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Distance", style = MaterialTheme.typography.titleLarge)
            }

            // Right Column (5f)
            Box(
                modifier = Modifier
                    .weight(5f)
                    .fillMaxHeight()
                    .border(1.dp, MaterialTheme.colorScheme.outline),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Roadbook Content", style = MaterialTheme.typography.titleLarge)
            }
        }

        // Bottom Button Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val buttonModifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(2.dp)

            // 1. KeyboardArrowDown
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = buttonModifier,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Bajar",
                    modifier = Modifier.size(48.dp)
                )
            }

            // 2. Refresh
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = buttonModifier,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar",
                    modifier = Modifier.size(48.dp)
                )
            }

            // 3. KeyboardArrowUp
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = buttonModifier,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Subir",
                    modifier = Modifier.size(48.dp)
                )
            }

            // 4. Clear
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = buttonModifier,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Limpiar",
                    modifier = Modifier.size(48.dp)
                )
            }

            // 5. Search (Importar .rn2)
            OutlinedButton(
                onClick = { launcher.launch("*/*") },
                modifier = buttonModifier,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Importar .rn2",
                    modifier = Modifier.size(48.dp)
                )
            }

            // 6. Map/Place
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = buttonModifier,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Mapa",
                    modifier = Modifier.size(48.dp)
                )
            }

            // 7. Settings
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = buttonModifier,
                shape = RectangleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Preview(
    name = "Light Mode - Tab Active 3",
    showBackground = true,
    device = "spec:width=1920px,height=1200px,dpi=280,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode - Tab Active 3",
    showBackground = true,
    device = "spec:width=1920px,height=1200px,dpi=280,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode - Phone",
    showBackground = true,
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode - Phone",
    showBackground = true,
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MainScreenPreview() {
    Rn2ViewerTheme {
        MainScreen()
    }
}
