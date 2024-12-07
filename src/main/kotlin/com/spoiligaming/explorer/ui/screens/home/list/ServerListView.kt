package com.spoiligaming.explorer.ui.screens.home.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.disableIconIndexing
import com.spoiligaming.explorer.disableServerInfoIndexing
import com.spoiligaming.explorer.server.LiveServerEntryList
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.ui.SettingsViewModel
import com.spoiligaming.explorer.ui.components.ServerElement
import com.spoiligaming.explorer.ui.extensions.asImageBitmap
import com.spoiligaming.explorer.ui.widgets.MapleVerticalScrollbar

var serverListSearchQuery: String? by mutableStateOf(null)

@Composable
fun ServerListView() {
    data class ServerItem(
        val originalIndex: Int,
        val name: String,
        val address: String,
    )

    val scrollState = rememberLazyListState()
    val configuration = ConfigurationHandler.getInstance()

    val shouldShowScrollbar =
        remember(
            configuration.generalSettings.scrollBarVisibility,
            configuration.themeSettings.windowScale,
        ) {
            configuration.generalSettings.scrollBarVisibility != "Disabled" &&
                when (configuration.themeSettings.windowScale) {
                    "100%", "1f", "1.0f" -> LiveServerEntryList.serverNameList.size > 4
                    "125%", "1.25f" -> LiveServerEntryList.serverNameList.size > 6
                    "150%", "1.5f" -> LiveServerEntryList.serverNameList.size > 8
                    else -> LiveServerEntryList.serverNameList.size > 8
                }
        }

    val serverItems by remember {
        derivedStateOf {
            LiveServerEntryList.serverNameList.zip(
                LiveServerEntryList.serverAddressList,
            ).mapIndexed { index, pair ->
                ServerItem(index, pair.first, pair.second)
            }
        }
    }

    val filteredServerItems =
        remember(serverListSearchQuery, serverItems) {
            serverListSearchQuery?.let { query ->
                serverItems.filter { it.name.contains(query, ignoreCase = true) }
            } ?: serverItems
        }

    disableServerInfoIndexing
        .takeIf { !it }
        ?.let {
            Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (SettingsViewModel.controlPanelPosition == "Top") {
                        ServerListControlPanelRow()
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            state = scrollState,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start =
                                            if (
                                                configuration.generalSettings.scrollBarVisibility ==
                                                "Left Side" &&
                                                shouldShowScrollbar
                                            ) {
                                                15.dp
                                            } else {
                                                0.dp
                                            },
                                        end =
                                            if (
                                                configuration.generalSettings.scrollBarVisibility
                                                == "Right Side" &&
                                                shouldShowScrollbar
                                            ) {
                                                15.dp
                                            } else {
                                                0.dp
                                            },
                                    ),
                        ) {
                            items(filteredServerItems.size) { index ->
                                val item = filteredServerItems[index]
                                ServerElement(
                                    serverIcon =
                                        ServerFileHandler.takeUnless { disableIconIndexing }
                                            ?.getServerIcon(item.originalIndex)
                                            .asImageBitmap(),
                                    serverIconRaw =
                                        ServerFileHandler.takeUnless { disableIconIndexing }
                                            ?.getRawIconValue(item.originalIndex),
                                    serverName = item.name,
                                    serverAddress = item.address,
                                    serverPositionInList = item.originalIndex,
                                    isSelected =
                                        item.originalIndex ==
                                            LiveServerEntryList.selectedServer,
                                    onClick = {
                                        LiveServerEntryList.selectedServer =
                                            item.originalIndex
                                    },
                                    onMoveUp = { moveServerUp(item.originalIndex) },
                                    onMoveDown = { moveServerDown(item.originalIndex) },
                                )
                            }
                        }

                        MapleVerticalScrollbar(shouldShowScrollbar, scrollState)
                    }
                    if (SettingsViewModel.controlPanelPosition == "Bottom") {
                        ServerListControlPanelRow()
                    }
                }
            }
        }
}

private fun moveServerUp(position: Int) {
    if (position > 0) {
        val newPosition = position - 1
        swapPositions(position, newPosition)
        ServerFileHandler.moveServerUp(position)
        if (LiveServerEntryList.selectedServer == position) {
            LiveServerEntryList.selectedServer = newPosition
        }
    }
}

private fun moveServerDown(position: Int) {
    if (position < LiveServerEntryList.serverNameList.size - 1) {
        val newPosition = position + 1
        swapPositions(position, newPosition)
        ServerFileHandler.moveServerDown(position)
        if (LiveServerEntryList.selectedServer == position) {
            LiveServerEntryList.selectedServer = newPosition
        }
    }
}

private fun swapPositions(
    fromIndex: Int,
    toIndex: Int,
) {
    LiveServerEntryList.serverNameList.swapPositions(fromIndex, toIndex)
    LiveServerEntryList.serverAddressList.swapPositions(fromIndex, toIndex)
}

private fun <T> MutableList<T>.swapPositions(
    fromIndex: Int,
    toIndex: Int,
) {
    if (fromIndex != toIndex) {
        this[fromIndex] = this[toIndex].also { this[toIndex] = this[fromIndex] }
    }
}
