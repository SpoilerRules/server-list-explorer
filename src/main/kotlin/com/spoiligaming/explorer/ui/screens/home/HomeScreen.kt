package com.spoiligaming.explorer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.disableIconIndexing
import com.spoiligaming.explorer.server.LiveServerEntryList
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.components.ServerInformationView
import com.spoiligaming.explorer.ui.components.StationaryView
import com.spoiligaming.explorer.ui.extensions.asImageBitmap
import com.spoiligaming.explorer.ui.screens.home.list.ServerListView

@Composable
fun HomeScreen() =
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
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
                            .weight(1f)
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
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .weight(1f),
                            ) {
                                ServerListView()
                            }

                            Surface(
                                modifier =
                                    Modifier
                                        .weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MapleColorPalette.menu,
                                shadowElevation = 8.dp,
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    val selectedServerIndex =
                                        LiveServerEntryList
                                            .selectedServer

                                    if (selectedServerIndex != null) {
                                        ServerInformationView(
                                            serverName =
                                                LiveServerEntryList
                                                    .serverNameList[
                                                    selectedServerIndex,
                                                ],
                                            serverAddress =
                                                LiveServerEntryList
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
                Spacer(Modifier)
            }
        }
    }
