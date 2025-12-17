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

package com.spoiligaming.explorer.ui.screens.multiplayer.querymethod.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.screens.multiplayer.querymethod.QueryMethodCheckboxSpec
import com.spoiligaming.explorer.ui.widgets.PocketInfoTooltip

@Composable
internal fun QueryMethodCheckboxItem(
    spec: QueryMethodCheckboxSpec,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(TITLE_WEIGHT),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TitleSpacing),
        ) {
            Text(
                text = spec.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!spec.description.isNullOrBlank()) {
                PocketInfoTooltip(
                    text = spec.description,
                    minWidth = TooltipMinWidth,
                )
            }
        }
        Checkbox(
            checked = spec.checked,
            onCheckedChange = spec.onCheckedChange,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        )
    }
}

private val TitleSpacing = 4.dp
private val TooltipMinWidth = 296.dp
private const val TITLE_WEIGHT = 1f
