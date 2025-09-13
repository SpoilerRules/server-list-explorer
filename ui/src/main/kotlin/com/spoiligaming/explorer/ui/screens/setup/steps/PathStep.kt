/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025 SpoilerRules
 *
 * Server List Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Server List Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Server List Explorer.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.spoiligaming.explorer.ui.screens.setup.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.minecraft.common.IModuleKind
import com.spoiligaming.explorer.minecraft.common.UnifiedModeInitializer
import com.spoiligaming.explorer.settings.manager.multiplayerSettingsManager
import com.spoiligaming.explorer.settings.manager.singleplayerSettingsManager
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.screens.setup.SetupStepContainer
import com.spoiligaming.explorer.ui.screens.setup.SetupUiState
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.util.rememberServerListFilePickerLauncher
import com.spoiligaming.explorer.ui.util.rememberWorldSavesPickerLauncher
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.compose.resources.stringResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.cd_browse
import server_list_explorer.ui.generated.resources.detecting_server_list
import server_list_explorer.ui.generated.resources.detecting_world_saves
import server_list_explorer.ui.generated.resources.detection_wait
import server_list_explorer.ui.generated.resources.error_server_list_missing
import server_list_explorer.ui.generated.resources.error_world_saves_missing
import server_list_explorer.ui.generated.resources.generic_file_placeholder
import server_list_explorer.ui.generated.resources.generic_folder_placeholder
import server_list_explorer.ui.generated.resources.label_server_list
import server_list_explorer.ui.generated.resources.label_world_saves
import server_list_explorer.ui.generated.resources.placeholder_generic
import server_list_explorer.ui.generated.resources.placeholder_server_list
import server_list_explorer.ui.generated.resources.placeholder_world_saves
import server_list_explorer.ui.generated.resources.setup_path_step_missing
import server_list_explorer.ui.generated.resources.setup_path_step_ready
import server_list_explorer.ui.generated.resources.setup_step_title_paths

@Composable
internal fun PathStep(state: SetupUiState) {
    var isDetectingServer by remember { mutableStateOf(true) }
    var isDetectingSaves by remember { mutableStateOf(true) }

    LaunchedEffect(state.serverFilePath) {
        if (state.serverFilePath == null) {
            logger.info { "Attempting automatic server file path detection..." }
            val detected =
                runCatching { UnifiedModeInitializer.autoDetect(IModuleKind.Multiplayer) }
                    .onFailure { logger.warn(it) { "Automatic server file detection threw" } }
                    .getOrNull()

            if (detected != null) {
                logger.info { "Server file path detected automatically: $detected" }
                multiplayerSettingsManager.updateSettings { current ->
                    current.copy(serverListFile = detected)
                }
                state.serverFilePath = detected
            } else {
                logger.warn { "Automatic server file detection failed." }
            }
        } else {
            logger.info { "Server file path already set: ${state.serverFilePath}" }
        }
        isDetectingServer = false
    }

    LaunchedEffect(state.worldSavesPath) {
        if (state.worldSavesPath == null) {
            logger.info { "Attempting automatic world saves detection..." }
            val detected =
                runCatching { UnifiedModeInitializer.autoDetect(IModuleKind.Singleplayer) }
                    .onFailure { logger.warn(it) { "Automatic world saves detection threw" } }
                    .getOrNull()

            if (detected != null) {
                logger.info { "World saves path detected automatically: $detected" }
                singleplayerSettingsManager.updateSettings {
                    it.copy(savesDirectory = detected)
                }
                state.worldSavesPath = detected
            } else {
                logger.warn { "Automatic world saves detection failed." }
            }
        } else {
            logger.info { "World saves path already set: ${state.worldSavesPath}" }
        }
        isDetectingSaves = false
    }

    val directoryPickerLauncher =
        rememberWorldSavesPickerLauncher(
            title = t(Res.string.placeholder_world_saves),
        ) { path ->
            singleplayerSettingsManager.updateSettings {
                it.copy(savesDirectory = path)
            }
            state.worldSavesPath = path
        }

    val filePickerLauncher =
        rememberServerListFilePickerLauncher(
            title = t(Res.string.placeholder_server_list),
        ) { path ->
            multiplayerSettingsManager.updateSettings { current ->
                current.copy(serverListFile = path)
            }
            state.serverFilePath = path
        }

    SetupStepContainer(
        title = t(Res.string.setup_step_title_paths),
        subtitle =
            if (state.worldSavesPath != null && state.serverFilePath != null) {
                t(Res.string.setup_path_step_ready)
            } else {
                t(Res.string.setup_path_step_missing)
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacerDp),
        ) {
            if (isDetectingSaves) {
                AutoDetectionCard(isDirectory = true)
            } else {
                FilePathSelector(
                    label = t(Res.string.label_world_saves),
                    path = state.worldSavesPath?.toString(),
                    isDirectory = true,
                    placeholder = t(Res.string.placeholder_world_saves),
                    onBrowse = { directoryPickerLauncher.launch() },
                    isError = state.worldSavesPath == null,
                    errorMessage = t(Res.string.error_world_saves_missing),
                )
            }

            if (isDetectingServer) {
                AutoDetectionCard(isDirectory = false)
            } else {
                FilePathSelector(
                    label = t(Res.string.label_server_list),
                    path = state.serverFilePath?.toString(),
                    isDirectory = false,
                    placeholder = t(Res.string.placeholder_server_list),
                    onBrowse = { filePickerLauncher.launch() },
                    isError = state.serverFilePath == null,
                    errorMessage = t(Res.string.error_server_list_missing),
                )
            }
        }
    }
}

