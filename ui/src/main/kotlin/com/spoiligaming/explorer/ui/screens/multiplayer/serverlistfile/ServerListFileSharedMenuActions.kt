/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2026 SpoilerRules
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

package com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.widgets.ActionItem
import com.spoiligaming.explorer.util.OSUtils
import org.jetbrains.compose.resources.StringResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.server_list_file_action_copy_absolute_path
import server_list_explorer.ui.generated.resources.server_list_file_action_copy_file_name
import server_list_explorer.ui.generated.resources.server_list_file_action_copy_folder_path
import server_list_explorer.ui.generated.resources.server_list_file_action_open_file_location
import server_list_explorer.ui.generated.resources.server_list_file_action_set_active_file
import server_list_explorer.ui.generated.resources.server_list_file_action_show_containing_folder

internal data class ServerListFileMenuActionHandlers(
    val onSetActiveFile: () -> Unit,
    val onOpenFileLocation: () -> Unit,
    val onShowContainingFolder: () -> Unit,
    val onCopyAbsolutePath: () -> Unit,
    val onCopyFolderPath: () -> Unit,
    val onCopyServerListFileName: () -> Unit,
)

internal enum class DesktopPlatform {
    Linux,
    MacOs,
    Windows,
}

internal data class ServerListFileMenuEnvironment(
    val platform: DesktopPlatform,
) {
    companion object {
        val Current
            get() =
                ServerListFileMenuEnvironment(
                    platform =
                        when {
                            OSUtils.isLinux -> DesktopPlatform.Linux
                            OSUtils.isMacOS -> DesktopPlatform.MacOs
                            else -> DesktopPlatform.Windows
                        },
                )
    }
}

internal enum class ServerListFileMenuAction(
    val label: StringResource,
    private val supportedPlatforms: Set<DesktopPlatform> = DesktopPlatform.entries.toSet(),
) {
    SetActiveFile(
        label = Res.string.server_list_file_action_set_active_file,
    ),
    OpenFileLocation(
        label = Res.string.server_list_file_action_open_file_location,
        supportedPlatforms = setOf(DesktopPlatform.MacOs, DesktopPlatform.Windows),
    ),
    ShowContainingFolder(
        label = Res.string.server_list_file_action_show_containing_folder,
    ),
    CopyAbsolutePath(
        label = Res.string.server_list_file_action_copy_absolute_path,
    ),
    CopyFolderPath(
        label = Res.string.server_list_file_action_copy_folder_path,
    ),
    CopyServerListFileName(
        label = Res.string.server_list_file_action_copy_file_name,
    ),
    ;

    fun isVisible(environment: ServerListFileMenuEnvironment) = environment.platform in supportedPlatforms

    fun execute(handlers: ServerListFileMenuActionHandlers) =
        when (this) {
            SetActiveFile -> handlers.onSetActiveFile()
            OpenFileLocation -> handlers.onOpenFileLocation()
            ShowContainingFolder -> handlers.onShowContainingFolder()
            CopyAbsolutePath -> handlers.onCopyAbsolutePath()
            CopyFolderPath -> handlers.onCopyFolderPath()
            CopyServerListFileName -> handlers.onCopyServerListFileName()
        }
}

internal object ServerListFileMenuActionCatalog {
    @Composable
    fun toDropdownItems(
        handlers: ServerListFileMenuActionHandlers,
        environment: ServerListFileMenuEnvironment = ServerListFileMenuEnvironment.Current,
    ) = ServerListFileMenuAction.entries
        .filter { action ->
            action.isVisible(environment)
        }.map { action ->
            ActionItem(
                text = t(action.label),
                onClick = { action.execute(handlers) },
            )
        }

    @Composable
    fun toContextItems(
        handlers: ServerListFileMenuActionHandlers,
        environment: ServerListFileMenuEnvironment = ServerListFileMenuEnvironment.Current,
    ) = ServerListFileMenuAction.entries
        .filter { action ->
            action.isVisible(environment)
        }.map { action ->
            ContextMenuItem(
                label = t(action.label),
                onClick = { action.execute(handlers) },
            )
        }
}
