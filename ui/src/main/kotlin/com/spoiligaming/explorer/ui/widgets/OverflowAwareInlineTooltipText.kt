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

@file:OptIn(ExperimentalFoundationApi::class)

package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun OverflowAwareInlineTooltipText(
    content: AnnotatedString,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    tooltipTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    tooltipMaxLines: Int = TOOLTIP_DEFAULT_MAX_LINES,
) {
    var isOverflowing by remember { mutableStateOf(false) }
    var canShowTooltip by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isOverflowing) {
        canShowTooltip = false
        if (isOverflowing) {
            delay(TOOLTIP_SHOW_DELAY_MS)
            if (isOverflowing && !isPressed) {
                canShowTooltip = true
            }
        }
    }

    val labelContent = @Composable {
        SelectionContainer {
            Text(
                text = content,
                style = textStyle,
                maxLines = LABEL_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { layout ->
                    isOverflowing = layout.hasVisualOverflow
                },
                modifier =
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                        )
                    },
            )
        }
    }

    if (isOverflowing && canShowTooltip) {
        TooltipArea(
            modifier =
                modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                                if (isOverflowing) {
                                    canShowTooltip = false
                                    scope.launch {
                                        delay(TOOLTIP_PRESS_RESET_DELAY_MS)
                                        if (isOverflowing && !isPressed) canShowTooltip = true
                                    }
                                }
                            },
                        )
                    },
            tooltip = {
                Surface(
                    modifier = Modifier.heightIn(min = TooltipMinHeight),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.inverseSurface,
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = TooltipHorizontalPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = content,
                            style = tooltipTextStyle,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            textAlign = TextAlign.Center,
                            maxLines = tooltipMaxLines,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
        ) {
            labelContent()
        }
    } else {
        labelContent()
    }
}

private const val TOOLTIP_SHOW_DELAY_MS = 2000L
private const val TOOLTIP_PRESS_RESET_DELAY_MS = 2000L
private const val LABEL_MAX_LINES = 1
private const val TOOLTIP_DEFAULT_MAX_LINES = 1
private val TooltipMinHeight = 24.dp
private val TooltipHorizontalPadding = 8.dp
