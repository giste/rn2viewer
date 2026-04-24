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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.giste.rn2viewer.domain.model.MapFile
import org.giste.rn2viewer.domain.model.RemoteMapInfo
import org.giste.rn2viewer.domain.model.settings.AppOrientation
import org.giste.rn2viewer.domain.model.settings.AppSettings
import org.giste.rn2viewer.domain.model.settings.AppTheme
import org.giste.rn2viewer.ui.theme.Rn2ViewerTheme
import org.giste.rn2viewer.ui.viewmodel.MapsUiState
import org.giste.rn2viewer.ui.viewmodel.MapsViewModel
import org.giste.rn2viewer.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    mapsViewModel: MapsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val mapsUiState by mapsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mapsUiState.errorMessage) {
        mapsUiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            mapsViewModel.clearError()
        }
    }

    SettingsScreenContent(
        settings = settings,
        mapsUiState = mapsUiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onThemeSelected = viewModel::onThemeSelected,
        onOrientationSelected = viewModel::onOrientationSelected,
        onShortDistanceThresholdChanged = viewModel::onShortDistanceThresholdChanged,
        onRestoreRoadbookDefaults = viewModel::restoreRoadbookDefaults,
        onOdometerSpeedThresholdChanged = viewModel::onOdometerSpeedThresholdChanged,
        onOdometerMinAccuracyChanged = viewModel::onOdometerMinAccuracyChanged,
        onOdometerMinVerticalAccuracyChanged = viewModel::onOdometerMinVerticalAccuracyChanged,
        onRestoreOdometerDefaults = viewModel::restoreOdometerDefaults,
        onDownloadMap = mapsViewModel::downloadMap,
        onDeleteMap = mapsViewModel::deleteMap
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    settings: AppSettings,
    mapsUiState: MapsUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
    onOrientationSelected: (AppOrientation) -> Unit,
    onShortDistanceThresholdChanged: (Double) -> Unit,
    onRestoreRoadbookDefaults: () -> Unit,
    onOdometerSpeedThresholdChanged: (Float) -> Unit,
    onOdometerMinAccuracyChanged: (Float) -> Unit,
    onOdometerMinVerticalAccuracyChanged: (Float) -> Unit,
    onRestoreOdometerDefaults: () -> Unit,
    onDownloadMap: (RemoteMapInfo) -> Unit,
    onDeleteMap: (MapFile) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.settings_tab_user),
        stringResource(R.string.settings_tab_roadbook),
        stringResource(R.string.settings_tab_advanced),
        "Mapas"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            Box(
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

                    2 -> AdvancedSettingsTab(
                        settings = settings,
                        onOdometerSpeedThresholdChanged = onOdometerSpeedThresholdChanged,
                        onOdometerMinAccuracyChanged = onOdometerMinAccuracyChanged,
                        onOdometerMinVerticalAccuracyChanged = onOdometerMinVerticalAccuracyChanged,
                        onRestoreDefaults = onRestoreOdometerDefaults
                    )
                    
                    3 -> MapsSettingsTab(
                        uiState = mapsUiState,
                        onDownloadMap = onDownloadMap,
                        onDeleteMap = onDeleteMap
                    )
                }
            }
        }
    }
}

