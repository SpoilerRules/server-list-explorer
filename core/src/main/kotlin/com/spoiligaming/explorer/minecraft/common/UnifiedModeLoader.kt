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

package com.spoiligaming.explorer.minecraft.common

import com.spoiligaming.explorer.minecraft.multiplayer.ServerListModule
import com.spoiligaming.explorer.minecraft.singleplayer.SingleplayerModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

object UnifiedModeInitializer {
    suspend fun autoDetect(mode: IModuleKind) =
        withContext(Dispatchers.Default) {
            when (mode) {
                IModuleKind.Multiplayer -> ServerListModule.autoResolvePath()
                IModuleKind.Singleplayer -> SingleplayerModule.autoResolvePath()
            }
        }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T> initialize(
        mode: IModuleKind,
        target: Path?,
    ) = withContext(Dispatchers.Default) {
        when (mode) {
            IModuleKind.Multiplayer -> ServerListModule.initialize(target) as Result<T>
            IModuleKind.Singleplayer -> SingleplayerModule.initialize(target) as Result<T>
        }
    }
}
