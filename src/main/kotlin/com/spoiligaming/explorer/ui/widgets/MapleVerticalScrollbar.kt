package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.ui.MapleColorPalette

@Composable
fun BoxScope.MapleVerticalScrollbar(
    visible: Boolean,
    scrollState: Any,
    modifier: Modifier = Modifier,
) {
    if (
        ConfigurationHandler.getInstance().generalSettings.scrollBarVisibility == "Disabled" ||
        !visible
    ) {
        return
    }

    val scrollbarAdapter: ScrollbarAdapter =
        when (scrollState) {
            is LazyListState -> rememberScrollbarAdapter(scrollState)
            is LazyGridState -> rememberScrollbarAdapter(scrollState)
            is ScrollState -> rememberScrollbarAdapter(scrollState)
            else -> throw IllegalArgumentException(
                "Unsupported scroll state type: ${scrollState::class.java}",
            )
        }

    VerticalScrollbar(
        modifier =
            modifier.clip(
                RoundedCornerShape(12.dp),
            ).background(
                if (ConfigurationHandler.getInstance().themeSettings.showScrollbarBackground) {
                    MapleColorPalette.tertiaryControl
                } else {
                    Color.Transparent
                },
                RoundedCornerShape(12.dp),
            ).align(
                when (ConfigurationHandler.getInstance().generalSettings.scrollBarVisibility) {
                    "Right Side" -> Alignment.CenterEnd
                    "Left Side" -> Alignment.CenterStart
                    else -> throw IllegalArgumentException(
                        "Invalid alignment",
                    )
                },
            ),
        adapter = scrollbarAdapter,
        style =
            ScrollbarStyle(
                thickness = 10.dp,
                shape = RoundedCornerShape(12.dp),
                minimalHeight = 48.dp,
                hoverColor = MapleColorPalette.secondaryControl,
                unhoverColor = MapleColorPalette.control,
                hoverDurationMillis = 240,
            ),
    )
}
