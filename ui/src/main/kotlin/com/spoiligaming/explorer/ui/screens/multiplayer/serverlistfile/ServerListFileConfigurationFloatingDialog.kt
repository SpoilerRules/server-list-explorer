/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2026 SpoilerRules
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

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalUuidApi::class)

package com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import com.spoiligaming.explorer.serverlist.bookmarks.ServerListFileBookmarkEntry
import com.spoiligaming.explorer.serverlist.bookmarks.ServerListFileBookmarksManager
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalAmoledActive
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.dialog.FloatingDialogBuilder
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.util.rememberServerListFilePickerLauncher
import com.spoiligaming.explorer.ui.widgets.ActionItem
import com.spoiligaming.explorer.ui.widgets.AppVerticalScrollbar
import com.spoiligaming.explorer.ui.widgets.HierarchicalDropdownMenu
import com.spoiligaming.explorer.util.ClipboardUtils
import com.spoiligaming.explorer.util.serverListBookmarkKey
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.mp_error_screen_action_clear_dead_entries
import server_list_explorer.ui.generated.resources.mp_error_screen_action_clear_inactive_entries
import server_list_explorer.ui.generated.resources.mp_error_screen_action_remove_from_list
import server_list_explorer.ui.generated.resources.mp_error_screen_cd_more_options
import server_list_explorer.ui.generated.resources.mp_error_screen_load_selected
import server_list_explorer.ui.generated.resources.mp_error_screen_picker_title_select_server_list_file
import server_list_explorer.ui.generated.resources.mp_error_screen_status_active
import server_list_explorer.ui.generated.resources.quick_action_add
import server_list_explorer.ui.generated.resources.server_list_file_configuration_cd_server_count
import server_list_explorer.ui.generated.resources.server_list_file_configuration_cd_status_active
import server_list_explorer.ui.generated.resources.server_list_file_configuration_cd_status_missing
import server_list_explorer.ui.generated.resources.server_list_file_configuration_dialog_title
import server_list_explorer.ui.generated.resources.server_list_file_configuration_status_missing
import sh.calvin.reorderable.ReorderableItem
import java.nio.file.Files
import java.nio.file.Path
import kotlin.uuid.ExperimentalUuidApi

@Composable
internal fun ServerListFileConfigurationFloatingDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onConfirm: (Path?) -> Unit,
) {
    val prefs = LocalPrefs.current
    val scope = rememberCoroutineScope()

    val controller =
        rememberServerListFileConfigurationFloatingDialogController(
            onDismissRequest = onDismissRequest,
            onConfirm = onConfirm,
        )

    val addFilePickerLauncher =
        rememberServerListFilePickerLauncher(
            title = t(Res.string.mp_error_screen_picker_title_select_server_list_file),
        ) { rawPath ->
            scope.launch {
                controller.handleAddFile(rawPath)
            }
        }

    FloatingDialogBuilder(
        visible = visible,
        onDismissRequest = onDismissRequest,
    ) {
        RichTooltip(
            maxWidth = TooltipMaxWidth,
            modifier =
                Modifier.then(
                    if (LocalAmoledActive.current) {
                        Modifier.border(
                            width = TooltipBorderWidth,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = TOOLTIP_BORDER_ALPHA),
                            shape = TooltipDefaults.richTooltipContainerShape,
                        )
                    } else {
                        Modifier
                    },
                ),
            title = { Text(t(Res.string.server_list_file_configuration_dialog_title)) },
            text = {
                ServerListFileDialogContent(
                    controller = controller,
                    scrollbarAlwaysVisible = prefs.settingsScrollbarAlwaysVisible,
                    onAddClick = {
                        addFilePickerLauncher.launch()
                        controller.listFocusRequester.requestFocus()
                    },
                )
            },
            action = {
                TextButton(
                    modifier =
                        Modifier.pointerHoverIcon(
                            if (controller.selectedEntryExists) PointerIcon.Hand else PointerIcon.Default,
                        ),
                    enabled = controller.selectedEntryExists,
                    onClick = {
                        scope.launch { controller.loadEntry(controller.selectedEntry) }
                    },
                ) {
                    Text(t(Res.string.mp_error_screen_load_selected))
                }
            },
        )
    }

    if (controller.editingEntryId != null && controller.editingEntry == null) {
        controller.dismissEditLabel()
    }

    controller.editingEntry?.let { editingEntry ->
        ServerListFileLabelEditDialog(
            entry = editingEntry,
            onConfirm = { value -> controller.confirmEditLabel(editingEntry, value) },
            onDismissRequest = { controller.dismissEditLabel() },
        )
    }
}

