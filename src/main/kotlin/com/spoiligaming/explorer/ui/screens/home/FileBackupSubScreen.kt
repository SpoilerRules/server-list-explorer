package com.spoiligaming.explorer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory

@Composable
fun FileBackupSubScreen() =
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MapleColorPalette.quaternary, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
            /* LazyColumn {

            }*/
                Text(
                    text = "Work in progress.\n\nStay tuned for what’s coming next.",
                    color = MapleColorPalette.text,
                    style =
                        TextStyle(
                            fontFamily = FontFactory.comfortaaMedium,
                            fontWeight = FontWeight.Normal,
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center,
                        ),
                )
            }

            InformationContainer()
            Spacer(Modifier)
        }
    }
