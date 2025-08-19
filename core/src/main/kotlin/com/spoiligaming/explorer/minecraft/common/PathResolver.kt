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

import java.nio.file.Files
import java.nio.file.Path

internal interface PathResolver {
    fun find(path: String): Path?

    fun findDefault(): Path?

    fun validate(path: Path?)
}

internal fun PathResolver.requireExistingDirectory(path: Path?) {
    requireNotNull(path) { "Path must not be null." }
    require(Files.exists(path)) { "Path does not exist: $path" }
    require(Files.isDirectory(path)) { "Path must be a directory: $path" }
    require(Files.isReadable(path)) { "Path is not readable: $path" }
}

internal fun PathResolver.requireExistingFile(path: Path?) {
    requireNotNull(path) { "File path must not be null." }
    require(Files.exists(path)) { "File path does not exist: $path" }
    require(Files.isRegularFile(path)) { "Path must be a file: $path" }
    require(Files.isReadable(path)) { "File is not readable: $path" }
}
