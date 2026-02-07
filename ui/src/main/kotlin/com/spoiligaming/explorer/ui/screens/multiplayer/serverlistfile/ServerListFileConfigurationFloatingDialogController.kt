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

@file:OptIn(ExperimentalFoundationApi::class, ExperimentalUuidApi::class)

package com.spoiligaming.explorer.ui.screens.multiplayer.serverlistfile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import com.spoiligaming.explorer.serverlist.bookmarks.ServerListFileBookmarkEntry
import com.spoiligaming.explorer.util.serverListBookmarkKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.nio.file.Files
import java.nio.file.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
internal fun rememberServerListFileConfigurationFloatingDialogController(
    onDismissRequest: () -> Unit,
    onConfirm: (Path?) -> Unit,
): ServerListFileConfigurationFloatingDialogController {
    val scope = rememberCoroutineScope()
    val bookmarksController = rememberServerListFileBookmarksController()
    val entries by bookmarksController.entries.collectAsState(emptyList())

    val lazyListState = rememberLazyListState()
    val scrollbarAdapter = rememberScrollbarAdapter(lazyListState)
    val reorderState =
        rememberReorderableLazyListState(lazyListState) { from, to ->
            if (from.index !in entries.indices || to.index !in entries.indices) {
                return@rememberReorderableLazyListState
            }
            bookmarksController.moveAsync(from.index, to.index)
        }
    val listFocusRequester = remember { FocusRequester() }

    val controller =
        remember {
            ServerListFileConfigurationFloatingDialogController(
                scope = scope,
                bookmarksController = bookmarksController,
                lazyListState = lazyListState,
                scrollbarAdapter = scrollbarAdapter,
                reorderState = reorderState,
                listFocusRequester = listFocusRequester,
            )
        }

    controller.entries = entries
    controller.onConfirm = onConfirm
    controller.onDismissRequest = onDismissRequest

    LaunchedEffect(entries) {
        controller.ensureValidSelection()
    }

    LaunchedEffect(entries, controller.flashKey) {
        controller.scrollToFlashKey()
    }

    return controller
}

