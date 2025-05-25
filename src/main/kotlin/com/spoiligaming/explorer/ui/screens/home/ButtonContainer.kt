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
import com.spoiligaming.explorer.StartupCoordinator
import com.spoiligaming.explorer.server.LiveServerEntryList
import com.spoiligaming.explorer.server.ServerDataDelegate
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.presentation.MapleContextMenuRepresentation
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.explorer.ui.widgets.MapleButton
import com.spoiligaming.explorer.utils.ClipboardUtility
import com.spoiligaming.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop

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
            val listButtonData =
                listOf(
                    "Create Server Entry" to { DialogController.showServerEntryCreationDialog() },
                    "Wipe Server List" to { DialogController.showWipeConfirmationDialog() },
                    //"Sort Server List" to {},
                    "Refresh Icons" to { refreshIcons() },
                )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listButtonData.forEach { (text, action) ->
                    MapleButton(
                        modifier = Modifier.weight(1f),
                        backgroundColor = MapleColorPalette.control,
                        text = text,
                        textColor = MapleColorPalette.text,
                        fontSize = 15.sp,
                        padding = null,
                        onClick = { action() },
                    )
                }
            }

            val fileButtonData =
                listOf(
                    "Load Server File" to
                        {
                            DialogController.showServerFilePickerDialog(true)
                        },
                    "Reload Server File" to { StartupCoordinator.retryLoad() },
                    "Force Encode" to
                        {
                            DialogController.showForceEncodeConfirmationDialog()
                        },
                )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                fileButtonData.forEach { (text, action) ->
                    MapleButton(
                        modifier = Modifier.weight(1f),
                        backgroundColor = MapleColorPalette.control,
                        text = text,
                        textColor = MapleColorPalette.text,
                        fontSize = 15.sp,
                        padding = null,
                        onClick = { action() },
                    )
                }
            }
            ServerPathText()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServerPathText() {
    val file = remember { ServerFileHandler.serverFilePath.toFile() }
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

    val totalAddresses = LiveServerEntryList.serverAddressList.size
    val startTime = System.currentTimeMillis()

    CoroutineScope(Dispatchers.IO).launch {
        var addressesProcessed = 0
        var failedAttempts = 0

        LiveServerEntryList.serverAddressList.forEachIndexed { index, serverAddress ->
            val result = runCatching { ServerDataDelegate.getServerIcon(serverAddress) }.getOrNull()

            if (result != null) {
                ServerFileHandler.modifyIcon(index, result)
            } else {
                failedAttempts++
            }

            addressesProcessed++
        }

        val timeTaken = System.currentTimeMillis() - startTime
        val successRatio = (addressesProcessed - failedAttempts) to totalAddresses

        withContext(Dispatchers.Default) {
            DialogController.showIconRefreshCompletedDialog(timeTaken, successRatio)
        }
    }
}
