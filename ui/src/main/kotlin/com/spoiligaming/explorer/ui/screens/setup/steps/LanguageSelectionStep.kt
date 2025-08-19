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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.spoiligaming.explorer.ui.screens.setup.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.settings.manager.preferenceSettingsManager
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.screens.setup.SetupStepContainer
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.widgets.LanguagePickerDropdownMenu
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.preferred_language_label
import server_list_explorer.ui.generated.resources.setup_step_title_localization

@Composable
internal fun LanguageSelectionStep() {
    val currentLocale = LocalPrefs.current.locale

    SetupStepContainer(title = t(Res.string.setup_step_title_localization)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LanguageStepItemSpacing),
        ) {
            Text(
                text = t(Res.string.preferred_language_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LanguagePickerDropdownMenu(
                selectedLocale = currentLocale,
                onLocaleSelected = { locale ->
                    preferenceSettingsManager.updateSettings {
                        it.copy(locale = locale)
                    }
                },
            )
        }
    }
}

private val LanguageStepItemSpacing = 4.dp
