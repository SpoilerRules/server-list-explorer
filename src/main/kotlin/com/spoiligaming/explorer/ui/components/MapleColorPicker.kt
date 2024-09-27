package com.spoiligaming.explorer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.widgets.MapleButton
import com.spoiligaming.explorer.ui.widgets.MapleButtonHeight
import com.spoiligaming.explorer.utils.toHex
import com.spoiligaming.explorer.utils.toHexRgba
import com.spoiligaming.explorer.utils.toNumericRgba
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MapleColorPicker(
    settingName: String,
    initialColor: Color,
    defaultColor: Color,
    onValueUpdate: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    var currentColor by remember { mutableStateOf(initialColor) }

    Popup(alignment = Alignment.Center, onDismissRequest = onDismiss) {
        Box(
            modifier =
                Modifier.width(300.dp)
                    .height(IntrinsicSize.Min)
                    .background(MapleColorPalette.menu, shape = RoundedCornerShape(18.dp)),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    Modifier.fillMaxWidth().padding(top = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = settingName,
                        color = MapleColorPalette.text,
                        style =
                            TextStyle(
                                fontFamily = FontFactory.comfortaaRegular,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                            ),
                    )
                }
                ClassicColorPicker(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp)
                            .weight(1f)
                            .padding(horizontal = 5.dp)
                            .align(Alignment.CenterHorizontally),
                    color = currentColor,
                    onColorChanged = { newValue ->
                        currentColor = newValue.toColor()
                        onValueUpdate(newValue.toColor())
                    },
                )
                ColorInfoSection(currentColor, defaultColor, onValueUpdate, onDismiss)
            }
        }
    }
}

@Composable
private fun ColorInfoSection(
    currentColor: Color,
    defaultColor: Color,
    onValueUpdate: (Color) -> Unit,
    onDismiss: () -> Unit,
) = Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
    ColorDetails(currentColor)
    ResetButton(defaultColor, onValueUpdate, onDismiss)
}

@Composable
private fun ColorDetails(color: Color) =
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)) {
        ColorDetailRow(label = "Hex: ", value = "#${color.toHex()}")
        ColorDetailRow(label = "HexRGBA: ", value = color.toHexRgba())
        ColorDetailRow(label = "NumericRGBA: ", value = color.toNumericRgba())
    }

@Composable
private fun ColorDetailRow(
    label: String,
    value: String,
) = Row {
    Text(
        text = label,
        color = MapleColorPalette.fadedText,
        style =
            TextStyle(
                fontFamily = FontFactory.comfortaaRegular,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            ),
    )
    Text(
        text = value,
        color = MapleColorPalette.text,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style =
            TextStyle(
                fontFamily = FontFactory.comfortaaBold,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
            ),
    )
}

@Composable
private fun ResetButton(
    defaultColor: Color,
    onValueUpdate: (Color) -> Unit,
    onDismiss: () -> Unit,
) = Box(
    modifier = Modifier.fillMaxWidth().padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
    contentAlignment = Alignment.Center,
) {
    MapleButton(
        modifier = Modifier.fillMaxWidth(),
        width = null,
        height = MapleButtonHeight.ORIGINAL.height,
        backgroundColor = MapleColorPalette.control,
        text = "Reset",
        textColor = MapleColorPalette.text,
        fontSize = 15.sp,
    ) {
        onValueUpdate(defaultColor)
        CoroutineScope(Dispatchers.Default).launch {
            delay(850)
            onDismiss()
        }
    }
}
