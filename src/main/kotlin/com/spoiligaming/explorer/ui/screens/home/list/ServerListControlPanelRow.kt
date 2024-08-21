package com.spoiligaming.explorer.ui.screens.home.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.icons.IconFactory
import com.spoiligaming.explorer.ui.navigation.NavigationController
import com.spoiligaming.explorer.ui.navigation.Screen
import com.spoiligaming.explorer.ui.presentation.MapleContextMenuRepresentation

@Preview
@Composable
fun ServerListControlPanelRow() {
    var isSearchBarFocused by remember { mutableStateOf(false) }
    var searchBarInput by remember { mutableStateOf("") }
    var isSearchBarPlaceholderVisible by remember { mutableStateOf(searchBarInput.isEmpty()) }
    val searchBarWidth by animateFloatAsState(
        targetValue = if (isSearchBarFocused) 1f else 0.35f,
        animationSpec = tween(durationMillis = 74),
    )
    val searchBarColor by animateColorAsState(
        targetValue = if (isSearchBarFocused) MapleColorPalette.tertiary else MapleColorPalette.secondary,
        animationSpec = tween(durationMillis = 74),
    )

    Box(Modifier.fillMaxWidth().height(32.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(searchBarWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp, vertical = 2.dp).border(
                            width = 1.dp,
                            color = if (isSearchBarFocused) MapleColorPalette.control else Color.Transparent,
                            shape = RoundedCornerShape(16.dp),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxSize(),
                    color = searchBarColor,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(horizontal = 10.dp),
                    ) {
                        Image(
                            bitmap = IconFactory.magnifyingGlassIcon,
                            contentDescription = "Magnifying Glass Icon for Search Bar",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(18.dp),
                        )

                        val contextMenuRepresentation =
                            remember { MapleContextMenuRepresentation(null, 1) }

                        CompositionLocalProvider(
                            LocalTextSelectionColors provides
                                TextSelectionColors(
                                    backgroundColor = MapleColorPalette.accent,
                                    handleColor = MapleColorPalette.accent,
                                ),
                            LocalContextMenuRepresentation provides contextMenuRepresentation,
                        ) {
                            val focusRequester = remember { FocusRequester() }
                            Box(
                                modifier =
                                    Modifier
                                        .focusRequester(focusRequester)
                                        .focusable()
                                        .onKeyEvent {
                                            if (it.key == Key.Escape) {
                                                searchBarInput = ""
                                                serverListSearchQuery = ""
                                                isSearchBarPlaceholderVisible = true
                                                focusRequester.requestFocus()
                                                true
                                            } else {
                                                false
                                            }
                                        },
                            ) {
                                BasicTextField(
                                    value =
                                        if (isSearchBarPlaceholderVisible || !isSearchBarFocused) {
                                            ""
                                        } else {
                                            searchBarInput
                                        },
                                    onValueChange = { newText ->
                                        searchBarInput = newText
                                        serverListSearchQuery = newText
                                        isSearchBarPlaceholderVisible = newText.isEmpty()
                                    },
                                    singleLine = true,
                                    textStyle =
                                        TextStyle(
                                            color = MapleColorPalette.text,
                                            fontFamily = FontFactory.comfortaaLight,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 15.sp,
                                        ),
                                    cursorBrush = SolidColor(Color.White),
                                    modifier =
                                        Modifier.onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                if (searchBarInput.isBlank()) {
                                                    isSearchBarFocused = true
                                                    if (isSearchBarPlaceholderVisible) {
                                                        searchBarInput = ""
                                                        serverListSearchQuery = ""
                                                        isSearchBarPlaceholderVisible = false
                                                    }
                                                }
                                            } else {
                                                if (searchBarInput.isBlank()) {
                                                    searchBarInput = ""
                                                    serverListSearchQuery = ""
                                                    isSearchBarFocused = false
                                                    isSearchBarPlaceholderVisible = true
                                                }
                                            }
                                        },
                                )

                                if (isSearchBarPlaceholderVisible) {
                                    Text(
                                        text = if (isSearchBarFocused) "Search server entry by name" else "Search",
                                        color = MapleColorPalette.fadedText,
                                        style =
                                            TextStyle(
                                                fontFamily = FontFactory.comfortaaMedium,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 16.sp,
                                            ),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!isSearchBarFocused) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    TextButton(
                        onClick = { NavigationController.navigateTo(Screen.FileBackupScreen) },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MapleColorPalette.menu,
                            ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .pointerHoverIcon(PointerIcon.Hand)
                                .padding(0.dp),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    bitmap = IconFactory.deleteIcon,
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    "Server File Recycle Bin",
                                    color = MapleColorPalette.fadedText,
                                    style =
                                        TextStyle(
                                            fontFamily = FontFactory.comfortaaMedium,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 16.sp,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
