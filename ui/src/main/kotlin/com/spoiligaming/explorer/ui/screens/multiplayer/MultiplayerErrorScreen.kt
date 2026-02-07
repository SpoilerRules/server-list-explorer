/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025-2026 SpoilerRules
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

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)

package com.spoiligaming.explorer.ui.screens.multiplayer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.serverlist.bookmarks.ServerListFileBookmarkEntry
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile.AddResult
import com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile.ServerListFileBookmarksController
import com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile.ServerListFileLabelEditDialog
import com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile.ServerListFileMenuActionCatalog
import com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile.ServerListFileMenuActionHandlers
import com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile.rememberServerListFileBookmarksController
import com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile.serverListFileEditLabelActionTextResource
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.theme.LocalDarkTheme
import com.spoiligaming.explorer.ui.theme.activeTint
import com.spoiligaming.explorer.ui.util.rememberServerListFilePickerLauncher
import com.spoiligaming.explorer.ui.widgets.ActionItem
import com.spoiligaming.explorer.ui.widgets.AppVerticalScrollbar
import com.spoiligaming.explorer.ui.widgets.HierarchicalDropdownMenu
import com.spoiligaming.explorer.ui.widgets.SubmenuItem
import com.spoiligaming.explorer.util.ClipboardUtils
import com.spoiligaming.explorer.util.serverListBookmarkKey
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.cd_search_icon
import server_list_explorer.ui.generated.resources.ic_fa_grip_vertical
import server_list_explorer.ui.generated.resources.mp_error_screen_action_clear_dead_entries
import server_list_explorer.ui.generated.resources.mp_error_screen_action_clear_inactive_entries
import server_list_explorer.ui.generated.resources.mp_error_screen_action_file_actions
import server_list_explorer.ui.generated.resources.mp_error_screen_action_remove_from_list
import server_list_explorer.ui.generated.resources.mp_error_screen_add_server_list_file
import server_list_explorer.ui.generated.resources.mp_error_screen_cd_drag_handle
import server_list_explorer.ui.generated.resources.mp_error_screen_cd_more_options
import server_list_explorer.ui.generated.resources.mp_error_screen_header_message
import server_list_explorer.ui.generated.resources.mp_error_screen_header_title
import server_list_explorer.ui.generated.resources.mp_error_screen_load_selected
import server_list_explorer.ui.generated.resources.mp_error_screen_picker_title_select_server_list_file
import server_list_explorer.ui.generated.resources.mp_error_screen_search_placeholder
import server_list_explorer.ui.generated.resources.mp_error_screen_server_count
import server_list_explorer.ui.generated.resources.mp_error_screen_status_active
import server_list_explorer.ui.generated.resources.mp_error_screen_status_inactive
import server_list_explorer.ui.generated.resources.mp_error_screen_status_path_not_found
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.nio.file.Files
import java.nio.file.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
internal fun MultiplayerErrorScreen(onReloadRequest: () -> Unit) {
    val prefs = LocalPrefs.current
    val scope = rememberCoroutineScope()
    val controller = rememberServerListFileBookmarksController()
    val bookmarkEntries by controller.entries.collectAsState(emptyList())
    val activePath = controller.activePath
    val selectedEntryId = controller.selectedEntryId

    var searchQuery by remember { mutableStateOf("") }
    var editingEntryId by remember { mutableStateOf<Uuid?>(null) }

    val filteredEntries =
        rememberFilteredEntries(
            entries = bookmarkEntries,
            searchQuery = searchQuery,
        )

    val lazyListState = rememberLazyListState()
    val scrollbarAdapter = rememberScrollbarAdapter(lazyListState)
    val reorderableState =
        rememberBookmarksReorderableState(
            lazyListState = lazyListState,
            filteredEntries = filteredEntries,
            bookmarkEntries = bookmarkEntries,
            onMove = controller::moveAsync,
        )

    EnsureValidSelection(
        controller = controller,
        bookmarkEntries = bookmarkEntries,
    )

    val addFilePickerLauncher =
        rememberServerListFilePickerLauncher(
            title = t(Res.string.mp_error_screen_picker_title_select_server_list_file),
        ) { rawPath ->
            scope.launch {
                handleAddResult(
                    result =
                        controller.addFile(
                            rawPath = rawPath,
                            autoSelect = false,
                        ),
                    controller = controller,
                    bookmarkEntries = bookmarkEntries,
                    lazyListState = lazyListState,
                )
            }
        }

    val loadEntry: suspend (ServerListFileBookmarkEntry?) -> Unit = { entry ->
        loadEntryAndReload(
            controller = controller,
            entry = entry,
            onReloadRequest = onReloadRequest,
        )
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(ScreenPadding)
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier =
                Modifier
                    .widthIn(min = DialogMinWidth, max = DialogMaxWidth)
                    .fillMaxWidth()
                    .heightIn(max = DialogMaxHeight),
            shape = DialogShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = DialogShadowElevation,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(DialogContentPadding)
                        .fillMaxHeight(),
            ) {
                HeaderSection()

                Spacer(Modifier.height(HeaderBottomSpacing))

                ControlBar(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onAddClick = { addFilePickerLauncher.launch() },
                    onClearDead = { controller.pruneMissingAsync() },
                    onClearInactive = { controller.clearExceptActiveAsync() },
                )

                Spacer(Modifier.height(ControlBarBottomSpacing))

                ServerListContent(
                    entries = filteredEntries,
                    allEntries = bookmarkEntries,
                    activePath = activePath,
                    selectedEntryId = selectedEntryId,
                    lazyListState = lazyListState,
                    reorderableState = reorderableState,
                    scrollbarAdapter = scrollbarAdapter,
                    scrollBarAlwaysVisible = prefs.settingsScrollbarAlwaysVisible,
                    onSelectEntry = { entryId -> toggleSelection(controller, entryId) },
                    onLoadSelected = {
                        scope.launch { loadEntry(bookmarkEntries.firstOrNull { it.id == selectedEntryId }) }
                    },
                    onEditLabel = { entryId -> editingEntryId = entryId },
                    onRemove = { entry ->
                        scope.launch {
                            removeEntryAndUpdateActive(
                                controller = controller,
                                entry = entry,
                                bookmarkEntries = bookmarkEntries,
                            )
                        }
                    },
                    onSetActive = { entry -> scope.launch { loadEntry(entry) } },
                    onOpenFileLocation = controller::openFileLocation,
                    onOpenContainingFolder = controller::openContainingFolder,
                    onCopyPath = { path -> ClipboardUtils.copy(path.toString()) },
                    onCopyDirectory = { path ->
                        path.parent?.let { parent -> ClipboardUtils.copy(parent.toString()) }
                    },
                    onCopyFileName = { path ->
                        val fileName = path.fileName?.toString() ?: path.toString()
                        ClipboardUtils.copy(fileName)
                    },
                    modifier = Modifier.weight(CONTENT_WEIGHT),
                )
            }
        }
    }

    editingEntryId =
        rememberEditingEntryId(
            editingEntryId = editingEntryId,
            bookmarkEntries = bookmarkEntries,
        )
    EditLabelDialog(
        editingEntryId = editingEntryId,
        bookmarkEntries = bookmarkEntries,
        onConfirm = { entry, value ->
            scope.launch {
                controller.updateLabel(entry, value)
            }
            editingEntryId = null
        },
        onDismiss = { editingEntryId = null },
    )
}