@Composable
private fun MapsSettingsTab(
    uiState: MapsUiState,
    onDownloadMap: (RemoteMapInfo) -> Unit,
    onDeleteMap: (MapFile) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Mapas descargados",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState.downloadedMaps.isEmpty()) {
            item {
                Text(
                    text = "No hay mapas descargados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(uiState.downloadedMaps) { mapFile ->
                DownloadedMapItem(
                    mapFile = mapFile,
                    onDeleteClick = { onDeleteMap(mapFile) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Mapas disponibles",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        uiState.availableCategories.forEach { category ->
            item {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(category.maps) { mapInfo ->
                val isDownloaded = uiState.downloadedMaps.any { it.name == mapInfo.relativeUrl.substringAfterLast("/") }
                val progress = uiState.downloadingMaps[mapInfo.id]
                
                AvailableMapItem(
                    mapInfo = mapInfo,
                    isDownloaded = isDownloaded,
                    downloadProgress = progress,
                    onDownloadClick = { onDownloadMap(mapInfo) }
                )
            }
        }
    }
}

@Composable
private fun DownloadedMapItem(
    mapFile: MapFile,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = mapFile.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${mapFile.size / 1024 / 1024} MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AvailableMapItem(
    mapInfo: RemoteMapInfo,
    isDownloaded: Boolean,
    downloadProgress: Float?,
    onDownloadClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = mapInfo.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${mapInfo.size / 1024 / 1024} MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        when {
            downloadProgress != null -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                    CircularProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            isDownloaded -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Downloaded",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            else -> {
                IconButton(onClick = onDownloadClick) {
                    Icon(Icons.Default.Download, contentDescription = "Download")
                }
            }
        }
    }
}

@Composable
private fun AdvancedSettingsTab(
    settings: AppSettings,
    onOdometerSpeedThresholdChanged: (Float) -> Unit,
    onOdometerMinAccuracyChanged: (Float) -> Unit,
    onOdometerMinVerticalAccuracyChanged: (Float) -> Unit,
    onRestoreDefaults: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_advanced_warning),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        AdvancedTextField(
            title = stringResource(R.string.settings_advanced_speed_threshold_title),
            value = settings.odometerSpeedThreshold,
            helperText = stringResource(R.string.settings_advanced_speed_threshold_helper),
            minValue = 0.0f,
            maxValue = 5.0f,
            onValueChange = onOdometerSpeedThresholdChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        AdvancedTextField(
            title = stringResource(R.string.settings_advanced_min_accuracy_title),
            value = settings.odometerMinAccuracy,
            helperText = stringResource(R.string.settings_advanced_min_accuracy_helper),
            minValue = 1.0f,
            maxValue = 100.0f,
            isInteger = true,
            onValueChange = onOdometerMinAccuracyChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        AdvancedTextField(
            title = stringResource(R.string.settings_advanced_min_vertical_accuracy_title),
            value = settings.odometerMinVerticalAccuracy,
            helperText = stringResource(R.string.settings_advanced_min_vertical_accuracy_helper),
            minValue = 1.0f,
            maxValue = 100.0f,
            isInteger = true,
            onValueChange = onOdometerMinVerticalAccuracyChanged
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onRestoreDefaults,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(text = stringResource(R.string.settings_advanced_restore_defaults))
        }
    }
}

@Composable
private fun AdvancedTextField(
    title: String,
    value: Float,
    helperText: String,
    minValue: Float,
    maxValue: Float,
    isInteger: Boolean = false,
    onValueChange: (Float) -> Unit
) {
    var textValue by remember(value) {
        val displayValue = if (isInteger) value.toInt().toString() else value.toString()
        mutableStateOf(displayValue)
    }

    val parsedValue = textValue.toFloatOrNull()
    val isValid = parsedValue != null && parsedValue in minValue..maxValue
    val isError = textValue.isNotEmpty() && !isValid

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() || (!isInteger && it == '.') }) {
                    textValue = newValue
                    newValue.toFloatOrNull()?.let {
                        if (it in minValue..maxValue) {
                            onValueChange(it)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isInteger) KeyboardType.Number else KeyboardType.Decimal
            ),
            singleLine = true,
            isError = isError,
            supportingText = {
                if (isError) {
                    val minStr = if (isInteger) minValue.toInt().toString() else minValue.toString()
                    val maxStr = if (isInteger) maxValue.toInt().toString() else maxValue.toString()
                    Text(text = stringResource(R.string.settings_advanced_range_error, minStr, maxStr))
                } else {
                    Text(text = helperText)
                }
            }
        )
    }
}

@Composable
private fun UserSettingsTab(
    settings: AppSettings,
    onThemeSelected: (AppTheme) -> Unit,
    onOrientationSelected: (AppOrientation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
    ) {
        SettingsScreenContent(
            settings = AppSettings(),
            mapsUiState = MapsUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
            onThemeSelected = {},
            onOrientationSelected = {},
            onShortDistanceThresholdChanged = {},
            onRestoreRoadbookDefaults = {},
            onOdometerSpeedThresholdChanged = {},
            onOdometerMinAccuracyChanged = {},
            onOdometerMinVerticalAccuracyChanged = {},
            onRestoreOdometerDefaults = {},
            onDownloadMap = {},
            onDeleteMap = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenDarkPreview() {
    Rn2ViewerTheme(
        windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp)),
    ) {
        SettingsScreenContent(
            settings = AppSettings(),
            mapsUiState = MapsUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
            onThemeSelected = {},
            onOrientationSelected = {},
            onShortDistanceThresholdChanged = {},
            onRestoreRoadbookDefaults = {},
            onOdometerSpeedThresholdChanged = {},
            onOdometerMinAccuracyChanged = {},
            onOdometerMinVerticalAccuracyChanged = {},
            onRestoreOdometerDefaults = {},
            onDownloadMap = {},
            onDeleteMap = {}
        )
    }
}
