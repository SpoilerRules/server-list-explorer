package com.spoiligaming.explorer.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.dialogs.dialog.MapleDialogBase
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.widgets.MapleButton
import com.spoiligaming.explorer.ui.widgets.MapleButtonHeight
import com.spoiligaming.explorer.ui.widgets.MapleButtonWidth
import com.spoiligaming.explorer.ui.widgets.MapleTextField

@Composable
fun MapleServerEntryCreationDialog(
    onAccept: (Triple<String, String, String?>) -> Unit,
    onDismiss: () -> Unit,
) {
    MapleDialogBase(
        0,
        true,
        onDismiss,
    ) {
        var serverCreationData by remember {
            mutableStateOf(Triple<String, String, String?>("", "", null))
        }

        Box(Modifier.widthIn(max = 400.dp, min = 400.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Name",
                        color = MapleColorPalette.text,
                        style =
                            TextStyle(
                                fontFamily = FontFactory.comfortaaLight,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                            ),
                        textAlign = TextAlign.Center,
                    )
                    MapleTextField(Modifier.fillMaxWidth(), "A Minecraft Server") { newValue ->
                        serverCreationData =
                            Triple(newValue, serverCreationData.second, serverCreationData.third)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            Text(
                                text = "Address (IP)",
                                color = MapleColorPalette.text,
                                style =
                                    TextStyle(
                                        fontFamily = FontFactory.comfortaaLight,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp,
                                    ),
                                textAlign = TextAlign.Center,
                            )
                        }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = "Cannot be left empty.",
                                color = MapleColorPalette.accent,
                                style =
                                    TextStyle(
                                        fontFamily = FontFactory.comfortaaRegular,
                                        fontWeight = FontWeight.Thin,
                                        fontSize = 15.sp,
                                    ),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    MapleTextField(Modifier.fillMaxWidth(), "play.awesomeserver.com") { newValue ->
                        serverCreationData =
                            Triple(serverCreationData.first, newValue, serverCreationData.third)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Icon in Base64 Format (Optional)",
                        color = MapleColorPalette.text,
                        style =
                            TextStyle(
                                fontFamily = FontFactory.comfortaaLight,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                            ),
                        textAlign = TextAlign.Center,
                    )
                    MapleTextField(
                        Modifier.fillMaxWidth(),
                        "aHR0cHM6Ly95b3V0dS5iZS94dkZaam81UGdHMA==",
                    ) { newValue ->
                        serverCreationData =
                            Triple(serverCreationData.first, serverCreationData.second, newValue)
                    }
                }

                Box(Modifier.padding(bottom = 74.dp)) {
                    MapleButton(
                        width = MapleButtonWidth.SERVER_ENTRY_CREATION_DIALOG.width,
                        height = MapleButtonHeight.DIALOG.height,
                        text = "Create Server Entry",
                    ) {
                        if (serverCreationData.second.isNotBlank()) {
                            onAccept(serverCreationData)
                        }
                    }
                }
            }
        }
    }
}
