package com.spoiligaming.explorer.ui.presentation

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuRepresentation
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.rememberCursorPositionProvider
import com.spoiligaming.explorer.controlButtonAsString
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.SettingsViewModel
import com.spoiligaming.explorer.ui.extensions.onHover
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.icons.IconFactory

class MapleContextMenuRepresentation(private val serverName: String?, private val type: Int) :
    ContextMenuRepresentation {
    @Composable
    override fun Representation(
        state: ContextMenuState,
        items: () -> List<ContextMenuItem>,
    ) {
        if (state.status !is ContextMenuState.Status.Open) return

        Popup(
            popupPositionProvider = rememberCursorPositionProvider(),
            onDismissRequest = { state.status = ContextMenuState.Status.Closed },
            properties = PopupProperties(focusable = true),
        ) {
            Column(
                modifier =
                    Modifier
                        .width(IntrinsicSize.Max)
                        .then(
                            Modifier.layout { measurable, constraints ->
                                with(measurable.measure(constraints)) {
                                    layout(width + 10.dp.roundToPx(), height) {
                                        placeRelative(0, 0)
                                    }
                                }
                            },
                        )
                        .shadow(16.dp)
                        .background(MapleColorPalette.menu, RoundedCornerShape(12.dp))
                        .padding(vertical = 5.dp)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                serverName?.let { DisplayServerName(it) }

                DisplayMenuItems(state, items())
            }
        }
    }

    @Composable
    private fun DisplayServerName(name: String) =
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier =
                Modifier
                    .padding(vertical = 5.dp),
        ) {
            Text(
                text = name,
                color = MapleColorPalette.accent,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaRegular,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = MapleColorPalette.control,
                modifier = Modifier.padding(horizontal = 5.dp),
            )
        }

    @Composable
    private fun DisplayMenuItems(
        state: ContextMenuState,
        items: List<ContextMenuItem>,
    ) = Column(modifier = Modifier.padding(horizontal = 5.dp)) {
        items
            .distinctBy { it.label }
            .forEach { item ->
                if (item.label in listOf("Erase Icon", "Copy as Toml")) {
                    Spacer(Modifier.height(5.dp))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MapleColorPalette.control,
                    )
                    Spacer(Modifier.height(5.dp))
                }

                MenuItemContent(
                    itemHoverColor = MapleColorPalette.secondaryControl,
                    onClick = {
                        state.status = ContextMenuState.Status.Closed
                        item.onClick()
                    },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 10.dp),
                        ) {
                            IconForItem(item.label)?.let { icon ->
                                when (icon) {
                                    is Painter ->
                                        Icon(
                                            painter = icon,
                                            contentDescription = null,
                                            tint = MapleColorPalette.text,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    is ImageBitmap ->
                                        Icon(
                                            bitmap = icon,
                                            contentDescription = null,
                                            tint = MapleColorPalette.text,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    is ImageVector ->
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = MapleColorPalette.text,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    else -> throw IllegalStateException(
                                        "Unexpected icon type: ${icon::class.simpleName}",
                                    )
                                }
                            }
                            Text(
                                text = item.label,
                                color = MapleColorPalette.text,
                                style =
                                    TextStyle(
                                        fontFamily = FontFactory.comfortaaRegular,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                    ),
                            )
                        }
                        DisplayShortcutText(item.label)
                    }
                }
            }
    }

    @Composable
    private fun DisplayShortcutText(label: String) {
        if (!SettingsViewModel.displayShortcutsInContextMenu) {
            return
        }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            val shortcutText =
                when (label) {
                    "Copy Address" -> "$controlButtonAsString+C"
                    "Delete Entry" -> "Delele"
                    "Erase Icon" -> "$controlButtonAsString+Delele"
                    "Navigate to Directory" -> "L-Click"
                    "Copy File Path" -> "Double L-Click"
                    "Copy" -> if (type == 1) "$controlButtonAsString+C" else null
                    "Cut" -> "$controlButtonAsString+X"
                    "Paste" -> "$controlButtonAsString+V"
                    "Select All" -> "$controlButtonAsString+A"
                    "Rename" -> "$controlButtonAsString+R"
                    "Modify Address" -> "$controlButtonAsString+M"
                    "Change Icon" -> "$controlButtonAsString+I"
                    "Copy as Toml" -> "$controlButtonAsString+T"
                    "Copy Name" -> "$controlButtonAsString+N"
                    else -> null
                }

            shortcutText?.let {
                Text(
                    text = it,
                    color = MapleColorPalette.fadedText,
                    style =
                        TextStyle(
                            fontFamily = FontFactory.comfortaaRegular,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                        ),
                    modifier = Modifier.padding(end = 10.dp),
                )
            }
        }
    }

    @Composable
    private fun IconForItem(label: String): Any? =
        when (label) {
            "Rename" -> IconFactory.editIcon
            "Modify Address" -> Icons.Filled.Key
            "Change Icon" -> Icons.Filled.Brush
            "Erase Icon" -> IconFactory.eraserIcon
            "Delete Entry" -> IconFactory.deleteIconWhite
            else ->
                when (label) {
                    "Copy File Path",
                    "Copy Name",
                    "Copy Address",
                    "Copy as Toml",
                    "Copy Icon as Base64",
                    "Copy Icon as Image",
                    -> IconFactory.copyIconRegular
                    "Navigate to Directory" -> IconFactory.arrowRightIcon
                    "Copy" -> if (type == 1) IconFactory.copyIconRegular else null
                    "Cut" -> Icons.Filled.ContentCut
                    "Paste" -> IconFactory.pasteIcon
                    else -> null
                }
        }

    @Composable
    private fun MenuItemContent(
        itemHoverColor: Color,
        onClick: () -> Unit,
        content: @Composable RowScope.() -> Unit,
    ) {
        var isHovered by remember { mutableStateOf(false) }
        val interactionSource = remember { MutableInteractionSource() }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .onHover { isHovered = it }
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isHovered) itemHoverColor else Color.Transparent,
                        RoundedCornerShape(10.dp),
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = Color.White),
                        onClick = onClick,
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}
