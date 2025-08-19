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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ItemSelectableDropdownMenu(
    title: String,
    description: String,
    note: String? = null,
    conflictReason: String? = null,
    selectedOption: DropdownOption,
    options: List<DropdownOption>,
    onOptionSelected: (DropdownOption) -> Unit,
) = SettingTile(
    title = title,
    description = description,
    note = note,
    conflictReason = conflictReason,
    trailingContent = {
        SelectableDropdown(
            selectedOption,
            options,
            onOptionSelected,
            modifier = Modifier.fillMaxWidth(DROPDOWN_WIDTH_RATIO),
        )
    },
)

private const val DROPDOWN_WIDTH_RATIO = 0.25f
