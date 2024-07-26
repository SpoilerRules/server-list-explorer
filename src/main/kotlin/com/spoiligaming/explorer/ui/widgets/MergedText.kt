package com.spoiligaming.explorer.ui.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.extensions.baseHoverColor
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.fonts.FontFactory

// hot mess. please refactor this if you're a developer

@Composable
fun MergedText(
    firstText: String,
    firstTextColor: Color,
    firstTextWeight: FontWeight,
    secondText: String,
    secondTextColor: Color,
    secondTextWeight: FontWeight,
) {
    Row {
        Text(
            text = firstText,
            color = firstTextColor,
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaRegular,
                    fontWeight = firstTextWeight,
                    fontSize = 16.sp,
                ),
        )
        Text(
            text = secondText,
            color = secondTextColor,
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaRegular,
                    fontWeight = secondTextWeight,
                    fontSize = 16.sp,
                ),
        )
    }
}

@Composable
fun MergedText(
    firstText: String,
    firstTextColor: Color,
    firstTextFontFamily: FontFamily = FontFactory.comfortaaRegular,
    firstTextWeight: FontWeight,
    secondText: String,
    secondTextColor: Color,
    secondTextFontFamily: FontFamily = FontFactory.comfortaaRegular,
    secondTextWeight: FontWeight,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        Text(
            text = firstText,
            color = firstTextColor,
            style =
                TextStyle(
                    fontFamily = firstTextFontFamily,
                    fontWeight = firstTextWeight,
                    fontSize = 16.sp,
                ),
        )
        Text(
            text = secondText,
            color = secondTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style =
                TextStyle(
                    fontFamily = secondTextFontFamily,
                    fontWeight = secondTextWeight,
                    fontSize = 16.sp,
                ),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MergedText(
    firstText: String,
    firstTextColor: Color,
    firstTextSize: TextUnit = 16.sp,
    firstTextFont: FontFamily,
    firstTextWeight: FontWeight,
    secondText: String,
    secondTextColor: Color,
    secondTextSize: TextUnit = 16.sp,
    secondTextFont: FontFamily,
    secondTextWeight: FontWeight,
    onSecondTextClick: () -> Unit = {},
) {
    var isHovered by remember { mutableStateOf(false) }
    val mutableInteractionSource = remember { MutableInteractionSource() }
    Row {
        Text(
            text = firstText,
            color = firstTextColor,
            style =
                TextStyle(
                    fontFamily = firstTextFont,
                    fontWeight = firstTextWeight,
                    fontSize = firstTextSize,
                ),
        )
        Text(
            text = secondText,
            color = secondTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style =
                TextStyle(
                    fontFamily = secondTextFont,
                    fontWeight = secondTextWeight,
                    fontSize = secondTextSize,
                ),
            modifier =
                Modifier.onHover { isHovered = it }
                    .pointerHoverIcon(PointerIcon.Hand)
                    .onClick { onSecondTextClick() },
        )
    }
}

@Composable
fun MergedInfoText(
    firstText: String,
    secondText: String,
    secondTextColor: Color,
    rowModifier: Modifier = Modifier,
) {
    Row(rowModifier) {
        Text(
            text = firstText,
            color = MapleColorPalette.fadedText,
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                ),
        )
        Text(
            text = secondText,
            color = secondTextColor,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaRegular,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                ),
        )
    }
}

@Composable
fun MergedInfoText(
    firstText: String,
    firstTextSize: TextUnit,
    secondText: String,
    secondTextSize: TextUnit,
    secondTextColor: Color,
) {
    Row {
        Text(
            text = firstText,
            color = MapleColorPalette.fadedText,
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = firstTextSize,
                ),
        )
        Text(
            text = secondText,
            color = secondTextColor,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaRegular,
                    fontWeight = FontWeight.Normal,
                    fontSize = secondTextSize,
                ),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModifiableMergedInfoText(
    firstText: String,
    secondText: String,
    secondTextModifier: Modifier = Modifier,
    selectableSecondaryText: Boolean,
    isChangeTextDisabled: Boolean = false,
    customChangeTextString: String = "Change",
    onClick: () -> Unit,
    offset: Dp = 10.dp,
) {
    var isChangeTextHovered by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    val shadowBlurRadius by
        animateFloatAsState(
            targetValue = if (isChangeTextHovered) 0.5f else 0f,
            animationSpec = tween(durationMillis = 300),
        )

    var changeTextColor by remember { mutableStateOf(MapleColorPalette.accent) }

    val secondaryText: @Composable () -> Unit = {
        Text(
            text = secondText,
            color = MapleColorPalette.fadedText,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaRegular,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                ),
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(offset)) {
        Row {
            Text(
                text = firstText,
                color = MapleColorPalette.fadedText,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    ),
            )
            if (selectableSecondaryText) {
                SelectionContainer { secondaryText() }
            } else {
                secondaryText()
            }
        }
        Text(
            text = customChangeTextString,
            color = if (isChangeTextDisabled) Color.Gray else changeTextColor,
            style =
                TextStyle(
                    fontFamily = FontFactory.comfortaaRegular,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                ),
            modifier =
                secondTextModifier
                    .onHover { isChangeTextHovered = it }
                    .baseHoverColor(MapleColorPalette.accent) {
                        changeTextColor =
                            if (isChangeTextHovered && !isChangeTextDisabled) {
                                it
                            } else {
                                MapleColorPalette.accent
                            }
                    }
                    .pointerHoverIcon(
                        if (isChangeTextDisabled) PointerIcon.Default else PointerIcon.Hand,
                    )
                    .onClick { onClick() }
                    .offset(y = with(density) { (1.4).sp.toDp() }),
        )
    }
}

@Composable
fun MergedText(
    firstText: String,
    firstTextColor: Color,
    firstTextWeight: FontWeight,
    firstTextOffset: Dp = 2.49988774.dp,
    content: @Composable () -> Unit,
) {
    Box(contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row {
                content()

                Spacer(modifier = Modifier.width(7.dp))

                Text(
                    modifier = Modifier.offset(x = firstTextOffset),
                    text = firstText,
                    color = firstTextColor,
                    style =
                        TextStyle(
                            fontFamily = FontFactory.comfortaaLight,
                            fontWeight = firstTextWeight,
                            fontSize = 15.sp,
                        ),
                )
            }
        }
    }
}

@Composable
fun MergedText(
    firstText: String,
    firstTextColor: Color,
    firstTextWeight: FontWeight,
    content: @Composable () -> Unit,
) {
    Box(contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row {
                content()

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = firstText,
                    color = firstTextColor,
                    style =
                        TextStyle(
                            fontFamily = FontFactory.comfortaaLight,
                            fontWeight = firstTextWeight,
                            fontSize = 15.sp,
                        ),
                    modifier = Modifier.offset(y = 3.5.dp),
                )
            }
        }
    }
}
