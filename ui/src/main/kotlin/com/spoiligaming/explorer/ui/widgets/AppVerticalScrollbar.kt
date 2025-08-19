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

import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun AppVerticalScrollbar(adapter: ScrollbarAdapter) =
    Box(
        modifier =
            Modifier
                .width(ScrollbarThickness)
                .background(
                    color =
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = BACKGROUND_ALPHA,
                        ),
                    shape = MaterialTheme.shapes.small,
                ),
        contentAlignment = Alignment.Center,
    ) {
        VerticalScrollbar(
            modifier = Modifier.fillMaxHeight(),
            adapter = adapter,
            style =
                ScrollbarStyle(
                    minimalHeight = MinimalScrollbarHeight,
                    thickness = ScrollbarThickness,
                    shape = MaterialTheme.shapes.small,
                    hoverDurationMillis = ScrollbarHoverDurationMillis,
                    unhoverColor =
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = UnhoverColorAlpha),
                    hoverColor = MaterialTheme.colorScheme.primary,
                ),
        )
    }

private val ScrollbarThickness = 8.dp
private const val BACKGROUND_ALPHA = 0.2f
private const val UnhoverColorAlpha = 0.4f
private val MinimalScrollbarHeight = 16.dp
private const val ScrollbarHoverDurationMillis = 250
