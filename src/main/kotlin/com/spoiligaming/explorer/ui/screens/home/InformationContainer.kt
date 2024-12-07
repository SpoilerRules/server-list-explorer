package com.spoiligaming.explorer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.spoiligaming.explorer.server.LiveServerEntryList
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.widgets.MapleHyperlink
import com.spoiligaming.explorer.ui.widgets.MergedText

@Composable
fun InformationContainer() =
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(26.dp)
                .background(MapleColorPalette.quaternary, RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MergedText(
            "Server Count: ",
            MapleColorPalette.fadedText,
            FontWeight.Normal,
            if (isBackupRestoreInProgress) {
                "unknown"
            } else {
                LiveServerEntryList.serverNameList.size.toString()
            },
            MapleColorPalette.fadedText,
            FontWeight.Normal,
        )

        MapleHyperlink(
            text = "Discord",
            color = MapleColorPalette.accent,
            fontSize = 16.sp,
            fontFamily = FontFactory.comfortaaMedium,
            fontWeight = FontWeight.Bold,
            url = SoftwareInformation.DISCORD_SERVER_LINK,
        )
    }