@Composable
private fun ServerListFileDialogContent(
    controller: ServerListFileConfigurationFloatingDialogController,
    scrollbarAlwaysVisible: Boolean,
    onAddClick: () -> Unit,
) = Column(
    modifier =
        Modifier
            .heightIn(max = DialogContentMaxHeight)
            .padding(top = DialogContentTopPadding),
    verticalArrangement = Arrangement.spacedBy(DialogContentVerticalSpacing),
) {
    ServerListFileDialogHeader(
        controller = controller,
        onAddClick = onAddClick,
    )

    ServerListFileDialogList(
        controller = controller,
        scrollbarAlwaysVisible = scrollbarAlwaysVisible,
    )
}

@Composable
private fun ServerListFileDialogHeader(
    controller: ServerListFileConfigurationFloatingDialogController,
    onAddClick: () -> Unit,
) = Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(HeaderHorizontalSpacing),
) {
    FilledTonalButton(
        onClick = onAddClick,
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
    ) {
        Text(t(Res.string.quick_action_add))
    }
    Spacer(Modifier.weight(HEADER_SPACER_WEIGHT))

    val actionMenuEntries =
        buildList {
            if (controller.hasBookmarks) {
                add(
                    ActionItem(
                        text = t(Res.string.mp_error_screen_action_clear_dead_entries),
                        enabled = controller.hasMissingEntries,
                        onClick = {
                            if (!controller.hasMissingEntries) return@ActionItem
                            controller.clearMissingEntries()
                        },
                    ),
                )

                add(
                    ActionItem(
                        text = t(Res.string.mp_error_screen_action_clear_inactive_entries),
                        enabled = controller.hasInactiveEntries,
                        onClick = {
                            if (!controller.hasInactiveEntries) return@ActionItem
                            controller.clearInactiveEntries()
                        },
                    ),
                )
            }
        }

    HierarchicalDropdownMenu(
        entries = actionMenuEntries,
        anchor = { expanded, toggle ->
            val wasExpanded = remember { mutableStateOf(expanded) }

            LaunchedEffect(expanded) {
                if (wasExpanded.value && !expanded) {
                    controller.listFocusRequester.requestFocus()
                }
                wasExpanded.value = expanded
            }

            val enabled = actionMenuEntries.isNotEmpty()
            IconButton(
                onClick = { if (enabled) toggle() },
                enabled = enabled,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = t(Res.string.mp_error_screen_cd_more_options),
                    tint =
                        if (expanded) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
        },
    )
}

@Composable
private fun ColumnScope.ServerListFileDialogList(
    controller: ServerListFileConfigurationFloatingDialogController,
    scrollbarAlwaysVisible: Boolean,
) {
    val scope = rememberCoroutineScope()
    val bookmarkEntries = controller.entries
    val activePath = controller.activePath
    val selectedEntryId = controller.selectedEntryId
    val lazyListState = controller.lazyListState
    val reorderState = controller.reorderState
    val listFocusRequester = controller.listFocusRequester

    Row(
        modifier = Modifier.weight(LIST_CONTAINER_WEIGHT).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ListHorizontalSpacing),
    ) {
        LazyColumn(
            modifier =
                Modifier
                    .weight(LIST_COLUMN_WEIGHT)
                    .focusRequester(listFocusRequester)
                    .focusable()
                    .onPreviewKeyEvent { event ->
                        controller.handleListKeyEvent(event)
                    },
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(ListItemVerticalSpacing),
        ) {
            items(
                items = bookmarkEntries,
                key = { it.id },
            ) { entry ->
                val entryKey = entry.path.serverListBookmarkKey()
                val isActive =
                    activePath?.serverListBookmarkKey() ==
                        entry.path.serverListBookmarkKey()
                val isSelected = entry.id == selectedEntryId

                val style =
                    when {
                        controller.flashKey == null || entryKey != controller.flashKey ->
                            HighlightStyle.None
                        controller.flashIsError -> HighlightStyle.Error
                        else -> HighlightStyle.Added
                    }
                ReorderableItem(reorderState, key = entry.id) { _ ->
                    ServerListFileEntry(
                        entry = entry,
                        active = isActive,
                        selected = isSelected,
                        canRemove = bookmarkEntries.size > MIN_ENTRIES_TO_ALLOW_REMOVAL,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .then(
                                    if (bookmarkEntries.size > DRAGGABLE_HANDLE_MIN_ENTRY_COUNT) {
                                        Modifier.draggableHandle()
                                    } else {
                                        Modifier
                                    },
                                ),
                        onSelect = { controller.setSelection(entry) },
                        onLoad = { scope.launch { controller.loadEntry(entry) } },
                        onOpenFileLocation = { controller.openFileLocation(entry.path) },
                        onOpenContainingFolder = { controller.openContainingFolder(entry.path) },
                        onCopyPath = { ClipboardUtils.copy(entry.path.toString()) },
                        onCopyDirectory = {
                            entry.path.parent?.let { parent ->
                                ClipboardUtils.copy(parent.toString())
                            }
                        },
                        onRemove = {
                            val originalIndex = bookmarkEntries.indexOf(entry)
                            if (originalIndex >= FIRST_VALID_INDEX) {
                                scope.launch { controller.removeEntry(entry, originalIndex) }
                            }
                        },
                        onEditLabel = { controller.onEditLabel(entry) },
                        highlightStyle = style,
                        onHighlightFinished = { controller.handleHighlightFinished(entry) },
                    )
                }
            }
        }

        if (lazyListState.canScrollForward || lazyListState.canScrollBackward) {
            AppVerticalScrollbar(
                controller.scrollbarAdapter,
                alwaysVisible = scrollbarAlwaysVisible,
            )
        }
    }
}

