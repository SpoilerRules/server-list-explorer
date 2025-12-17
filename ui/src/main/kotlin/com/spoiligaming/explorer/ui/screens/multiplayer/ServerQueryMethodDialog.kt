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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.spoiligaming.explorer.ui.screens.multiplayer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.settings.manager.multiplayerSettingsManager
import com.spoiligaming.explorer.settings.manager.serverQueryMethodConfigurationsManager
import com.spoiligaming.explorer.settings.model.ServerQueryMethod
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalServerQueryMethodConfigurations
import com.spoiligaming.explorer.ui.dialog.ExpressiveDialog
import com.spoiligaming.explorer.ui.dialog.onClick
import com.spoiligaming.explorer.ui.dialog.prominent
import com.spoiligaming.explorer.ui.screens.multiplayer.querymethod.QueryMethodConfigurationUpdater
import com.spoiligaming.explorer.ui.screens.multiplayer.querymethod.QueryMethodOptionCard
import com.spoiligaming.explorer.ui.screens.multiplayer.querymethod.queryMethodDefinitions
import com.spoiligaming.explorer.ui.t
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.dialog_cancel_button
import server_list_explorer.ui.generated.resources.query_method_save_and_refresh_all_entries
import server_list_explorer.ui.generated.resources.query_method_support_text
import server_list_explorer.ui.generated.resources.query_method_title

@Composable
internal fun ServerQueryMethodDialog(
    visible: Boolean,
    currentQueryMethod: ServerQueryMethod,
    onSaveAndRefresh: (ServerQueryMethod) -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (!visible) return

    var configurationTarget by remember { mutableStateOf<ServerQueryMethod?>(null) }
    val configurations = LocalServerQueryMethodConfigurations.current

    val queryMethodTitleText = t(Res.string.query_method_title)
    val queryMethodSupportText = t(Res.string.query_method_support_text)
    val queryMethodSaveAndRefreshAllEntriesText = t(Res.string.query_method_save_and_refresh_all_entries)
    val dialogCancelButtonText = t(Res.string.dialog_cancel_button)

    var pendingQueryMethod by remember(currentQueryMethod) {
        mutableStateOf(currentQueryMethod)
    }

    var pendingConfigurations by remember(configurations) {
        mutableStateOf(configurations)
    }

    val updateConfigurations: QueryMethodConfigurationUpdater = { update ->
        pendingConfigurations = update(pendingConfigurations)
    }

    ExpressiveDialog(
        onDismissRequest = onDismissRequest,
    ) {
        title(queryMethodTitleText)
        supportText(queryMethodSupportText)
        body {
            Column(verticalArrangement = Arrangement.spacedBy(DialogBodyVerticalSpacing)) {
                val definitions =
                    queryMethodDefinitions(
                        configurations = pendingConfigurations,
                        updateConfigurations = updateConfigurations,
                    )

                definitions.forEach { definition ->
                    QueryMethodOptionCard(
                        definition = definition,
                        selected = pendingQueryMethod == definition.method,
                        onSelect = { pendingQueryMethod = definition.method },
                        onConfigure = {
                            configurationTarget = definition.method
                        },
                        configurationVisible = configurationTarget == definition.method,
                        onConfigurationDismiss = { configurationTarget = null },
                    )
                }
            }
        }
        accept(
            queryMethodSaveAndRefreshAllEntriesText.prominent onClick {
                multiplayerSettingsManager.updateSettings {
                    it.copy(serverQueryMethod = pendingQueryMethod)
                }
                serverQueryMethodConfigurationsManager.saveSettings(pendingConfigurations)
                onSaveAndRefresh(pendingQueryMethod)
                onDismissRequest()
            },
        )
        cancel(
            dialogCancelButtonText onClick onDismissRequest,
        )
        modifier = Modifier.width(IntrinsicSize.Max)
    }
}

private val DialogBodyVerticalSpacing = 8.dp
