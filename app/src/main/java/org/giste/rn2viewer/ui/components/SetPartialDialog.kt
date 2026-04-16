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
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.giste.rn2viewer.ui.theme.Rn2Theme

@Composable
fun SetPartialDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var input by remember { mutableStateOf("") }
    val dimensions = Rn2Theme.dimensions
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .then(
                    if (isLandscape) {
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.6f)
                    } else {
                        Modifier.fillMaxSize()
                    }
                ),
            color = MaterialTheme.colorScheme.background,
            shape = if (isLandscape) RoundedCornerShape(dimensions.cornerRadius) else RoundedCornerShape(0.dp)
        ) {
            val scrollState = rememberScrollState()
            val numpadHeight = if (isLandscape) dimensions.numpadButtonHeightLandscape else dimensions.numpadButtonHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(dimensions.paddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
            ) {
                // Display Area
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SET PARTIAL DISTANCE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                    Text(
                        text = input.ifEmpty { "0.00" },
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                // Numpad Area
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf(".", "0", "DEL")
                    )

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
                                        when (char) {
                                            "DEL" -> {
                                                if (input.isNotEmpty()) {
                                                    input = input.dropLast(1)
                                                }
                                            }
                                            "." -> {
                                                if (!input.contains(".")) {
                                                    input += if (input.isEmpty()) "0." else "."
                                                }
                                            }
                                            else -> {
                                                if (input.length < 6) {
                                                    input += char
                                                }
                                            }
                                        }
                                    },
                                    containerColor = if (char == "DEL") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (char == "DEL") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    icon = if (char == "DEL") Icons.AutoMirrored.Filled.ArrowBack else null
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                    // Action Buttons
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
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                            Spacer(modifier = Modifier.width(dimensions.paddingSmall))
                            Text("CANCEL", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val newValue = input.toDoubleOrNull() ?: 0.0
                                onConfirm(newValue * 1000.0)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(dimensions.dialogButtonHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(dimensions.cornerRadius)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Confirm")
                            Spacer(modifier = Modifier.width(dimensions.paddingSmall))
                            Text("CONFIRM", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
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
