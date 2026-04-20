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

package org.giste.rn2viewer.ui.components.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.giste.rn2viewer.R
import org.giste.rn2viewer.domain.model.settings.AppOrientation
import org.giste.rn2viewer.domain.model.settings.AppSettings
import org.giste.rn2viewer.domain.model.settings.AppTheme
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import org.giste.rn2viewer.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    SettingsScreenContent(
        settings = settings,
        onBackClick = onBackClick,
        onThemeSelected = viewModel::onThemeSelected,
        onOrientationSelected = viewModel::onOrientationSelected,
        onShortDistanceThresholdChanged = viewModel::onShortDistanceThresholdChanged,
        onRestoreRoadbookDefaults = viewModel::restoreRoadbookDefaults
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    settings: AppSettings,
    onBackClick: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onOrientationSelected: (AppOrientation) -> Unit,
    onShortDistanceThresholdChanged: (Double) -> Unit,
    onRestoreRoadbookDefaults: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.settings_tab_user),
        stringResource(R.string.settings_tab_roadbook)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> UserSettingsTab(
                        settings = settings,
                        onThemeSelected = onThemeSelected,
                        onOrientationSelected = onOrientationSelected
                    )

                    1 -> RoadbookSettingsTab(
                        settings = settings,
                        onShortDistanceThresholdChanged = onShortDistanceThresholdChanged,
                        onRestoreDefaults = onRestoreRoadbookDefaults
                    )
                }
            }
        }
    }
}

@Composable
private fun UserSettingsTab(
    settings: AppSettings,
    onThemeSelected: (AppTheme) -> Unit,
    onOrientationSelected: (AppOrientation) -> Unit
) {
    SettingsSection(
        title = stringResource(R.string.settings_theme_title),
        modifier = Modifier.testTag("SettingsSectionTheme"),
        options = listOf(
            AppTheme.LIGHT to stringResource(R.string.settings_theme_light),
            AppTheme.DARK to stringResource(R.string.settings_theme_dark),
            AppTheme.DYNAMIC to stringResource(R.string.settings_theme_dynamic),
            AppTheme.FIA to stringResource(R.string.settings_theme_fia),
            AppTheme.FOLLOW_SYSTEM to stringResource(R.string.settings_theme_system)
        ),
        selectedOption = settings.theme,
        onOptionSelected = onThemeSelected
    )

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

    SettingsSection(
        title = stringResource(R.string.settings_orientation_title),
        modifier = Modifier.testTag("SettingsSectionOrientation"),
        options = listOf(
            AppOrientation.VERTICAL to stringResource(R.string.settings_orientation_vertical),
            AppOrientation.HORIZONTAL to stringResource(R.string.settings_orientation_horizontal),
            AppOrientation.FOLLOW_SYSTEM to stringResource(R.string.settings_orientation_system)
        ),
        selectedOption = settings.orientation,
        onOptionSelected = onOrientationSelected
    )
}

@Composable
private fun RoadbookSettingsTab(
    settings: AppSettings,
    onShortDistanceThresholdChanged: (Double) -> Unit,
    onRestoreDefaults: () -> Unit
) {
    var textValue by remember(settings.shortDistanceThreshold) {
        mutableStateOf(settings.shortDistanceThreshold.toInt().toString())
    }
    val isError = textValue.toIntOrNull()?.let { it !in 0..500 } ?: true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_roadbook_short_distance_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    textValue = newValue
                    newValue.toIntOrNull()?.let {
                        if (it in 0..500) {
                            onShortDistanceThresholdChanged(it.toDouble())
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = isError,
            supportingText = {
                Text(text = stringResource(R.string.settings_roadbook_short_distance_helper))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onRestoreDefaults,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(text = stringResource(R.string.settings_roadbook_restore_defaults))
        }
    }
}

@Composable
fun <T> SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit
) {
    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .semantics(mergeDescendants = true) {}
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(Modifier.selectableGroup()) {
            options.forEach { (option, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (option == selectedOption),
                            onClick = { onOptionSelected(option) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (option == selectedOption),
                        onClick = null // null recommended for accessibility with selectable
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    Rn2ViewerTheme(
        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp)),
        appTheme = AppTheme.FOLLOW_SYSTEM
    ) {
        SettingsScreenContent(
            settings = AppSettings(),
            onBackClick = {},
            onThemeSelected = {},
            onOrientationSelected = {},
            onShortDistanceThresholdChanged = {},
            onRestoreRoadbookDefaults = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenDarkPreview() {
    Rn2ViewerTheme(
        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp)),
        appTheme = AppTheme.FOLLOW_SYSTEM
    ) {
        SettingsScreenContent(
            settings = AppSettings(),
            onBackClick = {},
            onThemeSelected = {},
            onOrientationSelected = {},
            onShortDistanceThresholdChanged = {},
            onRestoreRoadbookDefaults = {}
        )
    }
}
