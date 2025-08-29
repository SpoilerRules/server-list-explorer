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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
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
import com.spoiligaming.explorer.multiplayer.history.ServerListHistoryService
import com.spoiligaming.explorer.multiplayer.history.applyRedo
import com.spoiligaming.explorer.multiplayer.history.applyUndo
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import com.spoiligaming.explorer.settings.manager.multiplayerSettingsManager
import com.spoiligaming.explorer.settings.model.ActionBarOrientation
import com.spoiligaming.explorer.settings.model.ServerQueryMethod
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalMultiplayerSettings
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.dialog.ExpressiveDialog
import com.spoiligaming.explorer.ui.dialog.onClick
import com.spoiligaming.explorer.ui.dialog.prominent
import com.spoiligaming.explorer.ui.extensions.clickWithModifiers
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
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import sh.calvin.reorderable.DragGestureDetector
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import java.nio.file.Files
import java.nio.file.Path
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * TODO: Refactor this file thoroughly
 *
 * Goals:
 * 1. Replace magic numbers with named constants
 * 2. Break this code into smaller, well-defined functions without over-fragmenting
 * 3. Add localization support
 * 4. Improve documentation and inline comments to help future contributors understand the code
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
        ExpressiveDialog(
            onDismissRequest = { /* no-op: require acknowledgment */ },
            properties =
                DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            icon(Icons.Filled.DatasetLinked)
            title("External File Change Detected")
            supportText("The server list file at $conflictPath has been modified externally.")
            accept(
                "OK".prominent onClick {
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
    val controller =
        remember(repo, historyService) {
            ServerListController(
                repo = repo,
                scope = scope,
                historyService = historyService,
                onSelectionChanged = { gridFocusRequester.requestFocus() },
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
                mpSettings.serverQueryMethod == ServerQueryMethod.McSrvStat,
                mpSettings.connectTimeoutMillis,
                mpSettings.socketTimeoutMillis,
            )
        } else {
            controller.refreshAll(
                mpSettings.serverQueryMethod == ServerQueryMethod.McSrvStat,
                mpSettings.connectTimeoutMillis,
                mpSettings.socketTimeoutMillis,
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
        ExpressiveDialog(
            onDismissRequest = { showDeleteAllDialog = false },
        ) {
            title("Delete All?")
            icon(Icons.Filled.DeleteForever)
            supportText(
                "Are you sure you want to delete all entries? This action cannot be undone.",
            )
            accept(
                "Delete All".prominent onClick {
                    controller.deleteAll()
                    showDeleteAllDialog = false
                },
            )
            cancel(
                "Cancel" onClick {
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
        var selectedSortType by remember { mutableStateOf(SortType.Ping) }
        ExpressiveDialog(onDismissRequest = { showSortDialog = false }) {
            title("Sort server list")
            supportText("Uses MCServerPing as the server query method")
            body {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SortType.entries.forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            RadioButton(
                                selected = selectedSortType == mode,
                                onClick = { selectedSortType = mode },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                            )
                            Text(stringResource(mode.label))
                        }
                    }
                }
            }
            accept(
                "Sort".prominent onClick {
                    showSortDialog = false
                    onSortRequest(selectedSortType, pendingOrder)
                },
            )
            cancel("Cancel" onClick { showSortDialog = false })
        }
    }

    if (showQueryMethodDialog) {
        var pendingQueryMethod by remember(mpSettings.serverQueryMethod) {
            mutableStateOf(mpSettings.serverQueryMethod)
        }

        ExpressiveDialog(
            onDismissRequest = { showQueryMethodDialog = false },
        ) {
            title("Select Query Method")
            supportText(
                "Select how Server List Explorer should check server status and information. " +
                    "This setting controls the method used to fetch data for all servers in your list.",
            )
            body {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        RadioButton(
                            selected = pendingQueryMethod == ServerQueryMethod.McSrvStat,
                            onClick = { pendingQueryMethod = ServerQueryMethod.McSrvStat },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        )
                        Text(ServerQueryMethod.McSrvStat.displayName)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        RadioButton(
                            selected = pendingQueryMethod == ServerQueryMethod.McServerPing,
                            onClick = { pendingQueryMethod = ServerQueryMethod.McServerPing },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        )
                        Text(ServerQueryMethod.McServerPing.displayName)
                    }
                }
            }
            accept(
                "Save and refresh all entries".prominent onClick {
                    multiplayerSettingsManager.updateSettings {
                        it.copy(serverQueryMethod = pendingQueryMethod)
                    }

                    controller.refreshAll(
                        useMCSrvStat = pendingQueryMethod == ServerQueryMethod.McSrvStat,
                        connectTimeoutMillis = mpSettings.connectTimeoutMillis,
                        socketTimeoutMillis = mpSettings.socketTimeoutMillis,
                    )

                    showQueryMethodDialog = false
                },
            )
            cancel(
                "Cancel" onClick {
                    showQueryMethodDialog = false
                },
            )
            modifier = Modifier.width(IntrinsicSize.Max)
        }
    }

    val shakeIntensity =
        mpSettings.dragShakeIntensityDegrees.toFloat()
    val infinite = rememberInfiniteTransition()

    LaunchedEffect(gridHasFocus) {
        if (!gridHasFocus) {
            delay(100)
            if (!gridHasFocus) {
                gridFocusRequester.requestFocus()
            }
        }
    }

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
                                                historyService.undo()
                                                    ?.let { change -> applyUndo(change, repo) }
                                                delay(prefs.undoRedoRepeatInitialDelayMillis)
                                                while (true) {
                                                    historyService.undo()
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
                                                historyService.redo()
                                                    ?.let { change -> applyRedo(change, repo) }
                                                delay(prefs.undoRedoRepeatInitialDelayMillis)
                                                while (true) {
                                                    historyService.redo()
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
            val percentage = mpSettings.serverEntrySizePercent.coerceIn(1, 100)
            val cellMinWidth =
                remember(percentage) {
                    // Wider min/max for extended, default otherwise
                    val (minDp, maxDp) = 260 to 1100
                    val step = 4
                    val totalSteps = (maxDp - minDp) / step
                    val raw = (percentage - 1) * totalSteps
                    val stepped = (raw + 99 / 2) / 99
                    (minDp + stepped * step).dp
                }

            if (mpSettings.actionBarOrientation == ActionBarOrientation.Right) {
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
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(cellMinWidth),
                    state = lazyGridState,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .focusRequester(gridFocusRequester)
                            .focusable()
                            .onFocusChanged { state ->
                                gridHasFocus = state.isFocused
                            }
                            .onPreviewKeyEvent { e ->
                                val isSelectionMod =
                                    if (hostOs == OS.MacOS) e.isMetaPressed else e.isCtrlPressed
                                when {
                                    // Ctrl+A (select all)
                                    isSelectionMod && e.key == Key.A -> {
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
                                            useMCSrvStat = mpSettings.serverQueryMethod == ServerQueryMethod.McSrvStat,
                                            connectTimeoutMillis = mpSettings.connectTimeoutMillis,
                                            socketTimeoutMillis = mpSettings.socketTimeoutMillis,
                                        )
                                        true
                                    }

                                    else -> false
                                }
                            },
                    contentPadding =
                        PaddingValues(
                            start = if (mpSettings.actionBarOrientation == ActionBarOrientation.Right) 12.dp else 0.dp,
                            end = if (mpSettings.actionBarOrientation == ActionBarOrientation.Left) 12.dp else 0.dp,
                            bottom = if (isAtBottom) 12.dp else 0.dp,
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
                                            .graphicsLayer { rotationZ = shakeRotation }
                                            .clip(CardDefaults.elevatedShape)
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
                                            )
                                            .clickWithModifiers { ctrl, shift, meta ->
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
                                        modifier = Modifier.fillMaxWidth(),
                                        highlight = (serverEntry.id == flashId.value),
                                        onHighlightFinished = {
                                            if (flashId.value == serverEntry.id) {
                                                flashId.value =
                                                    null
                                            }
                                        },
                                        onRefresh = {
                                            controller.refreshSingle(
                                                serverEntry.ip,
                                                mpSettings.serverQueryMethod == ServerQueryMethod.McSrvStat,
                                                mpSettings.connectTimeoutMillis,
                                                mpSettings.socketTimeoutMillis,
                                            )
                                        },
                                        onDelete = { controller.deleteSingle(serverEntry) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (mpSettings.actionBarOrientation == ActionBarOrientation.Left) {
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
            contentDescription = "No search results found",
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "No results for \"$query\"",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "We couldn't find anything matching your search.",
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
    val canUndo by historyService.canUndoFlow.collectAsState()
    val canRedo by historyService.canRedoFlow.collectAsState()

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
                    Modifier.pointerHoverIcon(
                        if (canUndo) PointerIcon.Hand else PointerIcon.Default,
                    ),
                enabled = canUndo,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
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
                    Modifier.pointerHoverIcon(
                        if (canRedo) PointerIcon.Hand else PointerIcon.Default,
                    ),
                enabled = canRedo,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
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

private val logger = KotlinLogging.logger {}
