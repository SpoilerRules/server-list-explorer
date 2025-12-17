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

package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.extensions.onHover

@Composable
internal fun PocketInfoTooltip(
    text: String,
    modifier: Modifier = Modifier,
    minWidth: Dp = DefaultTooltipMinWidth,
    iconSize: Dp = DefaultIconSize,
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    var isHovered by remember { mutableStateOf(false) }

    LaunchedEffect(isHovered) {
        if (isHovered) {
            tooltipState.show()
        } else {
            tooltipState.dismiss()
        }
    }

    TooltipBox(
        positionProvider = rememberTooltipPositionProvider(DefaultTooltipAnchorPosition),
        state = tooltipState,
        tooltip = {
            PlainTooltip(
                modifier = Modifier.widthIn(min = minWidth),
            ) {
                Text(text)
            }
        },
        modifier =
            modifier
                .onHover { isHovered = it }
                .pointerHoverIcon(PointerIcon.Hand),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(iconSize),
        )
    }
}

private val DefaultTooltipMinWidth = 240.dp
private val DefaultIconSize = 22.dp
private val DefaultTooltipAnchorPosition = TooltipAnchorPosition.Above
