/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025-2026 SpoilerRules
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

package com.spoiligaming.explorer.ui.screens.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.widgets.AppVerticalScrollbar

@Composable
internal fun SetupStepContainer(
    title: String,
    subtitle: String? = null,
    content: @Composable BoxScope.() -> Unit,
) = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
) {
    val prefs = LocalPrefs.current
    val scrollState = rememberScrollState()
    val scrollbarAdapter = rememberScrollbarAdapter(scrollState)
    val isScrollable = scrollState.canScrollForward || scrollState.canScrollBackward

    Card(
        modifier =
            Modifier
                .widthIn(min = ContainerMinWidth, max = ContainerMaxWidth)
                .wrapContentHeight(),
    ) {
        Column(
            modifier = Modifier.padding(ContainerPadding),
            verticalArrangement = Arrangement.spacedBy(ColumnArrangement),
        ) {
            Column {
                SelectionContainer {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                subtitle?.let {
                    SelectionContainer {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row(
                modifier = if (isScrollable) Modifier.fillMaxWidth() else Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(ContentRowSpacing),
            ) {
                Box(
                    modifier =
                        if (isScrollable) {
                            Modifier
                                .weight(ContentWeight)
                                .verticalScroll(scrollState)
                        } else {
                            Modifier.verticalScroll(scrollState)
                        },
                ) {
                    content()
                }

                if (isScrollable) {
                    AppVerticalScrollbar(
                        adapter = scrollbarAdapter,
                        alwaysVisible = prefs.settingsScrollbarAlwaysVisible,
                    )
                }
            }
        }
    }
}

private val ContainerPadding = 32.dp
private val ColumnArrangement = 16.dp
private val ContentRowSpacing = 4.dp
private val ContainerMinWidth = 400.dp
private val ContainerMaxWidth = 800.dp
private const val ContentWeight = 1f
