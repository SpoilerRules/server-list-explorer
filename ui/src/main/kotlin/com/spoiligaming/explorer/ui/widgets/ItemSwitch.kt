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

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

@Composable
internal fun ItemSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) = Switch(
    modifier =
        Modifier.pointerHoverIcon(
            icon = if (enabled) PointerIcon.Hand else PointerIcon.Default,
        ),
    checked = isChecked,
    onCheckedChange = onCheckedChange,
    enabled = enabled,
)

@Composable
internal fun ItemSwitch(
    title: String,
    description: String,
    note: String? = null,
    conflictReason: String? = null,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) = SettingTile(
    title = title,
    description = description,
    note = note,
    conflictReason = conflictReason,
    trailingContent = {
        ItemSwitch(
            isChecked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = conflictReason == null,
        )
    },
)