@Composable
private fun ServerListFileEntry(
    entry: ServerListFileBookmarkEntry,
    active: Boolean,
    selected: Boolean,
    canRemove: Boolean,
    onSelect: () -> Unit,
    onLoad: () -> Unit,
    onOpenFileLocation: () -> Unit,
    onOpenContainingFolder: () -> Unit,
    onCopyPath: () -> Unit,
    onCopyDirectory: () -> Unit,
    onRemove: () -> Unit,
    onEditLabel: () -> Unit,
    modifier: Modifier = Modifier,
    highlightStyle: HighlightStyle,
    onHighlightFinished: () -> Unit,
) {
    val fileName = entry.path.fileName?.toString() ?: entry.path.toString()
    var fileExists by remember(entry.id, entry.path) {
        mutableStateOf<Boolean?>(null) // null = not resolved yet
    }

    var serverCount by remember(entry.id, entry.serverCount) {
        mutableStateOf(entry.serverCount)
    }
    var isResolvingServerCount by remember(entry.id, entry.path) {
        mutableStateOf(false)
    }

    var previousHighlightStyle by remember {
        mutableStateOf(HighlightStyle.None)
    }

    LaunchedEffect(entry.id, entry.path) {
        isResolvingServerCount = true

        val loadResult =
            withContext(Dispatchers.IO) {
                val exists =
                    runCatching { Files.exists(entry.path) }
                        .onFailure { throwable ->
                            logger.debug(throwable) {
                                "Failed checking existence for ${entry.path}"
                            }
                        }.getOrDefault(false)

                if (!exists) {
                    ServerLoadResult(fileExists = false, serverCount = null)
                } else {
                    val count =
                        runCatching {
                            val repository = ServerListRepository(entry.path)
                            repository.load()
                            repository.all().size
                        }.onFailure { throwable ->
                            logger.debug(throwable) {
                                "Failed resolving server count for ${entry.path}"
                            }
                        }.getOrNull()

                    ServerLoadResult(fileExists = true, serverCount = count)
                }
            }

        fileExists = loadResult.fileExists
        serverCount = loadResult.serverCount
        isResolvingServerCount = false

        if (entry.serverCount != loadResult.serverCount) {
            ServerListFileBookmarksManager.updateServerCount(entry.id, loadResult.serverCount)
        }
    }

    val baseBackgroundColor =
        if (active) {
            MaterialTheme.colorScheme.surfaceContainerHighest
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }

    val highlightBackgroundColorOverride =
        when (highlightStyle) {
            HighlightStyle.Added -> MaterialTheme.colorScheme.secondaryContainer
            HighlightStyle.Error -> MaterialTheme.colorScheme.errorContainer
            HighlightStyle.None -> null
        }

    val shouldAnimateHighlight =
        highlightStyle != HighlightStyle.None || previousHighlightStyle != HighlightStyle.None

    val backgroundColor by animateColorAsState(
        targetValue = highlightBackgroundColorOverride ?: baseBackgroundColor,
        animationSpec =
            if (shouldAnimateHighlight) {
                tween(durationMillis = HIGHLIGHT_ANIMATION_MILLIS)
            } else {
                snap()
            },
        label = "ServerListFileEntryBackground",
    )

    LaunchedEffect(highlightStyle) {
        if (highlightStyle != HighlightStyle.None) {
            delay(HIGHLIGHT_ANIMATION_MILLIS.toLong())
            onHighlightFinished()
        }
        previousHighlightStyle = highlightStyle
    }

    val resolvedFileExists = fileExists == true

    val serverCountForDisplay =
        if (resolvedFileExists && !isResolvingServerCount) {
            serverCount
        } else {
            null
        }

    val borderStroke =
        if (selected) {
            BorderStroke(
                SelectedBorderWidth,
                MaterialTheme.colorScheme.primary.copy(alpha = SELECTED_BORDER_ALPHA),
            )
        } else {
            BorderStroke(ZeroDp, Color.Transparent)
        }

    val fileActionContextItems =
        if (resolvedFileExists) {
            ServerListFileMenuActionCatalog.toContextItems(
                handlers =
                    ServerListFileMenuActionHandlers(
                        onSetActiveFile = onLoad,
                        onOpenFileLocation = onOpenFileLocation,
                        onShowContainingFolder = onOpenContainingFolder,
                        onCopyAbsolutePath = onCopyPath,
                        onCopyFolderPath = onCopyDirectory,
                        onCopyServerListFileName = { ClipboardUtils.copy(fileName) },
                    ),
            )
        } else {
            emptyList()
        }
    val labelActionTitle = t(serverListFileEditLabelActionTextResource(entry.label))
    val removeFromListActionTitle = t(Res.string.mp_error_screen_action_remove_from_list)

    ContextMenuArea(
        items = {
            buildList {
                add(ContextMenuItem(labelActionTitle) { onEditLabel() })

                addAll(fileActionContextItems)

                if (canRemove) {
                    add(ContextMenuItem(removeFromListActionTitle) { onRemove() })
                }
            }
        },
    ) {
        Surface(
            modifier =
                modifier
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(enabled = fileExists == true, onClick = onSelect)
                    .pointerHoverIcon(if (fileExists == true) PointerIcon.Hand else PointerIcon.Default),
            color = backgroundColor,
            shape = MaterialTheme.shapes.medium,
            border = borderStroke,
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
            ) {
                Column(
                    modifier =
                        Modifier
                            .weight(ENTRY_CONTENT_WEIGHT)
                            .padding(
                                horizontal = EntryContentHorizontalPadding,
                                vertical = EntryContentVerticalPadding,
                            ),
                    verticalArrangement = Arrangement.spacedBy(EntryContentVerticalSpacing),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        entry.label?.let { label ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(LabelRowSpacing),
                                modifier =
                                    Modifier
                                        .weight(LABEL_ROW_WEIGHT)
                                        .padding(end = LabelRowEndPadding),
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = FILE_LABEL_MAX_LINES,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(FILE_LABEL_WEIGHT, fill = false),
                                )

                                Row {
                                    // TODO: abstract delimiters for CJK/full-width glyphs or region-specific typography once localization expands
                                    Text(
                                        text = "(",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )

                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = FILE_NAME_MAX_LINES,
                                        overflow = TextOverflow.Ellipsis,
                                    )

                                    Text(
                                        text = ")",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } ?: Text(
                            text = fileName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = FILE_NAME_MAX_LINES,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                                Modifier.weight(FILE_NAME_FALLBACK_WEIGHT).padding(end = FileNameFallbackEndPadding),
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(StatusRowSpacing),
                        ) {
                            if (resolvedFileExists) {
                                AnimatedContent(
                                    targetState = serverCountForDisplay,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "ServerCountAnimation",
                                ) { count ->
                                    if (count != null) {
                                        StatusIndicatorPill(
                                            label = count.toString(),
                                            leadingIcon = Icons.Default.Storage,
                                            leadingIconContentDescription =
                                                t(
                                                    Res.string.server_list_file_configuration_cd_server_count,
                                                ),
                                            colors = StatusIndicatorPillDefaults.primaryColors(),
                                        )
                                    }
                                }
                            }

                            if (active) {
                                StatusIndicatorPill(
                                    label = t(Res.string.mp_error_screen_status_active),
                                    leadingIcon = Icons.Outlined.Check,
                                    colors = StatusIndicatorPillDefaults.primaryColors(),
                                    leadingIconContentDescription =
                                        t(
                                            Res.string.server_list_file_configuration_cd_status_active,
                                        ),
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PathRowSpacing),
                    ) {
                        if (fileExists == false) {
                            StatusIndicatorPill(
                                label = t(Res.string.server_list_file_configuration_status_missing),
                                colors = StatusIndicatorPillDefaults.errorColors(),
                                contentHorizontalPadding =
                                    StatusIndicatorPillDefaults.compactContentHorizontalPadding(),
                                leadingIconContentDescription =
                                    t(
                                        Res.string.server_list_file_configuration_cd_status_missing,
                                    ),
                            )
                        }

                        Text(
                            text = entry.path.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = ENTRY_PATH_MAX_LINES,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

private data class ServerLoadResult(
    val fileExists: Boolean,
    val serverCount: Int?,
)

private enum class HighlightStyle { None, Added, Error }

private const val HIGHLIGHT_ANIMATION_MILLIS = 800
private const val FIRST_VALID_INDEX = 0
private const val TOOLTIP_BORDER_ALPHA = 0.35f
private const val HEADER_SPACER_WEIGHT = 1f
private const val LIST_CONTAINER_WEIGHT = 1f
private const val LIST_COLUMN_WEIGHT = 1f
private const val DRAGGABLE_HANDLE_MIN_ENTRY_COUNT = 2
private const val MIN_ENTRIES_TO_ALLOW_REMOVAL = 1
private const val SELECTED_BORDER_ALPHA = 0.8f
private const val LABEL_ROW_WEIGHT = 1f
private const val FILE_LABEL_MAX_LINES = 1
private const val FILE_NAME_MAX_LINES = 1
private const val FILE_LABEL_WEIGHT = 1f
private const val FILE_NAME_FALLBACK_WEIGHT = 1f
private const val ENTRY_PATH_MAX_LINES = 1
private const val ENTRY_CONTENT_WEIGHT = 1f

private val TooltipMaxWidth = 400.dp
private val TooltipBorderWidth = 1.dp
private val SelectedBorderWidth = 1.5.dp
private val DialogContentMaxHeight = 300.dp
private val DialogContentTopPadding = 4.dp
private val DialogContentVerticalSpacing = 4.dp
private val HeaderHorizontalSpacing = 8.dp
private val ListHorizontalSpacing = 4.dp
private val ListItemVerticalSpacing = 4.dp
private val ZeroDp = 0.dp
private val EntryContentHorizontalPadding = 16.dp
private val EntryContentVerticalPadding = 12.dp
private val EntryContentVerticalSpacing = 6.dp
private val LabelRowSpacing = 2.dp
private val LabelRowEndPadding = 4.dp
private val FileNameFallbackEndPadding = 4.dp
private val StatusRowSpacing = 4.dp
private val PathRowSpacing = 4.dp

private val logger = KotlinLogging.logger {}
