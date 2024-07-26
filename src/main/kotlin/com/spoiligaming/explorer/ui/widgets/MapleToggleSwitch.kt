package com.spoiligaming.explorer.ui.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.MapleColorPalette

@Composable
fun LabeledMapleToggleSwitch(
    title: String,
    currentValue: Boolean,
    onValueChange: (Boolean) -> Unit,
) = MergedText(title, MapleColorPalette.text, FontWeight.Light) {
    MapleToggleSwitch(initialValue = currentValue, onToggle = onValueChange)
}

@Composable
fun MapleToggleSwitch(
    initialValue: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    var isChecked by remember { mutableStateOf(initialValue) }

    val circleOffset by animateFloatAsState(targetValue = if (isChecked) 1f else 0f)
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = Modifier.width(50.dp).height(76.dp)) {
        Box(
            modifier =
                Modifier.width(50.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MapleColorPalette.menu)
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
                Modifier.size(26.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (50.dp - 26.dp) * circleOffset, y = (-3).dp)
                    .clip(CircleShape)
                    .background(MapleColorPalette.control),
        )
    }
}
