package com.spoiligaming.explorer.ui.screens.home

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.StartupCoordinator
import com.spoiligaming.explorer.server.ContemporaryServerEntryListData
import com.spoiligaming.explorer.server.ServerDataDelegate
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.presentation.MapleContextMenuRepresentation
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.explorer.ui.widgets.MapleButton
import com.spoiligaming.explorer.ui.widgets.MapleButtonHeight
import com.spoiligaming.explorer.ui.widgets.MapleButtonWidth
import com.spoiligaming.explorer.utils.ClipboardUtility
import com.spoiligaming.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

@Composable
fun ButtonContainer() {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(95.dp)
                .background(MapleColorPalette.quaternary, RoundedCornerShape(16.dp)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(7.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val buttonData =
                listOf(
                    "Load Server File" to
                        {
                            DialogController.showServerFilePickerDialog(true)
                        },
                    "Wipe Server List" to { DialogController.showWipeConfirmationDialog() },
                    "Refresh Icons" to { refreshIcons() },
                    "Reload Server File" to { StartupCoordinator.retryLoad() },
                    "Force Encode" to
                        {
                            DialogController.showForceEncodeConfirmationDialog()
                        },
                )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                buttonData.forEach { (text, action) ->
                    MapleButton(
                        width = MapleButtonWidth.PROFILE.width,
                        height = MapleButtonHeight.ORIGINAL.height,
                        backgroundColor = MapleColorPalette.control,
                        text = text,
                        textColor = MapleColorPalette.text,
                        fontSize = 15.sp,
                        padding = null,
                        onClick = { action() },
                    )
                }
            }

            Row {
                MapleButton(
                    modifier = Modifier.fillMaxWidth(),
                    width = null,
                    height = MapleButtonHeight.ORIGINAL.height,
                    backgroundColor = MapleColorPalette.control,
                    text = "Create Server Entry",
                    textColor = MapleColorPalette.text,
                    fontSize = 15.sp,
                    onClick = { DialogController.showServerEntryCreationDialog() },
                )
            }
            ServerPathText()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServerPathText() {
    val file = remember { File(ConfigurationHandler.getInstance().generalSettings.serverFilePath) }
    var isHovered by remember { mutableStateOf(false) }

    val contextMenuState = remember { ContextMenuState() }
    val contextMenuRepresentation = remember { MapleContextMenuRepresentation(null, 0) }

    val pathTextColor =
        if (isHovered && contextMenuState.status is ContextMenuState.Status.Closed) {
            MapleColorPalette.accent.copy(
                red = MapleColorPalette.accent.red * 1.1f,
                blue = MapleColorPalette.accent.blue * 1.1f,
                green = MapleColorPalette.accent.green * 1.1f,
            )
        } else {
            MapleColorPalette.accent
        }

    val openFolder = {
        runCatching {
            if (file.exists() &&
                Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.OPEN)
            ) {
                Desktop.getDesktop().open(file.parentFile)
            } else {
                Logger.printWarning("Cannot open folder: $file")
            }
        }
            .onFailure { Logger.printWarning("Error opening folder: ${it.message}") }
    }

    CompositionLocalProvider(LocalContextMenuRepresentation provides contextMenuRepresentation) {
        ContextMenuArea(
            items = {
                listOf(
                    ContextMenuItem("Navigate to Directory") { openFolder() },
                    ContextMenuItem("Copy File Path") { ClipboardUtility.copy(file.path) },
                )
            },
            state = contextMenuState,
        ) {
            Row {
                Text(
                    text = "Server File Location: ",
                    color = MapleColorPalette.fadedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        TextStyle(
                            fontFamily = FontFactory.comfortaaRegular,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        ),
                )
                Text(
                    text = file.path,
                    color = pathTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        TextStyle(
                            fontFamily = FontFactory.comfortaaRegular,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                        ),
                    modifier =
                        Modifier.onHover { isHovered = it }
                            .onClick(
                                onDoubleClick = { ClipboardUtility.copy(file.path) },
                                onClick = { openFolder() },
                            ),
                )
            }
        }
    }
}

private fun refreshIcons() {
    DialogController.showIconRefreshStartedDialog()

    val totalAddresses = ContemporaryServerEntryListData.serverAddressList.size
    val startTime = System.currentTimeMillis()

    CoroutineScope(Dispatchers.IO).launch {
        var addressesProcessed = 0
        var failedAttempts = 0

        ContemporaryServerEntryListData.serverAddressList.forEachIndexed { index, serverAddress ->
            val result = runCatching { ServerDataDelegate.getServerIcon(serverAddress) }.getOrNull()

            if (result != null) {
                ServerFileHandler.modifyIcon(index, result)
            } else {
                failedAttempts++
            }

            addressesProcessed++
        }

        val timeTaken = System.currentTimeMillis() - startTime
        val successRatio = totalAddresses to (addressesProcessed - failedAttempts)

        withContext(Dispatchers.Default) {
            DialogController.showIconRefreshCompletedDialog(timeTaken, successRatio)
        }
    }
}
