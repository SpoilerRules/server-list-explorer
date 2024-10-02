package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory

@Composable
fun MapleSectionLayout(
    modifier: Modifier,
    title: String,
    content: @Composable () -> Unit,
) = Column(
    modifier =
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MapleColorPalette.quaternary, RoundedCornerShape(18.dp)),
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(36.dp),
        color = MapleColorPalette.tertiary,
        shadowElevation = 6.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                color = MapleColorPalette.accent,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                    ),
            )
        }
    }
    content()
}
