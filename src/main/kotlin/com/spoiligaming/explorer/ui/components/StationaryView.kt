package com.spoiligaming.explorer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.SoftwareInformation
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.widgets.MapleHyperlink
import com.spoiligaming.explorer.utils.TipLoader

@Composable
fun StationaryView() {
    val tip = remember { TipLoader.getRandomTip() }

    Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1.35f),
                color = MapleColorPalette.quaternary,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Tip",
                            color = MapleColorPalette.accent,
                            fontSize = 24.sp,
                            fontFamily = FontFactory.comfortaaMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = tip,
                            color = MapleColorPalette.text,
                            fontSize = 16.sp,
                            fontFamily = FontFactory.comfortaaRegular,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                color = MapleColorPalette.quaternary,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text =
                            "More content in this section will be available soon.\n\nJoin our Discord server to learn more: ",
                        color = MapleColorPalette.text,
                        style =
                            TextStyle(
                                fontSize = 16.sp,
                                fontFamily = FontFactory.comfortaaMedium,
                                fontWeight = FontWeight.Normal,
                            ),
                        textAlign = TextAlign.Center,
                    )
                    MapleHyperlink(
                        "\n\n\n\n\nHyperlink to our Discord server",
                        MapleColorPalette.accent,
                        16.sp,
                        FontFactory.comfortaaMedium,
                        FontWeight.Normal,
                        SoftwareInformation.DISCORD_SERVER_LINK,
                    )
                }
            }
        }
    }
}
