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

@file:OptIn(FlowPreview::class)

package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlin.math.roundToInt

@Composable
internal inline fun <reified T : Number> ItemValueSlider(
    title: String,
    description: String,
    note: String? = null,
    modifier: Modifier = Modifier,
    value: T,
    valueRange: ClosedFloatingPointRange<Float>,
    crossinline onValueChange: (T) -> Unit,
) {
    val isInt = T::class == Int::class
    val isLong = T::class == Long::class

    val clamped = value.toFloat().coerceIn(valueRange)
    val autoSteps =
        if (isInt || isLong) {
            (valueRange.endInclusive - valueRange.start).roundToInt() - STEP_OFFSET
        } else {
            0
        }

    var localValue by remember { mutableStateOf(clamped) }

    LaunchedEffect(value) {
        localValue = value.toFloat().coerceIn(valueRange)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { localValue }
            .drop(1)
            .debounce(SLIDER_DEBOUNCE_MS)
            .collectLatest { raw ->
                val coerced = raw.coerceIn(valueRange)
                val final: T =
                    when {
                        isInt -> coerced.roundToInt() as T
                        isLong -> coerced.roundToInt().toLong() as T
                        else -> coerced as T
                    }
                if (final != value) onValueChange(final)
            }
    }

    SettingTile(
        title = title,
        description = description,
        note = note,
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(SliderSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        when (value) {
                            is Int -> value.toString()
                            is Long -> value.toString()
                            is Float -> String.format(FLOAT_FORMAT, value)
                            else -> value.toString()
                        },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = localValue,
                    onValueChange = {
                        localValue = it
                        val coerced = it.coerceIn(valueRange)
                        val final: T =
                            when {
                                isInt -> coerced.roundToInt() as T
                                isLong -> coerced.roundToInt().toLong() as T
                                else -> coerced as T
                            }
                        if (final != value) onValueChange(final)
                    },
                    valueRange = valueRange,
                    steps = autoSteps,
                    modifier = modifier.fillMaxWidth(SLIDER_WIDTH_RATIO),
                )
            }
        },
    )
}

private const val SLIDER_WIDTH_RATIO = 0.25f
private const val SLIDER_DEBOUNCE_MS = 300L
private const val STEP_OFFSET = 1
private val SliderSpacing = 8.dp
private const val FLOAT_FORMAT = "%.1f"
