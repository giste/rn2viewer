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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.giste.rn2viewer.R
import org.giste.rn2viewer.ui.theme.Rn2Theme
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme

@Composable
fun SetPartialDialog(
    windowSizeClass: WindowSizeClass,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    
    val isWide = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
    val isShort = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    val useLandscapeLayout = isWide || isShort
    val currentAppTheme = Rn2Theme.appTheme

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Rn2ViewerTheme(
            windowSizeClass = windowSizeClass,
            appTheme = currentAppTheme
        ) {
            if (useLandscapeLayout) {
                SetPartialLandscape(
                    input = input,
                    onInputChanged = { input = it },
                    onDismiss = onDismiss,
                    onConfirm = onConfirm
                )
            } else {
                SetPartialPortrait(
                    input = input,
                    onInputChanged = { input = it },
                    onDismiss = onDismiss,
                    onConfirm = onConfirm
                )
            }
        }
    }
}

@Composable
private fun SetPartialPortrait(
    input: String,
    onInputChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val dimensions = Rn2Theme.dimensions
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DialogHeader(title = stringResource(R.string.set_partial_title))
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(dimensions.paddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium)
            ) {
                DisplayArea(input)
                Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                NumpadGrid(
                    input = input,
                    onInputChanged = onInputChanged,
                    numpadHeight = dimensions.numpadButtonHeight
                )
                Spacer(modifier = Modifier.height(dimensions.paddingMedium))
                ActionButtons(
                    onDismiss = onDismiss,
                    onConfirm = {
                        val newValue = input.toDoubleOrNull() ?: 0.0
                        onConfirm(newValue * 1000.0)
                    }
                )
            }
        }
    }
}

@Composable
private fun SetPartialLandscape(
    input: String,
    onInputChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val dimensions = Rn2Theme.dimensions
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.9f),
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(dimensions.cornerRadius)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DialogHeader(title = stringResource(R.string.set_partial_title))
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensions.paddingLarge),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingLarge)
            ) {
                // Display Area (Left)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    DisplayArea(input)
                }

                // Numpad Area (Right)
                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
                ) {
                    NumpadGrid(
                        input = input,
                        onInputChanged = onInputChanged,
                        numpadHeight = dimensions.numpadButtonHeightLandscape
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                    ActionButtons(
                        onDismiss = onDismiss,
                        onConfirm = {
                            val newValue = input.toDoubleOrNull() ?: 0.0
                            onConfirm(newValue * 1000.0)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(Rn2Theme.dimensions.paddingMedium),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DisplayArea(input: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = input.ifEmpty { "0.00" },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}


@Composable
private fun NumpadGrid(
    input: String,
    onInputChanged: (String) -> Unit,
    numpadHeight: androidx.compose.ui.unit.Dp
) {
    val dimensions = Rn2Theme.dimensions
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "DEL")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
            ) {
                row.forEach { char ->
                    NumpadButton(
                        text = char,
                        modifier = Modifier.weight(1f),
                        buttonHeight = numpadHeight,
                        onClick = {
                            var newInput = input
                            when (char) {
                                "DEL" -> {
                                    if (newInput.isNotEmpty()) {
                                        newInput = newInput.dropLast(1)
                                    }
                                }
                                "." -> {
                                    if (!newInput.contains(".")) {
                                        newInput += if (newInput.isEmpty()) "0." else "."
                                    }
                                }
                                else -> {
                                    if (newInput.length < 6) {
                                        newInput += char
                                    }
                                }
                            }
                            onInputChanged(newInput)
                        },
                        containerColor = if (char == "DEL") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (char == "DEL") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        icon = if (char == "DEL") Icons.AutoMirrored.Filled.ArrowBack else null
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val dimensions = Rn2Theme.dimensions
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
    ) {
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .height(dimensions.dialogButtonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(dimensions.cornerRadius)
        ) {
            Icon(
                Icons.Default.Close, 
                contentDescription = stringResource(R.string.action_cancel),
                modifier = Modifier.size(dimensions.actionIconSize)
            )
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .weight(1f)
                .height(dimensions.dialogButtonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(dimensions.cornerRadius)
        ) {
            Icon(
                Icons.Default.Check, 
                contentDescription = stringResource(R.string.action_confirm),
                modifier = Modifier.size(dimensions.actionIconSize)
            )
        }
    }
}

@Composable
fun NumpadButton(
    text: String,
    modifier: Modifier = Modifier,
    buttonHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    icon: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(buttonHeight),
        shape = RoundedCornerShape(Rn2Theme.dimensions.cornerRadius),
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(icon, contentDescription = text, modifier = Modifier.size(Rn2Theme.dimensions.actionIconSize))
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
private fun SetPartialPortraitPreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp))
    Rn2ViewerTheme(windowSizeClass = windowSizeClass) {
        SetPartialPortrait(
            input = "12.34",
            onInputChanged = {},
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,orientation=landscape")
@Composable
private fun SetPartialLandscapePreview() {
    val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(1280.dp, 800.dp))
    Rn2ViewerTheme(windowSizeClass = windowSizeClass) {
        SetPartialLandscape(
            input = "12.34",
            onInputChanged = {},
            onDismiss = {},
            onConfirm = {}
        )
    }
}