@Composable
private fun rememberFilteredEntries(
    entries: List<ServerListFileBookmarkEntry>,
    searchQuery: String,
) = remember(entries, searchQuery) {
    val query = searchQuery.trim()
    if (query.isBlank()) {
        entries
    } else {
        entries.filter { entry ->
            entry.label?.contains(query, ignoreCase = true) == true ||
                entry.path.toString().contains(query, ignoreCase = true)
        }
    }
}

@Composable
private fun rememberBookmarksReorderableState(
    lazyListState: LazyListState,
    filteredEntries: List<ServerListFileBookmarkEntry>,
    bookmarkEntries: List<ServerListFileBookmarkEntry>,
    onMove: (Int, Int) -> Unit,
) = rememberReorderableLazyListState(lazyListState) { from, to ->
    val fromEntry = filteredEntries.getOrNull(from.index) ?: return@rememberReorderableLazyListState
    val toEntry = filteredEntries.getOrNull(to.index) ?: return@rememberReorderableLazyListState
    val fromIndex = bookmarkEntries.indexOfFirst { it.id == fromEntry.id }
    val toIndex = bookmarkEntries.indexOfFirst { it.id == toEntry.id }
    if (fromIndex == -1 || toIndex == -1) return@rememberReorderableLazyListState
    onMove(fromIndex, toIndex)
}

