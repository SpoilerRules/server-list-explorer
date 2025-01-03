package com.spoiligaming.explorer.ui.dialogs.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import com.spoiligaming.explorer.SoftwareInformation
import com.spoiligaming.explorer.isWindowMaximized
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.widgets.MapleHyperlink
import com.spoiligaming.explorer.ui.widgets.MapleSectionLayout

@Composable
fun MapleDialogBase(
    heightType: Int,
    isCloseable: Boolean,
    onDismiss: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    var isHovered by remember { mutableStateOf(false) }

    Popup(
        alignment = Alignment.Center,
        onDismissRequest =
            when {
                !isHovered && isCloseable -> onDismiss
                else -> {
                    {}
                }
            },
        properties = PopupProperties(focusable = isCloseable),
    ) {
        Box(
            modifier =
                Modifier.run {
                    when (isCloseable) {
                        true ->
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Color(0x80000000),
                                    RoundedCornerShape(if (isWindowMaximized) 0.dp else 24.dp),
                                )
                        false -> Modifier.height(IntrinsicSize.Max)
                    }
                }
                    .clickable(
                        onClick = { if (!isHovered && isCloseable) onDismiss() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier =
                        Modifier.width(IntrinsicSize.Max)
                            .run {
                                when (heightType) {
                                    1 -> height(130.dp)
                                    2 -> height(148.dp)
                                    3 -> height(270.dp)
                                    else -> height(IntrinsicSize.Max)
                                }
                            }
                            .onHover { isHovered = it },
                    color = MapleColorPalette.tertiary,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 12.dp,
                ) {
                    if (isCloseable) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopEnd,
                        ) {
                            var isCloseButtonHovered by remember {
                                mutableStateOf(false)
                            }

                            Box(
                                Modifier.onHover { isCloseButtonHovered = it }
                                    .size(60.dp, 40.dp)
                                    .background(
                                        if (isCloseButtonHovered) {
                                            Color(0xFF7E1515)
                                        } else {
                                            Color.Transparent
                                        },
                                        RoundedCornerShape(10.dp),
                                    )
                                    .clickable(
                                        onClick = onDismiss,
                                        indication = null,
                                        interactionSource =
                                            remember {
                                                MutableInteractionSource()
                                            },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "x",
                                    color = MapleColorPalette.fadedText,
                                    style =
                                        TextStyle(
                                            fontFamily =
                                                FontFactory.comfortaaMedium,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 17.sp,
                                        ),
                                    modifier = Modifier.padding(10.dp),
                                )
                            }
                        }
                    }

                    Box(
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    when (heightType) {
                                        3 -> 16.dp
                                        else -> 25.dp
                                    },
                            )
                                .offset(
                                    y =
                                        when (isCloseable) {
                                            true -> 50.dp
                                            false -> 20.dp
                                        },
                                ),
                    ) {
                        content()
                    }
                }
            }
        }
    }

    if (!isCloseable) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(bottom = 10.dp)
                    .zIndex(1f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            MapleSectionLayout(modifier = Modifier.width(IntrinsicSize.Max), title = "Attention!") {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 20.dp),
                ) {
                    Text(
                        text = "Looking for assistance? Join our ",
                        color = MapleColorPalette.text,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        style =
                            TextStyle(
                                fontFamily = FontFactory.comfortaaLight,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                            ),
                    )
                    MapleHyperlink(
                        "Discord server",
                        MapleColorPalette.accent,
                        14.sp,
                        FontFactory.comfortaaMedium,
                        FontWeight.Normal,
                        SoftwareInformation.DISCORD_SERVER_LINK,
                    )
                }
            }
        }
    }
}
