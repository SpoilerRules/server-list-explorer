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
)

package com.spoiligaming.explorer.ui.screens.multiplayer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.DatasetLinked
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.spoiligaming.explorer.multiplayer.MultiplayerServer
import com.spoiligaming.explorer.multiplayer.history.ReorderServersChange
import com.spoiligaming.explorer.multiplayer.history.ServerListHistoryService
import com.spoiligaming.explorer.multiplayer.history.applyRedo
import com.spoiligaming.explorer.multiplayer.history.applyUndo
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import com.spoiligaming.explorer.settings.model.ActionBarOrientation
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalMultiplayerSettings
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalServerQueryMethodConfigurations
import com.spoiligaming.explorer.ui.dialog.ExpressiveDialog
import com.spoiligaming.explorer.ui.dialog.onClick
import com.spoiligaming.explorer.ui.dialog.prominent
import com.spoiligaming.explorer.ui.extensions.clickWithModifiers
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.window.WindowManager
import com.spoiligaming.explorer.util.OSUtils
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.cd_no_search_results
import server_list_explorer.ui.generated.resources.cd_redo
import server_list_explorer.ui.generated.resources.cd_undo
import server_list_explorer.ui.generated.resources.delete_all_support_text
import server_list_explorer.ui.generated.resources.delete_all_title_question
import server_list_explorer.ui.generated.resources.dialog_cancel_button
import server_list_explorer.ui.generated.resources.dialog_reload_server_list_button
import server_list_explorer.ui.generated.resources.dialog_support_text_external_change
import server_list_explorer.ui.generated.resources.dialog_title_external_change
import server_list_explorer.ui.generated.resources.no_search_matches_message
import server_list_explorer.ui.generated.resources.no_search_results_for
import server_list_explorer.ui.generated.resources.quick_action_delete_all
import server_list_explorer.ui.generated.resources.quick_action_sort
import server_list_explorer.ui.generated.resources.sort_dialog_support_text_mc_utils
import server_list_explorer.ui.generated.resources.sort_dialog_title_select_sort
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import java.nio.file.Files
import java.nio.file.Path
import java.util.Collections
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random
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
internal fun MultiplayerScreen(
    repo: ServerListRepository,
    historyService: ServerListHistoryService,
    onReloadRequest: () -> Unit,
    onSortRequest: (SortType, List<MultiplayerServer>) -> Unit,
) {
    val mpSettings = LocalMultiplayerSettings.current
    val prefs = LocalPrefs.current
    val queryConfigs = LocalServerQueryMethodConfigurations.current

    val scope = rememberCoroutineScope()
    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictPath by remember { mutableStateOf<Path?>(null) }

    LaunchedEffect(repo.monitor) {
        repo.monitor.changeFlow.collect { path ->
            path?.let {
                conflictPath = it
                showConflictDialog = true
            }
        }
    }

    if (showConflictDialog && conflictPath != null) {
        val dialogTitleExternalChangeText = t(Res.string.dialog_title_external_change)
        val dialogSupportTextExternalChangeText =
            t(Res.string.dialog_support_text_external_change, conflictPath.toString())
        val dialogReloadServerListButtonText = t(Res.string.dialog_reload_server_list_button)

        ExpressiveDialog(
            onDismissRequest = { /* no-op: require acknowledgment */ },
            properties =
                DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            icon(Icons.Filled.DatasetLinked)
            title(dialogTitleExternalChangeText)
            supportText(dialogSupportTextExternalChangeText)
            accept(
                dialogReloadServerListButtonText.prominent onClick {
                    scope.launch {
                        Files.deleteIfExists(repo.createTempServerListFile())
                        repo.load()
                    }
                    showConflictDialog = false
                    onReloadRequest()
                },
            )
            modifier = Modifier.width(IntrinsicSize.Max)
        }
    }
    val gridFocusRequester = remember { FocusRequester() }
    var gridHasFocus by remember { mutableStateOf(false) }
    var searchHasFocus by remember { mutableStateOf(false) }
    val controller =
        remember(repo, historyService) {
            ServerListController(
                repo = repo,
                scope = scope,
                historyService = historyService,
                onSelectionChanged = {
                    if (!searchHasFocus) {
                        gridFocusRequester.requestFocus()
                    }
                },
            )
        }
    val selectedIds by controller.selection.selectedIds.collectAsState()
    val entries by repo.servers.collectAsState()
    var pendingOrder by remember(entries) { mutableStateOf(entries) }
    LaunchedEffect(entries) {
        pendingOrder = entries
    }
    var lastMove by remember { mutableStateOf<Pair<MultiplayerServer, Pair<Int, Int>>?>(null) }
    val lazyGridState = rememberLazyGridState()
    var columnCount by remember { mutableStateOf(1) }
    LaunchedEffect(lazyGridState) {
        snapshotFlow {
            lazyGridState.layoutInfo.visibleItemsInfo
                .maxOfOrNull { it.column }
                ?.plus(1)
        }.collect { value ->
            if (value != null && value > 0) {
                columnCount = value
            }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var searchFilter by remember { mutableStateOf(SearchFilter.NameAndAddress) }
    val filteredEntries =
        remember(pendingOrder, searchQuery, searchFilter) {
            val q = searchQuery.trim().lowercase()
            if (q.isBlank()) {
                pendingOrder
            } else {
                pendingOrder.filter { e ->
                    when (searchFilter) {
                        SearchFilter.NameAndAddress ->
                            e.name.contains(q, true) || e.ip.contains(q, true)

                        SearchFilter.NameOnly -> e.name.contains(q, true)
                        SearchFilter.AddressOnly -> e.ip.contains(q, true)
                    }
                }
            }
        }

    val reorderState =
        rememberReorderableLazyGridState(lazyGridState) { from, to ->
            // find which server in the filtered view is being dragged
            val server =
                filteredEntries.getOrNull(from.index) ?: return@rememberReorderableLazyGridState
            // map to its index in pendingOrder
            val oldIdx = pendingOrder.indexOf(server)
            // find the target position's server in filteredEntries
            val target = filteredEntries.getOrNull(to.index) ?: return@rememberReorderableLazyGridState
            val newIdx = pendingOrder.indexOf(target)
            if (oldIdx != -1 && newIdx != -1) {
                // reorder the in-memory list
                val m = pendingOrder.toMutableList()
                m.removeAt(oldIdx)
                m.add(newIdx, server)
                pendingOrder = m
                // record that single move
                lastMove = server to (oldIdx to newIdx)
            }
        }

    var blockedMoveFeedback by remember { mutableStateOf<BlockedMoveFeedback?>(null) }
    var blockedMoveFeedbackToken by remember { mutableStateOf(0L) }

    fun handleKeyboardMove(direction: ServerEntryMoveDirection): Boolean {
        if (selectedIds.isEmpty()) return false

        val selectedIndices =
            controller.selection.indicesOf(pendingOrder.map { it.id })
        if (selectedIndices.isEmpty()) return false
        val selection =
            selectedIndices.mapNotNull { index ->
                pendingOrder.getOrNull(index)?.let { server -> index to server }
            }
        if (selection.isEmpty()) return false
        val selectionIds = selection.map { (_, server) -> server.id }.toSet()

        fun emitBlockedFeedback(): Boolean {
            blockedMoveFeedbackToken += 1
            blockedMoveFeedback =
                BlockedMoveFeedback(
                    direction = direction,
                    targetIds = selectionIds,
                    token = blockedMoveFeedbackToken,
                )
            return false
        }

        if (searchQuery.isNotBlank()) {
            return emitBlockedFeedback()
        }

        val effectiveColumnCount = columnCount.coerceAtLeast(1)

        val offset =
            when (direction) {
                ServerEntryMoveDirection.Up -> -effectiveColumnCount
                ServerEntryMoveDirection.Down -> effectiveColumnCount
                ServerEntryMoveDirection.Left -> -1
                ServerEntryMoveDirection.Right -> 1
            }
        if (offset == 0) return false

        val lastIndex = pendingOrder.lastIndex
        if (lastIndex == -1) return false

        val isAllowed =
            selection.all { (index, _) ->
                val target = index + offset
                when (direction) {
                    ServerEntryMoveDirection.Left ->
                        // ensure we don't move past the left edge (target >= 0) and not wrapping to the previous row
                        target >= 0 && index % effectiveColumnCount != 0

                    ServerEntryMoveDirection.Right ->
                        /*
                         Ensure we don't move past the right edge:
                         - target <= lastIndex: stay within bounds
                         - index % effectiveColumnCount != effectiveColumnCount - 1: not already at the last column
                         - target / effectiveColumnCount == index / effectiveColumnCount: stay in the same row (no wrapping to next)
                         */
                        target <= lastIndex &&
                            index % effectiveColumnCount != effectiveColumnCount - 1 &&
                            target / effectiveColumnCount == index / effectiveColumnCount

                    ServerEntryMoveDirection.Up ->
                        // ensure we don't move above the first row
                        index >= effectiveColumnCount

                    ServerEntryMoveDirection.Down ->
                        // ensure we don't move past the last row
                        target <= lastIndex
                }
            }

        if (!isAllowed) return emitBlockedFeedback()

        val beforeOrder = pendingOrder.toList()
        val workingOrder = pendingOrder.toMutableList()
        val step = if (offset > 0) 1 else -1
        val stepCount = abs(offset)
        val orderedSelection =
            if (offset > 0) {
                selection.asReversed()
            } else {
                selection
            }

        for ((_, server) in orderedSelection) {
            var currentIndex = workingOrder.indexOf(server)

            repeat(stepCount) {
                val nextIndex = currentIndex + step
                if (nextIndex !in 0..workingOrder.lastIndex) {
                    return emitBlockedFeedback()
                }

                Collections.swap(workingOrder, currentIndex, nextIndex)
                currentIndex = nextIndex
            }
        }

        val newOrder = workingOrder.toList()
        pendingOrder = newOrder
        lastMove = null
        blockedMoveFeedback = null

        scope.launch {
            repo.reorder(newOrder)
            repo.commit()
            historyService.recordChange(
                ReorderServersChange(
                    oldOrder = beforeOrder,
                    newOrder = newOrder,
                ),
            )
        }

        return true
    }

    val flashId = remember { mutableStateOf<Uuid?>(null) }
    val scrollTargetId = remember { mutableStateOf<Uuid?>(null) }
    // when scrollTargetId flips non-null, wait for the list to include it, then scroll & flash
    LaunchedEffect(scrollTargetId.value) {
        val id = scrollTargetId.value
        if (id != null) {
            snapshotFlow { pendingOrder.map { it.id } }
                .first { it.contains(id) }

            if (prefs.scrollAfterAdd) {
                val idx = filteredEntries.indexOfFirst { it.id == id }
                if (idx >= 0) {
                    lazyGridState.animateScrollToItem(idx)
                }
            }

            if (prefs.highlightAfterScroll) {
                delay(prefs.highlightAfterScrollDelayMillis)
                flashId.value = id
            }

            scrollTargetId.value = null
        }
    }

    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    val isEntriesEmpty = entries.isEmpty()

    var showAddDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showQueryMethodDialog by remember { mutableStateOf(false) }
    var showServerFileDialog by remember { mutableStateOf(false) }

    val refreshAction: (Boolean) -> Unit = { onlySelected ->
        if (onlySelected) {
            controller.refreshSelected(
                queryMode = mpSettings.serverQueryMethod,
                configurations = queryConfigs,
            )
        } else {
            controller.refreshAll(
                queryMode = mpSettings.serverQueryMethod,
                configurations = queryConfigs,
            )
        }
    }

    val quickActionsToolbar: @Composable () -> Unit = {
        QuickActionsToolbar(
            shimmer = if (isEntriesEmpty) shimmerInstance else null,
            totalCount = entries.size,
            selectedCount = selectedIds.size,
            onAdd = { showAddDialog = true },
            onSort = { showSortDialog = true },
            onSelectAll = { controller.selectAll() },
            onClearSelection = { controller.selection.clear() },
            onRefresh = refreshAction,
            onDeleteAll = { showDeleteAllDialog = true },
            onDeleteSelected = { controller.deleteSelected() },
            onShowQueryMethodDialog = { showQueryMethodDialog = true },
            onShowServerListFileConfigurationDialog = { showServerFileDialog = true },
        )
    }

    if (showAddDialog) {
        AddServerDialog(
            onDismissRequest = { showAddDialog = false },
            onAddConfirmed = { server ->
                scope.launch {
                    controller.add(server).join()
                    if (prefs.scrollAfterAdd || prefs.highlightAfterScroll) {
                        scrollTargetId.value = server.id
                    }
                }
            },
        )
    }

    if (showDeleteAllDialog) {
        val deleteAllTitleQuestionText = t(Res.string.delete_all_title_question)
        val deleteAllSupportText = t(Res.string.delete_all_support_text)
        val quickActionDeleteAllText = t(Res.string.quick_action_delete_all)
        val dialogCancelButtonText = t(Res.string.dialog_cancel_button)

        ExpressiveDialog(
            onDismissRequest = { showDeleteAllDialog = false },
        ) {
            title(deleteAllTitleQuestionText)
            icon(Icons.Filled.DeleteForever)
            supportText(deleteAllSupportText)
            accept(
                quickActionDeleteAllText.prominent onClick {
                    controller.deleteAll()
                    showDeleteAllDialog = false
                },
            )
            cancel(
                dialogCancelButtonText onClick {
                    showDeleteAllDialog = false
                },
            )
            modifier = Modifier.width(IntrinsicSize.Max)
        }
    }

    LaunchedEffect(searchQuery) {
        controller.selection.clear()
    }

    if (showServerFileDialog) {
        ServerListFileConfigurationDialog(
            onDismissRequest = { showServerFileDialog = false },
            onConfirm = {
                // reload repo, then notify caller
                scope.launch { repo.load() }
                onReloadRequest()
                showServerFileDialog = false
            },
        )
    }

    if (showSortDialog) {
        val sortDialogTitleSelectSortText = t(Res.string.sort_dialog_title_select_sort)
        val sortDialogSupportTextMcpingText = t(Res.string.sort_dialog_support_text_mc_utils)
        val quickActionSortText = t(Res.string.quick_action_sort)
        val dialogCancelButtonText = t(Res.string.dialog_cancel_button)

        var selectedSortType by remember { mutableStateOf(SortType.Ping) }
        ExpressiveDialog(onDismissRequest = { showSortDialog = false }) {
            title(sortDialogTitleSelectSortText)
            supportText(sortDialogSupportTextMcpingText)
            body {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SortType.entries.forEach { mode ->
                        val modeLabelText = t(mode.label)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            RadioButton(
                                selected = selectedSortType == mode,
                                onClick = { selectedSortType = mode },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                            )
                            Text(modeLabelText)
                        }
                    }
                }
            }
            accept(
                quickActionSortText.prominent onClick {
                    showSortDialog = false
                    onSortRequest(selectedSortType, pendingOrder)
                },
            )
            cancel(dialogCancelButtonText onClick { showSortDialog = false })
        }
    }

    ServerQueryMethodDialog(
        visible = showQueryMethodDialog,
        currentQueryMethod = mpSettings.serverQueryMethod,
        onSaveAndRefresh = { queryMethod ->
            controller.refreshAll(
                queryMode = queryMethod,
                configurations = queryConfigs,
            )
        },
        onDismissRequest = { showQueryMethodDialog = false },
    )

    val shakeIntensity =
        mpSettings.dragShakeIntensityDegrees.toFloat()
    val infinite = rememberInfiniteTransition()

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = lazyGridState.layoutInfo
            val last = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = layoutInfo.totalItemsCount
            last == total - 1
        }
    }

    var undoRepeatJob by remember { mutableStateOf<Job?>(null) }
    var redoRepeatJob by remember { mutableStateOf<Job?>(null) }

    Column(
        modifier =
            Modifier
                .padding(start = 12.dp, top = 12.dp, end = 12.dp)
                .onKeyEvent { keyEvent ->
                    when (keyEvent.type) {
                        KeyEventType.KeyDown -> {
                            val isMac = OSUtils.isMacOS
                            val isCmd = keyEvent.isMetaPressed
                            val isCtrl = keyEvent.isCtrlPressed
                            val isShift = keyEvent.isShiftPressed

                            val undo =
                                (isMac && isCmd && !isShift && keyEvent.key == Key.Z) ||
                                    (!isMac && isCtrl && !isShift && keyEvent.key == Key.Z)

                            val redo =
                                (isMac && isCmd && isShift && keyEvent.key == Key.Z) ||
                                    (!isMac && isCtrl && isShift && keyEvent.key == Key.Z) ||
                                    (!isMac && isCtrl && !isShift && keyEvent.key == Key.Y)

                            when {
                                undo -> {
                                    if (undoRepeatJob == null) {
                                        undoRepeatJob =
                                            scope.launch {
                                                historyService
                                                    .undo()
                                                    ?.let { change -> applyUndo(change, repo) }
                                                delay(prefs.undoRedoRepeatInitialDelayMillis)
                                                while (true) {
                                                    historyService
                                                        .undo()
                                                        ?.let { change -> applyUndo(change, repo) }
                                                    delay(prefs.undoRedoRepeatIntervalMillis)
                                                }
                                            }
                                    }
                                    true
                                }

                                redo -> {
                                    if (redoRepeatJob == null) {
                                        redoRepeatJob =
                                            scope.launch {
                                                historyService
                                                    .redo()
                                                    ?.let { change -> applyRedo(change, repo) }
                                                delay(prefs.undoRedoRepeatInitialDelayMillis)
                                                while (true) {
                                                    historyService
                                                        .redo()
                                                        ?.let { change -> applyRedo(change, repo) }
                                                    delay(prefs.undoRedoRepeatIntervalMillis)
                                                }
                                            }
                                    }
                                    true
                                }

                                else -> false
                            }
                        }

                        KeyEventType.KeyUp -> {
                            if (keyEvent.key == Key.Z || keyEvent.key == Key.Y) {
                                undoRepeatJob?.cancel()
                                undoRepeatJob = null
                                redoRepeatJob?.cancel()
                                redoRepeatJob = null
                                return@onKeyEvent true
                            }
                            false
                        }

                        else -> false
                    }
                },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(
                targetState = WindowManager.isWindowCompact,
                transitionSpec = {
                    fadeIn(
                        animationSpec =
                            tween(
                                200,
                                easing = LinearOutSlowInEasing,
                            ),
                    ) togetherWith
                        fadeOut(
                            animationSpec =
                                tween(
                                    200,
                                    easing = LinearOutSlowInEasing,
                                ),
                        )
                },
            ) { isCompact ->
                if (isCompact) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (isEntriesEmpty) {
                                DockedSearchBarShimmer(shimmerInstance)
                            } else {
                                DockedSearchScreen(
                                    expanded = true,
                                    onSearch = { q ->
                                        searchQuery = q
                                    },
                                    onFocusChange = { focused -> searchHasFocus = focused },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        ServerListHistoryControlCard(historyService, repo, scope)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (isEntriesEmpty) {
                            DockedSearchBarShimmer(shimmerInstance)
                        } else {
                            DockedSearchScreen(
                                expanded = true,
                                onSearch = { q ->
                                    searchQuery = q
                                },
                                onFocusChange = { focused -> searchHasFocus = focused },
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        ServerListHistoryControlCard(historyService, repo, scope)
                    }
                }
            }
        }

        if (mpSettings.actionBarOrientation == ActionBarOrientation.Top) {
            quickActionsToolbar()
        }

        Row(modifier = Modifier.weight(1f)) {
            val scale =
                mpSettings.serverEntryScale
                    .coerceAtLeast(1f)
                    .also { coerced ->
                        if (mpSettings.serverEntryScale < 1f) {
                            logger.warn { "serverEntryScale was below 1, coerced to $coerced" }
                        }
                    }

            val cellMinWidth =
                remember(scale) {
                    val step = 4.dp
                    val target = 420.dp.value * scale
                    ((target / step.value).roundToInt() * step.value).dp
                }

            if (mpSettings.actionBarOrientation == ActionBarOrientation.Left) {
                quickActionsToolbar()
            }

            if (filteredEntries.isEmpty() && searchQuery.isNotBlank()) {
                NoSearchResultsPlaceholder(
                    query = searchQuery,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                )
            } else {
                CompositionLocalProvider(
                    LocalBlockParentShortcuts provides remember { mutableStateOf(0) },
                ) {
                    val blockCount = LocalBlockParentShortcuts.current

                    LaunchedEffect(gridHasFocus) {
                        if (!gridHasFocus && blockCount.value == 0 && !searchHasFocus) {
                            gridFocusRequester.requestFocus()
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(cellMinWidth),
                        state = lazyGridState,
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .focusRequester(gridFocusRequester)
                                .focusable()
                                .onFocusChanged { state -> gridHasFocus = state.isFocused }
                                .onPreviewKeyEvent { e ->
                                    if (blockCount.value != 0) return@onPreviewKeyEvent false

                                    if (e.type == KeyEventType.KeyDown) {
                                        val handled =
                                            when (e.key) {
                                                Key.DirectionUp -> handleKeyboardMove(ServerEntryMoveDirection.Up)
                                                Key.DirectionDown -> handleKeyboardMove(ServerEntryMoveDirection.Down)
                                                Key.DirectionLeft -> handleKeyboardMove(ServerEntryMoveDirection.Left)
                                                Key.DirectionRight -> handleKeyboardMove(ServerEntryMoveDirection.Right)
                                                else -> false
                                            }
                                        if (handled) {
                                            return@onPreviewKeyEvent true
                                        }
                                    }

                                    val isSelectionMod =
                                        if (hostOs == OS.MacOS) e.isMetaPressed else e.isCtrlPressed

                                    when {
                                        // Ctrl+A (select all)
                                        isSelectionMod && e.key == Key.A && blockCount.value == 0 -> {
                                            controller.selectAll()
                                            true
                                        }
                                        // Delete (delete selected)
                                        e.key == Key.Delete && selectedIds.isNotEmpty() -> {
                                            controller.deleteSelected()
                                            true
                                        }

                                        // Refresh (refresh selected)
                                        isSelectionMod && e.key == Key.R -> {
                                            controller.refreshSelected(
                                                queryMode = mpSettings.serverQueryMethod,
                                                configurations = queryConfigs,
                                            )
                                            true
                                        }

                                        else -> false
                                    }
                                },
                        contentPadding =
                            PaddingValues(
                                start =
                                    if (mpSettings.actionBarOrientation == ActionBarOrientation.Left) {
                                        12.dp
                                    } else {
                                        0.dp
                                    },
                                end =
                                    if (mpSettings.actionBarOrientation == ActionBarOrientation.Right) {
                                        12.dp
                                    } else {
                                        0.dp
                                    },
                                bottom =
                                    if (isAtBottom) {
                                        12.dp
                                    } else {
                                        0.dp
                                    },
                            ),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (isEntriesEmpty) {
                            items(Random.nextInt(from = 3, until = 8)) {
                                ShimmerServerEntry(
                                    modifier = Modifier.aspectRatio(6f / 3f),
                                    shimmer = shimmerInstance,
                                )
                            }
                        } else {
                            items(
                                items = filteredEntries,
                                key = { it.id },
                            ) { serverEntry ->
                                var hasDragged by remember { mutableStateOf(false) }
                                var isShaking by remember { mutableStateOf(false) }
                                val shakeRotation by if (isShaking) {
                                    infinite.animateFloat(
                                        initialValue = -shakeIntensity,
                                        targetValue = shakeIntensity,
                                        animationSpec =
                                            infiniteRepeatable(
                                                animation =
                                                    tween(
                                                        durationMillis = 80,
                                                        easing = LinearEasing,
                                                    ),
                                                repeatMode = RepeatMode.Reverse,
                                            ),
                                    )
                                } else {
                                    remember { mutableStateOf(0f) }
                                }
                                ReorderableItem(reorderState, key = serverEntry.id) { _ ->
                                    Box(
                                        modifier =
                                            Modifier
                                                .animateItem()
                                                .draggableHandle(
                                                    onDragStarted = {
                                                        isShaking = true
                                                        hasDragged = true
                                                    },
                                                    onDragStopped = {
                                                        isShaking = false
                                                        lastMove?.let { (server, indices) ->
                                                            val (_, newPendingIdx) = indices
                                                            val oldRepoIdx = entries.indexOf(server)
                                                            val toRepoIdx =
                                                                newPendingIdx.coerceIn(
                                                                    0,
                                                                    entries.lastIndex,
                                                                )
                                                            if (oldRepoIdx != -1 && oldRepoIdx != toRepoIdx) {
                                                                controller.move(
                                                                    server = server,
                                                                    fromIndex = oldRepoIdx,
                                                                    toIndex = toRepoIdx,
                                                                )
                                                            }
                                                        }
                                                        lastMove = null
                                                        pendingOrder = entries
                                                    },
                                                    dragGestureDetector = DragGestureDetector.LongPress,
                                                ).clickWithModifiers { ctrl, shift, meta ->
                                                    gridFocusRequester.requestFocus()
                                                    if (!hasDragged) {
                                                        controller.selection.handlePointerClick(
                                                            id = serverEntry.id,
                                                            entries = entries.map { it.id },
                                                            ctrlMeta = ctrl || meta,
                                                            shift = shift,
                                                        )
                                                    }
                                                    hasDragged = false
                                                },
                                    ) {
                                        ServerEntry(
                                            selected = serverEntry.id in selectedIds,
                                            data = serverEntry,
                                            repo = repo,
                                            historyService = historyService,
                                            searchQuery = searchQuery,
                                            scope = scope,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .graphicsLayer {
                                                        rotationZ = shakeRotation
                                                    },
                                            highlight = serverEntry.id == flashId.value,
                                            onHighlightFinished = {
                                                if (flashId.value == serverEntry.id) {
                                                    flashId.value =
                                                        null
                                                }
                                            },
                                            onRefresh = {
                                                controller.refreshSingle(
                                                    serverEntry.ip,
                                                    queryMode = mpSettings.serverQueryMethod,
                                                    configurations = queryConfigs,
                                                )
                                            },
                                            onDelete = { controller.deleteSingle(serverEntry) },
                                            onMoveRequest = { direction ->
                                                handleKeyboardMove(direction)
                                            },
                                            blockedFeedback = blockedMoveFeedback,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (mpSettings.actionBarOrientation == ActionBarOrientation.Right) {
                quickActionsToolbar()
            }
        }

        if (mpSettings.actionBarOrientation == ActionBarOrientation.Bottom) {
            quickActionsToolbar()
        }
    }
}

@Composable
private fun NoSearchResultsPlaceholder(
    query: String,
    modifier: Modifier = Modifier,
) = Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = t(Res.string.cd_no_search_results),
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = t(Res.string.no_search_results_for, query),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = t(Res.string.no_search_matches_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ServerListHistoryControlCard(
    historyService: ServerListHistoryService,
    repo: ServerListRepository,
    scope: CoroutineScope,
) {
    val canUndo by historyService.canUndo.collectAsState()
    val canRedo by historyService.canRedo.collectAsState()

    ElevatedCard(
        modifier =
            Modifier
                .height(56.dp)
                .width(IntrinsicSize.Max),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        historyService.undo()?.let { change ->
                            applyUndo(change, repo)
                        }
                    }
                },
                modifier =
                    Modifier
                        .pointerHoverIcon(
                            if (canUndo) PointerIcon.Hand else PointerIcon.Default,
                        ).focusProperties { canFocus = false },
                enabled = canUndo,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = t(Res.string.cd_undo),
                    modifier = Modifier.size(24.dp),
                    tint =
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (canUndo) 1f else 0.38f,
                        ),
                )
            }

            IconButton(
                onClick = {
                    scope.launch {
                        historyService.redo()?.let { change ->
                            applyRedo(change, repo)
                        }
                    }
                },
                modifier =
                    Modifier
                        .pointerHoverIcon(
                            if (canRedo) PointerIcon.Hand else PointerIcon.Default,
                        ).focusProperties { canFocus = false },
                enabled = canRedo,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = t(Res.string.cd_redo),
                    modifier = Modifier.size(24.dp),
                    tint =
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (canRedo) 1f else 0.38f,
                        ),
                )
            }
        }
    }
}

internal val LocalBlockParentShortcuts = compositionLocalOf { mutableStateOf(0) }

private val logger = KotlinLogging.logger {}
