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

package com.spoiligaming.explorer.ui.screens.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import com.spoiligaming.explorer.settings.manager.multiplayerSettingsManager
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalMultiplayerSettings
import com.spoiligaming.explorer.ui.dialog.ExpressiveDialog
import com.spoiligaming.explorer.ui.dialog.onClick
import com.spoiligaming.explorer.ui.dialog.prominent
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.util.rememberServerListFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.button_browse
import server_list_explorer.ui.generated.resources.button_open_folder
import server_list_explorer.ui.generated.resources.cd_browse
import server_list_explorer.ui.generated.resources.cd_open_folder
import server_list_explorer.ui.generated.resources.cd_server_list_file
import server_list_explorer.ui.generated.resources.dialog_cancel_button
import server_list_explorer.ui.generated.resources.dialog_save_button
import server_list_explorer.ui.generated.resources.dialog_support_text_manage_server_list
import server_list_explorer.ui.generated.resources.dialog_title_manage_server_list
import server_list_explorer.ui.generated.resources.file_picker_title_server_list
import server_list_explorer.ui.generated.resources.text_no_file_selected
import server_list_explorer.ui.generated.resources.text_server_count
import java.awt.Desktop
import java.nio.file.Files
import kotlin.io.path.exists

@Composable
internal fun ServerListFileConfigurationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    val mp = LocalMultiplayerSettings.current

    var pendingPath by remember { mutableStateOf(mp.serverListFile) }
    var serverCount: Int? by remember { mutableStateOf(null) }

    val filePickerLauncher =
        rememberServerListFilePickerLauncher(
            title = t(Res.string.file_picker_title_server_list),
        ) { path ->
            pendingPath = path
        }

    LaunchedEffect(pendingPath) {
        serverCount = null
        pendingPath?.takeIf { it.exists() }?.let { path ->
            runCatching {
                val repo = ServerListRepository(path)
                repo.load()
                repo.all().size
            }.onSuccess { serverCount = it }
                .onFailure { serverCount = null }
        }
    }

    val hasChanges = pendingPath != mp.serverListFile

    val dialogTitleManageServerList = t(Res.string.dialog_title_manage_server_list)
    val dialogSupportTextManageServerList = t(Res.string.dialog_support_text_manage_server_list)
    val dialogSaveButton = t(Res.string.dialog_save_button)
    val dialogCancelButton = t(Res.string.dialog_cancel_button)

    ExpressiveDialog(
        onDismissRequest = onDismissRequest,
    ) {
        title(dialogTitleManageServerList)
        supportText(dialogSupportTextManageServerList)

        accept(
            dialogSaveButton.prominent.onClick(enabled = hasChanges) {
                multiplayerSettingsManager.updateSettings { it.copy(serverListFile = pendingPath) }
                onConfirm()
            },
        )
        cancel(dialogCancelButton.onClick(onDismissRequest))

        body {
            Column(
                verticalArrangement = Arrangement.spacedBy(DialogContentSpacing),
            ) {
                OutlinedCard(
                    colors =
                        CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(CardContentPadding).fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SpacingAfterIcon),
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = t(Res.string.cd_server_list_file),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(IconSize),
                            )
                            SelectionContainer {
                                Text(
                                    text =
                                        pendingPath?.toString()
                                            ?: t(Res.string.text_no_file_selected),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if (serverCount != null) {
                            Text(
                                text = stringResource(Res.string.text_server_count, serverCount!!),
                                style = MaterialTheme.typography.labelLarge,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = SERVER_COUNT_TEXT_ALPHA,
                                    ),
                                modifier =
                                    Modifier.padding(
                                        top = ServerCountTopPadding,
                                        start = ServerCountStartPadding,
                                    ),
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonRowSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = filePickerLauncher::launch,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = t(Res.string.cd_browse),
                            modifier = Modifier.size(ButtonIconSize),
                        )
                        Spacer(Modifier.width(SpacingAfterIcon))
                        Text(t(Res.string.button_browse))
                    }

                    OutlinedButton(
                        onClick = {
                            pendingPath?.let { path ->
                                val toOpen = if (Files.isDirectory(path)) path else path.parent
                                toOpen?.let { Desktop.getDesktop().open(it.toFile()) }
                            }
                        },
                        enabled = pendingPath?.exists() == true,
                        modifier =
                            Modifier.pointerHoverIcon(
                                if (pendingPath?.exists() == true) PointerIcon.Hand else PointerIcon.Default,
                            ),
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = t(Res.string.cd_open_folder),
                            modifier = Modifier.size(ButtonIconSize),
                        )
                        Spacer(Modifier.width(SpacingAfterIcon))
                        Text(t(Res.string.button_open_folder))
                    }
                }
            }
        }
    }
}

private val DialogContentSpacing = 16.dp
private val CardContentPadding = 12.dp
private val IconSize = 20.dp
private val SpacingAfterIcon = 8.dp
private val ServerCountTopPadding = 6.dp
private val ServerCountStartPadding = 28.dp
private val ButtonRowSpacing = 8.dp
private val ButtonIconSize = 18.dp

private const val SERVER_COUNT_TEXT_ALPHA = 0.9f
