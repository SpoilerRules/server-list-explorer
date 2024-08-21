package com.spoiligaming.explorer.ui.widgets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.GestureCancellationException
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastMinByOrNull
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

suspend fun AwaitPointerEventScope.awaitHorizontalPointerSlopOrCancellation(
    pointerId: PointerId,
    pointerType: PointerType,
    onPointerSlopReached: (change: PointerInputChange, overSlop: Float) -> Unit,
) = awaitPointerSlopOrCancellation(
    pointerId = pointerId,
    pointerType = pointerType,
    onPointerSlopReached = onPointerSlopReached,
    getDragDirectionValue = { it.x },
)

/**
 * Waits for drag motion along one axis based on [getDragDirectionValue] to pass pointer slop, using
 * [pointerId] as the pointer to examine. If [pointerId] is raised, another pointer from those that
 * are down will be chosen to lead the gesture, and if none are down, `null` is returned. If
 * [pointerId] is not down when [awaitPointerSlopOrCancellation] is called, then `null` is returned.
 *
 * When pointer slop is detected, [onPointerSlopReached] is called with the change and the distance
 * beyond the pointer slop. [getDragDirectionValue] should return the position change in the
 * direction of the drag axis. If [onPointerSlopReached] does not consume the position change,
 * pointer slop will not have been considered detected and the detection will continue or, if it is
 * consumed, the [PointerInputChange] that was consumed will be returned.
 *
 *
 * @return The [PointerInputChange] of the event that was consumed in [onPointerSlopReached] or
 *   `null` if all pointers are raised or the position change was consumed by another gesture
 *   detector.
 */
private suspend inline fun AwaitPointerEventScope.awaitPointerSlopOrCancellation(
    pointerId: PointerId,
    pointerType: PointerType,
    onPointerSlopReached: (PointerInputChange, Float) -> Unit,
    getDragDirectionValue: (Offset) -> Float,
): PointerInputChange? {
    if (currentEvent.isPointerUp(pointerId)) {
        return null // The pointer has already been lifted, so the gesture is canceled
    }
    val touchSlop = viewConfiguration.pointerSlop(pointerType)
    var pointer: PointerId = pointerId
    var totalPositionChange = 0f

    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.fastFirstOrNull { it.id == pointer }!!
        if (dragEvent.isConsumed) {
            return null
        } else if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) {
                // This is the last "up"
                return null
            } else {
                pointer = otherDown.id
            }
        } else {
            val currentPosition = dragEvent.position
            val previousPosition = dragEvent.previousPosition
            val positionChange =
                getDragDirectionValue(currentPosition) - getDragDirectionValue(previousPosition)
            totalPositionChange += positionChange

            val inDirection = abs(totalPositionChange)
            if (inDirection < touchSlop) {
                // verify that nothing else consumed the drag event
                awaitPointerEvent(PointerEventPass.Final)
                if (dragEvent.isConsumed) {
                    return null
                }
            } else {
                onPointerSlopReached(
                    dragEvent,
                    totalPositionChange - (sign(totalPositionChange) * touchSlop),
                )
                if (dragEvent.isConsumed) {
                    return dragEvent
                } else {
                    totalPositionChange = 0f
                }
            }
        }
    }
}

private fun PointerEvent.isPointerUp(pointerId: PointerId): Boolean =
    changes.fastFirstOrNull { it.id == pointerId }?.pressed != true

private val mouseSlop = 0.125.dp
private val defaultTouchSlop = 18.dp // The default touch slop on Android devices
private val mouseToTouchSlopRatio = mouseSlop / defaultTouchSlop

private fun ViewConfiguration.pointerSlop(pointerType: PointerType): Float =
    when (pointerType) {
        PointerType.Mouse -> touchSlop * mouseToTouchSlopRatio
        else -> touchSlop
    }

/**
 * Denotes that the annotated element should be an int or long in the given range.
 *
 * Example:
 * ```
 * @IntRange(from=0,to=255)
 * public int getAlpha() {
 *     ...
 * }
 * ```
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.ANNOTATION_CLASS,
)
annotation class IntRange(
    /** Smallest value, inclusive */
    val from: Long = Long.MIN_VALUE,
    /** Largest value, inclusive */
    val to: Long = Long.MAX_VALUE,
)

