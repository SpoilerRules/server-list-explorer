package com.spoiligaming.explorer.ui.widgets

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.fonts.FontFactory
import org.jetbrains.compose.resources.painterResource
import server_list_explorer.generated.resources.Res
import server_list_explorer.generated.resources.chevron_down
import kotlin.math.max
import kotlin.math.min

@Composable
fun DropdownMenuWithLabel(
    label: String,
    currentValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp),
) {
    MapleDropdownMenu(
        false,
        currentValue,
        options
    ) { newValue ->
        onValueChange(newValue)
    }
    Text(
        text = label,
        color = MapleColorPalette.text,
        style =
            TextStyle(
                fontFamily = FontFactory.comfortaaLight,
                fontWeight = FontWeight.Light,
                fontSize = 15.sp,
            ),
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapleDropdownMenu(
    elevation: Boolean,
    defaultValue: String,
    options: List<String>,
    onValueUpdate: (String) -> Unit,
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(defaultValue) }
    val dropdownExpandedState = remember { MutableTransitionState(false) }
    val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
    val density = LocalDensity.current

    dropdownExpandedState.targetState = isDropdownExpanded

    val popupPositionProvider =
        remember(DpOffset(0.dp, 5.dp), density) {
            DropdownMenuPositionProvider(DpOffset(0.dp, 5.dp), density) { parentBounds, menuBounds,
                ->
                transformOriginState.value = calculateTransformOrigin(parentBounds, menuBounds)
            }
        }

    Box(
        modifier =
            Modifier.width(255.dp)
                .height(26.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MapleColorPalette.control, RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.White),
                ) {
                    isDropdownExpanded = true
                },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedOption,
                color = MapleColorPalette.text,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaLight,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                    ),
            )

            Box(modifier = Modifier.size(16.dp), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(Res.drawable.chevron_down),
                    tint = MapleColorPalette.text,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        if (dropdownExpandedState.currentState || dropdownExpandedState.targetState) {
            var focusManager: FocusManager? by mutableStateOf(null)
            var inputModeManager: InputModeManager? by mutableStateOf(null)

            Popup(
                onDismissRequest = { isDropdownExpanded = false },
                popupPositionProvider = popupPositionProvider,
                properties = PopupProperties(focusable = true),
                onKeyEvent = {
                    if (it.type == KeyEventType.KeyDown) {
                        when (it.key) {
                            Key.DirectionDown -> {
                                inputModeManager?.requestInputMode(InputMode.Keyboard)
                                focusManager?.moveFocus(FocusDirection.Next)
                                true
                            }
                            Key.DirectionUp -> {
                                inputModeManager?.requestInputMode(InputMode.Keyboard)
                                focusManager?.moveFocus(FocusDirection.Previous)
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                },
            ) {
                focusManager = LocalFocusManager.current
                inputModeManager = LocalInputModeManager.current

                DropdownMenuContent(
                    expandedState = dropdownExpandedState,
                    transformOriginState = transformOriginState,
                    scrollState = rememberScrollState(),
                    elevation = elevation,
                    modifier =
                        Modifier.width(255.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = MapleColorPalette.tertiaryControl,
                                RoundedCornerShape(12.dp),
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication =
                                    ripple(color = MapleColorPalette.control),
                            ) {},
                ) {
                    options.forEach { option ->
                        var isHovering by remember { mutableStateOf(false) }
                        Row(
                            modifier =
                                Modifier
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {
                                            selectedOption = option
                                            isDropdownExpanded = false
                                            onValueUpdate(option)
                                        },
                                    )
                                    .width(245.dp)
                                    .height(26.dp)
                                    .offset(x = 5.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .onHover { isHovering = it }
                                    .background(
                                        when {
                                            isHovering -> Color(0xFF565656)
                                            option == selectedOption && !isHovering ->
                                                MapleColorPalette.control
                                            else -> Color.Transparent
                                        },
                                        RoundedCornerShape(12.dp),
                                    )
                                    .padding(horizontal = 15.dp, vertical = 0.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = option,
                                color = MapleColorPalette.text,
                                style =
                                    TextStyle(
                                        fontFamily = FontFactory.comfortaaLight,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp,
                                    ),
                                modifier =
                                    Modifier.fillMaxWidth().offset(x = (-10).dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownMenuContent(
    expandedState: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    scrollState: ScrollState,
    elevation: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Menu open/close animation.
    val transition = rememberTransition(expandedState, "DropdownMenu")

    val scale by
        transition.animateFloat(
            transitionSpec = {
                if (false isTransitioningTo true) {
                    // Dismissed to expanded
                    tween(durationMillis = 120, easing = LinearOutSlowInEasing)
                } else {
                    // Expanded to dismissed.
                    tween(durationMillis = 1, delayMillis = 74)
                }
            },
        ) {
            if (it) {
                // Menu is expanded.
                1f
            } else {
                // Menu is dismissed.
                0.8f
            }
        }

    val alpha by
        transition.animateFloat(
            transitionSpec = {
                if (false isTransitioningTo true) {
                    // Dismissed to expanded
                    tween(durationMillis = 30)
                } else {
                    // Expanded to dismissed.
                    tween(durationMillis = 75)
                }
            },
        ) {
            if (it) {
                // Menu is expanded.
                1f
            } else {
                // Menu is dismissed.
                0f
            }
        }

    Box(
        modifier =
            Modifier
                .shadow(
                    elevation = if (elevation) 2.dp else 0.dp,
                    shape = RoundedCornerShape(12.dp),
                )
                .clip(RoundedCornerShape(12.dp))
                .background(MapleColorPalette.secondaryControl, RoundedCornerShape(12.dp))
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    transformOrigin = transformOriginState.value
                },
    ) {
        Column(
            modifier =
                modifier
                    .width(IntrinsicSize.Max)
                    .verticalScroll(scrollState),
        ) {
            Spacer(Modifier.height(5.dp))
            content()
            Spacer(Modifier.height(5.dp))
        }
    }
}

private fun calculateTransformOrigin(
    parentBounds: IntRect,
    menuBounds: IntRect,
): TransformOrigin {
    val calculatePivot: (Int, Int, Int, Int, Int) -> Float =
        { start, end, menuStart, menuEnd, menuSize ->
            when {
                menuStart >= end -> 0f
                menuEnd <= start -> 1f
                menuSize == 0 -> 0f
                else -> {
                    val intersectionCenter = (max(start, menuStart) + min(end, menuEnd)) / 2
                    (intersectionCenter - menuStart).toFloat() / menuSize
                }
            }
        }

    val pivotX =
        calculatePivot(
            parentBounds.left,
            parentBounds.right,
            menuBounds.left,
            menuBounds.right,
            menuBounds.width,
        )
    val pivotY =
        calculatePivot(
            parentBounds.top,
            parentBounds.bottom,
            menuBounds.top,
            menuBounds.bottom,
            menuBounds.height,
        )

    return TransformOrigin(pivotX, pivotY)
}

@Immutable
private data class DropdownMenuPositionProvider(
    val contentOffset: DpOffset,
    val density: Density,
    val onPositionCalculated: (IntRect, IntRect) -> Unit = { _, _ -> },
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        // The min margin above and below the menu, relative to the screen.
        val verticalMargin = with(density) { 120.dp.roundToPx() }
        // The content offset specified using the dropdown offset parameter.
        val contentOffsetX =
            with(density) {
                contentOffset.x.roundToPx() *
                    (if (layoutDirection == LayoutDirection.Ltr) 1 else -1)
            }
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

        // Compute horizontal position.
        val leftToAnchorLeft = anchorBounds.left + contentOffsetX
        val rightToAnchorRight = anchorBounds.right - popupContentSize.width + contentOffsetX
        val rightToWindowRight = windowSize.width - popupContentSize.width
        val leftToWindowLeft = 0
        val x =
            if (layoutDirection == LayoutDirection.Ltr) {
                sequenceOf(
                    leftToAnchorLeft,
                    rightToAnchorRight,
                    // If the anchor gets outside the window on the left, we want to position
                    // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
                    if (anchorBounds.left >= 0) rightToWindowRight else leftToWindowLeft,
                )
            } else {
                sequenceOf(
                    rightToAnchorRight,
                    leftToAnchorLeft,
                    // If the anchor gets outside the window on the right, we want to position
                    // toDisplayRight for proximity to the anchor. Otherwise, toDisplayLeft.
                    if (anchorBounds.right <= windowSize.width) {
                        leftToWindowLeft
                    } else {
                        rightToWindowRight
                    },
                )
            }
                .firstOrNull { it >= 0 && it + popupContentSize.width <= windowSize.width }
                ?: rightToAnchorRight

        // Compute vertical position.
        val topToAnchorBottom = maxOf(anchorBounds.bottom + contentOffsetY, verticalMargin)
        val bottomToAnchorTop = anchorBounds.top - popupContentSize.height + contentOffsetY
        val centerToAnchorTop = anchorBounds.top - popupContentSize.height / 2 + contentOffsetY
        val bottomToWindowBottom = windowSize.height - popupContentSize.height - verticalMargin
        val y =
            sequenceOf(
                topToAnchorBottom, bottomToAnchorTop, centerToAnchorTop, bottomToWindowBottom,
            )
                .firstOrNull {
                    it >= verticalMargin &&
                        it + popupContentSize.height <= windowSize.height - verticalMargin
                } ?: bottomToAnchorTop

        onPositionCalculated(
            anchorBounds,
            IntRect(x, y, x + popupContentSize.width, y + popupContentSize.height),
        )
        return IntOffset(x, y)
    }
}
