package com.spoiligaming.explorer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.server.ContemporaryServerEntryListData
import com.spoiligaming.explorer.server.ServerDataDelegate
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.dialogs.ValueReplacementType
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.explorer.ui.widgets.DisabledMapleButton
import com.spoiligaming.explorer.ui.widgets.MapleButton
import com.spoiligaming.explorer.ui.widgets.MapleTooltip
import com.spoiligaming.explorer.ui.widgets.MergedInfoText
import com.spoiligaming.explorer.ui.widgets.ModifiableMergedInfoText
import com.spoiligaming.explorer.utils.ClipboardUtility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ServerInformationView(
    serverName: String,
    serverAddress: String,
    serverIcon: ImageBitmap?,
    serverIconRaw: String?,
    serverPositionInList: Int,
) {
    var serverInfo by remember { mutableStateOf<ServerDataDelegate.ServerData?>(null) }
    var isServerInfoLoading by remember { mutableStateOf(true) }
    var hasFailedToLoad by remember { mutableStateOf(false) }

    val fetchServerInfo: (Boolean) -> Unit = { forceRefresh ->
        hasFailedToLoad = false
        isServerInfoLoading = true

        CoroutineScope(Dispatchers.IO).launch {
            ServerDataDelegate.getServerData(serverAddress, forceRefresh)
                .also { result ->
                    if (result is ServerDataDelegate.ServerDelegateResult.Success) {
                        serverInfo = result.serverData
                        hasFailedToLoad = false
                    } else {
                        hasFailedToLoad = true
                    }
                }
                .run { isServerInfoLoading = false }
        }
    }

    LaunchedEffect(serverAddress) { fetchServerInfo(false) }

    Box(Modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp),
                color = MapleColorPalette.quaternary,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                    Box(Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ModifiableMergedInfoText(
                                    firstText = "Name: ",
                                    secondText = serverName,
                                    selectableSecondaryText = true,
                                    isChangeTextDisabled = false,
                                    onClick = {
                                        DialogController.showValueReplacementDialog(
                                            serverName = serverName,
                                            serverAddress = serverAddress,
                                            serverIcon = serverIcon,
                                            serverIconRaw = serverIconRaw,
                                            serverPositionInList = serverPositionInList,
                                            type = ValueReplacementType.NAME,
                                        ) { newName ->
                                            ContemporaryServerEntryListData
                                                .updateServerName(
                                                    serverPositionInList,
                                                    newName,
                                                )
                                        }
                                    },
                                )
                                ModifiableMergedInfoText(
                                    firstText = "Address: ",
                                    secondText = serverAddress,
                                    selectableSecondaryText = true,
                                    isChangeTextDisabled = false,
                                    onClick = {
                                        DialogController.showValueReplacementDialog(
                                            serverName = serverName,
                                            serverAddress = serverAddress,
                                            serverIcon = serverIcon,
                                            serverIconRaw = serverIconRaw,
                                            serverPositionInList = serverPositionInList,
                                            type = ValueReplacementType.ADDRESS,
                                        ) { newAddress ->
                                            ContemporaryServerEntryListData
                                                .updateServerAddress(
                                                    serverPositionInList,
                                                    newAddress,
                                                )
                                        }
                                    },
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                MapleButton(
                                    Modifier.fillMaxWidth().height(26.dp),
                                    text = "Erase Icon",
                                ) {
                                    ServerFileHandler.deleteServerIcon(serverPositionInList)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    MapleButton(
                                        Modifier.weight(1f),
                                        text = "Copy Icon as Image",
                                    ) {
                                        ClipboardUtility.copyIconAsImage(serverIconRaw)
                                    }
                                    MapleButton(
                                        Modifier.weight(1f),
                                        text = "Copy Icon as Base64",
                                    ) {
                                        ClipboardUtility.copy(serverIconRaw)
                                    }
                                }
                                DisabledMapleButton(
                                    Modifier.fillMaxWidth().height(26.dp),
                                    text = "Move to Specified Index in List",
                                    hoverTooltipText =
                                        "This feature is under consideration for future updates." +
                                            "\n\nIts availability will depend on project growth and user demand.",
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                modifier =
                    Modifier.fillMaxSize().padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                color = MapleColorPalette.quaternary,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
            ) {
                Box(
                    Modifier.fillMaxWidth().fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Box(Modifier.padding(10.dp)) {
                        when {
                            hasFailedToLoad -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    MapleButton(
                                        modifier = Modifier.fillMaxWidth().height(26.dp),
                                        text = "Refresh",
                                    ) {
                                        fetchServerInfo(true)
                                    }
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text =
                                                "Unable to retrieve server information." +
                                                    "\nThe server may be offline.",
                                            color = MapleColorPalette.fadedText,
                                            fontSize = 16.sp,
                                            maxLines = 2,
                                            fontFamily = FontFactory.comfortaaMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                            isServerInfoLoading -> {
                                Box(
                                    Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        color = MapleColorPalette.accent,
                                    )
                                }
                            }
                            else -> {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        MapleButton(
                                            Modifier.weight(1f).height(26.dp),
                                            text = "Refresh",
                                        ) {
                                            fetchServerInfo(true)
                                        }
                                        DisabledMapleButton(
                                            Modifier.weight(1f).height(26.dp),
                                            text = "View MOTD",
                                            hoverTooltipText =
                                                "This feature is under consideration for future updates." +
                                                    "\n\nIts availability will depend on project growth and user demand.",
                                        )
                                        MapleButton(
                                            modifier = Modifier.weight(1f).height(26.dp),
                                            text = "Refresh Icon",
                                        ) {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                val result =
                                                    ServerDataDelegate.getServerIcon(
                                                        serverAddress,
                                                    )
                                                withContext(Dispatchers.Default) {
                                                    ContemporaryServerEntryListData
                                                        .updateServerIcon(
                                                            serverPositionInList,
                                                            result,
                                                        )
                                                }
                                            }
                                        }
                                    }
                                    DisabledMapleButton(
                                        Modifier.fillMaxWidth().height(26.dp),
                                        text = "Classic View",
                                        hoverTooltipText =
                                            "This feature is under consideration for future updates." +
                                                "\n\nIts availability will depend on project growth and user demand.",
                                    )

                                    serverInfo?.let {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Box(Modifier.fillMaxWidth()) {
                                                MergedInfoText(
                                                    "Ping: ",
                                                    19.sp,
                                                    formatPing(it.ping),
                                                    19.sp,
                                                    MapleColorPalette.fadedText,
                                                )
                                            }
                                            MapleTooltip(
                                                tooltip =
                                                    "${it.normalizedServerVersion}\n\nDetected protocol: ${it.minimumServerProtocol}",
                                                tooltipDelay = 1000,
                                            ) {
                                                MergedInfoText(
                                                    "Version: ",
                                                    19.sp,
                                                    it.normalizedServerVersion,
                                                    19.sp,
                                                    MapleColorPalette.fadedText,
                                                )
                                            }

                                            MapleTooltip(
                                                tooltip =
                                                    "Total players: ${formatNumberWithCommas(
                                                        it.onlinePlayerCount,
                                                    )} / ${
                                                        formatNumberWithCommas(
                                                            it.maxPlayerCount,
                                                        )
                                                    }",
                                                tooltipDelay = 1000,
                                            ) {
                                                MergedInfoText(
                                                    "Player Count: ",
                                                    19.sp,
                                                    "${formatNumberWithCommas(
                                                        it.onlinePlayerCount,
                                                    )} / ${
                                                        formatNumberWithCommas(
                                                            it.maxPlayerCount,
                                                        )
                                                    }",
                                                    19.sp,
                                                    MapleColorPalette.fadedText,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatPing(milliseconds: String) =
    milliseconds.toLongOrNull()?.let { millis ->
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        "${if (hours > 0) "${hours}h" else ""}${if (minutes > 0) "${minutes % 60}m" else ""}${if (seconds > 0) "${seconds % 60}s" else "$millis milliseconds"}"
    } ?: "Unknown"

private fun formatNumberWithCommas(numberString: String) =
    numberString.toLongOrNull()?.let { number ->
        NumberFormat.getNumberInstance(Locale.US).format(number)
    } ?: numberString
