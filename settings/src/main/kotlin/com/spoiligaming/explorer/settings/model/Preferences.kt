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

package com.spoiligaming.explorer.settings.model

import com.spoiligaming.explorer.settings.serializer.LocaleSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
data class Preferences(
    @SerialName("locale")
    @Serializable(with = LocaleSerializer::class)
    val locale: Locale = determineDefaultLocale(),
    @SerialName("snackbar_at_top")
    val snackbarAtTop: Boolean = false,
    @SerialName("max_undo_history_size")
    val maxUndoHistorySize: Int = 25,
    @SerialName("undo_redo_repeat_initial_delay_ms")
    val undoRedoRepeatInitialDelayMillis: Long = 400,
    @SerialName("undo_redo_repeat_interval_ms")
    val undoRedoRepeatIntervalMillis: Long = 50,
    @SerialName("scroll_after_add")
    val scrollAfterAdd: Boolean = true,
    @SerialName("highlight_after_scroll")
    val highlightAfterScroll: Boolean = true,
    @SerialName("highlight_after_scroll_delay_ms")
    val highlightAfterScrollDelayMillis: Long = 300,
    @SerialName("vsync")
    val vsync: Boolean = true,
    @SerialName("show_fps_overlay")
    val showFpsOverlay: Boolean = false,
    @SerialName("window_title_show_build_info")
    val windowTitleShowBuildInfo: Boolean = false,
    @SerialName("scrollbar_always_visible")
    val settingsScrollbarAlwaysVisible: Boolean = false,
    @SerialName("nav_rail_items_centered")
    val navRailItemsCentered: Boolean = true,
) {
    companion object {
        /**
         * Returns the app's default locale, applying overrides for English regional variants
         * that typically follow British spelling.
         */
        fun determineDefaultLocale(): Locale {
            val systemLocale = Locale.getDefault()

            return when (systemLocale.language to systemLocale.country) {
                "en" to "AU",
                "en" to "NZ",
                "en" to "CA",
                "en" to "IE",
                "en" to "ZA",
                "en" to "HK",
                "en" to "IN",
                -> Locale.UK

                else -> systemLocale
            }
        }
    }
}
