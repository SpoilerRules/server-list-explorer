package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.disableIconIndexing
import com.spoiligaming.explorer.ui.icons.IconFactory

@Composable
fun ServerIconImage(
    icon: ImageBitmap?,
    serverName: String,
) {
    if (!disableIconIndexing) {
        Image(
            bitmap = icon ?: IconFactory.unknownServerIcon,
            contentDescription = "Server Icon of $serverName",
            modifier =
                Modifier.fillMaxSize()
                    .background(color = Color.Transparent)
                    .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Fit,
        )
    }
}
