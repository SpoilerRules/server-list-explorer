package com.spoiligaming.explorer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.disableIconIndexing
import com.spoiligaming.explorer.server.ContemporaryServerEntryListData
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.components.ServerInformationView
import com.spoiligaming.explorer.ui.components.StationaryView
import com.spoiligaming.explorer.ui.extensions.asImageBitmap

@Composable
fun HomeScreen() =
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 61.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ButtonContainer()

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(376.dp)
                            .background(
                                MapleColorPalette.quaternary,
                                RoundedCornerShape(16.dp),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier =
                                    Modifier.size(377.dp, 356.dp)
                                        .offset(x = 10.dp)
                                        .background(Color.Transparent),
                            ) {
                                ServerListView()
                            }

                            Surface(
                                modifier =
                                    Modifier.size(377.dp, 356.dp).offset(x = 10.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MapleColorPalette.menu,
                                shadowElevation = 8.dp,
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    val selectedServerIndex =
                                        ContemporaryServerEntryListData
                                            .selectedServer

                                    if (selectedServerIndex != null) {
                                        ServerInformationView(
                                            serverName =
                                                ContemporaryServerEntryListData
                                                    .serverNameList[
                                                    selectedServerIndex,
                                                ],
                                            serverAddress =
                                                ContemporaryServerEntryListData
                                                    .serverAddressList[
                                                    selectedServerIndex,
                                                ],
                                            serverIcon =
                                                ServerFileHandler.getServerIcon(
                                                    selectedServerIndex,
                                                )
                                                    .asImageBitmap(),
                                            serverIconRaw =
                                                ServerFileHandler.takeUnless {
                                                    disableIconIndexing
                                                }
                                                    ?.getRawIconValue(
                                                        selectedServerIndex,
                                                    ),
                                            serverPositionInList =
                                            selectedServerIndex,
                                        )
                                    } else {
                                        StationaryView()
                                    }
                                }
                            }
                        }
                    }
                }

                InformationContainer()
            }
        }
    }
