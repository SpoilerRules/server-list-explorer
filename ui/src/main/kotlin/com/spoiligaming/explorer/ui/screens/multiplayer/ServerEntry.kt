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

@file:OptIn(
    ExperimentalUuidApi::class,
    ExperimentalMaterial3Api::class,
)

package com.spoiligaming.explorer.ui.screens.multiplayer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuDataProvider
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.IServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.McServerPingOnlineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.McSrvStatOnlineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OnlineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OnlineServerDataResourceResult
import com.spoiligaming.explorer.multiplayer.AcceptTexturesState
import com.spoiligaming.explorer.multiplayer.HiddenState
import com.spoiligaming.explorer.multiplayer.MultiplayerServer
import com.spoiligaming.explorer.multiplayer.history.ServerListHistoryService
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import com.spoiligaming.explorer.settings.model.ServerQueryMethod
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalAmoledActive
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalMultiplayerSettings
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.dialog.FloatingDialogBuilder
import com.spoiligaming.explorer.ui.extensions.safeAsImageBitmapOrNull
import com.spoiligaming.explorer.ui.extensions.toGroupedString
import com.spoiligaming.explorer.ui.extensions.toPngBase64
import com.spoiligaming.explorer.ui.extensions.toPngInputStream
import com.spoiligaming.explorer.ui.layout.TwinSpillRows
import com.spoiligaming.explorer.ui.snackbar.SnackbarController
import com.spoiligaming.explorer.ui.snackbar.SnackbarEvent
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.theme.isDarkTheme
import com.spoiligaming.explorer.ui.widgets.ActionItem
import com.spoiligaming.explorer.ui.widgets.DropdownOption
import com.spoiligaming.explorer.ui.widgets.HackedSelectionContainer
import com.spoiligaming.explorer.ui.widgets.HierarchicalDropdownMenu
import com.spoiligaming.explorer.ui.widgets.InlineEditableLabel
import com.spoiligaming.explorer.ui.widgets.SelectableGroupItem
import com.spoiligaming.explorer.util.ClipboardUtils
import com.spoiligaming.explorer.util.toHumanReadableDuration
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.accept_textures_disabled
import server_list_explorer.ui.generated.resources.accept_textures_enabled
import server_list_explorer.ui.generated.resources.accept_textures_prompt
import server_list_explorer.ui.generated.resources.cd_icon_custom
import server_list_explorer.ui.generated.resources.cd_icon_default
import server_list_explorer.ui.generated.resources.cd_info_chip_icon
import server_list_explorer.ui.generated.resources.cd_server_info
import server_list_explorer.ui.generated.resources.context_copy_icon_base64
import server_list_explorer.ui.generated.resources.context_copy_icon_png
import server_list_explorer.ui.generated.resources.context_revert_default_icon
import server_list_explorer.ui.generated.resources.copy_with_color_codes
import server_list_explorer.ui.generated.resources.description_label
import server_list_explorer.ui.generated.resources.error_copy_icon_base64_failed
import server_list_explorer.ui.generated.resources.error_copy_icon_png_failed
import server_list_explorer.ui.generated.resources.error_revert_icon_failed
import server_list_explorer.ui.generated.resources.hidden_state_hidden
import server_list_explorer.ui.generated.resources.hidden_state_not_hidden
import server_list_explorer.ui.generated.resources.info_blocked_by_mojang
import server_list_explorer.ui.generated.resources.infochip_compact_label
import server_list_explorer.ui.generated.resources.infochip_expand_label
import server_list_explorer.ui.generated.resources.invalid_server_data
import server_list_explorer.ui.generated.resources.menu_delete
import server_list_explorer.ui.generated.resources.menu_hidden
import server_list_explorer.ui.generated.resources.menu_refresh
import server_list_explorer.ui.generated.resources.menu_server_resource_packs
import server_list_explorer.ui.generated.resources.ok_label
import server_list_explorer.ui.generated.resources.protocol_format
import server_list_explorer.ui.generated.resources.rate_limited_message
import server_list_explorer.ui.generated.resources.server_description_title
import server_list_explorer.ui.generated.resources.server_unreachable
import server_list_explorer.ui.generated.resources.texture_unknown_server
import kotlin.random.Random
import kotlin.random.Random.Default.nextInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * TODO: Refactor this file thoroughly
 *
 * Goals:
 * 1. Replace magic numbers with named constants
 * 2. Break this code into smaller, well-defined functions without over-fragmenting
 * 3. Improve documentation and inline comments to help future contributors understand the code
 */

