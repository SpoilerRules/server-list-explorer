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

package com.spoiligaming.explorer.multiplayer.history

import com.spoiligaming.explorer.multiplayer.AcceptTexturesState
import com.spoiligaming.explorer.multiplayer.HiddenState
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import kotlin.uuid.ExperimentalUuidApi

private enum class Direction { UNDO, REDO }

private fun ServerListRepository.indexOfServer(id: Any?) =
    servers.value.indexOfFirst { it.id == id }.takeIf { it != -1 }

suspend fun applyUndo(
    change: ServerListChange,
    repo: ServerListRepository,
) = change.applyTo(repo, Direction.UNDO)

suspend fun applyRedo(
    change: ServerListChange,
    repo: ServerListRepository,
) = change.applyTo(repo, Direction.REDO)

private suspend fun ServerListChange.applyTo(
    repo: ServerListRepository,
    direction: Direction,
) {
    val didMutate =
        when (this) {
            is AddServerChange ->
                when (direction) {
                    Direction.UNDO ->
                        repo.indexOfServer(server.id)?.let {
                            repo.delete(it)
                            true
                        } ?: false

                    Direction.REDO -> {
                        repo.add(server)
                        true
                    }
                }

            is AddServerAtIndexChange ->
                when (direction) {
                    Direction.UNDO ->
                        repo.indexOfServer(server.id)?.let {
                            repo.delete(it)
                            true
                        } ?: false

                    Direction.REDO -> {
                        repo.addAt(index, server)
                        true
                    }
                }

            is DeleteServerChange ->
                when (direction) {
                    Direction.UNDO -> {
                        val restored =
                            repo.all().toMutableList().apply {
                                if (index <= size) {
                                    add(index, server)
                                } else {
                                    add(server)
                                }
                            }
                        repo.deleteAll()
                        restored.forEach { repo.add(it) }
                        true
                    }

                    Direction.REDO ->
                        repo.indexOfServer(server.id)?.let { idx ->
                            repo.delete(idx)
                            true
                        } ?: false
                }

            is DeleteMultipleServersChange ->
                when (direction) {
                    Direction.UNDO -> {
                        val currentList = repo.all().toMutableList()
                        serversWithIndices
                            .sortedBy { it.first }
                            .forEach { (index, server) ->
                                if (index <= currentList.size) {
                                    currentList.add(index, server)
                                } else {
                                    currentList.add(server)
                                }
                            }
                        repo.deleteAll()
                        currentList.forEach { repo.add(it) }
                        true
                    }

                    Direction.REDO -> {
                        val idsToRemove = serversWithIndices.map { it.second.id }.toSet()
                        repo.all()
                            .mapIndexed { idx, srv -> idx to srv }
                            .filter { it.second.id in idsToRemove }
                            .map { it.first }
                            .sortedDescending()
                            .forEach { repo.delete(it) }
                        true
                    }
                }

            is DeleteAllServersChange ->
                when (direction) {
                    Direction.UNDO -> {
                        repo.deleteAll()
                        servers.forEach {
                            repo.add(it)
                        }
                        true
                    }

                    Direction.REDO -> {
                        repo.deleteAll()
                        true
                    }
                }

            is EditAcceptedTexturesChange ->
                when (direction) {
                    Direction.UNDO ->
                        repo.indexOfServer(server.id)?.let { idx ->
                            when (oldState) {
                                AcceptTexturesState.Enabled,
                                AcceptTexturesState.Disabled,
                                ->
                                    repo.replace(
                                        idx,
                                        repo.servers.value[idx].copy(acceptTextures = oldState),
                                    )

                                AcceptTexturesState.Prompt -> repo.removeAcceptedTexturesAt(idx)
                            }
                            true
                        } ?: false

                    Direction.REDO ->
                        repo.indexOfServer(server.id)?.let { idx ->
                            when (newState) {
                                AcceptTexturesState.Enabled,
                                AcceptTexturesState.Disabled,
                                ->
                                    repo.replace(
                                        idx,
                                        repo.servers.value[idx].copy(acceptTextures = newState),
                                    )

                                AcceptTexturesState.Prompt -> repo.removeAcceptedTexturesAt(idx)
                            }
                            true
                        } ?: false
                }

            is MoveServerChange ->
                when (direction) {
                    Direction.UNDO -> {
                        repo.move(toIndex, fromIndex)
                        true
                    }

                    Direction.REDO -> {
                        repo.move(fromIndex, toIndex)
                        true
                    }
                }

            is SortChange -> {
                val listToApply = if (direction == Direction.UNDO) oldServers else newServers
                repo.deleteAll()
                listToApply.forEach { repo.add(it) }
                true
            }

            is SetHiddenChange ->
                when (direction) {
                    Direction.UNDO ->
                        repo.indexOfServer(serverId)?.let { idx ->
                            val current = repo.servers.value[idx]
                            val restored =
                                current.copy(
                                    hidden =
                                        if (oldHidden) {
                                            HiddenState.Hidden
                                        } else {
                                            HiddenState.NotHidden
                                        },
                                )
                            repo.replace(idx, restored)
                            true
                        } ?: false

                    Direction.REDO ->
                        repo.indexOfServer(serverId)?.let { idx ->
                            val current = repo.servers.value[idx]
                            val applied =
                                current.copy(
                                    hidden =
                                        if (newHidden) {
                                            HiddenState.Hidden
                                        } else {
                                            HiddenState.NotHidden
                                        },
                                )
                            repo.replace(idx, applied)
                            true
                        } ?: false
                }

            is EditServerFieldsChange ->
                when (direction) {
                    Direction.UNDO ->
                        repo.indexOfServer(serverId)?.let { idx ->
                            val current = repo.servers.value[idx]
                            val restored = current.copy(name = oldName, ip = oldIp)
                            repo.replace(idx, restored)
                            true
                        } ?: false

                    Direction.REDO ->
                        repo.indexOfServer(serverId)?.let { idx ->
                            val current = repo.servers.value[idx]
                            val applied = current.copy(name = newName, ip = newIp)
                            repo.replace(idx, applied)
                            true
                        } ?: false
                }

            is RemoveIconChange ->
                when (direction) {
                    Direction.UNDO ->
                        repo.indexOfServer(server.id)?.let { idx ->
                            repo.updateIcon(idx, oldIconBase64)
                            true
                        } ?: false

                    Direction.REDO ->
                        repo.indexOfServer(server.id)?.let { idx ->
                            repo.removeIconAt(idx)
                            true
                        } ?: false
                }

            is UpdateIconChange ->
                when (direction) {
                    Direction.UNDO ->
                        repo.indexOfServer(serverId)?.let { idx ->
                            repo.updateIcon(idx, oldIconBase64)
                            true
                        } ?: false

                    Direction.REDO ->
                        repo.indexOfServer(serverId)?.let { idx ->
                            repo.updateIcon(idx, newIconBase64)
                            true
                        } ?: false
                }
        }

    if (didMutate) {
        repo.commit()
    }
}
