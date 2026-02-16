/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2026 SpoilerRules
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

package com.spoiligaming.explorer.ui.util

import com.spoiligaming.explorer.settings.model.ComputerStartupBehavior
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.startup_behavior_do_not_start
import server_list_explorer.ui.generated.resources.startup_behavior_start_minimized_to_system_tray
import server_list_explorer.ui.generated.resources.startup_behavior_start_visible

internal val ComputerStartupBehavior.displayNameResource
    get() =
        when (this) {
            ComputerStartupBehavior.DoNotStart -> Res.string.startup_behavior_do_not_start
            ComputerStartupBehavior.StartVisible -> Res.string.startup_behavior_start_visible
            ComputerStartupBehavior.StartMinimizedToSystemTray ->
                Res.string.startup_behavior_start_minimized_to_system_tray
        }
