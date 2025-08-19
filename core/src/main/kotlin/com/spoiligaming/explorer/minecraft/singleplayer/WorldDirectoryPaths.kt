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

package com.spoiligaming.explorer.minecraft.singleplayer

import com.spoiligaming.explorer.minecraft.common.PathResolver
import com.spoiligaming.explorer.minecraft.common.requireExistingDirectory
import com.spoiligaming.explorer.util.OSUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object WorldDirectoryPaths : PathResolver {
    override fun find(path: String) = Paths.get(path).takeIf { Files.exists(it) }

    override fun findDefault(): Path? {
        val home = System.getProperty("user.home") ?: return null
        val p =
            when {
                OSUtils.isWindows -> Paths.get(home, "AppData", "Roaming", ".minecraft", "saves")
                OSUtils.isMacOS ->
                    Paths.get(
                        home,
                        "Library",
                        "Application Support",
                        "minecraft",
                        "saves",
                    )

                OSUtils.isLinux -> Paths.get(home, ".minecraft", "saves")
                else -> return null
            }
        return p.takeIf(Files::exists)
    }

    override fun validate(path: Path?) = requireExistingDirectory(path)
}
