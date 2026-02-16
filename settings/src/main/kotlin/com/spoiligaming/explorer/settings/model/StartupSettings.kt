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

package com.spoiligaming.explorer.settings.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ComputerStartupBehavior {
    @SerialName("do_not_start")
    DoNotStart,

    @SerialName("start_visible")
    StartVisible,

    @SerialName("start_minimized_to_system_tray")
    StartMinimizedToSystemTray,
}

@Serializable
data class StartupSettings(
    @SerialName("computer_startup_behavior")
    val computerStartupBehavior: ComputerStartupBehavior = ComputerStartupBehavior.DoNotStart,
    @SerialName("minimize_to_system_tray_on_close")
    val minimizeToSystemTrayOnClose: Boolean = true,
    @SerialName("single_instance_handling")
    val singleInstanceHandling: Boolean = true,
    @SerialName("persistent_session_state")
    val persistentSessionState: Boolean = false,
) {
    val shouldStartMinimizedToSystemTray
        get() = computerStartupBehavior == ComputerStartupBehavior.StartMinimizedToSystemTray

    val isSystemTrayFeatureEnabled get() = shouldStartMinimizedToSystemTray || minimizeToSystemTrayOnClose
}