@Composable
fun Slider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0)
    steps: Int = 0,
    size: DpSize = DpSize(144.dp, 26.dp),
    thumbSize: Dp = 26.dp,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    colors: SliderColors = SliderDefaults.colors(),
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    require(steps >= 0) { "steps should be >= 0" }
    val onValueChangeState = rememberUpdatedState(onValueChange)
    val tickFractions =
        remember(steps) {
            stepsToTickFractions(steps)
        }
    BoxWithConstraints(
        modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(
                minWidth = size.width,
                maxWidth = size.width,
                minHeight = size.height,
                maxHeight = size.height,
            )
            .sliderSemantics(
                value,
                enabled,
                onValueChange,
                onValueChangeFinished,
                valueRange,
                steps,
            )
            .focusable(enabled, interactionSource),
    ) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val widthPx = constraints.maxWidth.toFloat()
        val maxPx: Float
        val minPx: Float

        with(LocalDensity.current) {
            maxPx = max(widthPx - ThumbRadius.toPx(), 0f)
            minPx = min(ThumbRadius.toPx(), maxPx)
        }

        fun scaleToUserValue(offset: Float) =
            scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)

        fun scaleToOffset(userValue: Float) =
            scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)

        val scope = rememberCoroutineScope()
        val rawOffset = remember { mutableFloatStateOf(scaleToOffset(value)) }
        val pressOffset = remember { mutableFloatStateOf(0f) }

        val draggableState =
            remember(minPx, maxPx, valueRange) {
                SliderDraggableState {
                    rawOffset.floatValue = (rawOffset.floatValue + it + pressOffset.floatValue)
                    pressOffset.floatValue = 0f
                    val offsetInTrack = rawOffset.floatValue.coerceIn(minPx, maxPx)
                    onValueChangeState.value.invoke(scaleToUserValue(offsetInTrack))
                }
            }

        CorrectValueSideEffect(::scaleToOffset, valueRange, minPx..maxPx, rawOffset, value)

        val gestureEndAction =
            rememberUpdatedState<(Float) -> Unit> { velocity: Float ->
                val current = rawOffset.floatValue
                val target = snapValueToTick(current, tickFractions, minPx, maxPx)
                if (current != target) {
                    scope.launch {
                        animateToTarget(draggableState, current, target, velocity)
                        onValueChangeFinished?.invoke()
                    }
                } else if (!draggableState.isDragging) {
                    // check ifDragging in case the change is still in progress (touch -> drag case)
                    onValueChangeFinished?.invoke()
                }
            }
        val press =
            Modifier.sliderTapModifier(
                draggableState,
                interactionSource,
                widthPx,
                isRtl,
                rawOffset,
                gestureEndAction,
                pressOffset,
                enabled,
            )

        val drag =
            Modifier.draggable(
                orientation = Orientation.Horizontal,
                reverseDirection = isRtl,
                enabled = enabled,
                interactionSource = interactionSource,
                onDragStopped = { velocity -> gestureEndAction.value.invoke(velocity) },
                startDragImmediately = draggableState.isDragging,
                state = draggableState,
            )

        val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
        val fraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)
        SliderImpl(
            enabled,
            fraction,
            colors,
            maxPx - minPx,
            thumbSize,
            modifier = press.then(drag),
        )
    }
}