internal class ServerListFileConfigurationFloatingDialogController(
    private val scope: CoroutineScope,
    private val bookmarksController: ServerListFileBookmarksController,
    val lazyListState: LazyListState,
    val scrollbarAdapter: ScrollbarAdapter,
    val reorderState: ReorderableLazyListState,
    val listFocusRequester: FocusRequester,
) {
    var entries by mutableStateOf<List<ServerListFileBookmarkEntry>>(emptyList())

    var onConfirm: (Path?) -> Unit = {}
    var onDismissRequest = {}

    var editingEntryId by mutableStateOf<Uuid?>(null)
    var flashKey by mutableStateOf<String?>(null)
    var flashIsError by mutableStateOf(false)

    val activePath
        get() = bookmarksController.activePath

    var selectedEntryId
        get() = bookmarksController.selectedEntryId
        set(value) {
            bookmarksController.selectedEntryId = value
        }

    val selectedEntry
        get() = entries.firstOrNull { it.id == selectedEntryId }

    val selectedEntryExists
        get() = selectedEntry?.let { Files.exists(it.path) } ?: false

    val editingEntry
        get() = editingEntryId?.let { id -> entries.firstOrNull { it.id == id } }

    val hasBookmarks
        get() = entries.isNotEmpty()

    val hasMissingEntries
        get() = entries.any { entry -> !Files.exists(entry.path) }

    val hasInactiveEntries: Boolean
        get() {
            val activeKey = activePath?.serverListBookmarkKey() ?: return false
            return entries.any { it.path.serverListBookmarkKey() != activeKey }
        }

    fun ensureValidSelection() {
        val hasValidSelection =
            selectedEntryId?.let { id -> entries.any { it.id == id } } ?: false

        if (entries.isEmpty()) {
            selectedEntryId = null
            return
        }

        if (!hasValidSelection) {
            selectedEntryId = null
        }
    }

    suspend fun handleAddFile(rawPath: Path) {
        when (
            val result =
                bookmarksController.addFile(
                    rawPath = rawPath,
                    autoSelect = false,
                )
        ) {
            is AddResult.Added -> {
                val key = result.entry.path.serverListBookmarkKey()
                flashKey = key
                flashIsError = false
                val target = entries.indexOfFirst { it.id == result.entry.id }
                if (target >= 0) {
                    lazyListState.animateScrollToItem(target)
                }
                listFocusRequester.requestFocus()
            }
            is AddResult.Duplicate -> {
                val key = result.entry.path.serverListBookmarkKey()
                flashKey = key
                flashIsError = true
                val existingIndex = entries.indexOfFirst { it.id == result.entry.id }
                if (existingIndex >= 0) {
                    lazyListState.animateScrollToItem(existingIndex)
                }
                bookmarksController.showDuplicateFileWarning()
            }
            AddResult.Error -> Unit
        }
    }

    suspend fun loadEntry(entry: ServerListFileBookmarkEntry?) {
        if (entry == null) {
            bookmarksController.selectEntry(null)
            onConfirm(null)
            return
        }

        val selected = bookmarksController.selectEntry(entry)
        if (!selected) {
            flashKey = entry.path.serverListBookmarkKey()
            flashIsError = true
            return
        }

        flashKey = entry.path.serverListBookmarkKey()
        flashIsError = false
        onDismissRequest()
        onConfirm(bookmarksController.activePath)
    }

    suspend fun removeEntry(
        entry: ServerListFileBookmarkEntry,
        originalIndex: Int,
    ) {
        if (entries.size <= 1) return

        val removedKey = entry.path.serverListBookmarkKey()
        val result = bookmarksController.remove(entry)

        if (result != null) {
            val currentActiveKey = bookmarksController.activePath?.serverListBookmarkKey()
            if (removedKey == currentActiveKey) {
                loadEntry(result)
            }
        }

        if (originalIndex in entries.indices) {
            lazyListState.animateScrollToItem(originalIndex.coerceAtMost(entries.lastIndex))
        }
    }

    fun setSelection(entry: ServerListFileBookmarkEntry) {
        selectedEntryId = if (selectedEntryId == entry.id) null else entry.id
    }

    fun handleHighlightFinished(entry: ServerListFileBookmarkEntry) {
        val entryKey = entry.path.serverListBookmarkKey()
        if (entryKey == flashKey) {
            flashKey = null
        }
    }

    fun onEditLabel(entry: ServerListFileBookmarkEntry) {
        editingEntryId = entry.id
    }

    fun dismissEditLabel() {
        editingEntryId = null
    }

    fun confirmEditLabel(
        entry: ServerListFileBookmarkEntry,
        value: String,
    ) {
        scope.launch {
            bookmarksController.updateLabel(entry, value)
        }
        editingEntryId = null
    }

    fun clearMissingEntries() {
        bookmarksController.pruneMissingAsync()
    }

    fun clearInactiveEntries() {
        val currentActive = activePath ?: return
        scope.launch {
            bookmarksController.clearExceptActive()
            flashKey = currentActive.serverListBookmarkKey()
            flashIsError = false
        }
    }

    fun handleListKeyEvent(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        val idx = entries.indexOfFirst { it.id == selectedEntryId }
        if (idx !in entries.indices) return false
        val selected = entries[idx]
        return when (event.key) {
            Key.Delete -> {
                if (entries.size > 1) {
                    scope.launch { removeEntry(selected, idx) }
                    true
                } else {
                    false
                }
            }
            Key.DirectionUp -> {
                if (entries.size > 1) {
                    val target = (idx - 1).coerceAtLeast(0)
                    if (target != idx) {
                        scope.launch {
                            bookmarksController.move(idx, target)
                        }
                    }
                } else {
                    val target = (idx - 1).coerceAtLeast(0)
                    selectedEntryId = entries.getOrNull(target)?.id
                }
                true
            }
            Key.DirectionDown -> {
                if (entries.size > 1) {
                    val target = (idx + 1).coerceAtMost(entries.lastIndex)
                    if (target != idx) {
                        scope.launch {
                            bookmarksController.move(idx, target)
                        }
                    }
                } else {
                    val target = (idx + 1).coerceAtMost(entries.lastIndex)
                    selectedEntryId = entries.getOrNull(target)?.id
                }
                true
            }
            Key.Enter -> {
                scope.launch { loadEntry(selected) }
                true
            }
            else -> false
        }
    }

    fun openFileLocation(path: Path) {
        bookmarksController.openFileLocation(path)
    }

    fun openContainingFolder(path: Path) {
        bookmarksController.openContainingFolder(path)
    }

    suspend fun scrollToFlashKey() {
        val key = flashKey ?: return
        val index = entries.indexOfFirst { it.path.serverListBookmarkKey() == key }
        if (index >= 0) {
            lazyListState.animateScrollToItem(index)
        }
    }
}
