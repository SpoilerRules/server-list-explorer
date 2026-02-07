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

import androidx.compose.runtime.Composable
import com.spoiligaming.explorer.serverlist.bookmarks.ServerListFileBookmarkEntry
import com.spoiligaming.explorer.ui.dialog.SimpleTextEditDialog
import com.spoiligaming.explorer.ui.t
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.server_list_file_edit_label_add_label
import server_list_explorer.ui.generated.resources.server_list_file_edit_label_edit_label
import server_list_explorer.ui.generated.resources.server_list_file_edit_label_field_label
import server_list_explorer.ui.generated.resources.server_list_file_edit_label_placeholder
import server_list_explorer.ui.generated.resources.server_list_file_edit_label_too_long

@Composable
internal fun ServerListFileLabelEditDialog(
    entry: ServerListFileBookmarkEntry,
    onConfirm: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val maxLabelLengthError =
        t(
            Res.string.server_list_file_edit_label_too_long,
            MAX_SERVER_LIST_FILE_LABEL_LENGTH,
        )

    SimpleTextEditDialog(
        title = t(serverListFileEditLabelActionTextResource(entry.label)),
        initialText = entry.label.orEmpty(),
        label = t(Res.string.server_list_file_edit_label_field_label),
        placeholder = t(Res.string.server_list_file_edit_label_placeholder),
        onConfirm = onConfirm,
        onDismissRequest = onDismissRequest,
        textValidator = { input ->
            when {
                input.length > MAX_SERVER_LIST_FILE_LABEL_LENGTH -> maxLabelLengthError
                else -> null
            }
        },
    )
}

internal fun serverListFileEditLabelActionTextResource(currentLabel: String?) =
    if (currentLabel.isNullOrBlank()) {
        Res.string.server_list_file_edit_label_add_label
    } else {
        Res.string.server_list_file_edit_label_edit_label
    }

private const val MAX_SERVER_LIST_FILE_LABEL_LENGTH = 16
