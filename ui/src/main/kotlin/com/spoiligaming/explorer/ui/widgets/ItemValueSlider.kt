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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun <T> DebouncedSlider(
    value: T,
    onValueChange: (T) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    adapter: SliderValueAdapter<T>,
    debounceMillis: Long = ValueSliderDefaults.DEBOUNCE_MILLIS,
    content: @Composable (
        sliderPosition: Float,
        onSliderPositionChange: (Float) -> Unit,
        previewValue: T,
    ) -> Unit,
) {
    var sliderPosition by remember {
        mutableFloatStateOf(adapter.toSliderPosition(value).coerceIn(valueRange))
    }

    LaunchedEffect(value, valueRange) {
        sliderPosition = adapter.toSliderPosition(value).coerceIn(valueRange)
    }

    val currentValue by rememberUpdatedState(value)
    val currentOnChange by rememberUpdatedState(onValueChange)

    LaunchedEffect(adapter, valueRange, debounceMillis) {
        snapshotFlow { sliderPosition }
            .drop(1) // ignore initial emission
            .collectLatest { raw ->
                if (debounceMillis > 0) delay(debounceMillis)
                val coerced = raw.coerceIn(valueRange)
                val final = adapter.fromSliderPosition(coerced)
                if (final != currentValue) currentOnChange(final)
            }
    }

    val previewValue = adapter.fromSliderPosition(sliderPosition.coerceIn(valueRange))

    content(
        sliderPosition,
        { sliderPosition = it.coerceIn(valueRange) },
        previewValue,
    )
}

@Composable
internal fun <T> ValueSliderSettingTile(
    title: String,
    description: String,
    value: T,
    onValueChange: (T) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    adapter: SliderValueAdapter<T>,
    modifier: Modifier = Modifier,
    note: String? = null,
    enabled: Boolean = true,
    debounceMillis: Long = ValueSliderDefaults.DEBOUNCE_MILLIS,
    steps: Int = adapter.defaultSteps(valueRange),
    sliderModifier: Modifier = Modifier.fillMaxWidth(ValueSliderDefaults.TILE_SLIDER_WIDTH_FRACTION),
    valueContent: @Composable (previewValue: T) -> Unit = { preview ->
        Text(
            text = adapter.format(preview),
            style = MaterialTheme.typography.bodyMedium,
        )
    },
) = SettingTile(
    title = title,
    description = description,
    note = note,
    trailingContent = {
        DebouncedSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            adapter = adapter,
            debounceMillis = debounceMillis,
        ) { sliderPosition, onSliderPositionChange, previewValue ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(ValueSliderDefaults.Spacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                valueContent(previewValue)
                Slider(
                    value = sliderPosition,
                    onValueChange = onSliderPositionChange,
                    valueRange = valueRange,
                    steps = steps,
                    enabled = enabled,
                    modifier = sliderModifier.then(modifier),
                )
            }
        }
    },
)

@Composable
internal inline fun <reified T : Number> ItemValueSlider(
    title: String,
    description: String,
    note: String? = null,
    modifier: Modifier = Modifier,
    value: T,
    valueRange: ClosedFloatingPointRange<Float>,
    noinline onValueChange: (T) -> Unit,
    enabled: Boolean = true,
    debounceMillis: Long = ValueSliderDefaults.DEBOUNCE_MILLIS,
) {
    @Suppress("UNCHECKED_CAST")
    val adapter =
        when (T::class) {
            Int::class -> SliderValueAdapters.IntAdapter as SliderValueAdapter<T>
            Long::class -> SliderValueAdapters.LongAdapter as SliderValueAdapter<T>
            Float::class -> SliderValueAdapters.FloatAdapter as SliderValueAdapter<T>
            Double::class -> SliderValueAdapters.DoubleAdapter as SliderValueAdapter<T>
            else -> error("Unsupported slider type: ${T::class.qualifiedName}")
        }

    ValueSliderSettingTile(
        title = title,
        description = description,
        note = note,
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        adapter = adapter,
        enabled = enabled,
        debounceMillis = debounceMillis,
        modifier = modifier,
    )
}

@Immutable
internal interface SliderValueAdapter<T> {
    fun toSliderPosition(value: T): Float

    fun fromSliderPosition(position: Float): T

    fun format(
        value: T,
        locale: Locale = Locale.getDefault(),
    ): String

    fun defaultSteps(valueRange: ClosedFloatingPointRange<Float>): Int = 0
}

internal object SliderValueAdapters {
    @Stable
    object IntAdapter : SliderValueAdapter<Int> {
        override fun toSliderPosition(value: Int) = value.toFloat()

        override fun fromSliderPosition(position: Float) = position.roundToInt()

        override fun format(
            value: Int,
            locale: Locale,
        ) = value.toString()

        override fun defaultSteps(valueRange: ClosedFloatingPointRange<Float>): Int {
            val span = (valueRange.endInclusive - valueRange.start).roundToInt()
            // steps = number of values between endpoints
            return (span - 1).coerceAtLeast(0)
        }
    }

    @Stable
    object LongAdapter : SliderValueAdapter<Long> {
        override fun toSliderPosition(value: Long) = value.toFloat()

        override fun fromSliderPosition(position: Float) = position.roundToInt().toLong()

        override fun format(
            value: Long,
            locale: Locale,
        ) = value.toString()

        override fun defaultSteps(valueRange: ClosedFloatingPointRange<Float>): Int {
            val span = (valueRange.endInclusive - valueRange.start).roundToInt()
            return (span - 1).coerceAtLeast(0)
        }
    }

    @Stable
    object FloatAdapter : SliderValueAdapter<Float> {
        override fun toSliderPosition(value: Float) = value

        override fun fromSliderPosition(position: Float) = position

        override fun format(
            value: Float,
            locale: Locale,
        ) = String.format(locale, ValueSliderDefaults.FLOAT_LABEL_FORMAT, value)
    }

    @Stable
    object DoubleAdapter : SliderValueAdapter<Double> {
        override fun toSliderPosition(value: Double) = value.toFloat()

        override fun fromSliderPosition(position: Float) = position.toDouble()

        override fun format(
            value: Double,
            locale: Locale,
        ) = String.format(locale, ValueSliderDefaults.DOUBLE_LABEL_FORMAT, value)
    }
}

internal object ValueSliderDefaults {
    const val DEBOUNCE_MILLIS = 300L

    const val FLOAT_LABEL_FORMAT = "%.1f"
    const val DOUBLE_LABEL_FORMAT = "%.2f"

    val Spacing = 8.dp

    const val TILE_SLIDER_WIDTH_FRACTION = 0.25f
}
