/*
 * This file contains a modified version of the Slider component originally
 * found at: https://github.com/Konyaco/compose-fluent-ui/blob/master/fluent/src/commonMain/kotlin/com/konyaco/fluent/component/Slider.kt
 *
 * The original work has been adapted for use in this project.
 */

package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.spoiligaming.explorer.ui.MapleColorPalette

@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    rail: @Composable () -> Unit = { SliderDefaults.Rail() },
    track: @Composable (progress: Float, width: Dp) -> Unit = { fraction, width ->
        SliderDefaults.Track(
            fraction,
            width,
        )
    },
    thumb: @Composable (progress: Float, width: Dp) -> Unit = { fraction, width ->
        SliderDefaults.Thumb(
            fraction,
            width,
        )
    },
) {
    val progress = valueToFraction(value, valueRange.start, valueRange.endInclusive)
    Slider(
        modifier = modifier,
        progress = progress,
        onProgressChange = {
            onValueChange(fractionToValue(it, valueRange.start, valueRange.endInclusive))
        },
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = interactionSource,
        rail = rail,
        track = track,
        thumb = thumb,
    )
}

@Composable
private fun Slider(
    progress: Float,
    onProgressChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    rail: @Composable () -> Unit = { SliderDefaults.Rail() },
    track: @Composable (
        progress: Float,
        width: Dp,
    ) -> Unit = { fraction, width -> SliderDefaults.Track(fraction, width) },
    thumb: @Composable (
        progress: Float,
        width: Dp,
    ) -> Unit = { fraction, width -> SliderDefaults.Thumb(fraction, width) },
) {
    val currentOnProgressChange by rememberUpdatedState(onProgressChange)
    BoxWithConstraints(
        modifier = modifier.size(144.dp, 26.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val width = constraints.minWidth
        var dragging by remember { mutableStateOf(false) }
        var thumbOffset by remember { mutableStateOf(Offset.Zero) }

        val density = LocalDensity.current
        val thumbSizePx = with(density) { ThumbSize.toPx() }

        fun calcProgress(offset: Offset): Float {
            val maxOffset = width - thumbSizePx
            return valueToFraction(offset.x.coerceIn(0f, maxOffset), 0f, maxOffset)
        }

        Box(
            modifier =
                Modifier
                    .draggable(
                        state =
                            rememberDraggableState { delta ->
                                val newOffset = Offset(thumbOffset.x + delta, thumbOffset.y)
                                thumbOffset = Offset(newOffset.x.coerceIn(0f, width - thumbSizePx), newOffset.y)
                                currentOnProgressChange(calcProgress(thumbOffset))
                            },
                        interactionSource = interactionSource,
                        onDragStarted = {
                            dragging = true
                        },
                        onDragStopped = {
                            dragging = false
                            onValueChangeFinished?.invoke()
                        },
                        orientation = Orientation.Horizontal,
                    )
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            dragging = true
                            val clickedOffset = down.position.x - (thumbSizePx / 2)
                            thumbOffset = Offset(clickedOffset, down.position.y)
                            currentOnProgressChange(calcProgress(thumbOffset))
                            waitForUpOrCancellation()
                            dragging = false
                        }
                    },
            contentAlignment = Alignment.CenterStart,
        ) {
            rail()
            track(progress, width.dp)
            thumb(progress, width.dp)
        }
    }
}

private fun fractionToValue(
    fraction: Float,
    start: Float,
    end: Float,
): Float = (end - start) * fraction + start

private fun valueToFraction(
    value: Float,
    start: Float,
    end: Float,
): Float = (value - start) / (end - start)

object SliderDefaults {
    @Composable
    fun Track(
        fraction: Float,
        maxWidth: Dp,
        modifier: Modifier = Modifier,
    ) {
        val effectiveWidth = maxWidth - ThumbSize

        val trackProgressWidth = effectiveWidth * fraction

        Box(
            modifier =
                modifier
                    .width(trackProgressWidth + ThumbSize)
                    .height(TrackHeight)
                    .background(MapleColorPalette.control, CircleShape),
        )
    }

    @Composable
    fun Rail() =
        Box(
            modifier =
                Modifier.fillMaxWidth().height(
                    TrackHeight,
                ).background(MapleColorPalette.tertiaryControl, CircleShape).clip(CircleShape),
        )

    @Composable
    fun Thumb(
        fraction: Float,
        maxWidth: Dp,
        modifier: Modifier = Modifier,
    ) {
        val effectiveWidth = maxWidth - ThumbSize
        val thumbOffset = fraction * effectiveWidth

        Box(
            modifier =
                modifier
                    .offset { IntOffset(x = thumbOffset.roundToPx(), y = 0) }
                    .size(ThumbSize)
                    .clip(CircleShape)
                    .background(MapleColorPalette.secondaryControl, CircleShape),
        )
    }
}

private val ThumbSize = 26.dp
private val TrackHeight = 20.dp
