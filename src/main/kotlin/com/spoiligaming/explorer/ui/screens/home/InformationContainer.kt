package com.spoiligaming.explorer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.SoftwareInformation
import com.spoiligaming.explorer.isBackupRestoreInProgress
import com.spoiligaming.explorer.server.ContemporaryServerEntryListData
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.widgets.MapleHyperlink
import com.spoiligaming.explorer.ui.widgets.MergedText

@Composable
fun InformationContainer() =
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .height(26.dp)
                .background(MapleColorPalette.quaternary, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.fillMaxSize().padding(start = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            MergedText(
                "Server Count: ",
                MapleColorPalette.fadedText,
                FontWeight.Normal,
                if (isBackupRestoreInProgress) "unknown" else ContemporaryServerEntryListData.serverNameList.size.toString(),
                MapleColorPalette.fadedText,
                FontWeight.Normal,
            )
        }
        Box(
            Modifier.fillMaxSize().padding(end = 10.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            MapleHyperlink(
                text = "Discord",
                color = MapleColorPalette.accent,
                fontSize = 16.sp,
                fontFamily = FontFactory.comfortaaMedium,
                fontWeight = FontWeight.Bold,
                url = SoftwareInformation.DISCORD_SERVER_LINK,
            )
        }
    }
