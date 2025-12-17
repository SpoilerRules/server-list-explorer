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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.screens.multiplayer.querymethod.TimeoutSliderSpec
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.widgets.DebouncedSlider
import com.spoiligaming.explorer.ui.widgets.PocketInfoTooltip
import com.spoiligaming.explorer.ui.widgets.SliderValueAdapters
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.query_method_timeout_preview_seconds

@Composable
internal fun QueryMethodTimeoutItem(
    spec: TimeoutSliderSpec,
    modifier: Modifier = Modifier,
) = DebouncedSlider(
    value = spec.valueSeconds,
    onValueChange = spec.onValueChangeSeconds,
    valueRange = spec.valueRangeSeconds,
    adapter = SliderValueAdapters.IntAdapter,
) { sliderPosition, onSliderPositionChange, previewSeconds ->
    Column(
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
            Text(
                text = t(Res.string.query_method_timeout_preview_seconds, previewSeconds),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = sliderPosition,
            onValueChange = onSliderPositionChange,
            valueRange = spec.valueRangeSeconds,
            steps = SLIDER_STEPS,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val TitleSpacing = 4.dp
private val TooltipMinWidth = 296.dp
private const val TITLE_WEIGHT = 1f
private const val SLIDER_STEPS = 0
