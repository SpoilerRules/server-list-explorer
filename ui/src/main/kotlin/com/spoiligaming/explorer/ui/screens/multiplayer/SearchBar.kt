/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025 SpoilerRules
 *
 * Server List Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Server List Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Server List Explorer.  If not, see <https://www.gnu.org/licenses/>.
*/

@file:OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)

package com.spoiligaming.explorer.ui.screens.multiplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.util.rememberAdaptiveWidth
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import org.jetbrains.compose.resources.StringResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.cd_advanced_options
import server_list_explorer.ui.generated.resources.cd_search_icon
import server_list_explorer.ui.generated.resources.search_bar_placeholder
import server_list_explorer.ui.generated.resources.search_filter_address_only
import server_list_explorer.ui.generated.resources.search_filter_name_and_address
import server_list_explorer.ui.generated.resources.search_filter_name_only

@Composable
internal fun DockedSearchScreen(
    expanded: Boolean,
    onSearch: (query: String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var query by remember { mutableStateOf("") }
    // var filter by remember { mutableStateOf(SearchFilter.NameAndAddress) }

    val width = rememberAdaptiveWidth(min = DockedSearchBarMinWidth, max = DockedSearchBarMaxWidth)

    LaunchedEffect(Unit) {
        snapshotFlow { query }
            .debounce(SEARCH_DEBOUNCE_MILLIS)
            .collectLatest { q -> onSearch(q) }
    }
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { query = it },
                onSearch = {},
                expanded = expanded,
                onExpandedChange = {},
                modifier =
                    Modifier
                        .onPreviewKeyEvent { e ->
                    /*
                     * This logic works around an issue in Compose Multiplatform version 1.8.2
                     * where Escape key events may be reported as Key.Unknown or with incorrect keyCode or event type.
                     * The code below ensures Escape is detected reliably by checking the Key, keyCode, and utf16CodePoint.
                     * This can be revisited if Escape key handling is fixed in future Compose Multiplatform versions.
                     */
                            val isEscapeByKey = e.key == Key.Escape
                            val isEscapeByCode = e.key.keyCode == ESCAPE_KEY_CODE
                            val isEscapeByUtf16 = e.utf16CodePoint == ESCAPE_UTF16_CODE_POINT

                            val detected = isEscapeByKey || isEscapeByCode || isEscapeByUtf16

                            if ((e.type == KeyEventType.KeyDown && detected) ||
                                (e.type == KeyEventType.Unknown && isEscapeByUtf16)
                            ) {
                                when {
                                    isEscapeByKey -> logger.debug { "Escape detected by Key.Escape mapping" }
                                    isEscapeByCode -> logger.debug { "Escape detected by GLFW keyCode == 256L" }
                                    else -> logger.debug { "Escape detected by utf16CodePoint == 27 fallback" }
                                }

                                query = ""
                                focusManager.clearFocus()
                                true
                            }
                            false
                        }.onFocusChanged { onFocusChange(it.isFocused) },
                placeholder = {
                    Text(
                        t(Res.string.search_bar_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = t(Res.string.cd_search_icon))
                },
                trailingIcon = {
                    // TODO

                    /*
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        onClick = {}
                    ) {
                        BadgedBox(
                            badge = {
                                Badge() // TODO: show only if advanced settings are modified
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = t(Res.string.cd_advanced_options)
                            )
                        }
                    }
                     */
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier =
            Modifier
                .width(width)
                .height(SearchBarHeight)
                // adjusts for M3 search bar's default inset padding affecting MultiplayerScreen.kt layout
                .offset(y = SearchBarOffsetY),
    ) {}
}

@Composable
internal fun DockedSearchBarShimmer(shimmer: Shimmer) {
    val width = rememberAdaptiveWidth(min = DockedSearchBarMinWidth, max = DockedSearchBarMaxWidth)

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = "",
                onQueryChange = {},
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = {
                    Box(
                        modifier =
                            Modifier
                                .shimmer(shimmer)
                                .width(ShimmerPlaceholderWidth)
                                .height(ShimmerPlaceholderHeight)
                                .background(
                                    color =
                                        MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = SHIMMER_BACKGROUND_ALPHA,
                                        ),
                                    shape = MaterialTheme.shapes.small,
                                ),
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = t(Res.string.cd_search_icon),
                        modifier = Modifier.shimmer(shimmer),
                    )
                },
                trailingIcon = {
                    IconButton(
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = t(Res.string.cd_advanced_options),
                            modifier = Modifier.shimmer(shimmer),
                        )
                    }
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier =
            Modifier
                .shimmer(shimmer)
                .width(width)
                .height(SearchBarHeight)
                .pointerInput(Unit) {
                    // no need to handle events, just consume them all
                    detectTapGestures(onTap = {})
                },
    ) {}
}

internal enum class SearchFilter(
    val label: StringResource,
) {
    NameAndAddress(Res.string.search_filter_name_and_address),
    NameOnly(Res.string.search_filter_name_only),
    AddressOnly(Res.string.search_filter_address_only),
}

private val logger = KotlinLogging.logger {}

private val SearchBarHeight = 56.dp
private val SearchBarOffsetY = -4.dp
private val DockedSearchBarMinWidth = 360.dp
private val DockedSearchBarMaxWidth = 720.dp
private val ShimmerPlaceholderWidth = 64.dp
private val ShimmerPlaceholderHeight = 20.dp

private const val SEARCH_DEBOUNCE_MILLIS = 300L
private const val ESCAPE_KEY_CODE = 256L
private const val ESCAPE_UTF16_CODE_POINT = 27
private const val SHIMMER_BACKGROUND_ALPHA = 0.12f
