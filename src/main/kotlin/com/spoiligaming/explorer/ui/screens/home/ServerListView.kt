package com.spoiligaming.explorer.ui.screens.home

import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.disableIconIndexing
import com.spoiligaming.explorer.disableServerInfoIndexing
import com.spoiligaming.explorer.server.ContemporaryServerEntryListData
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.SettingsViewModel
import com.spoiligaming.explorer.ui.components.ServerElement
import com.spoiligaming.explorer.ui.extensions.asImageBitmap

@Composable
fun ServerListView() {
    val scrollState = rememberLazyListState()

    val configuration = ConfigurationHandler.getInstance().generalSettings

    val isScrollbarVisible = configuration.scrollBarVisibility != "Disabled"
    val isScrollable = ContemporaryServerEntryListData.serverNameList.size > 4

    disableServerInfoIndexing
        .takeIf { !it }
        ?.let {
            Box(modifier = Modifier.width(377.dp).height(356.dp).background(Color.Transparent)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    state = scrollState,
                    modifier =
                        Modifier.fillMaxWidth()
                            .offset(
                                x =
                                    if (configuration.scrollBarVisibility == "Left Side") {
                                        15.dp
                                    } else {
                                        0.dp
                                    },
                            ),
                ) {
                    items(ContemporaryServerEntryListData.serverNameList.size) { index ->
                        ServerElement(
                            serverIcon =
                                ServerFileHandler.takeUnless { disableIconIndexing }
                                    ?.getServerIcon(index)
                                    .asImageBitmap(),
                            serverIconRaw =
                                ServerFileHandler.takeUnless { disableIconIndexing }
                                    ?.getRawIconValue(index),
                            serverName = ContemporaryServerEntryListData.serverNameList[index],
                            serverAddress =
                                ContemporaryServerEntryListData.serverAddressList[index],
                            serverPositionInList = index,
                            isSelected =
                                index == ContemporaryServerEntryListData.selectedServer,
                            onClick = {
                                ContemporaryServerEntryListData.selectedServer = index
                            },
                            onMoveUp = ::moveServerUp,
                            onMoveDown = ::moveServerDown,
                        )
                    }
                }

                if (isScrollbarVisible && isScrollable) {
                    SettingsViewModel.scrollbarVisibility = configuration.scrollBarVisibility

                    VerticalScrollbar(
                        modifier =
                            Modifier.align(
                                when (configuration.scrollBarVisibility) {
                                    "Right Side" -> Alignment.CenterEnd
                                    "Left Side" -> Alignment.CenterStart
                                    else -> throw IllegalArgumentException()
                                },
                            ),
                        adapter = rememberScrollbarAdapter(scrollState),
                        style =
                            ScrollbarStyle(
                                thickness = 10.dp,
                                shape = RoundedCornerShape(12.dp),
                                minimalHeight = 48.dp,
                                hoverColor = MapleColorPalette.control.copy(alpha = 1f),
                                unhoverColor = MapleColorPalette.control.copy(alpha = 0.6f),
                                hoverDurationMillis = 240,
                            ),
                    )
                } else if (isScrollbarVisible) {
                    SettingsViewModel.scrollbarVisibility = "Disabled"
                }
            }
        }
}

private fun moveServerUp(position: Int) {
    if (position > 0) {
        val newPosition = position - 1
        swapPositions(position, newPosition)
        ServerFileHandler.moveServerUp(position)
        if (ContemporaryServerEntryListData.selectedServer == position) {
            ContemporaryServerEntryListData.selectedServer = newPosition
        }
    }
}

private fun moveServerDown(position: Int) {
    if (position < ContemporaryServerEntryListData.serverNameList.size - 1) {
        val newPosition = position + 1
        swapPositions(position, newPosition)
        ServerFileHandler.moveServerDown(position)
        if (ContemporaryServerEntryListData.selectedServer == position) {
            ContemporaryServerEntryListData.selectedServer = newPosition
        }
    }
}

private fun swapPositions(
    fromIndex: Int,
    toIndex: Int,
) {
    ContemporaryServerEntryListData.serverNameList.swapPositions(fromIndex, toIndex)
    ContemporaryServerEntryListData.serverAddressList.swapPositions(fromIndex, toIndex)
}

private fun <T> MutableList<T>.swapPositions(
    fromIndex: Int,
    toIndex: Int,
) {
    if (fromIndex != toIndex) {
        this[fromIndex] = this[toIndex].also { this[toIndex] = this[fromIndex] }
    }
}
