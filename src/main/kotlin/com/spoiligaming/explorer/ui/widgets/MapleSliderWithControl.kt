package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// will be implemented when needed
@Composable
fun MapleSliderWithControl() {
    var sliderValue by remember { mutableStateOf(4.2f) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            valueRange = 0f..10f,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = "%.1f".format(sliderValue), fontSize = 20.sp, modifier = Modifier.padding(8.dp))

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = { if (sliderValue > 0) sliderValue -= 0.1f }) { Text("-") }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(onClick = { if (sliderValue < 10) sliderValue += 0.1f }) { Text("+") }
    }
}
