package com.spoiligaming.explorer.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.dialogs.dialog.MapleDialogBase

@Composable
fun MapleIndexMoveDialog(onDismiss: () -> Unit) =
    MapleDialogBase(
        0,
        true,
        onDismiss,
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Column {
                val scrollState = rememberScrollState()
                Column(
                    modifier =
                        Modifier
                            .height(68.dp * 3)
                            .background(MapleColorPalette.control, RoundedCornerShape(12.dp))
                            .padding(5.dp)
                            .verticalScroll(scrollState),
                ) {
                }
            }
            Column {
            }
        }
    }