@Composable
private fun EnsureValidSelection(
    controller: ServerListFileBookmarksController,
    bookmarkEntries: List<ServerListFileBookmarkEntry>,
) {
    val selectedEntryId = controller.selectedEntryId
    LaunchedEffect(bookmarkEntries, selectedEntryId) {
        val hasValidSelection =
            selectedEntryId?.let { id -> bookmarkEntries.any { it.id == id } } ?: false

        if (bookmarkEntries.isEmpty()) {
            controller.selectedEntryId = null
            return@LaunchedEffect
        }

        if (!hasValidSelection) {
            controller.selectedEntryId = null
        }
    }
}

private fun toggleSelection(
    controller: ServerListFileBookmarksController,
    entryId: Uuid,
) {
    controller.selectedEntryId =
        if (controller.selectedEntryId == entryId) null else entryId
}

private suspend fun loadEntryAndReload(
    controller: ServerListFileBookmarksController,
    entry: ServerListFileBookmarkEntry?,
    onReloadRequest: () -> Unit,
) {
    if (entry == null) return
    val selected = controller.selectEntry(entry)
    if (!selected) return
    onReloadRequest()
}

private suspend fun removeEntryAndUpdateActive(
    controller: ServerListFileBookmarksController,
    entry: ServerListFileBookmarkEntry,
    bookmarkEntries: List<ServerListFileBookmarkEntry>,
) {
    if (bookmarkEntries.size <= 1) return

    val removedKey = entry.path.serverListBookmarkKey()
    val result = controller.remove(entry)

    if (result != null) {
        val currentActiveKey = controller.activePath?.serverListBookmarkKey()
        if (removedKey == currentActiveKey) {
            controller.selectEntry(result)
        }
    }
}

private suspend fun handleAddResult(
    result: AddResult,
    controller: ServerListFileBookmarksController,
    bookmarkEntries: List<ServerListFileBookmarkEntry>,
    lazyListState: LazyListState,
) {
    when (result) {
        is AddResult.Added -> {
            val targetIndex = bookmarkEntries.indexOfFirst { it.id == result.entry.id }
            if (targetIndex >= 0) {
                lazyListState.animateScrollToItem(targetIndex)
            }
        }
        is AddResult.Duplicate -> {
            val existingIndex = bookmarkEntries.indexOfFirst { it.id == result.entry.id }
            if (existingIndex >= 0) {
                lazyListState.animateScrollToItem(existingIndex)
            }
            controller.showDuplicateFileWarning()
        }
        AddResult.Error -> Unit
    }
}

@Composable
private fun rememberEditingEntryId(
    editingEntryId: Uuid?,
    bookmarkEntries: List<ServerListFileBookmarkEntry>,
): Uuid? {
    val editingEntry =
        remember(editingEntryId, bookmarkEntries) {
            editingEntryId?.let { id -> bookmarkEntries.firstOrNull { it.id == id } }
        }
    return if (editingEntryId != null && editingEntry == null) null else editingEntryId
}

