package com.spoiligaming.explorer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.SoftwareInformation
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory

@Composable
fun NavigationRail(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (!isNavigationRailVisible) {
            Box {
                content()
            }
        }

        if (isNavigationRailVisible) {
            Row {
                Box(
                    modifier =
                    Modifier
                        .fillMaxHeight()
                        .background(MapleColorPalette.control),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.width(IntrinsicSize.Max).padding(vertical = 10.dp),
                    ) {
                        Text(
                            SoftwareInformation.WINDOW_TITLE,
                            color = MapleColorPalette.accent,
                            style =
                            TextStyle(
                                fontFamily = FontFactory.comfortaaMedium,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )
                        HorizontalDivider(
                            color = MapleColorPalette.secondaryControl,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 5.dp),
                        )
                    }
                }
                ContextualActionView()
            }
        }
    }
}
