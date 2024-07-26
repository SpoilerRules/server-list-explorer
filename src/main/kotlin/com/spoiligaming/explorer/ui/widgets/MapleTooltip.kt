package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MapleTooltip(
    tooltip: String,
    tooltipColor: Color = MapleColorPalette.text,
    tooltipDelay: Int = 500,
    content: @Composable () -> Unit,
) = TooltipArea(
    tooltip = {
        Surface(
            color = MapleColorPalette.control,
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 8.dp,
        ) {
            Text(
                text = tooltip,
                color = tooltipColor,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaRegular,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                    ),
                modifier = Modifier.padding(8.dp),
            )
        }
    },
    delayMillis = tooltipDelay,
    tooltipPlacement =
        TooltipPlacement.CursorPoint(
            alignment = Alignment.BottomEnd,
        ),
) {
    content()
}