@Composable
private fun EditLabelDialog(
    editingEntryId: Uuid?,
    bookmarkEntries: List<ServerListFileBookmarkEntry>,
    onConfirm: (ServerListFileBookmarkEntry, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val editingEntry =
        remember(editingEntryId, bookmarkEntries) {
            editingEntryId?.let { id -> bookmarkEntries.firstOrNull { it.id == id } }
        }
    if (editingEntry == null) return

    ServerListFileLabelEditDialog(
        entry = editingEntry,
        onConfirm = { value -> onConfirm(editingEntry, value) },
        onDismissRequest = onDismiss,
    )
}

@Composable
private fun HeaderSection() =
    Column(verticalArrangement = Arrangement.spacedBy(HeaderSectionSpacing)) {
        Text(
            text = t(Res.string.mp_error_screen_header_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = t(Res.string.mp_error_screen_header_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

@Composable
private fun ControlBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onClearDead: () -> Unit,
    onClearInactive: () -> Unit,
) = Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(ControlBarSectionSpacing),
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = searchQuery,
                onQueryChange = onSearchChange,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                placeholder = {
                    Text(
                        t(Res.string.mp_error_screen_search_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = SINGLE_LINE_MAX_LINES,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = t(Res.string.cd_search_icon))
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier =
            Modifier
                .fillMaxWidth()
                .height(SearchBarHeight),
    ) {}

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ControlBarActionsSpacing),
    ) {
        FilledTonalButton(
            onClick = onAddClick,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AddButtonContentSpacing),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(AddButtonIconSize),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = t(Res.string.mp_error_screen_add_server_list_file),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        ManageSplitButton(
            onPrimaryExecute = { action ->
                when (action) {
                    ManageAction.ClearDead -> onClearDead()
                    ManageAction.ClearInactive -> onClearInactive()
                }
            },
            onAlternateExecute = { action ->
                when (action) {
                    ManageAction.ClearDead -> onClearDead()
                    ManageAction.ClearInactive -> onClearInactive()
                }
            },
        )
    }
}

private enum class ManageAction(
    val label: StringResource,
) {
    ClearDead(Res.string.mp_error_screen_action_clear_dead_entries),
    ClearInactive(Res.string.mp_error_screen_action_clear_inactive_entries),
}

@Composable
private fun ManageSplitButton(
    onPrimaryExecute: (ManageAction) -> Unit,
    onAlternateExecute: (ManageAction) -> Unit,
) {
    val clearInactiveLabel = t(ManageAction.ClearInactive.label)
    val clearDeadLabel = t(ManageAction.ClearDead.label)
    val entries =
        remember(onAlternateExecute, clearInactiveLabel) {
            listOf(
                ActionItem(
                    text = clearInactiveLabel,
                    onClick = { onAlternateExecute(ManageAction.ClearInactive) },
                ),
            )
        }

    SplitButtonLayout(
        leadingButton = {
            SplitButtonDefaults.OutlinedLeadingButton(
                onClick = { onPrimaryExecute(ManageAction.ClearDead) },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
            ) {
                Text(
                    text = clearDeadLabel,
                    maxLines = SINGLE_LINE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        trailingButton = {
            HierarchicalDropdownMenu(
                entries = entries,
                anchor = { expanded, toggle ->
                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) EXPANDED_CHEVRON_ROTATION else COLLAPSED_CHEVRON_ROTATION,
                        label = "ChevronRotation",
                    )

                    SplitButtonDefaults.OutlinedTrailingButton(
                        checked = expanded,
                        onCheckedChange = { toggle() },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .size(SplitButtonDefaults.TrailingIconSize)
                                    .rotate(rotation),
                        )
                    }
                },
            )
        },
    )
}

@Composable
private fun ServerListContent(
    entries: List<ServerListFileBookmarkEntry>,
    allEntries: List<ServerListFileBookmarkEntry>,
    activePath: Path?,
    selectedEntryId: Uuid?,
    lazyListState: LazyListState,
    reorderableState: ReorderableLazyListState,
    scrollbarAdapter: ScrollbarAdapter,
    scrollBarAlwaysVisible: Boolean,
    onSelectEntry: (Uuid) -> Unit,
    onLoadSelected: () -> Unit,
    onEditLabel: (Uuid) -> Unit,
    onRemove: (ServerListFileBookmarkEntry) -> Unit,
    onSetActive: (ServerListFileBookmarkEntry) -> Unit,
    onOpenFileLocation: (Path) -> Unit,
    onOpenContainingFolder: (Path) -> Unit,
    onCopyPath: (Path) -> Unit,
    onCopyDirectory: (Path) -> Unit,
    onCopyFileName: (Path) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedEntry = selectedEntryId?.let { entryId -> allEntries.firstOrNull { it.id == entryId } }
    val canLoadSelected = selectedEntry != null && Files.exists(selectedEntry.path)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ServerListContentSpacing),
    ) {
        Row(
            modifier = Modifier.weight(CONTENT_WEIGHT).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ServerListMainRowSpacing),
        ) {
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(ServerListItemsSpacing),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(CONTENT_WEIGHT),
            ) {
                items(entries, key = { it.id }) { entry ->
                    ReorderableItem(reorderableState, key = entry.id) { isDragging ->
                        val handleModifier = with(this) { Modifier.draggableHandle() }
                        ServerListItem(
                            entry = entry,
                            isDragging = isDragging,
                            isSelected = entry.id == selectedEntryId,
                            activePath = activePath,
                            dragHandleModifier = handleModifier,
                            onSelect = { onSelectEntry(entry.id) },
                            onEditLabel = { onEditLabel(entry.id) },
                            onRemove = { onRemove(entry) },
                            onSetActive = { onSetActive(entry) },
                            onOpenFileLocation = { onOpenFileLocation(entry.path) },
                            onOpenContainingFolder = { onOpenContainingFolder(entry.path) },
                            onCopyPath = { onCopyPath(entry.path) },
                            onCopyDirectory = { onCopyDirectory(entry.path) },
                            onCopyFileName = { onCopyFileName(entry.path) },
                        )
                    }
                }
            }

            if (lazyListState.canScrollForward || lazyListState.canScrollBackward) {
                AppVerticalScrollbar(
                    scrollbarAdapter,
                    alwaysVisible = scrollBarAlwaysVisible,
                )
            }
        }

        FilledTonalButton(
            onClick = onLoadSelected,
            enabled = canLoadSelected,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (canLoadSelected) {
                            Modifier.pointerHoverIcon(PointerIcon.Hand)
                        } else {
                            Modifier
                        },
                    ),
        ) {
            Text(
                text = t(Res.string.mp_error_screen_load_selected),
                maxLines = SINGLE_LINE_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ServerListItem(
    entry: ServerListFileBookmarkEntry,
    isDragging: Boolean,
    isSelected: Boolean,
    activePath: Path?,
    dragHandleModifier: Modifier,
    onSelect: () -> Unit,
    onEditLabel: () -> Unit,
    onRemove: () -> Unit,
    onSetActive: () -> Unit,
    onOpenFileLocation: () -> Unit,
    onOpenContainingFolder: () -> Unit,
    onCopyPath: () -> Unit,
    onCopyDirectory: () -> Unit,
    onCopyFileName: () -> Unit,
) {
    val isDark = LocalDarkTheme.current
    val activeKey = activePath?.serverListBookmarkKey()
    val entryKey = entry.path.serverListBookmarkKey()

    var fileExists by remember(entry.id, entry.path) { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(entry.id, entry.path) {
        val exists =
            withContext(Dispatchers.IO) {
                runCatching { Files.exists(entry.path) }
                    .onFailure { e ->
                        logger.debug(e) { "Failed checking existence for ${entry.path}" }
                    }.getOrDefault(false)
            }
        fileExists = exists
    }

    val isDead = fileExists == false
    val isActive = activeKey != null && activeKey == entryKey

    val activeTint = MaterialTheme.colorScheme.activeTint
    val deadTint = MaterialTheme.colorScheme.error
    val selectionBorderTint =
        if (isDark) {
            MaterialTheme.colorScheme.primary.copy(alpha = SELECTION_BORDER_DARK_ALPHA)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = SELECTION_BORDER_LIGHT_ALPHA)
        }
    val accentColor =
        when {
            isActive -> activeTint
            isDead -> deadTint
            else -> null
        }

    val selectionOverlayBase =
        accentColor?.copy(alpha = ACCENT_OVERLAY_BASE_ALPHA) ?: if (isDark) Color.White else Color.Black
    val dragOverlayBase =
        accentColor?.copy(alpha = ACCENT_OVERLAY_BASE_ALPHA) ?: if (isDark) Color.White else Color.Black

    val selectionOverlayAlpha =
        if (accentColor != null) {
            SELECTION_OVERLAY_ACCENT_ALPHA
        } else if (isDark) {
            SELECTION_OVERLAY_DARK_ALPHA
        } else {
            SELECTION_OVERLAY_LIGHT_ALPHA
        }
    val dragOverlayAlpha =
        if (accentColor != null) {
            DRAG_OVERLAY_ACCENT_ALPHA
        } else if (isDark) {
            DRAG_OVERLAY_DARK_ALPHA
        } else {
            DRAG_OVERLAY_LIGHT_ALPHA
        }
    val dragOverlaySelectedAlpha =
        if (accentColor != null) {
            DRAG_OVERLAY_SELECTED_ACCENT_ALPHA
        } else if (isDark) {
            DRAG_OVERLAY_SELECTED_DARK_ALPHA
        } else {
            DRAG_OVERLAY_SELECTED_LIGHT_ALPHA
        }

    val selectionOverlayTint = selectionOverlayBase.copy(alpha = selectionOverlayBase.alpha * selectionOverlayAlpha)
    val dragOverlayTint = dragOverlayBase.copy(alpha = dragOverlayBase.alpha * dragOverlayAlpha)
    val dragOverlaySelectedTint = dragOverlayBase.copy(alpha = dragOverlayBase.alpha * dragOverlaySelectedAlpha)

    val containerColor =
        when {
            isDragging -> if (isSelected) dragOverlaySelectedTint else dragOverlayTint
            isSelected -> selectionOverlayTint
            else -> Color.Transparent
        }

    val (statusIcon, statusTint, statusText) =
        when {
            isDead ->
                Triple(
                    Icons.Default.Warning,
                    deadTint,
                    t(Res.string.mp_error_screen_status_path_not_found),
                )
            isActive ->
                Triple(
                    Icons.Default.CheckCircle,
                    activeTint,
                    t(Res.string.mp_error_screen_status_active),
                )
            else ->
                Triple(
                    Icons.Default.PauseCircle,
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = INACTIVE_STATUS_ALPHA),
                    t(Res.string.mp_error_screen_status_inactive),
                )
        }

    val borderColor =
        when {
            accentColor != null -> accentColor
            isSelected -> selectionBorderTint
            else -> MaterialTheme.colorScheme.outline
        }

    val displayLabel =
        entry.label?.takeIf { it.isNotBlank() }
            ?: entry.path.fileName?.toString()
            ?: entry.path.toString()

    val titleColor =
        when {
            isDead -> deadTint
            isActive -> activeTint
            else -> MaterialTheme.colorScheme.onSurface
        }
    val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant

    val handleTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HANDLE_TINT_ALPHA)

    val menuTint = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier =
            Modifier.fillMaxWidth().clip(EntryShape).clickable(
                onClick = onSelect,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ),
        shape = EntryShape,
        color = containerColor,
        border = BorderStroke(EntryBorderWidth, borderColor),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = EntryContentHorizontalPadding,
                    vertical = EntryContentVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = dragHandleModifier.size(DragHandleTouchTargetSize),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_fa_grip_vertical),
                    contentDescription = t(Res.string.mp_error_screen_cd_drag_handle),
                    tint = handleTint,
                    modifier = Modifier.size(DragHandleIconSize),
                )
            }
            Spacer(Modifier.width(DragHandleToContentSpacing))

            Row(
                modifier = Modifier.weight(CONTENT_WEIGHT),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // file info
                Column(
                    modifier = Modifier.weight(CONTENT_WEIGHT),
                ) {
                    Text(
                        text = displayLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = titleColor,
                        maxLines = SINGLE_LINE_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = entry.path.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryColor,
                        maxLines = SINGLE_LINE_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // right metadata + status
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = MetadataStartPadding, end = MetadataEndPadding),
                ) {
                    val serverCount = entry.serverCount
                    if (!isDead && serverCount != null) {
                        Text(
                            text = t(Res.string.mp_error_screen_server_count, serverCount),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = SERVER_COUNT_ALPHA),
                        )
                    }

                    Spacer(Modifier.height(StatusTopSpacing))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusTint,
                            modifier = Modifier.size(StatusIconSize),
                        )
                        Spacer(Modifier.width(StatusTextSpacing))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusTint,
                            maxLines = SINGLE_LINE_MAX_LINES,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                val labelActionTitle =
                    t(serverListFileEditLabelActionTextResource(entry.label))
                val resolvedFileExists = fileExists == true

                HierarchicalDropdownMenu(
                    entries =
                        buildList {
                            add(
                                ActionItem(
                                    text = labelActionTitle,
                                    onClick = onEditLabel,
                                ),
                            )
                            add(
                                ActionItem(
                                    text = t(Res.string.mp_error_screen_action_remove_from_list),
                                    onClick = onRemove,
                                ),
                            )
                            if (resolvedFileExists) {
                                add(
                                    SubmenuItem(
                                        text = t(Res.string.mp_error_screen_action_file_actions),
                                        children =
                                            ServerListFileMenuActionCatalog.toDropdownItems(
                                                handlers =
                                                    ServerListFileMenuActionHandlers(
                                                        onSetActiveFile = onSetActive,
                                                        onOpenFileLocation = onOpenFileLocation,
                                                        onShowContainingFolder = onOpenContainingFolder,
                                                        onCopyAbsolutePath = onCopyPath,
                                                        onCopyFolderPath = onCopyDirectory,
                                                        onCopyServerListFileName = onCopyFileName,
                                                    ),
                                            ),
                                    ),
                                )
                            }
                        },
                    anchor = { _, toggle ->
                        IconButton(
                            onClick = toggle,
                            modifier = Modifier.size(EntryMenuButtonSize).pointerHoverIcon(PointerIcon.Hand),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent),
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = t(Res.string.mp_error_screen_cd_more_options),
                                tint = menuTint,
                                modifier = Modifier.size(EntryMenuIconSize),
                            )
                        }
                    },
                )
            }
        }
    }
}