@Composable
@ExperimentalMaterialApi
fun RangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0)
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
) {
    val startInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val endInteractionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    require(steps >= 0) { "steps should be >= 0" }
    val onValueChangeState = rememberUpdatedState(onValueChange)
    val tickFractions =
        remember(steps) {
            stepsToTickFractions(steps)
        }

    BoxWithConstraints(
        modifier =
            modifier
                .minimumInteractiveComponentSize()
                .requiredSizeIn(minWidth = ThumbRadius * 4, minHeight = ThumbRadius * 2),
    ) {
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val widthPx = constraints.maxWidth.toFloat()
        val maxPx: Float
        val minPx: Float

        with(LocalDensity.current) {
            maxPx = widthPx - ThumbRadius.toPx()
            minPx = ThumbRadius.toPx()
        }

        fun scaleToUserValue(offset: ClosedFloatingPointRange<Float>) =
            scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)

        fun scaleToOffset(userValue: Float) =
            scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)

        val rawOffsetStart = remember { mutableFloatStateOf(scaleToOffset(value.start)) }
        val rawOffsetEnd = remember { mutableFloatStateOf(scaleToOffset(value.endInclusive)) }

        CorrectValueSideEffect(
            ::scaleToOffset,
            valueRange,
            minPx..maxPx,
            rawOffsetStart,
            value.start,
        )
        CorrectValueSideEffect(
            ::scaleToOffset,
            valueRange,
            minPx..maxPx,
            rawOffsetEnd,
            value.endInclusive,
        )

        val scope = rememberCoroutineScope()
        val gestureEndAction =
            rememberUpdatedState<(Boolean) -> Unit> { isStart ->
                val current = (if (isStart) rawOffsetStart else rawOffsetEnd).floatValue
                // target is the closest anchor to the `current`, if exists
                val target = snapValueToTick(current, tickFractions, minPx, maxPx)
                if (current == target) {
                    onValueChangeFinished?.invoke()
                    return@rememberUpdatedState
                }

                scope.launch {
                    Animatable(initialValue = current).animateTo(
                        target,
                        SliderToTickAnimation,
                        0f,
                    ) {
                        (if (isStart) rawOffsetStart else rawOffsetEnd).floatValue = this.value
                        onValueChangeState.value.invoke(
                            scaleToUserValue(rawOffsetStart.floatValue..rawOffsetEnd.floatValue),
                        )
                    }

                    onValueChangeFinished?.invoke()
                }
            }

        val onDrag =
            rememberUpdatedState<(Boolean, Float) -> Unit> { isStart, offset ->
                val offsetRange =
                    if (isStart) {
                        rawOffsetStart.floatValue = (rawOffsetStart.floatValue + offset)
                        rawOffsetEnd.floatValue = scaleToOffset(value.endInclusive)
                        val offsetEnd = rawOffsetEnd.floatValue
                        val offsetStart = rawOffsetStart.floatValue.coerceIn(minPx, offsetEnd)
                        offsetStart..offsetEnd
                    } else {
                        rawOffsetEnd.floatValue = (rawOffsetEnd.floatValue + offset)
                        rawOffsetStart.floatValue = scaleToOffset(value.start)
                        val offsetStart = rawOffsetStart.floatValue
                        val offsetEnd = rawOffsetEnd.floatValue.coerceIn(offsetStart, maxPx)
                        offsetStart..offsetEnd
                    }

                onValueChangeState.value.invoke(scaleToUserValue(offsetRange))
            }

        val pressDrag =
            Modifier.rangeSliderPressDragModifier(
                startInteractionSource,
                endInteractionSource,
                rawOffsetStart,
                rawOffsetEnd,
                enabled,
                isRtl,
                widthPx,
                valueRange,
                gestureEndAction,
                onDrag,
            )

        // The positions of the thumbs are dependent on each other.
        val coercedStart = value.start.coerceIn(valueRange.start, value.endInclusive)
        val coercedEnd = value.endInclusive.coerceIn(value.start, valueRange.endInclusive)
        val fractionStart = calcFraction(valueRange.start, valueRange.endInclusive, coercedStart)
        val fractionEnd = calcFraction(valueRange.start, valueRange.endInclusive, coercedEnd)
        val startSteps = floor(steps * fractionEnd).toInt()
        val endSteps = floor(steps * (1f - fractionStart)).toInt()

        val startThumbSemantics =
            Modifier.sliderSemantics(
                coercedStart,
                enabled,
                { value -> onValueChangeState.value.invoke(value..coercedEnd) },
                onValueChangeFinished,
                valueRange.start..coercedEnd,
                startSteps,
            )
        val endThumbSemantics =
            Modifier.sliderSemantics(
                coercedEnd,
                enabled,
                { value -> onValueChangeState.value.invoke(coercedStart..value) },
                onValueChangeFinished,
                coercedStart..valueRange.endInclusive,
                endSteps,
            )

        RangeSliderImpl(
            enabled,
            fractionStart,
            fractionEnd,
            colors,
            maxPx - minPx,
            startInteractionSource,
            endInteractionSource,
            modifier = pressDrag,
            startThumbSemantics,
            endThumbSemantics,
        )
    }
}

