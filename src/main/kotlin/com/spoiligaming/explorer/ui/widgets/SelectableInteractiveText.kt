package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.onClick
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.spoiligaming.explorer.ui.presentation.MapleContextMenuRepresentation

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableInteractiveText(
    text: String,
    textColor: Color,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit,
) {
    val contextMenuRepresentation = remember { MapleContextMenuRepresentation(null, 1) }

    CompositionLocalProvider(LocalContextMenuRepresentation provides contextMenuRepresentation) {
        Box(
            modifier = modifier.fillMaxWidth(),
        ) {
            SelectionContainer {
                Text(
                    text = text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        TextStyle(
                            color = textColor,
                            fontFamily = fontFamily,
                            fontWeight = fontWeight,
                            fontSize = fontSize,
                        ),
                    modifier =
                        modifier.onClick(
                            enabled = true,
                            matcher = PointerMatcher.Primary,
                            onClick = {},
                            onLongClick = onLongClick,
                        ),
                )
            }
        }
    }
}
