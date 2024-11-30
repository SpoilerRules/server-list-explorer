package com.spoiligaming.explorer.ui.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun MapleToggleSwitch(
    label: String,
    initialValue: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    var isChecked by remember { mutableStateOf(initialValue) }

    val circleOffset by animateFloatAsState(targetValue = if (isChecked) 1f else 0f)
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = Modifier.height(ThumbHeight)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(modifier = Modifier.width(TrackWidth), contentAlignment = Alignment.CenterStart) {
                Box(
                    modifier =
                        Modifier
                            .width(TrackWidth)
                            .height(TrackHeight)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color =
                                    if (!isChecked) {
                                        MapleColorPalette.tertiaryControl
                                    } else {
                                        MapleColorPalette.control
                                    },
                            )
                            .clickable(
                                onClick = {
                                    isChecked = !isChecked
                                    onToggle(isChecked)
                                },
                                interactionSource = interactionSource,
                                indication = null,
                            ),
                )
                Box(
                    modifier =
                        Modifier
                            .size(ThumbHeight)
                            .align(Alignment.TopStart)
                            .offset(x = (TrackWidth - ThumbHeight) * circleOffset)
                            .clip(CircleShape)
                            .background(
                                color =
                                    if (!isChecked) {
                                        MapleColorPalette.control
                                    } else {
                                        MapleColorPalette.secondaryControl
                                    },
                            ),
                )
            }
            Text(
                text = label,
                color = MapleColorPalette.text,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaLight,
                        fontWeight = FontWeight.Light,
                        fontSize = 15.sp,
                    ),
            )
        }
    }
}

private val TrackWidth = 50.dp
private val TrackHeight = 20.dp
private val ThumbHeight = 26.dp
