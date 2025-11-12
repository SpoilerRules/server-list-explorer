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

package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingTile(
    title: String,
    description: String,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
) = SettingTile(
    title = title,
    description = description,
    note = null,
    conflictReason = null,
    trailingContent = trailingContent,
    modifier = modifier,
    colors = colors,
)

@Composable
internal fun SettingTile(
    title: String,
    description: String? = null,
    note: String? = null,
    conflictReason: String? = null,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
) = ListItem(
    headlineContent = {
        Column(verticalArrangement = Arrangement.spacedBy(TitleContentSpacing)) {
            conflictReason?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(ConflictReasonSpacing)) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(WarningIconSize),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    SelectionContainer {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            SelectionContainer {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
            }
        }
    },
    supportingContent = {
        Column(verticalArrangement = Arrangement.spacedBy(DescriptionNoteSpacing)) {
            description?.let {
                SelectionContainer {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            note?.let {
                SelectionContainer {
                    Text(
                        text = "Note: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = NOTE_ALPHA),
                    )
                }
            }
        }
    },
    trailingContent = trailingContent,
    modifier = modifier.fillMaxWidth(),
    colors = colors,
)

@Composable
internal fun FlexibleSettingTile(
    title: String,
    description: String,
    supportingContent: @Composable () -> Unit = { Text(text = description) },
    trailingContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
) = if (trailingContent == null) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = supportingContent,
        modifier = modifier.fillMaxWidth(),
        colors = colors,
    )
} else {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = supportingContent,
        trailingContent = trailingContent,
        modifier = modifier.fillMaxWidth(),
        colors = colors,
    )
}

private val TitleContentSpacing = 16.dp
private val ConflictReasonSpacing = 4.dp
private val WarningIconSize = 20.dp
private val DescriptionNoteSpacing = 4.dp
private const val NOTE_ALPHA = 0.7f
