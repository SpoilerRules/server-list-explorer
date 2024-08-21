package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.presentation.MapleContextMenuRepresentation

@Composable
fun MapleTextField(
    modifier: Modifier = Modifier,
    placeholder: String,
    onValueChange: (String) -> Unit,
) = MapleTextField(
    modifier,
    28.dp,
    placeholder,
    true,
    {},
    onValueChange,
)

@Composable
fun MapleTextField(
    modifier: Modifier = Modifier,
    height: Dp = 28.dp,
    placeholder: String,
    wipePlaceholderOnInteraction: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
) {
    var text by remember { mutableStateOf(if (wipePlaceholderOnInteraction) "" else placeholder) }
    var isPlaceholderVisible by remember {
        mutableStateOf(text.isEmpty() && !wipePlaceholderOnInteraction)
    }

    val contextMenuRepresentation = remember { MapleContextMenuRepresentation(null, 1) }

    CompositionLocalProvider(
        LocalTextSelectionColors provides
            TextSelectionColors(
                backgroundColor = MapleColorPalette.accent,
                handleColor = MapleColorPalette.accent,
            ),
        LocalContextMenuRepresentation provides contextMenuRepresentation,
    ) {
        Box(
            modifier =
                modifier
                    .height(height)
                    .background(
                        color = MapleColorPalette.control,
                        shape = RoundedCornerShape(12.dp),
                    ),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                    onValueChange(text)
                    isPlaceholderVisible = text.isEmpty() && wipePlaceholderOnInteraction
                },
                singleLine = true,
                textStyle =
                    TextStyle(
                        color = MapleColorPalette.text,
                        fontFamily = FontFactory.comfortaaLight,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                    ),
                cursorBrush = SolidColor(MapleColorPalette.text),
                modifier =
                    modifier.padding(5.dp).onFocusChanged { focusState ->
                        onFocusChange.invoke(focusState.isFocused)
                        if (focusState.isFocused) {
                            if (wipePlaceholderOnInteraction) {
                                if (isPlaceholderVisible) {
                                    text = ""
                                    isPlaceholderVisible = false
                                }
                            }
                        } else {
                            isPlaceholderVisible = text.isEmpty() && wipePlaceholderOnInteraction
                        }
                    },
            )

            if (isPlaceholderVisible && wipePlaceholderOnInteraction) {
                Text(
                    text = placeholder,
                    color = MapleColorPalette.fadedText,
                    style =
                        TextStyle(
                            fontFamily = FontFactory.comfortaaLight,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                        ),
                    modifier = Modifier.padding(5.dp),
                )
            }
        }
    }
}