/**
 * Object to hold defaults used by [Slider]
 */
object SliderDefaults {
    /**
     * Creates a [SliderColors] that represents the different colors used in parts of the
     * [Slider] in different states.
     *
     * For the name references below the words "active" and "inactive" are used. Active part of
     * the slider is filled with progress, so if slider's progress is 30% out of 100%, left (or
     * right in RTL) 30% of the track will be active, the rest is not active.
     *
     * @param thumbColor thumb color when enabled
     * @param disabledThumbColor thumb colors when disabled
     * @param activeTrackColor color of the track in the part that is "active", meaning that the
     * thumb is ahead of it
     * @param inactiveTrackColor color of the track in the part that is "inactive", meaning that the
     * thumb is before it
     * @param disabledActiveTrackColor color of the track in the "active" part when the Slider is
     * disabled
     * @param disabledInactiveTrackColor color of the track in the "inactive" part when the
     * Slider is disabled
     */
    @Composable
    fun colors(
        thumbColor: Color = MaterialTheme.colors.primary,
        disabledThumbColor: Color =
            MaterialTheme.colors.onSurface
                .copy(alpha = ContentAlpha.disabled)
                .compositeOver(MaterialTheme.colors.surface),
        activeTrackColor: Color = MaterialTheme.colors.primary,
        inactiveTrackColor: Color = activeTrackColor.copy(alpha = INACTIVE_TRACK_ALPHA),
        disabledActiveTrackColor: Color =
            MaterialTheme.colors.onSurface.copy(alpha = DISABLED_ACTIVE_TRACK_ALPHA),
        disabledInactiveTrackColor: Color =
            disabledActiveTrackColor.copy(alpha = DISABLED_INACTIVE_TRACK_ALPHA),
    ): SliderColors =
        DefaultSliderColors(
            thumbColor = thumbColor,
            disabledThumbColor = disabledThumbColor,
            activeTrackColor = activeTrackColor,
            inactiveTrackColor = inactiveTrackColor,
            disabledActiveTrackColor = disabledActiveTrackColor,
            disabledInactiveTrackColor = disabledInactiveTrackColor,
        )

    /**
     * Default alpha of the inactive part of the track
     */
    private const val INACTIVE_TRACK_ALPHA = 0.24f

    /**
     * Default alpha for the track when it is disabled but active
     */
    private const val DISABLED_INACTIVE_TRACK_ALPHA = 0.12f

    /**
     * Default alpha for the track when it is disabled and inactive
     */
    private const val DISABLED_ACTIVE_TRACK_ALPHA = 0.32f
}

/**
 * Represents the colors used by a [Slider] and its parts in different states
 *
 * See [SliderDefaults.colors] for the default implementation that follows Material
 * specifications.
 */
@Stable
interface SliderColors {
    /**
     * Represents the color used for the slider's thumb, depending on [enabled].
     *
     * @param enabled whether the [Slider] is enabled or not
     */
    @Composable
    fun thumbColor(enabled: Boolean): State<Color>

    /**
     * Represents the color used for the slider's track, depending on [enabled] and [active].
     *
     * Active part is filled with progress, so if sliders progress is 30% out of 100%, left (or
     * right in RTL) 30% of the track will be active, the rest is not active.
     *
     * @param enabled whether the [Slider] is enabled or not
     * @param active whether the part of the track is active of not
     */
    @Composable
    fun trackColor(
        enabled: Boolean,
        active: Boolean,
    ): State<Color>
}

