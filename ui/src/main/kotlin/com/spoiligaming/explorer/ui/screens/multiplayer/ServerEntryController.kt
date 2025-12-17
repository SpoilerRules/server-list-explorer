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

import com.spoiligaming.explorer.minecraft.multiplayer.online.OnlineServerDataFacade
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.IServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OnlineServerData
import com.spoiligaming.explorer.minecraft.multiplayer.online.backend.common.OnlineServerDataResourceResult
import com.spoiligaming.explorer.multiplayer.AcceptTexturesState
import com.spoiligaming.explorer.multiplayer.HiddenState
import com.spoiligaming.explorer.multiplayer.MultiplayerServer
import com.spoiligaming.explorer.multiplayer.history.EditAcceptedTexturesChange
import com.spoiligaming.explorer.multiplayer.history.EditServerFieldsChange
import com.spoiligaming.explorer.multiplayer.history.RemoveIconChange
import com.spoiligaming.explorer.multiplayer.history.ServerListHistoryService
import com.spoiligaming.explorer.multiplayer.history.SetHiddenChange
import com.spoiligaming.explorer.multiplayer.history.UpdateIconChange
import com.spoiligaming.explorer.multiplayer.repository.ServerListRepository
import com.spoiligaming.explorer.settings.model.QueryMethodRequestKey
import com.spoiligaming.explorer.settings.model.ServerQueryMethod
import com.spoiligaming.explorer.settings.model.ServerQueryMethodConfigurations
import com.spoiligaming.explorer.settings.model.requestKeyFor
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal object ServerEntryController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val cache =
        mutableMapOf<Key, MutableStateFlow<OnlineServerDataResourceResult<IServerData>>>()
    private val lastAppliedRemoteIcon: MutableMap<Uuid, String> = mutableMapOf()
    private val iconSyncVersionFlows: MutableMap<Uuid, MutableStateFlow<Long>> = mutableMapOf()

    private fun getSyncVersionFlow(serverId: Uuid) = iconSyncVersionFlows.getOrPut(serverId) { MutableStateFlow(0L) }

    fun getServerDataFlow(
        address: String,
        queryMode: ServerQueryMethod,
        configurations: ServerQueryMethodConfigurations,
    ): StateFlow<OnlineServerDataResourceResult<IServerData>> {
        val requestKey = configurations.requestKeyFor(queryMode)
        val key = Key(address, queryMode, requestKey)
        return cache.getOrPut(key) {
            MutableStateFlow<OnlineServerDataResourceResult<IServerData>>(
                OnlineServerDataResourceResult.Loading,
            ).also { flow ->
                scope.launch {
                    OnlineServerDataFacade(
                        serverAddress = address,
                        queryMode = queryMode,
                        configurations = configurations,
                    ).serverDataFlow().collect { flow.value = it }
                }
            }
        }
    }

    fun refresh(
        address: String,
        queryMode: ServerQueryMethod,
        configurations: ServerQueryMethodConfigurations,
    ) {
        val requestKey = configurations.requestKeyFor(queryMode)
        val key = Key(address, queryMode, requestKey)

        val flow =
            cache
                .getOrPut(key) {
                    MutableStateFlow(
                        OnlineServerDataResourceResult.Loading,
                    )
                }.also { it.value = OnlineServerDataResourceResult.Loading }

        scope.launch {
            OnlineServerDataFacade(
                serverAddress = address,
                queryMode = queryMode,
                configurations = configurations,
            ).serverDataFlow().collect { flow.value = it }
        }
    }

    fun changeName(
        serverId: Uuid,
        newName: String,
        repo: ServerListRepository,
        historyService: ServerListHistoryService,
    ) {
        require(newName.isNotBlank()) { "New name must not be blank." }

        scope.launch {
            val list = repo.all()
            val index = list.indexOfFirst { it.id == serverId }
            if (index == -1) {
                logger.warn { "Attempted to change name for unknown server $serverId" }
                return@launch
            }

            val old = list[index]
            if (old.name == newName) {
                return@launch
            }

            historyService.recordChange(
                EditServerFieldsChange(
                    serverId = serverId,
                    oldName = old.name,
                    newName = newName,
                    oldIp = old.ip,
                    newIp = old.ip,
                ),
            )

            repo.replace(index, old.copy(name = newName))
            repo.commit()
            logger.info { "Changed name for server $serverId from '${old.name}' to '$newName'" }
        }
    }

    fun changeAddress(
        serverId: Uuid,
        newAddress: String,
        repo: ServerListRepository,
        historyService: ServerListHistoryService,
    ) {
        require(newAddress.isNotBlank()) { "New address must not be blank." }

        scope.launch {
            val list = repo.all()
            val index = list.indexOfFirst { it.id == serverId }
            if (index == -1) {
                logger.warn { "Attempted to change address for unknown server $serverId" }
                return@launch
            }

            val old = list[index]
            if (old.ip == newAddress) {
                return@launch
            }

            historyService.recordChange(
                EditServerFieldsChange(
                    serverId = serverId,
                    oldName = old.name,
                    newName = old.name,
                    oldIp = old.ip,
                    newIp = newAddress,
                ),
            )

            repo.replace(index, old.copy(ip = newAddress))
            repo.commit()
            lastAppliedRemoteIcon.remove(serverId)
            logger.info { "Changed address for server $serverId from '${old.ip}' to '$newAddress'" }
        }
    }

    fun deleteIcon(
        server: MultiplayerServer,
        repo: ServerListRepository,
        historyService: ServerListHistoryService,
    ) = scope.launch {
        val index = repo.all().indexOfFirst { it.id == server.id }
        require(index != -1) { "Server with id ${server.id} not found" }

        val currentServer = repo.servers.value[index]
        val oldIconBase64 =
            requireNotNull(currentServer.iconBase64) {
                "No icon to delete for server ${server.id}"
            }

        historyService.recordChange(
            RemoveIconChange(
                server = currentServer,
                oldIconBase64 = oldIconBase64,
            ),
        )

        repo.removeIconAt(index)
        repo.commit()
        lastAppliedRemoteIcon.remove(server.id)
        getSyncVersionFlow(server.id).value += 1
        logger.info { "Removed icon for server ${server.id}" }
    }

    fun changeTexturesMode(
        server: MultiplayerServer,
        newState: AcceptTexturesState,
        repo: ServerListRepository,
        historyService: ServerListHistoryService,
    ) = scope.launch {
        val list = repo.all()
        val index = list.indexOfFirst { it.id == server.id }
        if (index == -1) {
            logger.warn { "Attempted to change address for unknown server ${server.id}" }
            return@launch
        }

        val old = list[index]
        if (old.acceptTextures == newState) return@launch

        historyService.recordChange(
            EditAcceptedTexturesChange(
                server = server,
                oldState = old.acceptTextures,
                newState = newState,
            ),
        )

        repo.replace(index, old.copy(acceptTextures = newState))
        repo.commit()

        logger.info { "Set textures mode to ${newState.name} for server ${server.id}" }
    }

    fun changeHiddenState(
        server: MultiplayerServer,
        newState: HiddenState,
        repo: ServerListRepository,
        historyService: ServerListHistoryService,
    ) = scope.launch {
        val list = repo.all()
        val index = list.indexOfFirst { it.id == server.id }
        if (index == -1) {
            logger.warn { "Attempted to change hidden state for unknown server ${server.id}" }
            return@launch
        }

        val old = list[index]
        if (old.hidden == newState) return@launch

        historyService.recordChange(
            SetHiddenChange(
                serverId = server.id,
                oldHidden = old.hidden == HiddenState.Hidden,
                newHidden = newState == HiddenState.Hidden,
            ),
        )

        repo.replace(index, old.copy(hidden = newState))
        repo.commit()

        logger.info { "Set hidden state to ${newState.name} for server ${server.id}" }
    }

    fun syncServerIcon(
        server: MultiplayerServer,
        result: OnlineServerDataResourceResult<IServerData>,
        repo: ServerListRepository,
        historyService: ServerListHistoryService,
    ) = scope.launch {
        val servers = repo.servers.value
        val data = (result as? OnlineServerDataResourceResult.Success<*>)?.data as? OnlineServerData
        val favicon =
            data?.icon ?: run {
                logger.info { "No icon found in server data for ${server.ip}. Sync skipped." }
                return@launch
            }

        val idx = servers.indexOfFirst { it.id == server.id }
        if (idx == -1) {
            logger.warn { "Server with ID ${server.id} not found. Icon sync aborted." }
            return@launch
        }

        if (lastAppliedRemoteIcon[server.id] == favicon) {
            logger.debug {
                "Icon for ${server.ip} is already the same as the last applied remote icon. Sync skipped."
            }
            return@launch
        }

        if (favicon == server.iconBase64) {
            lastAppliedRemoteIcon[server.id] = favicon
            logger.debug {
                "Icon for ${server.ip} is already up to date. Updating last applied remote icon cache."
            }
            return@launch
        }

        val current = servers[idx]
        if (favicon == current.iconBase64) {
            lastAppliedRemoteIcon[server.id] = favicon
            logger.debug { "Icon for ${server.ip} is already up to date. Cache refreshed." }
            return@launch
        }

        val oldIconBase64 = current.iconBase64.orEmpty()
        logger.debug { "Starting icon sync for server ${server.id} (${server.ip})." }

        runCatching {
            repo.updateIcon(idx, favicon)
            repo.commit()
            lastAppliedRemoteIcon[server.id] = favicon
            val flow = getSyncVersionFlow(server.id)
            flow.value += 1
            logger.info { "Successfully synced and persisted new icon for ${server.ip}." }
        }.onFailure { e ->
            logger.warn(e) { "Failed to persist icon for ${server.ip}." }
        }.onSuccess {
            historyService.recordChange(
                UpdateIconChange(
                    serverId = server.id,
                    oldIconBase64 = oldIconBase64,
                    newIconBase64 = favicon,
                ),
            )
        }
    }

    fun iconSyncVersionFlow(serverId: Uuid) = getSyncVersionFlow(serverId)

    fun clearCache() {
        cache.clear()
        iconSyncVersionFlows.clear()
        lastAppliedRemoteIcon.clear()
    }
}

private data class Key(
    val address: String,
    val queryMode: ServerQueryMethod,
    val configuration: QueryMethodRequestKey,
)

private val logger = KotlinLogging.logger {}
