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

package com.spoiligaming.explorer.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val SecureChatLight = Color(0xFF1565C0) // blue 800
internal val SecureChatDark = Color(0xFF64B5F6) // blue 300

internal val LinkLight = Color(0xFF2F6FEB)
internal val LinkDark = Color(0xFF6EA8FE)

internal val ActiveLight = Color(0xFF2E7D32)
internal val ActiveDark = Color(0xFF81C784)

@Suppress("UnusedReceiverParameter")
internal val ColorScheme.secureChatTint
    @Composable
    get() = if (LocalDarkTheme.current) SecureChatDark else SecureChatLight

@Suppress("UnusedReceiverParameter")
internal val ColorScheme.linkTint
    @Composable
    get() = if (LocalDarkTheme.current) LinkDark else LinkLight

@Suppress("UnusedReceiverParameter")
internal val ColorScheme.activeTint
    @Composable
    get() = if (LocalDarkTheme.current) ActiveDark else ActiveLight