@Composable
private fun SliderImpl(
    enabled: Boolean,
    positionFraction: Float,
    colors: SliderColors,
    width: Float,
    thumbSize: Dp,
    modifier: Modifier,
) {
    Box(modifier.then(DefaultSliderConstraints).background(Color.Blue)) {
        val trackStrokeWidth: Float
        val thumbPx: Float
        val widthDp: Dp
        with(LocalDensity.current) {
            trackStrokeWidth = TrackHeight.toPx()
            thumbPx = ThumbRadius.toPx()
            widthDp = width.toDp()
        }

        val thumbOffsetDp = thumbSize / 7
        val maxOffset = widthDp - thumbOffsetDp

        val offset =
            (thumbOffsetDp + (maxOffset - thumbOffsetDp) * positionFraction).coerceIn(
                thumbOffsetDp,
                maxOffset,
            )

        Track(
            Modifier.fillMaxSize(),
            colors,
            enabled,
            0f,
            positionFraction,
            thumbPx,
            trackStrokeWidth,
        )
        SliderThumb(Modifier, offset, colors, enabled, thumbSize)
    }
}

@Composable
private fun RangeSliderImpl(
    enabled: Boolean,
    positionFractionStart: Float,
    positionFractionEnd: Float,
    colors: SliderColors,
    width: Float,
    startInteractionSource: MutableInteractionSource,
    endInteractionSource: MutableInteractionSource,
    modifier: Modifier,
    startThumbSemantics: Modifier,
    endThumbSemantics: Modifier,
) {
    Box(modifier.then(DefaultSliderConstraints)) {
        val trackStrokeWidth: Float
        val thumbPx: Float
        val widthDp: Dp
        with(LocalDensity.current) {
            trackStrokeWidth = TrackHeight.toPx()
            thumbPx = ThumbRadius.toPx()
            widthDp = width.toDp()
        }

        val thumbSize = ThumbRadius * 2
        val offsetStart = widthDp * positionFractionStart
        val offsetEnd = widthDp * positionFractionEnd
        Track(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxSize(),
            colors,
            enabled,
            positionFractionStart,
            positionFractionEnd,
            thumbPx,
            trackStrokeWidth,
        )

        SliderThumb(
            Modifier
                .semantics(mergeDescendants = true) {
                    // no-op
                }
                .focusable(true, startInteractionSource)
                .then(startThumbSemantics),
            offsetStart,
            colors,
            enabled,
            thumbSize,
        )
        SliderThumb(
            Modifier
                .semantics(mergeDescendants = true) {
                    // no-op
                }
                .focusable(true, endInteractionSource)
                .then(endThumbSemantics),
            offsetEnd,
            colors,
            enabled,
            thumbSize,
        )
    }
}

@Composable
private fun BoxScope.SliderThumb(
    modifier: Modifier,
    offset: Dp,
    colors: SliderColors,
    enabled: Boolean,
    thumbSize: Dp,
) {
    Box(
        Modifier
            .padding(start = offset)
            .align(Alignment.CenterStart),
    ) {
        Spacer(
            modifier
                .size(thumbSize)
                .background(colors.thumbColor(enabled).value, CircleShape),
        )
    }
}

@Composable
private fun Track(
    modifier: Modifier,
    colors: SliderColors,
    enabled: Boolean,
    positionFractionStart: Float,
    positionFractionEnd: Float,
    thumbPx: Float,
    trackStrokeWidth: Float,
) {
    val inactiveTrackColor = colors.trackColor(enabled, active = false)
    val activeTrackColor = colors.trackColor(enabled, active = true)

    Canvas(modifier) {
        val isRtl = layoutDirection == LayoutDirection.Rtl

        val sliderStart = Offset(thumbPx - 4, center.y)
        val sliderEnd = Offset(size.width - thumbPx, center.y)

        val actualSliderStart = if (isRtl) sliderEnd else sliderStart
        val actualSliderEnd = if (isRtl) sliderStart else sliderEnd

        val inactiveTrackPath =
            Path().apply {
                moveTo(actualSliderStart.x, actualSliderStart.y)
                lineTo(actualSliderEnd.x, actualSliderEnd.y)
            }
        drawPath(
            path = inactiveTrackPath,
            color = inactiveTrackColor.value,
            style =
                Stroke(
                    width = trackStrokeWidth,
                    cap = StrokeCap.Round,
                ),
        )

        val trackLength = actualSliderEnd.x - actualSliderStart.x
        val sliderValueStart =
            Offset(
                actualSliderStart.x + trackLength * positionFractionStart,
                center.y,
            )
        val sliderValueEnd =
            Offset(
                actualSliderStart.x + trackLength * positionFractionEnd,
                center.y,
            )

        val activeTrackPath =
            Path().apply {
                moveTo(sliderValueStart.x, sliderValueStart.y)
                lineTo(sliderValueEnd.x, sliderValueEnd.y)
            }
        drawPath(
            path = activeTrackPath,
            color = activeTrackColor.value,
            style =
                Stroke(
                    width = trackStrokeWidth,
                    cap = StrokeCap.Round,
                ),
        )
    }
}

