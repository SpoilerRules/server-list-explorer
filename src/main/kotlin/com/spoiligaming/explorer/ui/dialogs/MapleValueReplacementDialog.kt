package com.spoiligaming.explorer.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.dialogs.dialog.MapleDialogBase
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.explorer.ui.widgets.MapleButton
import com.spoiligaming.explorer.ui.widgets.MapleButtonHeight
import com.spoiligaming.explorer.ui.widgets.MapleTextField
import com.spoiligaming.explorer.ui.widgets.MergedText
import com.spoiligaming.explorer.ui.widgets.ServerIconImage

private val encodedStringRegex = Regex(".*iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAAC.*")

@Composable
fun MapleServerEntryValueReplacementDialog(
    serverName: String,
    serverAddress: String,
    serverIcon: ImageBitmap?,
    type: ValueReplacementType,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember { mutableStateOf("") }
    val placeholderValue =
        when (type) {
            ValueReplacementType.NAME -> serverName
            ValueReplacementType.ADDRESS -> serverAddress
            ValueReplacementType.ICON -> "aHR0cHM6Ly95b3V0dS5iZS9paWsyNXdxSXVGbw=="
        }

    MapleDialogBase(
        false,
        3,
        true,
        onDismiss,
    ) {
        Box(Modifier.widthIn(400.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        Modifier.size(48.dp)
                            .background(MapleColorPalette.tertiary, RoundedCornerShape(10.dp)),
                    ) {
                        ServerIconImage(serverIcon, serverName)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Server Information",
                            color = MapleColorPalette.fadedText,
                            style =
                                TextStyle(
                                    fontFamily = FontFactory.comfortaaMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp,
                                ),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            MergedText(
                                "${if (type == ValueReplacementType.NAME) "Current" else "Server"} Name: ",
                                MapleColorPalette.fadedText,
                                FontFactory.comfortaaMedium,
                                FontWeight.Bold,
                                serverName,
                                MapleColorPalette.fadedText,
                                FontFactory.comfortaaRegular,
                                FontWeight.Normal,
                            )
                            MergedText(
                                "${if (type == ValueReplacementType.ADDRESS) "Current" else "Server"} Address: ",
                                MapleColorPalette.fadedText,
                                FontFactory.comfortaaMedium,
                                FontWeight.Bold,
                                serverAddress,
                                MapleColorPalette.fadedText,
                                FontFactory.comfortaaRegular,
                                FontWeight.Normal,
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text =
                            when (type) {
                                ValueReplacementType.NAME -> "New Name"
                                ValueReplacementType.ADDRESS -> "New Address"
                                ValueReplacementType.ICON -> "Icon (Base64 Encoded)"
                            },
                        color = MapleColorPalette.text,
                        style =
                            TextStyle(
                                fontFamily = FontFactory.comfortaaLight,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                            ),
                        textAlign = TextAlign.Center,
                    )
                    MapleTextField(Modifier.fillMaxWidth(), placeholderValue) { value = it }
                }
                MapleButton(
                    modifier = Modifier.fillMaxWidth(),
                    width = null,
                    height = MapleButtonHeight.DIALOG.height,
                    text = "Confirm Modification",
                ) {
                    if (value.isNotBlank()) {
                        when (type) {
                            ValueReplacementType.NAME,
                            ValueReplacementType.ADDRESS,
                            -> onConfirm(value)
                            ValueReplacementType.ICON -> {
                                if (encodedStringRegex.containsMatchIn(value)) {
                                    onConfirm(value)
                                } else {
                                    DialogController.showIconValueDecodeFailureDialog()
                                    onDismiss()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class ValueReplacementType {
    NAME,
    ADDRESS,
    ICON,
}
