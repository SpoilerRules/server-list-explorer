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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.minecraft.common.IModuleKind
import com.spoiligaming.explorer.minecraft.common.UnifiedModeInitializer
import com.spoiligaming.explorer.serverlist.bookmarks.ServerListFileBookmarksManager
import com.spoiligaming.explorer.settings.manager.singleplayerSettingsManager
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.screens.setup.SetupStepContainer
import com.spoiligaming.explorer.ui.screens.setup.SetupUiState
import com.spoiligaming.explorer.ui.snackbar.SnackbarController
import com.spoiligaming.explorer.ui.snackbar.SnackbarEvent
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.util.rememberServerListFilePickerLauncher
import com.spoiligaming.explorer.ui.util.rememberWorldSavesPickerLauncher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.cd_browse
import server_list_explorer.ui.generated.resources.cd_server_list_path_locked_notice
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
import server_list_explorer.ui.generated.resources.setup_server_list_path_locked_message
import server_list_explorer.ui.generated.resources.setup_server_list_path_locked_title
import server_list_explorer.ui.generated.resources.setup_server_list_path_save_detected_failed
import server_list_explorer.ui.generated.resources.setup_server_list_path_save_selected_failed
import server_list_explorer.ui.generated.resources.setup_step_title_paths

@Composable
internal fun PathStep(state: SetupUiState) {
    var isDetectingServer by remember { mutableStateOf(true) }
    var isDetectingSaves by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val bookmarkEntries by ServerListFileBookmarksManager.entries.collectAsState()
    val isServerListFilePathSelectionLocked = bookmarkEntries.isNotEmpty()

    // detected
    val saveDetectedServerListFailedMessage = t(Res.string.setup_server_list_path_save_detected_failed)
    // selected
    val saveSelectedServerListFailedMessage = t(Res.string.setup_server_list_path_save_selected_failed)

    LaunchedEffect(state.serverFilePath) {
        if (state.serverFilePath == null) {
            logger.info { "Attempting automatic server file path detection..." }
            val detected =
                runCatching { UnifiedModeInitializer.autoDetect(IModuleKind.Multiplayer) }
                    .onFailure { logger.warn(it) { "Automatic server file detection threw" } }
                    .getOrNull()

            if (detected != null) {
                logger.info { "Server file path detected automatically: $detected" }
                runCatching { ServerListFileBookmarksManager.setActivePath(detected) }
                    .onFailure { e ->
                        logger.error(e) { "Failed to store detected server list file path" }
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = saveDetectedServerListFailedMessage,
                                duration = SnackbarDuration.Short,
                            ),
                        )
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
            scope.launch {
                runCatching { ServerListFileBookmarksManager.setActivePath(path) }
                    .onFailure { e ->
                        logger.error(e) { "Failed to save selected server list file path" }
                        SnackbarController.sendEvent(
                            SnackbarEvent(
                                message = saveSelectedServerListFailedMessage,
                                duration = SnackbarDuration.Short,
                            ),
                        )
                    }.onSuccess {
                        state.serverFilePath = path
                    }
            }
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
                Column(verticalArrangement = Arrangement.spacedBy(ServerListPathSectionSpacing)) {
                    FilePathSelector(
                        label = t(Res.string.label_server_list),
                        path = state.serverFilePath?.toString(),
                        isDirectory = false,
                        placeholder = t(Res.string.placeholder_server_list),
                        onBrowse = { filePickerLauncher.launch() },
                        isError = !isServerListFilePathSelectionLocked && state.serverFilePath == null,
                        errorMessage = t(Res.string.error_server_list_missing),
                        allowBrowse = !isServerListFilePathSelectionLocked,
                    )

                    if (isServerListFilePathSelectionLocked) {
                        SetupServerListPathLockedNotice()
                    }
                }
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
    allowBrowse: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var isHovered by remember { mutableStateOf(false) }
    val isReadOnly = !allowBrowse
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
    val fieldBackgroundColor =
        if (isReadOnly) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface
    val fieldTextColor =
        if (isReadOnly) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fieldBorderColor =
        when {
            isError -> MaterialTheme.colorScheme.error
            isReadOnly -> MaterialTheme.colorScheme.outlineVariant
            else -> MaterialTheme.colorScheme.outline
        }

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
                    color = fieldBorderColor,
                ),
            modifier = Modifier.fillMaxWidth().height(SurfaceHeight),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier =
                    Modifier
                        .background(fieldBackgroundColor)
                        .padding(horizontal = RowSpacerDp),
            ) {
                if (path != null) {
                    SelectionContainer {
                        Text(
                            text = path,
                            style = MaterialTheme.typography.bodyLarge,
                            color = fieldTextColor,
                            maxLines = PATH_TEXT_MAX_LINES,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(PATH_TEXT_WEIGHT).padding(end = PaddingDpEnd),
                        )
                    }
                } else {
                    Text(
                        text = placeholder ?: genericPlaceholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = placeholderColor,
                        maxLines = PATH_TEXT_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(PATH_TEXT_WEIGHT).padding(end = PaddingDpEnd),
                    )
                }

                if (allowBrowse) {
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
private fun SetupServerListPathLockedNotice(modifier: Modifier = Modifier) =
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = LockedNoticeElevation,
    ) {
        Row(
            modifier = Modifier.padding(LockedNoticePadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LockedNoticeIconSpacing),
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = t(Res.string.cd_server_list_path_locked_notice),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(LockedNoticeIconSize),
            )
            Column(verticalArrangement = Arrangement.spacedBy(LockedNoticeTextSpacing)) {
                Text(
                    text = t(Res.string.setup_server_list_path_locked_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = t(Res.string.setup_server_list_path_locked_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private val ServerListPathSectionSpacing = 12.dp
private val LockedNoticePadding = 12.dp
private val LockedNoticeIconSize = 24.dp
private val LockedNoticeIconSpacing = 10.dp
private val LockedNoticeElevation = 2.dp
private val LockedNoticeTextSpacing = 4.dp
private const val PATH_TEXT_MAX_LINES = 1
private const val PATH_TEXT_WEIGHT = 1f