private fun snapValueToTick(
    current: Float,
    tickFractions: List<Float>,
    minPx: Float,
    maxPx: Float,
): Float {
    // target is the closest anchor to the `current`, if exists
    return tickFractions
        .fastMinByOrNull { abs(lerp(minPx, maxPx, it) - current) }
        ?.run { lerp(minPx, maxPx, this) }
        ?: current
}

private suspend fun AwaitPointerEventScope.awaitSlop(
    id: PointerId,
    type: PointerType,
): Pair<PointerInputChange, Float>? {
    var initialDelta = 0f
    val postPointerSlop = { pointerInput: PointerInputChange, offset: Float ->
        pointerInput.consume()
        initialDelta = offset
    }
    val afterSlopResult = awaitHorizontalPointerSlopOrCancellation(id, type, postPointerSlop)
    return if (afterSlopResult != null) afterSlopResult to initialDelta else null
}

private fun stepsToTickFractions(steps: Int): List<Float> {
    return if (steps == 0) emptyList() else List(steps + 2) { it.toFloat() / (steps + 1) }
}

// Scale x1 from a1..b1 range to a2..b2 range
private fun scale(
    a1: Float,
    b1: Float,
    x1: Float,
    a2: Float,
    b2: Float,
) = lerp(a2, b2, calcFraction(a1, b1, x1))

// Scale x.start, x.endInclusive from a1..b1 range to a2..b2 range
private fun scale(
    a1: Float,
    b1: Float,
    x: ClosedFloatingPointRange<Float>,
    a2: Float,
    b2: Float,
) = scale(a1, b1, x.start, a2, b2)..scale(a1, b1, x.endInclusive, a2, b2)

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
private fun calcFraction(
    a: Float,
    b: Float,
    pos: Float,
) = (if (b - a == 0f) 0f else (pos - a) / (b - a)).fastCoerceIn(0f, 1f)

@Composable
private fun CorrectValueSideEffect(
    scaleToOffset: (Float) -> Float,
    valueRange: ClosedFloatingPointRange<Float>,
    trackRange: ClosedFloatingPointRange<Float>,
    valueState: MutableState<Float>,
    value: Float,
) = SideEffect {
    val error = (valueRange.endInclusive - valueRange.start) / 1000
    val newOffset = scaleToOffset(value)
    if (abs(newOffset - valueState.value) > error) {
        if (valueState.value in trackRange) {
            valueState.value = newOffset
        }
    }
}

private fun Modifier.sliderSemantics(
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
): Modifier {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    return semantics {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                var newValue = targetValue.coerceIn(valueRange.start, valueRange.endInclusive)
                val originalVal = newValue
                val resolvedValue =
                    if (steps > 0) {
                        var distance: Float = newValue
                        for (i in 0..steps + 1) {
                            val stepValue =
                                lerp(
                                    valueRange.start,
                                    valueRange.endInclusive,
                                    i.toFloat() / (steps + 1),
                                )
                            if (abs(stepValue - originalVal) <= distance) {
                                distance = abs(stepValue - originalVal)
                                newValue = stepValue
                            }
                        }
                        newValue
                    } else {
                        newValue
                    }
                // This is to keep it consistent with AbsSeekbar.java: return false if no
                // change from current.
                if (resolvedValue == coerced) {
                    false
                } else {
                    onValueChange(resolvedValue)
                    onValueChangeFinished?.invoke()
                    true
                }
            },
        )
    }.progressSemantics(value, valueRange, steps)
}

