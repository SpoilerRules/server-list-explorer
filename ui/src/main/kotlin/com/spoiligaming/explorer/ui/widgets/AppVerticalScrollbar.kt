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

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.extensions.onHover
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
internal fun AppVerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    alwaysVisible: Boolean = true,
) {
    var isHovered by remember { mutableStateOf(false) }
    var isScrolling by remember { mutableStateOf(false) }
    var lastScrollOffset by remember { mutableStateOf(adapter.scrollOffset) }

    LaunchedEffect(adapter.scrollOffset) {
        val currentOffset = adapter.scrollOffset
        val scrollDelta = (currentOffset - lastScrollOffset).absoluteValue

        if (scrollDelta > SCROLL_DELTA_THRESHOLD) {
            isScrolling = true
            lastScrollOffset = currentOffset

            delay(SCROLLBAR_VISIBLE_AFTER_SCROLL_MILLIS)
            isScrolling = false
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (alwaysVisible || isHovered || isScrolling) FULL_OPACITY else HIDDEN_OPACITY,
        animationSpec =
            tween(
                durationMillis = SCROLLBAR_FADE_DURATION_MILLIS,
                easing = LinearOutSlowInEasing,
            ),
        label = "ScrollBarVisibility",
    )

    Box(
        modifier =
            modifier
                .width(ScrollbarThickness)
                .onHover { isHovered = it }
                .graphicsLayer { this.alpha = alpha }
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
                    hoverDurationMillis = SCROLLBAR_HOVER_DURATION_MILLIS,
                    unhoverColor =
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = UNHOVER_COLOR_ALPHA),
                    hoverColor = MaterialTheme.colorScheme.primary,
                ),
        )
    }
}

private val ScrollbarThickness = 8.dp
private val MinimalScrollbarHeight = 16.dp

private const val BACKGROUND_ALPHA = 0.2f
private const val UNHOVER_COLOR_ALPHA = 0.4f
private const val FULL_OPACITY = 1f
private const val HIDDEN_OPACITY = 0f

private const val SCROLLBAR_HOVER_DURATION_MILLIS = 250
private const val SCROLLBAR_FADE_DURATION_MILLIS = 300
private const val SCROLLBAR_VISIBLE_AFTER_SCROLL_MILLIS = 1100L

private const val SCROLL_DELTA_THRESHOLD = 0.5