@Composable
private fun FilePathSelector(
    label: String,
    path: String?,
    isDirectory: Boolean,
    placeholder: String? = null,
    onBrowse: () -> Unit,
    isError: Boolean = false,
    errorMessage: String = "",
    modifier: Modifier = Modifier,
) {
    var isHovered by remember { mutableStateOf(false) }
    val genericPlaceholder =
        stringResource(
            Res.string.placeholder_generic,
            if (isDirectory) {
                t(
                    Res.string.generic_folder_placeholder,
                )
            } else {
                t(Res.string.generic_file_placeholder)
            },
        )

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ColumnSpacerDp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Surface(
            shape = MaterialTheme.shapes.medium,
            border =
                BorderStroke(
                    width = BorderWidth,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                ),
            modifier = Modifier.fillMaxWidth().height(SurfaceHeight),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = RowSpacerDp),
            ) {
                if (path != null) {
                    SelectionContainer {
                        Text(
                            text = path,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = PaddingDpEnd),
                        )
                    }
                } else {
                    Text(
                        text = placeholder ?: genericPlaceholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = PaddingDpEnd),
                    )
                }

                IconButton(
                    onClick = onBrowse,
                    modifier =
                        Modifier
                            .size(IconButtonSize)
                            .onHover { isHovered = it }
                            .pointerHoverIcon(PointerIcon.Hand),
                    colors =
                        IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    val icon =
                        when {
                            isDirectory -> Icons.Filled.FolderOpen
                            isHovered && !isDirectory -> Icons.AutoMirrored.Filled.InsertDriveFile
                            else -> Icons.AutoMirrored.Outlined.InsertDriveFile
                        }
                    Icon(
                        imageVector = icon,
                        contentDescription = t(Res.string.cd_browse),
                        modifier = Modifier.size(IconSize),
                    )
                }
            }
        }

        if (isError) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = PaddingDpStart),
            )
        }
    }
}

@Composable
private fun AutoDetectionCard(
    isDirectory: Boolean,
    modifier: Modifier = Modifier,
) = ElevatedCard(
    colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    modifier = modifier.fillMaxWidth(),
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RowSpacerDp),
        modifier = Modifier.padding(CardPadding),
    ) {
        CircularProgressIndicator(
            strokeWidth = ProgressIndicatorStrokeWidth,
            modifier = Modifier.size(ProgressIndicatorSize),
        )
        Column {
            Text(
                text =
                    if (isDirectory) {
                        t(
                            Res.string.detecting_world_saves,
                        )
                    } else {
                        t(Res.string.detecting_server_list)
                    },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = t(Res.string.detection_wait),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val logger = KotlinLogging.logger {}

private val SpacerDp = 20.dp
private val RowSpacerDp = 16.dp
private val ColumnSpacerDp = 4.dp
private val BorderWidth = 1.dp
private val SurfaceHeight = 56.dp
private val IconButtonSize = 40.dp
private val IconSize = 24.dp
private val PaddingDpStart = 16.dp
private val PaddingDpEnd = 16.dp
private val ProgressIndicatorSize = 24.dp
private val ProgressIndicatorStrokeWidth = 2.dp
private val CardPadding = 16.dp
