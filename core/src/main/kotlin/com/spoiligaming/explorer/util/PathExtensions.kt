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

package com.spoiligaming.explorer.util

import java.nio.file.Path
import java.util.Locale

fun Path.canonicalize(): Path = toAbsolutePath().normalize()

fun Path.serverListBookmarkKey(): String {
    val snapshot = toString()
    return if (OSUtils.isWindows) {
        snapshot.lowercase(Locale.getDefault())
    } else {
        snapshot
    }
}

fun Path.readableName() = fileName?.toString() ?: toString()
