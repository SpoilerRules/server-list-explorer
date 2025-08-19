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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spoiligaming.explorer.settings.manager.multiplayerSettingsManager
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalMultiplayerSettings
import com.spoiligaming.explorer.ui.navigation.MultiplayerServerListScreen
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.util.rememberServerListFilePickerLauncher
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.button_browse
import server_list_explorer.ui.generated.resources.cd_browse
import server_list_explorer.ui.generated.resources.cd_server_list_file
import server_list_explorer.ui.generated.resources.error_unable_load_servers
import server_list_explorer.ui.generated.resources.error_unable_load_servers_message
import server_list_explorer.ui.generated.resources.placeholder_server_list
import server_list_explorer.ui.generated.resources.server_list_file_not_set
import kotlin.math.max

@Composable
internal fun MultiplayerErrorScreen(navController: NavController) {
    val mpSettings = LocalMultiplayerSettings.current

    var descriptionMaxWidthPx by remember { mutableStateOf(0) }
    val descriptionWidthDp = with(LocalDensity.current) { descriptionMaxWidthPx.toDp() }

    val filePickerLauncher =
        rememberServerListFilePickerLauncher(
            title = t(Res.string.placeholder_server_list),
        ) { path ->
            multiplayerSettingsManager.updateSettings { current ->
                current.copy(serverListFile = path)
            }
            navController.navigate(MultiplayerServerListScreen) {
                popUpTo(MultiplayerServerListScreen) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(ScreenPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ColumnSpacing),
        ) {
            Text(
                text = t(Res.string.error_unable_load_servers),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = t(Res.string.error_unable_load_servers_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                onTextLayout = { layoutResult ->
                    descriptionMaxWidthPx = max(descriptionMaxWidthPx, layoutResult.size.width)
                },
            )

            Column(
                modifier = Modifier.width(descriptionWidthDp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ButtonColumnSpacing),
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .padding(CardContentPadding)
                                .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CardHorizontalArrangement),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = t(Res.string.cd_server_list_file),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        SelectionContainer {
                            Text(
                                text =
                                    mpSettings.serverListFile?.toString()
                                        ?: t(Res.string.server_list_file_not_set),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { filePickerLauncher.launch() },
                    modifier = Modifier.fillMaxWidth().pointerHoverIcon(PointerIcon.Hand),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = t(Res.string.cd_browse),
                        modifier = Modifier.size(ButtonIconSize),
                    )
                    Spacer(Modifier.width(ButtonSpacing))
                    Text(t(Res.string.button_browse))
                }
            }
        }
    }
}

private val ScreenPadding = 24.dp
private val ColumnSpacing = 16.dp
private val CardContentPadding = 12.dp
private val CardHorizontalArrangement = 8.dp
private val ButtonIconSize = 20.dp
private val ButtonSpacing = 8.dp
private val ButtonColumnSpacing = 8.dp
