package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.MapleColorPalette

@Composable
fun MapleSingleValueSlider() {
    var sliderValue by remember { mutableStateOf(24f) }
    val sliderValueRange by remember { mutableStateOf(0f..24f) }
    var textValue by remember { mutableStateOf("%.0f".format(sliderValue)) }
    var lastValidValue by remember { mutableStateOf(sliderValue) }

    Box(
        modifier = Modifier.width(254.dp).height(26.dp).background(Color.White).offset(x = (-4).dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue
                    textValue = "%.0f".format(newValue)
                },
                valueRange = 0f..24f,
                colors =
                    SliderDefaults.colors(
                        thumbColor = MapleColorPalette.secondaryControl,
                        activeTrackColor = MapleColorPalette.control,
                        inactiveTrackColor = MapleColorPalette.tertiaryControl,
                    ),
                modifier = Modifier.weight(1f),
                size = DpSize(146.dp, 26.dp),
                thumbSize = 26.dp,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MapleTextField(
                    Modifier.width(
                        40.dp,
                    ).clip(
                        RoundedCornerShape(12.dp),
                    ).background(MapleColorPalette.control, RoundedCornerShape(12.dp)),
                    26.dp,
                    textValue,
                    false,
                    onFocusChange = { isFocused ->
                        if (!isFocused) {
                            if (textValue.isNotBlank()) {
                                val newValue = textValue.toFloatOrNull()
                                if (newValue != null) {
                                    sliderValue = newValue
                                    lastValidValue = newValue
                                } else {
                                    textValue = "%.0f".format(lastValidValue)
                                }
                            } else {
                                textValue = "%.0f".format(lastValidValue)
                            }
                        }
                    },
                    onValueChange = { newValue ->
                        textValue = newValue
                        if (newValue.isNotBlank()) {
                            val newSliderValue = newValue.toFloatOrNull()
                            if (newSliderValue != null) {
                                sliderValue = newSliderValue
                                lastValidValue = newSliderValue
                            }
                        }
                    },
                )

                MapleButton(Modifier.width(26.dp).height(26.dp), text = "-") {
                    if (sliderValue > sliderValueRange.start) {
                        sliderValue -= 1f
                        textValue = "%.0f".format(sliderValue)
                    }
                }

                MapleButton(Modifier.width(26.dp).height(26.dp), text = "+") {
                    if (sliderValue < sliderValueRange.endInclusive) {
                        sliderValue += 1f
                        textValue = "%.0f".format(sliderValue)
                    }
                }
            }
        }
    }
}
