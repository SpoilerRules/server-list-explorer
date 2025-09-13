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

@file:OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)

package com.spoiligaming.explorer.ui.screens.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.spoiligaming.explorer.multiplayer.AcceptTexturesState
import com.spoiligaming.explorer.multiplayer.HiddenState
import com.spoiligaming.explorer.multiplayer.MultiplayerServer
import com.spoiligaming.explorer.ui.dialog.ExpressiveDialog
import com.spoiligaming.explorer.ui.dialog.onClick
import com.spoiligaming.explorer.ui.dialog.prominent
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.util.rememberAdaptiveWidth
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.add_server_button_add
import server_list_explorer.ui.generated.resources.add_server_hide_label
import server_list_explorer.ui.generated.resources.add_server_hide_note
import server_list_explorer.ui.generated.resources.add_server_hide_tooltip
import server_list_explorer.ui.generated.resources.add_server_label_address
import server_list_explorer.ui.generated.resources.add_server_label_name
import server_list_explorer.ui.generated.resources.add_server_placeholder_name
import server_list_explorer.ui.generated.resources.add_server_resource_packs
import server_list_explorer.ui.generated.resources.add_server_title
import server_list_explorer.ui.generated.resources.cd_server_list_file
import server_list_explorer.ui.generated.resources.dialog_cancel_button
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
internal fun AddServerDialog(
    onDismissRequest: () -> Unit,
    onAddConfirmed: (MultiplayerServer) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var acceptTextures by remember { mutableStateOf(AcceptTexturesState.Prompt) }
    var hidden by remember { mutableStateOf(false) }

    val placeholderServer =
        remember {
            MultiplayerServer(name = "", ip = "", iconBase64 = null)
        }

    val width = rememberAdaptiveWidth(min = DialogMinWidth, max = DialogMaxWidth)

    val titleText = t(Res.string.add_server_title)
    val addButtonText = t(Res.string.add_server_button_add)
    val cancelButtonText = t(Res.string.dialog_cancel_button)

    ExpressiveDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismissRequest,
    ) {
        title(titleText)
        body {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingLarge),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(t(Res.string.add_server_label_name)) },
                    placeholder = { Text(t(Res.string.add_server_placeholder_name)) },
                    modifier = Modifier.width(width),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    singleLine = true,
                    label = { Text(t(Res.string.add_server_label_address)) },
                    placeholder = { Text("play.${t(Res.string.add_server_placeholder_name)}.com") },
                    modifier = Modifier.width(width),
                )

                Column(verticalArrangement = Arrangement.spacedBy(SpacingSmall)) {
                    Text(
                        text = t(Res.string.add_server_resource_packs),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    SingleChoiceSegmentedButtonRow {
                        AcceptTexturesState.entries.forEach { stateOpt ->
                            SegmentedButton(
                                selected = acceptTextures == stateOpt,
                                onClick = { acceptTextures = stateOpt },
                                shape =
                                    SegmentedButtonDefaults.itemShape(
                                        index = stateOpt.ordinal,
                                        count = AcceptTexturesState.entries.size,
                                    ),
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                                colors =
                                    SegmentedButtonDefaults
                                        .colors()
                                        .copy(inactiveContainerColor = Color.Transparent),
                            ) {
                                Text(stateOpt.displayName)
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingMedium),
                ) {
                    Switch(
                        checked = hidden,
                        onCheckedChange = { hidden = it },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                    )
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SpacingMedium),
                        ) {
                            Text(
                                text = t(Res.string.add_server_hide_label),
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                tooltip = {
                                    PlainTooltip(
                                        modifier = Modifier.widthIn(min = TooltipMinWidth),
                                    ) {
                                        Text(t(Res.string.add_server_hide_tooltip))
                                    }
                                },
                                state = rememberTooltipState(isPersistent = true),
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = t(Res.string.cd_server_list_file),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(IconSize),
                                )
                            }
                        }
                        Text(
                            text = t(Res.string.add_server_hide_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        accept(
            addButtonText.prominent onClick {
                val newServer =
                    placeholderServer.copy(
                        id = Uuid.random(),
                        name = name.trim(),
                        ip = address.trim(),
                        hidden = if (hidden) HiddenState.Hidden else HiddenState.NotHidden,
                        acceptTextures = acceptTextures,
                    )
                onAddConfirmed(newServer)
                onDismissRequest()
            },
        )
        cancel(cancelButtonText onClick onDismissRequest)
        modifier = Modifier.width(IntrinsicSize.Max)
    }
}

private val DialogMinWidth = 360.dp
private val DialogMaxWidth = 720.dp
private val SpacingLarge = 16.dp
private val SpacingMedium = 8.dp
private val SpacingSmall = 4.dp
private val TooltipMinWidth = 360.dp
private val IconSize = 22.dp