@Composable
internal fun ServerEntry(
    selected: Boolean,
    repo: ServerListRepository,
    data: MultiplayerServer,
    historyService: ServerListHistoryService,
    searchQuery: String,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    onHighlightFinished: () -> Unit = {},
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
) {
    val amoledOn = LocalAmoledActive.current
    val mpSettings = LocalMultiplayerSettings.current
    val useMcSrvStat = mpSettings.serverQueryMethod == ServerQueryMethod.McSrvStat
    val serverFlow =
        remember(
            data.ip,
            useMcSrvStat,
            mpSettings.connectTimeoutMillis,
            mpSettings.socketTimeoutMillis,
        ) {
            ServerEntryController.getServerDataFlow(
                address = data.ip,
                useMCSrvStat = useMcSrvStat,
                connectTimeoutMillis = mpSettings.connectTimeoutMillis,
                socketTimeoutMillis = mpSettings.socketTimeoutMillis,
            )
        }
    val result by serverFlow.collectAsState(initial = serverFlow.value)
    LaunchedEffect((result as? OnlineServerDataResourceResult.Success)?.data) {
        if (result is OnlineServerDataResourceResult.Success) {
            ServerEntryController.syncServerIcon(data, result, repo, historyService)
        }
    }

    val baseColor =
        if (amoledOn) {
            Color.Black
        } else {
            CardDefaults.elevatedCardColors().containerColor
        }

    val flashColor = MaterialTheme.colorScheme.secondaryContainer

    val bg by animateColorAsState(
        targetValue = if (highlight) flashColor else baseColor,
        animationSpec = tween(durationMillis = 800),
    )

    LaunchedEffect(highlight) {
        if (highlight) {
            delay(800)
            onHighlightFinished()
        }
    }

    // FOR CONTRIBUTORS: make sure to edit ShimmerServerEntry.kt when you edit this
    ElevatedCard(
        modifier =
            modifier
                .border(
                    border =
                        if (selected || amoledOn) {
                            CardDefaults.outlinedCardBorder()
                        } else {
                            BorderStroke(
                                0.dp,
                                Color.Transparent,
                            )
                        },
                    shape = CardDefaults.shape,
                ).onKeyEvent {
                    if (it.isShiftPressed && selected) {
                        when (it.key) {
                            Key.DirectionUp -> {
                            }
                        }
                        true
                    }
                    false
                },
        colors =
            CardDefaults.elevatedCardColors().copy(
                containerColor = bg,
                contentColor = CardDefaults.elevatedCardColors().contentColor,
            ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
                val bmp by rememberServerBitmap(data.iconBytes)
                val textureDefaultServer = imageResource(Res.drawable.texture_unknown_server)

                val iconSyncVersion by ServerEntryController
                    .iconSyncVersionFlow(data.id)
                    .collectAsState(initial = 0L)
                var previousSyncVersion by remember(data.id) { mutableStateOf(0L) }
                val shouldAnimateIcon =
                    remember(iconSyncVersion, previousSyncVersion) {
                        val changed =
                            previousSyncVersion != 0L && iconSyncVersion != previousSyncVersion
                        previousSyncVersion = iconSyncVersion
                        changed
                    }

                val rememberOnNameSave: (String) -> Unit =
                    remember(data.id) {
                        { newName ->
                            ServerEntryController.changeName(data.id, newName, repo, historyService)
                        }
                    }
                val rememberOnAddressSave: (String) -> Unit =
                    remember(data.id) {
                        { newIp ->
                            ServerEntryController.changeAddress(
                                data.id,
                                newIp,
                                repo,
                                historyService,
                            )
                        }
                    }

                Row(
                    Modifier.height(64.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    IconColumn(
                        serverIcon = bmp,
                        defaultServerIcon = textureDefaultServer,
                        serverAddress = data.ip,
                        scope = scope,
                        animateIcon = shouldAnimateIcon,
                    ) {
                        ServerEntryController.deleteIcon(data, repo, historyService)
                    }
                    MotdCard(result, shimmerInstance, modifier.fillMaxHeight())
                }

                ServerNameAddress(
                    serverName = buildHighlightedString(data.name, searchQuery),
                    serverAddress = buildHighlightedString(data.ip, searchQuery),
                    onNameSave = rememberOnNameSave,
                    onAddressSave = rememberOnAddressSave,
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                )

                AnimatedContent(
                    targetState = result,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                ) { animatedResult ->
                    when (animatedResult) {
                        is OnlineServerDataResourceResult.Error,
                        is OnlineServerDataResourceResult.RateLimited,
                        -> OnlineServerDataChipRowsSkeleton()
                        OnlineServerDataResourceResult.Loading ->
                            ShimmerOnlineServerDataRow(
                                shimmerInstance,
                                uniqueKey = data.id,
                            )

                        is OnlineServerDataResourceResult.Success<*> ->
                            OnlineServerDataRow(
                                result = animatedResult,
                            )
                    }
                }
            }

            var selectedState by remember(data.acceptTextures) {
                mutableStateOf(data.acceptTextures)
            }
            var selectedHiddenState by remember(data.hidden) {
                mutableStateOf(data.hidden)
            }

            val hiddenOptions =
                HiddenState.entries.map { state ->
                    val label =
                        when (state) {
                            HiddenState.Hidden -> t(Res.string.hidden_state_hidden)
                            HiddenState.NotHidden -> t(Res.string.hidden_state_not_hidden)
                        }
                    DropdownOption(label)
                }
            val textureOptions =
                AcceptTexturesState.entries.map { state ->
                    val label =
                        when (state) {
                            AcceptTexturesState.Enabled -> t(Res.string.accept_textures_enabled)
                            AcceptTexturesState.Disabled -> t(Res.string.accept_textures_disabled)
                            AcceptTexturesState.Prompt -> t(Res.string.accept_textures_prompt)
                        }
                    DropdownOption(label)
                }

            val hiddenStateHiddenText = t(Res.string.hidden_state_hidden)
            val hiddenStateNotHiddenText = t(Res.string.hidden_state_not_hidden)

            val acceptTexturesEnabledText = t(Res.string.accept_textures_enabled)
            val acceptTexturesDisabledText = t(Res.string.accept_textures_disabled)
            val acceptTexturesPromptText = t(Res.string.accept_textures_prompt)

            val selectedHiddenLabel =
                when (selectedHiddenState) {
                    HiddenState.Hidden -> hiddenStateHiddenText
                    HiddenState.NotHidden -> hiddenStateNotHiddenText
                }
            val selectedTextureLabel =
                when (selectedState) {
                    AcceptTexturesState.Enabled -> acceptTexturesEnabledText
                    AcceptTexturesState.Disabled -> acceptTexturesDisabledText
                    AcceptTexturesState.Prompt -> acceptTexturesPromptText
                }

            HierarchicalDropdownMenu(
                entries =
                    listOf(
                        ActionItem(
                            text = t(Res.string.menu_refresh),
                            icon = Icons.Filled.Refresh,
                            onClick = onRefresh,
                        ),
                        ActionItem(
                            text = t(Res.string.menu_delete),
                            icon = Icons.Filled.Delete,
                            onClick = onDelete,
                        ),
                        SelectableGroupItem(
                            text = t(Res.string.menu_hidden),
                            options = hiddenOptions,
                            selected = DropdownOption(selectedHiddenLabel),
                            onOptionSelected = { option ->
                                val state =
                                    when (option.text) {
                                        hiddenStateHiddenText -> HiddenState.Hidden
                                        hiddenStateNotHiddenText -> HiddenState.NotHidden
                                        else -> selectedHiddenState
                                    }
                                selectedHiddenState = state
                                ServerEntryController.changeHiddenState(
                                    server = data,
                                    newState = state,
                                    repo = repo,
                                    historyService = historyService,
                                )
                            },
                        ),
                        SelectableGroupItem(
                            text = t(Res.string.menu_server_resource_packs),
                            options = textureOptions,
                            selected = DropdownOption(selectedTextureLabel),
                            onOptionSelected = { option ->
                                val state =
                                    when (option.text) {
                                        acceptTexturesEnabledText -> AcceptTexturesState.Enabled
                                        acceptTexturesDisabledText -> AcceptTexturesState.Disabled
                                        acceptTexturesPromptText -> AcceptTexturesState.Prompt
                                        else -> selectedState
                                    }
                                selectedState = state
                                ServerEntryController.changeTexturesMode(
                                    server = data,
                                    newState = state,
                                    repo = repo,
                                    historyService = historyService,
                                )
                            },
                        ),
                    ),
                modifier =
                    Modifier
                        .padding(end = 4.dp, bottom = 4.dp)
                        .zIndex(1f)
                        .align(Alignment.BottomEnd)
                        .pointerHoverIcon(PointerIcon.Hand),
            ) { _, toggle ->
                IconButton(onClick = toggle) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = t(Res.string.cd_server_info),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun IconColumn(
    serverIcon: ImageBitmap?,
    defaultServerIcon: ImageBitmap,
    serverAddress: String,
    scope: CoroutineScope,
    animateIcon: Boolean,
    onClearCustomIcon: suspend () -> Unit,
) {
    val contextCopyIconPngText = t(Res.string.context_copy_icon_png)
    val contextCopyIconBase64Text = t(Res.string.context_copy_icon_base64)
    val contextRevertDefaultIconText = t(Res.string.context_revert_default_icon)

    val errorCopyIconPngFailedText = t(Res.string.error_copy_icon_png_failed, serverAddress)
    val errorCopyIconBase64FailedText = t(Res.string.error_copy_icon_base64_failed, serverAddress)
    val errorRevertIconFailedText = t(Res.string.error_revert_icon_failed, serverAddress)

    ContextMenuArea(
        items = {
            val items =
                mutableListOf(
                    ContextMenuItem(contextCopyIconPngText) {
                        runCatching {
                            ClipboardUtils.copyImageFromStream(
                                (serverIcon ?: defaultServerIcon).toPngInputStream(),
                            )
                        }.onFailure {
                            scope.launch {
                                SnackbarController.sendEvent(
                                    SnackbarEvent(errorCopyIconPngFailedText, SnackbarDuration.Short),
                                )
                            }
                        }
                    },
                    ContextMenuItem(contextCopyIconBase64Text) {
                        runCatching {
                            ClipboardUtils.copy((serverIcon ?: defaultServerIcon).toPngBase64())
                        }.onFailure {
                            scope.launch {
                                SnackbarController.sendEvent(
                                    SnackbarEvent(errorCopyIconBase64FailedText, SnackbarDuration.Short),
                                )
                            }
                        }
                    },
                )
            if (serverIcon != null) {
                items +=
                    ContextMenuItem(contextRevertDefaultIconText) {
                        scope.launch {
                            runCatching { onClearCustomIcon() }
                                .onFailure {
                                    SnackbarController.sendEvent(
                                        SnackbarEvent(errorRevertIconFailedText, SnackbarDuration.Short),
                                    )
                                }
                        }
                    }
            }
            items
        },
    ) {
        if (animateIcon) {
            AnimatedContent(
                targetState = serverIcon,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
            ) { icon ->
                Image(
                    bitmap = icon ?: defaultServerIcon,
                    contentDescription =
                        serverIcon?.let {
                            t(Res.string.cd_icon_custom, serverAddress)
                        } ?: t(Res.string.cd_icon_default, serverAddress),
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        } else {
            Image(
                bitmap = serverIcon ?: defaultServerIcon,
                contentDescription =
                    serverIcon?.let {
                        t(Res.string.cd_icon_custom, serverAddress)
                    } ?: t(Res.string.cd_icon_default, serverAddress),
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun MotdCard(
    result: OnlineServerDataResourceResult<IServerData>,
    shimmerInstance: Shimmer,
    modifier: Modifier = Modifier,
) = Card {
    Box(modifier = modifier.fillMaxWidth().padding(MotdInnerPadding)) {
        AnimatedContent(
            targetState = result,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
        ) { result ->
            when (result) {
                is OnlineServerDataResourceResult.Loading ->
                    Column(
                        modifier =
                            Modifier
                                .shimmer(shimmerInstance)
                                .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(0.8f)
                                    .weight(1f)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        shape = MaterialTheme.shapes.small,
                                    ),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(0.6f)
                                    .weight(1f)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        shape = MaterialTheme.shapes.small,
                                    ),
                        )
                    }

                is OnlineServerDataResourceResult.Success -> {
                    val data = result.data
                    if (data !is OnlineServerData) {
                        Text(
                            text = t(Res.string.invalid_server_data),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            lineHeight = MotdLineHeight,
                        )
                        return@AnimatedContent
                    }

                    val motdRaw = data.motd

                    var isSelecting by remember { mutableStateOf(false) }
                    var allSelected by remember { mutableStateOf(false) }

                    var obfuscationSeed by remember { mutableStateOf(MotdObfuscationMinimumSeed) }
                    val selectingState = rememberUpdatedState(isSelecting)

                    LaunchedEffect(motdRaw) {
                        while (isActive) {
                            if (!selectingState.value) {
                                obfuscationSeed =
                                    nextInt(
                                        MotdObfuscationMinimumSeed,
                                        Int.MAX_VALUE,
                                    )
                            }
                            delay(MotdObfuscationUpdateInterval)
                        }
                    }

                    val blockCount = LocalBlockParentShortcuts.current

                    val wasSelecting = remember { mutableStateOf(false) }

                    LaunchedEffect(isSelecting) {
                        if (isSelecting && !wasSelecting.value) {
                            blockCount.value += 1
                        }
                        if (!isSelecting && wasSelecting.value) {
                            blockCount.value = (blockCount.value - 1).coerceAtLeast(0)
                        }
                        wasSelecting.value = isSelecting
                    }

                    val annotatedMotd =
                        remember(motdRaw, obfuscationSeed) {
                            motdRaw.toMinecraftAnnotatedString(obfuscationSeed)
                        }

                    val copyWithColorCodesText = t(Res.string.copy_with_color_codes)
                    ContextMenuDataProvider(
                        items = {
                            buildList {
                                if (allSelected && motdRaw.isNotEmpty()) {
                                    add(
                                        ContextMenuItem(copyWithColorCodesText) {
                                            ClipboardUtils.copy(motdRaw)
                                        },
                                    )
                                }
                            }
                        },
                    ) {
                        HackedSelectionContainer(
                            modifier =
                                Modifier
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                val event = awaitPointerEvent()
                                                if (event.type == PointerEventType.Press) {
                                                    event.changes.forEach { it.consume() }
                                                }
                                            }
                                        }
                                    },
                            onSelectedChange = { isSelecting = it },
                            onAllSelectedChange = { allSelected = it },
                        ) {
                            Text(
                                text = annotatedMotd,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = MotdMaxLines,
                                lineHeight = MotdLineHeight,
                            )
                        }
                    }
                }

                is OnlineServerDataResourceResult.Error ->
                    SelectionContainer(
                        modifier =
                            Modifier.pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.type == PointerEventType.Press) {
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                }
                            },
                    ) {
                        Text(
                            text = t(Res.string.server_unreachable),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                is OnlineServerDataResourceResult.RateLimited ->
                    SelectionContainer(
                        modifier =
                            Modifier.pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        if (event.type == PointerEventType.Press) {
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                }
                            },
                    ) {
                        Text(
                            text = t(Res.string.rate_limited_message),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = MotdMaxLines,
                            lineHeight = MotdLineHeight,
                        )
                    }
            }
        }
    }
}

@Composable
private fun OnlineServerDataRow(
    modifier: Modifier = Modifier,
    result: OnlineServerDataResourceResult<IServerData>,
) {
    val locale = LocalPrefs.current.locale

    val onlineData = (result as OnlineServerDataResourceResult.Success).data as OnlineServerData

    val protocolVersionText = t(Res.string.protocol_format, onlineData.protocolVersion.toString())
    val playerCountText =
        "${onlineData.onlinePlayers.toGroupedString(locale)} / ${onlineData.maxPlayers.toGroupedString(locale)}"

    var obfuscationSeed by remember { mutableStateOf(0) }
    LaunchedEffect(result) {
        while (true) {
            obfuscationSeed = (MotdObfuscationMinimumSeed..Int.MAX_VALUE).random()
            delay(MotdObfuscationUpdateInterval)
        }
    }

    var showDescription by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TwinSpillRows(
            firstRowItems =
                buildList {
                    if (onlineData is McSrvStatOnlineServerData && onlineData.eulaBlocked) {
                        add {
                            InfoChip(
                                Icons.Filled.Error,
                                t(Res.string.info_blocked_by_mojang),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    if (onlineData is McSrvStatOnlineServerData) {
                        onlineData.info?.let { serverData ->
                            var descriptionObfuscationSeed by remember { mutableStateOf(MotdObfuscationMinimumSeed) }

                            var descIsSelecting by remember { mutableStateOf(false) }
                            var descAllSelected by remember { mutableStateOf(false) }
                            val descSelectingState = rememberUpdatedState(descIsSelecting)

                            LaunchedEffect(serverData) {
                                while (true) {
                                    if (!descSelectingState.value) {
                                        descriptionObfuscationSeed =
                                            (MotdObfuscationMinimumSeed..Int.MAX_VALUE).random()
                                    }
                                    delay(MotdObfuscationUpdateInterval)
                                }
                            }

                            add {
                                AssistChip(
                                    onClick = { showDescription = true },
                                    label = { Text(t(Res.string.description_label)) },
                                    modifier = Modifier.height(32.dp).pointerHoverIcon(PointerIcon.Hand),
                                    enabled = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.Subject,
                                            contentDescription = t(Res.string.description_label),
                                            modifier = Modifier.size(18.dp),
                                        )
                                        val copyWithColorCodesText = t(Res.string.copy_with_color_codes)
                                        FloatingDialogBuilder(
                                            visible = showDescription,
                                            onDismissRequest = { showDescription = false },
                                        ) {
                                            RichTooltip(
                                                title = { Text(t(Res.string.server_description_title)) },
                                                text = {
                                                    ContextMenuDataProvider(
                                                        items = {
                                                            buildList {
                                                                if (descAllSelected && serverData.isNotEmpty()) {
                                                                    add(
                                                                        ContextMenuItem(copyWithColorCodesText) {
                                                                            ClipboardUtils.copy(serverData)
                                                                        },
                                                                    )
                                                                }
                                                            }
                                                        },
                                                    ) {
                                                        Card(modifier = Modifier.padding(top = 12.dp)) {
                                                            Box(
                                                                Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(MotdInnerPadding),
                                                            ) {
                                                                val focusRequester = remember { FocusRequester() }

                                                                LaunchedEffect(Unit) {
                                                                    focusRequester.requestFocus()
                                                                }

                                                                HackedSelectionContainer(
                                                                    modifier =
                                                                        Modifier
                                                                            .focusRequester(focusRequester)
                                                                            .focusable()
                                                                            .pointerInput(Unit) {
                                                                                awaitPointerEventScope {
                                                                                    while (true) {
                                                                                        val event = awaitPointerEvent()
                                                                                        if (event.type ==
                                                                                            PointerEventType.Press
                                                                                        ) {
                                                                                            event.changes.forEach {
                                                                                                it.consume()
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            },
                                                                    onSelectedChange = { descIsSelecting = it },
                                                                    onAllSelectedChange = { descAllSelected = it },
                                                                ) {
                                                                    Text(
                                                                        text =
                                                                            serverData.toMinecraftAnnotatedString(
                                                                                descriptionObfuscationSeed,
                                                                            ),
                                                                        style = MaterialTheme.typography.bodyMedium,
                                                                        lineHeight = MotdLineHeight,
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                action = {
                                                    TextButton(
                                                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                                                        onClick = { showDescription = false },
                                                    ) { Text(t(Res.string.ok_label)) }
                                                },
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                    add {
                        InfoChip(
                            Icons.Filled.Info,
                            onlineData.versionInfo.toMinecraftAnnotatedString(obfuscationSeed),
                            sizeToggleEnabled = true,
                        )
                    }
                    if (onlineData is McSrvStatOnlineServerData) {
                        onlineData.versionName?.let {
                            add { InfoChip(Icons.AutoMirrored.Filled.Label, it) }
                        }
                    }
                    add { InfoChip(Icons.Filled.SettingsEthernet, protocolVersionText) }
                },
            secondRowItems =
                buildList {
                    add { InfoChip(Icons.Filled.People, playerCountText) }
                    add { InfoChip(Icons.Filled.Dns, onlineData.ip) }
                    if (onlineData is McServerPingOnlineServerData) {
                        add {
                            InfoChip(
                                icon = Icons.Filled.NetworkPing,
                                label = onlineData.ping.toHumanReadableDuration(),
                                tint = getPingColor(onlineData.ping),
                            )
                        }
                    }
                },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    label: Any,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    sizeToggleEnabled: Boolean = false,
) {
    require(label is String || label is AnnotatedString) {
        "label must be either String or AnnotatedString"
    }

    val labelText =
        when (label) {
            is String -> label
            is AnnotatedString -> label.text
            else -> "" // unreachable
        }

    var isCompact by remember { mutableStateOf(false) }
    if (!sizeToggleEnabled && isCompact) {
        isCompact = false
    }

    val surfaceModifier =
        Modifier
            .height(32.dp)
            .animateContentSize(
                animationSpec =
                    tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing,
                    ),
            ).then(
                if (isCompact) Modifier.width(100.dp) else Modifier,
            )

    val infoChipExpandLabelText = t(Res.string.infochip_expand_label)
    val infoChipCompactLabelText = t(Res.string.infochip_compact_label)
    val menuItems =
        remember(sizeToggleEnabled, isCompact) {
            if (!sizeToggleEnabled) {
                emptyList()
            } else {
                if (isCompact) {
                    listOf(ContextMenuItem(infoChipExpandLabelText) { isCompact = false })
                } else {
                    listOf(ContextMenuItem(infoChipCompactLabelText) { isCompact = true })
                }
            }
        }

    val chipContent = @Composable {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = t(Res.string.cd_info_chip_icon, labelText),
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            SelectionContainer(
                modifier =
                    Modifier.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Press) {
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                    },
            ) {
                when (label) {
                    is String ->
                        Text(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium,
                            color = tint,
                        )

                    is AnnotatedString ->
                        Text(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium,
                            color = tint,
                        )
                }
            }
        }
    }

    if (menuItems.isNotEmpty()) {
        ContextMenuArea(items = { menuItems }) {
            ContextMenuDataProvider(items = { menuItems }) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = surfaceModifier,
                ) {
                    chipContent()
                }
            }
        }
    } else {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = surfaceModifier,
        ) {
            chipContent()
        }
    }
}

@Composable
private fun ShimmerOnlineServerDataRow(
    shimmer: Shimmer,
    uniqueKey: Uuid,
    modifier: Modifier = Modifier,
) {
    val baseRandom = remember(uniqueKey) { Random(uniqueKey.hashCode().toLong()) }
    val requirementWidth =
        remember(uniqueKey) { generateRequirementWidth(Random(baseRandom.nextInt().toLong())) }
    val versionWidth =
        remember(uniqueKey) { generateVersionWidth(Random(baseRandom.nextInt().toLong())) }
    val protocolWidth =
        remember(uniqueKey) { generateProtocolWidth(Random(baseRandom.nextInt().toLong())) }

    val playersChipWidth = 100.dp // corresponds roughly to "500 / 1,000"
    val pingChipWidth = 60.dp // corresponds to "50 ms"

    Column(
        modifier = modifier.fillMaxWidth().shimmer(shimmer),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerInfoChip(width = requirementWidth, shimmer = shimmer)
            ShimmerInfoChip(width = versionWidth, shimmer = shimmer)
            ShimmerInfoChip(width = protocolWidth, shimmer = shimmer)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerInfoChip(width = playersChipWidth, shimmer = shimmer)
            ShimmerInfoChip(width = pingChipWidth, shimmer = shimmer)
        }
    }
}

private fun generateRequirementWidth(random: Random): Dp {
    val r = random.nextFloat()
    return when {
        r < 0.6f -> lerp(50.dp, 100.dp, r / 0.6f)
        r < 0.9f -> lerp(100.dp, 140.dp, (r - 0.6f) / 0.3f)
        else -> lerp(140.dp, 180.dp, (r - 0.9f) / 0.1f)
    }
}

private fun generateVersionWidth(random: Random): Dp {
    val r = random.nextFloat()
    return when {
        r < 0.7f -> lerp(50.dp, 80.dp, r / 0.7f)
        else -> lerp(80.dp, 110.dp, (r - 0.7f) / 0.3f)
    }
}

private fun generateProtocolWidth(random: Random): Dp {
    val r = random.nextFloat()
    return when {
        r < 0.01f -> 32.dp // 1% chance for a one-digit protocol
        r < 0.9999f ->
            lerp(
                50.dp,
                70.dp,
                (r - 0.01f) / 0.9899f,
            ) // 98.99% chance for two/three digits
        else -> 140.dp // 0.01% chance for an extremely wide protocol (e.g., 0x40000092)
    }
}

@Composable
private fun ShimmerInfoChip(
    modifier: Modifier = Modifier,
    width: Dp,
    shimmer: Shimmer,
) {
    val textHeightDp =
        with(LocalDensity.current) {
            MaterialTheme.typography.labelMedium.fontSize
                .toDp()
        }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.height(32.dp).shimmer(shimmer),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant),
            )
            Box(
                modifier =
                    Modifier
                        .height(textHeightDp)
                        .width(width)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        ),
            )
        }
    }
}

@Composable
private fun OnlineServerDataChipRowsSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.height(32.dp).background(Color.White))
        Box(Modifier.height(32.dp).background(Color.White))
    }
}

private fun getPingColor(ping: Long) =
    if (isDarkTheme) {
        when {
            ping <= 1L -> Color(0xFF2E7D32)
            ping <= 200L -> {
                val t = (ping - 1) / 199f
                lerp(Color(0xFF2E7D32), Color(0xFFFFEB3B), t)
            }

            ping <= 500L -> {
                val t = (ping - 200) / 300f
                lerp(Color(0xFFFFEB3B), Color(0xFFF44336), t)
            }

            ping <= 1000L -> {
                val t = (ping - 500) / 500f
                lerp(Color(0xFFF44336), Color(0xFF9C27B0), t)
            }

            else -> Color(0xFF9C27B0)
        }
    } else {
        when {
            ping <= 1L -> Color(0xFF2E7D32)
            ping <= 200L -> {
                val t = (ping - 1) / 199f
                lerp(
                    Color(0xFF2E7D32),
                    Color(0xFFFFA000),
                    t,
                )
            }

            ping <= 500L -> {
                val t = (ping - 200) / 300f
                lerp(Color(0xFFFFA000), Color(0xFFD32F2F), t)
            }

            ping <= 1000L -> {
                val t = (ping - 500) / 500f
                lerp(Color(0xFFD32F2F), Color(0xFF6A1B9A), t)
            }

            else -> Color(0xFF6A1B9A)
        }
    }

@Composable
private fun ServerNameAddress(
    serverName: AnnotatedString,
    serverAddress: AnnotatedString,
    onNameSave: (String) -> Unit,
    onAddressSave: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(MotdInnerPadding),
        ) {
            InlineEditableLabel(
                text = serverName,
                textStyle = MaterialTheme.typography.titleMedium,
                onSave = onNameSave,
                modifier = Modifier.fillMaxWidth(),
            )

            InlineEditableLabel(
                text = serverAddress,
                textStyle = MaterialTheme.typography.bodyMedium,
                onSave = onAddressSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun buildHighlightedString(
    source: String,
    query: String,
    style: SpanStyle =
        SpanStyle(
            background = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
        ),
) = if (query.isBlank()) {
    AnnotatedString(source)
} else {
    val regex = Regex(Regex.escape(query), RegexOption.IGNORE_CASE)
    buildAnnotatedString {
        var lastIndex = 0
        regex.findAll(source).forEach { matchResult ->
            val range = matchResult.range
            // append text before match
            append(source.substring(lastIndex, range.first))
            // append matched text with style
            withStyle(style) {
                append(source.substring(range))
            }
            lastIndex = range.last + 1
        }
        // append remaining text
        if (lastIndex < source.length) {
            append(source.substring(lastIndex))
        }
    }
}

@Composable
private fun rememberServerBitmap(iconBytes: ByteArray?) =
    produceState<ImageBitmap?>(initialValue = null, key1 = iconBytes) {
        value =
            iconBytes
                ?.inputStream()
                ?.safeAsImageBitmapOrNull()
    }

private val MotdLineHeight = 20.sp

/*
 Official Minecraft obfuscation update interval
 Source: ChatGPT-4.1, July 20, 2025
 */
private const val MotdObfuscationUpdateInterval = 80L
private const val MotdObfuscationMinimumSeed = 0
private const val MotdMaxLines = 2
private val MotdInnerPadding = 12.dp
