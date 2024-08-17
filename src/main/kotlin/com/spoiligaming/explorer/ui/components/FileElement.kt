package com.spoiligaming.explorer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.widgets.MergedInfoText

@Composable
fun FileElement() {
    Box(
        modifier =
            Modifier
                .aspectRatio(1f)
                .requiredSize(100.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MapleColorPalette.menu,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp,
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.TopStart,
            ) {}
            Column {
                MergedInfoText(
                    "Creation Date: ",
                    16.sp,
                    "16 August, 2024",
                    16.sp,
                    MapleColorPalette.fadedText,
                )
            }
        }
    }
}