private fun Modifier.sliderTapModifier(
    draggableState: DraggableState,
    interactionSource: MutableInteractionSource,
    maxPx: Float,
    isRtl: Boolean,
    rawOffset: State<Float>,
    gestureEndAction: State<(Float) -> Unit>,
    pressOffset: MutableState<Float>,
    enabled: Boolean,
) = composed(
    factory = {
        if (enabled) {
            val scope = rememberCoroutineScope()
            pointerInput(draggableState, interactionSource, maxPx, isRtl) {
                detectTapGestures(
                    onPress = { pos ->
                        val to = if (isRtl) maxPx - pos.x else pos.x
                        pressOffset.value = to - rawOffset.value
                        try {
                            awaitRelease()
                        } catch (_: GestureCancellationException) {
                            pressOffset.value = 0f
                        }
                    },
                    onTap = {
                        scope.launch {
                            draggableState.drag(MutatePriority.UserInput) {
                                // just trigger animation, press offset will be applied
                                dragBy(0f)
                            }
                            gestureEndAction.value.invoke(0f)
                        }
                    },
                )
            }
        } else {
            this
        }
    },
    inspectorInfo =
        debugInspectorInfo {
            name = "sliderTapModifier"
            properties["draggableState"] = draggableState
            properties["interactionSource"] = interactionSource
            properties["maxPx"] = maxPx
            properties["isRtl"] = isRtl
            properties["rawOffset"] = rawOffset
            properties["gestureEndAction"] = gestureEndAction
            properties["pressOffset"] = pressOffset
            properties["enabled"] = enabled
        },
)

private suspend fun animateToTarget(
    draggableState: DraggableState,
    current: Float,
    target: Float,
    velocity: Float,
) {
    draggableState.drag {
        var latestValue = current
        Animatable(initialValue = current).animateTo(target, SliderToTickAnimation, velocity) {
            dragBy(this.value - latestValue)
            latestValue = this.value
        }
    }
}

private fun Modifier.rangeSliderPressDragModifier(
    startInteractionSource: MutableInteractionSource,
    endInteractionSource: MutableInteractionSource,
    rawOffsetStart: State<Float>,
    rawOffsetEnd: State<Float>,
    enabled: Boolean,
    isRtl: Boolean,
    maxPx: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    gestureEndAction: State<(Boolean) -> Unit>,
    onDrag: State<(Boolean, Float) -> Unit>,
): Modifier =
    if (enabled) {
        pointerInput(startInteractionSource, endInteractionSource, maxPx, isRtl, valueRange) {
            val rangeSliderLogic =
                RangeSliderLogic(
                    startInteractionSource,
                    endInteractionSource,
                    rawOffsetStart,
                    rawOffsetEnd,
                    onDrag,
                )
            coroutineScope {
                awaitEachGesture {
                    val event = awaitFirstDown(requireUnconsumed = false)
                    val interaction = DragInteraction.Start()
                    var posX = if (isRtl) maxPx - event.position.x else event.position.x
                    val compare = rangeSliderLogic.compareOffsets(posX)
                    var draggingStart =
                        if (compare != 0) {
                            compare < 0
                        } else {
                            rawOffsetStart.value > posX
                        }

                    awaitSlop(event.id, event.type)?.let {
                        val slop = viewConfiguration.pointerSlop(event.type)
                        val shouldUpdateCapturedThumb =
                            abs(rawOffsetEnd.value - posX) < slop &&
                                abs(rawOffsetStart.value - posX) < slop
                        if (shouldUpdateCapturedThumb) {
                            val dir = it.second
                            draggingStart = if (isRtl) dir >= 0f else dir < 0f
                            posX += it.first.positionChange().x
                        }
                    }

                    rangeSliderLogic.captureThumb(
                        draggingStart,
                        posX,
                        interaction,
                        this@coroutineScope,
                    )

                    val finishInteraction =
                        try {
                            val success =
                                horizontalDrag(pointerId = event.id) {
                                    val deltaX = it.positionChange().x
                                    onDrag.value.invoke(
                                        draggingStart,
                                        if (isRtl) -deltaX else deltaX,
                                    )
                                }
                            if (success) {
                                DragInteraction.Stop(interaction)
                            } else {
                                DragInteraction.Cancel(interaction)
                            }
                        } catch (e: CancellationException) {
                            DragInteraction.Cancel(interaction)
                        }

                    gestureEndAction.value.invoke(draggingStart)
                    launch {
                        rangeSliderLogic
                            .activeInteraction(draggingStart)
                            .emit(finishInteraction)
                    }
                }
            }
        }
    } else {
        this
    }

