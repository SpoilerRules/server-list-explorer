package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.presentation.MapleContextMenuRepresentation

@Composable
fun MapleSingleValueSlider(
    title: String,
    initialValue: Float,
    sliderValueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    var sliderValue by remember { mutableStateOf(initialValue) }
    var text by remember { mutableStateOf("%.0f".format(sliderValue)) }
    var lastValidValue by remember { mutableStateOf(sliderValue) }

    Box(modifier = Modifier.height(26.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier.width(254.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Slider(
                        value = sliderValue,
                        onValueChange = { newValue ->
                            sliderValue = newValue
                            text = "%.0f".format(newValue)
                            onValueChange(newValue)
                        },
                        valueRange = sliderValueRange,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        CompositionLocalProvider(
                            LocalTextSelectionColors provides
                                TextSelectionColors(
                                    backgroundColor = MapleColorPalette.accent,
                                    handleColor = MapleColorPalette.accent,
                                ),
                            LocalContextMenuRepresentation provides
                                remember {
                                    MapleContextMenuRepresentation(
                                        null,
                                        1,
                                    )
                                },
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .width(40.dp)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            MapleColorPalette.control,
                                            RoundedCornerShape(12.dp),
                                        ),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                val focusRequester = remember { FocusRequester() }
                                BasicTextField(
                                    value = text,
                                    onValueChange = { newText ->
                                        text = newText

                                        val parsedValue = newText.toFloatOrNull()
                                        if (parsedValue != null) {
                                            if (parsedValue in sliderValueRange) {
                                                sliderValue = parsedValue
                                                lastValidValue = parsedValue
                                                onValueChange(
                                                    parsedValue,
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    textStyle =
                                        TextStyle(
                                            color = MapleColorPalette.text,
                                            fontFamily = FontFactory.comfortaaLight,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 15.sp,
                                        ),
                                    keyboardOptions =
                                        KeyboardOptions.Default.copy(
                                            keyboardType = KeyboardType.Number,
                                        ),
                                    cursorBrush = SolidColor(MapleColorPalette.text),
                                    modifier =
                                        Modifier.padding(5.dp)
                                            .focusRequester(focusRequester)
                                            .focusable()
                                            .onKeyEvent {
                                                if (it.key == Key.Enter) {
                                                    focusRequester.requestFocus()
                                                    return@onKeyEvent true
                                                }
                                                false
                                            }
                                            .onFocusChanged { focusState ->
                                                if (!focusState.isFocused) {
                                                    if (text.isEmpty()) {
                                                        text = "%.0f".format(lastValidValue)
                                                    } else {
                                                        val parsedValue = text.toFloatOrNull()
                                                        if (parsedValue != null &&
                                                            parsedValue !in sliderValueRange
                                                        ) {
                                                            text = "%.0f".format(lastValidValue)
                                                        }
                                                    }
                                                }
                                            },
                                )
                            }
                        }

                        MapleButton(Modifier.width(26.dp).height(26.dp), text = "-") {
                            if (sliderValue > sliderValueRange.start) {
                                sliderValue -= 1f
                                text = "%.0f".format(sliderValue)
                                onValueChange(sliderValue)
                            }
                        }

                        MapleButton(Modifier.width(26.dp).height(26.dp), text = "+") {
                            if (sliderValue < sliderValueRange.endInclusive) {
                                sliderValue += 1f
                                text = "%.0f".format(sliderValue)
                                onValueChange(sliderValue)
                            }
                        }
                    }
                }
            }
            Text(
                text = title,
                color = MapleColorPalette.text,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaLight,
                        fontWeight = FontWeight.Light,
                        fontSize = 15.sp,
                    ),
            )
        }
    }
}