private val logger = KotlinLogging.logger {}

private val ScreenPadding = 24.dp
private val DialogShape = RoundedCornerShape(24.dp)
private val EntryShape = RoundedCornerShape(12.dp)
private val DialogMinWidth = 600.dp
private val DialogMaxWidth = 800.dp
private val DialogMaxHeight = 719.dp
private val DialogShadowElevation = 10.dp
private val DialogContentPadding = 32.dp
private val HeaderBottomSpacing = 20.dp
private val ControlBarBottomSpacing = 12.dp
private val HeaderSectionSpacing = 4.dp
private val ControlBarSectionSpacing = 12.dp
private val ServerListContentSpacing = 12.dp
private val ServerListMainRowSpacing = 4.dp
private val ServerListItemsSpacing = 8.dp
private val SearchBarHeight = 56.dp
private val ControlBarActionsSpacing = 12.dp
private val AddButtonContentSpacing = 8.dp
private val AddButtonIconSize = 18.dp
private const val EXPANDED_CHEVRON_ROTATION = 180f
private const val COLLAPSED_CHEVRON_ROTATION = 0f
private const val SELECTION_BORDER_DARK_ALPHA = 0.85f
private const val SELECTION_BORDER_LIGHT_ALPHA = 0.75f
private const val ACCENT_OVERLAY_BASE_ALPHA = 0.5f
private const val SELECTION_OVERLAY_ACCENT_ALPHA = 0.12f
private const val SELECTION_OVERLAY_DARK_ALPHA = 0.08f
private const val SELECTION_OVERLAY_LIGHT_ALPHA = 0.06f
private const val DRAG_OVERLAY_ACCENT_ALPHA = 0.18f
private const val DRAG_OVERLAY_DARK_ALPHA = 0.12f
private const val DRAG_OVERLAY_LIGHT_ALPHA = 0.09f
private const val DRAG_OVERLAY_SELECTED_ACCENT_ALPHA = 0.26f
private const val DRAG_OVERLAY_SELECTED_DARK_ALPHA = 0.18f
private const val DRAG_OVERLAY_SELECTED_LIGHT_ALPHA = 0.14f
private const val INACTIVE_STATUS_ALPHA = 0.75f
private const val HANDLE_TINT_ALPHA = 0.55f
private val EntryBorderWidth = 1.dp
private val EntryContentHorizontalPadding = 12.dp
private val EntryContentVerticalPadding = 12.dp
private val DragHandleTouchTargetSize = 24.dp
private val DragHandleIconSize = 18.dp
private val DragHandleToContentSpacing = 6.dp
private val MetadataStartPadding = 16.dp
private val MetadataEndPadding = 8.dp
private const val SERVER_COUNT_ALPHA = 0.78f
private val StatusTopSpacing = 4.dp
private val StatusIconSize = 18.dp
private val StatusTextSpacing = 4.dp
private val EntryMenuButtonSize = 36.dp
private val EntryMenuIconSize = 20.dp

private const val CONTENT_WEIGHT = 1f
private const val SINGLE_LINE_MAX_LINES = 1
