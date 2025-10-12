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

@file:OptIn(ExperimentalUuidApi::class)

package com.spoiligaming.explorer.ui.screens.multiplayer

import com.spoiligaming.explorer.multiplayer.MultiplayerServer
import com.spoiligaming.explorer.multiplayer.history.AddServerAtIndexChange
import com.spoiligaming.explorer.multiplayer.history.AddServerChange
import com.spoiligaming.explorer.multiplayer.history.DeleteAllServersChange
import com.spoiligaming.explorer.multiplayer.history.DeleteMultipleServersChange
import com.spoiligaming.explorer.multiplayer.history.DeleteServerChange
import com.spoiligaming.explorer.multiplayer.history.MoveServerChange
import com.spoiligaming.explorer.multiplayer.history.ServerListHistoryService
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import com.spoiligaming.explorer.settings.model.ServerQueryMethod
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

internal class ServerListController(
    private val repo: ServerListRepository,
    private val scope: CoroutineScope,
    private val historyService: ServerListHistoryService,
    onSelectionChanged: (() -> Unit)? = null,
) {
    val selection = ServerSelectionController(onSelectionChanged)
    val entries: StateFlow<List<MultiplayerServer>> = repo.servers

    fun selectAll() {
        selection.selectAll(entries.value.map { it.id })
    }

    fun deleteSingle(server: MultiplayerServer) =
        scope.launch {
            val index = repo.all().indexOfFirst { it.id == server.id }
            if (index == -1) {
                logger.warn { "Attempted to delete unknown server ${server.id}" }
                return@launch
            }

            repo.delete(index)
            repo.commit()
            selection.clear()
            historyService.recordChange(
                DeleteServerChange(
                    index = index,
                    server = server,
                ),
            )
        }

    fun deleteSelected() =
        scope.launch {
            val toDelete = selection.selectedIds.value
            if (toDelete.isEmpty()) return@launch

            val current = entries.value

            val targets =
                current
                    .mapIndexed { idx, srv -> idx to srv }
                    .filter { it.second.id in toDelete }

            if (targets.isEmpty()) return@launch

            // perform deletion in reverse index order to avoid shifting
            targets
                .map { it.first }
                .sortedDescending()
                .forEach { repo.delete(it) }

            repo.commit()
            selection.clear()

            if (targets.size == 1) {
                val (index, server) = targets.first()
                historyService.recordChange(
                    DeleteServerChange(
                        index = index,
                        server = server,
                    ),
                )
            } else {
                historyService.recordChange(
                    DeleteMultipleServersChange(targets),
                )
            }
        }

    fun deleteAll() =
        scope.launch {
            val currentEntries = repo.all()
            if (currentEntries.isEmpty()) return@launch

            repo.deleteAll()
            repo.commit()
            selection.clear()

            historyService.recordChange(DeleteAllServersChange(currentEntries))
        }

    fun refreshSingle(
        address: String,
        queryMode: ServerQueryMethod,
        connectTimeoutMillis: Long,
        socketTimeoutMillis: Long,
    ) = ServerEntryController.refresh(
        address = address,
        queryMode = queryMode,
        connectTimeoutMillis = connectTimeoutMillis,
        socketTimeoutMillis = socketTimeoutMillis,
    )

    fun refreshSelected(
        queryMode: ServerQueryMethod,
        connectTimeoutMillis: Long,
        socketTimeoutMillis: Long,
    ) = entries.value
        .filter { it.id in selection.selectedIds.value }
        .forEach { server ->
            refreshSingle(server.ip, queryMode, connectTimeoutMillis, socketTimeoutMillis)
        }

    fun refreshAll(
        queryMode: ServerQueryMethod,
        connectTimeoutMillis: Long,
        socketTimeoutMillis: Long,
    ) = entries.value.forEach { server ->
        refreshSingle(server.ip, queryMode, connectTimeoutMillis, socketTimeoutMillis)
    }

    fun move(
        server: MultiplayerServer,
        fromIndex: Int,
        toIndex: Int,
    ) = scope.launch {
        repo.move(fromIndex, toIndex)
        repo.commit()
        historyService.recordChange(
            MoveServerChange(
                server = server,
                fromIndex = fromIndex,
                toIndex = toIndex,
            ),
        )
    }

    fun moveUp(
        server: MultiplayerServer,
        index: Int,
    ) = scope.launch {
        if (index in 1 until entries.value.size) {
            repo.moveUp(index)
            repo.commit()
            historyService.recordChange(
                MoveServerChange(
                    server = server,
                    fromIndex = index,
                    toIndex = index - 1,
                ),
            )
        }
    }

    fun moveDown(
        server: MultiplayerServer,
        index: Int,
    ) = scope.launch {
        val lastIndex = entries.value.lastIndex
        if (index in 0 until lastIndex) {
            repo.moveDown(index)
            repo.commit()
            historyService.recordChange(
                MoveServerChange(
                    server = server,
                    fromIndex = index,
                    toIndex = index + 1,
                ),
            )
        }
    }

    fun add(server: MultiplayerServer) =
        scope.launch {
            repo.add(server)
            repo.commit()
            historyService.recordChange(AddServerChange(server))
        }

    fun addAt(
        index: Int,
        server: MultiplayerServer,
    ) = scope.launch {
        repo.addAt(index, server)
        repo.commit()
        historyService.recordChange(AddServerAtIndexChange(index, server))
    }
}

private val logger = KotlinLogging.logger {}