private class RangeSliderLogic(
    val startInteractionSource: MutableInteractionSource,
    val endInteractionSource: MutableInteractionSource,
    val rawOffsetStart: State<Float>,
    val rawOffsetEnd: State<Float>,
    val onDrag: State<(Boolean, Float) -> Unit>,
) {
    fun activeInteraction(draggingStart: Boolean): MutableInteractionSource =
        if (draggingStart) startInteractionSource else endInteractionSource

    fun compareOffsets(eventX: Float): Int {
        val diffStart = abs(rawOffsetStart.value - eventX)
        val diffEnd = abs(rawOffsetEnd.value - eventX)
        return diffStart.compareTo(diffEnd)
    }

    fun captureThumb(
        draggingStart: Boolean,
        posX: Float,
        interaction: Interaction,
        scope: CoroutineScope,
    ) {
        onDrag.value.invoke(
            draggingStart,
            posX - if (draggingStart) rawOffsetStart.value else rawOffsetEnd.value,
        )
        scope.launch {
            activeInteraction(draggingStart).emit(interaction)
        }
    }
}

@Immutable
private class DefaultSliderColors(
    private val thumbColor: Color,
    private val disabledThumbColor: Color,
    private val activeTrackColor: Color,
    private val inactiveTrackColor: Color,
    private val disabledActiveTrackColor: Color,
    private val disabledInactiveTrackColor: Color,
) : SliderColors {
    @Composable
    override fun thumbColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) thumbColor else disabledThumbColor)
    }

    @Composable
    override fun trackColor(
        enabled: Boolean,
        active: Boolean,
    ): State<Color> {
        return rememberUpdatedState(
            if (enabled) {
                if (active) activeTrackColor else inactiveTrackColor
            } else {
                if (active) disabledActiveTrackColor else disabledInactiveTrackColor
            },
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DefaultSliderColors

        if (thumbColor != other.thumbColor) return false
        if (disabledThumbColor != other.disabledThumbColor) return false
        if (activeTrackColor != other.activeTrackColor) return false
        if (inactiveTrackColor != other.inactiveTrackColor) return false
        if (disabledActiveTrackColor != other.disabledActiveTrackColor) return false
        if (disabledInactiveTrackColor != other.disabledInactiveTrackColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = thumbColor.hashCode()
        result = 31 * result + disabledThumbColor.hashCode()
        result = 31 * result + activeTrackColor.hashCode()
        result = 31 * result + inactiveTrackColor.hashCode()
        result = 31 * result + disabledActiveTrackColor.hashCode()
        result = 31 * result + disabledInactiveTrackColor.hashCode()
        return result
    }
}

private val ThumbRadius = 13.dp

private val TrackHeight = 20.dp // TODO: allow user to pass this in [RangeSlider] and [Slider]
private val SliderHeight = 26.dp
private val SliderMinWidth = 144.dp // TODO: clarify min width
private val DefaultSliderConstraints =
    Modifier
        .width(SliderMinWidth)
        .height(SliderHeight)

private val SliderToTickAnimation = TweenSpec<Float>(durationMillis = 100)

private class SliderDraggableState(
    val onDelta: (Float) -> Unit,
) : DraggableState {
    var isDragging by mutableStateOf(false)
        private set

    private val dragScope: DragScope =
        object : DragScope {
            override fun dragBy(pixels: Float): Unit = onDelta(pixels)
        }

    private val scrollMutex = MutatorMutex()

    override suspend fun drag(
        dragPriority: MutatePriority,
        block: suspend DragScope.() -> Unit,
    ): Unit =
        coroutineScope {
            isDragging = true
            scrollMutex.mutateWith(dragScope, dragPriority, block)
            isDragging = false
        }

    override fun dispatchRawDelta(delta: Float) = onDelta(delta)
}
