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

package com.spoiligaming.explorer.ui.screens.multiplayer.querymethod

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.screens.multiplayer.querymethod.items.QueryMethodCheckboxItem
import com.spoiligaming.explorer.ui.screens.multiplayer.querymethod.items.QueryMethodTimeoutItem

@Composable
internal fun TooltipScope.QueryMethodConfigurationTooltip(
    title: String,
    items: List<QueryMethodSettingItem>,
    modifier: Modifier = Modifier,
) = RichTooltip(
    title = { Text(text = title) },
    text = {
        Card(
            modifier = modifier.padding(top = TooltipSpacingBetweenTitleAndCard),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(TooltipContentPadding),
                verticalArrangement = Arrangement.spacedBy(ItemSpacing),
            ) {
                items.forEachIndexed { index, item ->
                    when (item) {
                        is QueryMethodCheckboxSpec ->
                            QueryMethodCheckboxItem(
                                spec = item,
                                modifier = Modifier.fillMaxWidth(),
                            )

                        is TimeoutSliderSpec ->
                            QueryMethodTimeoutItem(
                                spec = item,
                                modifier = Modifier.fillMaxWidth(),
                            )
                    }

                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            thickness = DividerThickness,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }
        }
    },
)

private val TooltipSpacingBetweenTitleAndCard = 12.dp
private val TooltipContentPadding = 12.dp
private val ItemSpacing = 8.dp
private val DividerThickness = 0.5.dp
