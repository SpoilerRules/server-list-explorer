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

package com.spoiligaming.explorer.ui.screens.setup.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.settings.model.ComputerStartupBehavior
import com.spoiligaming.explorer.ui.screens.setup.SetupStepContainer
import com.spoiligaming.explorer.ui.screens.setup.SetupUiState
import com.spoiligaming.explorer.ui.screens.setup.widgets.OnboardingItemSwitch
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.util.displayNameResource
import com.spoiligaming.explorer.util.OSUtils
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.setup_startup_low_memory_icon_content_description
import server_list_explorer.ui.generated.resources.setup_startup_low_memory_message
import server_list_explorer.ui.generated.resources.setup_startup_low_memory_title
import server_list_explorer.ui.generated.resources.setup_startup_step_subtitle
import server_list_explorer.ui.generated.resources.setup_startup_step_subtitle_close_behavior
import server_list_explorer.ui.generated.resources.setup_step_title_startup
import server_list_explorer.ui.generated.resources.startup_minimize_to_tray_on_close_title
import server_list_explorer.ui.generated.resources.startup_restore_previous_state_description_short
import server_list_explorer.ui.generated.resources.startup_restore_previous_state_title
import server_list_explorer.ui.generated.resources.startup_single_instance_handling_description
import server_list_explorer.ui.generated.resources.startup_single_instance_handling_title
import server_list_explorer.ui.generated.resources.startup_when_computer_starts_section_title

@Composable
internal fun StartupStep(state: SetupUiState) {
    val setupStepSubtitle =
        if (supportsStartupRegistration) {
            t(Res.string.setup_startup_step_subtitle)
        } else {
            t(Res.string.setup_startup_step_subtitle_close_behavior)
        }

    SetupStepContainer(
        title = t(Res.string.setup_step_title_startup),
        subtitle = setupStepSubtitle,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(StartupSectionSpacing),
        ) {
            if (isLowMemoryDevice) {
                LowMemoryNoticeCard()
            }

            Column(verticalArrangement = Arrangement.spacedBy(SectionSpacing)) {
                if (supportsStartupRegistration) {
                    StartupModeSection(
                        selectedBehavior = state.startupSettings.computerStartupBehavior,
                        onBehaviorSelected = { newBehavior ->
                            state.startupSettings =
                                state.startupSettings.copy(
                                    computerStartupBehavior = newBehavior,
                                )
                        },
                    )
                }
                StartupToggleSection(state = state)
            }
        }
    }
}

@Composable
private fun StartupModeSection(
    selectedBehavior: ComputerStartupBehavior,
    onBehaviorSelected: (ComputerStartupBehavior) -> Unit,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(SectionContentSpacing),
) {
    SelectionContainer {
        Text(
            text = t(Res.string.startup_when_computer_starts_section_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = SectionTitleInset),
        )
    }

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ComputerStartupBehavior.entries.forEach { behavior ->
            SegmentedButton(
                selected = selectedBehavior == behavior,
                onClick = { onBehaviorSelected(behavior) },
                shape =
                    SegmentedButtonDefaults.itemShape(
                        index = behavior.ordinal,
                        count = ComputerStartupBehavior.entries.size,
                    ),
                modifier = Modifier.weight(SplitButtonWeight).pointerHoverIcon(PointerIcon.Hand),
            ) {
                Text(text = t(behavior.displayNameResource))
            }
        }
    }
}

@Composable
private fun StartupToggleSection(
    state: SetupUiState,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(SectionContentSpacing),
) {
    OnboardingItemSwitch(
        title = t(Res.string.startup_minimize_to_tray_on_close_title),
        isChecked = state.startupSettings.minimizeToSystemTrayOnClose,
        onCheckedChange = { newValue ->
            state.startupSettings = state.startupSettings.copy(minimizeToSystemTrayOnClose = newValue)
        },
    )

    HorizontalDivider(
        modifier =
            Modifier
                .height(StartupToggleDividerThickness)
                .padding(horizontal = StartupToggleDividerHorizontalInset),
        color = MaterialTheme.colorScheme.outlineVariant,
    )

    OnboardingItemSwitch(
        title = t(Res.string.startup_single_instance_handling_title),
        description = t(Res.string.startup_single_instance_handling_description),
        isChecked = state.startupSettings.singleInstanceHandling,
        onCheckedChange = { newValue ->
            state.startupSettings = state.startupSettings.copy(singleInstanceHandling = newValue)
        },
    )

    HorizontalDivider(
        modifier =
            Modifier
                .height(StartupToggleDividerThickness)
                .padding(horizontal = StartupToggleDividerHorizontalInset),
        color = MaterialTheme.colorScheme.outlineVariant,
    )

    OnboardingItemSwitch(
        title = t(Res.string.startup_restore_previous_state_title),
        description = t(Res.string.startup_restore_previous_state_description_short),
        isChecked = state.startupSettings.persistentSessionState,
        onCheckedChange = { newValue ->
            state.startupSettings = state.startupSettings.copy(persistentSessionState = newValue)
        },
    )
}

@Composable
private fun LowMemoryNoticeCard(modifier: Modifier = Modifier) =
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = StartupSettingsCardElevation,
    ) {
        Row(
            modifier = Modifier.padding(LowMemoryNoticePadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LowMemoryNoticeContentSpacing),
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = t(Res.string.setup_startup_low_memory_icon_content_description),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(LowMemoryNoticeIconSize),
            )
            Column(verticalArrangement = Arrangement.spacedBy(LowMemoryNoticeTextSpacing)) {
                SelectionContainer {
                    Text(
                        text = t(Res.string.setup_startup_low_memory_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                SelectionContainer {
                    Text(
                        text = t(Res.string.setup_startup_low_memory_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }

val supportsStartupRegistration = (OSUtils.isWindows || OSUtils.isDebian) && !OSUtils.isRunningOnBareJvm
private val isLowMemoryDevice by lazy {
    OSUtils.totalPhysicalMemoryBytes in 1..<LOW_MEMORY_THRESHOLD_BYTES
}

private val StartupSectionSpacing = 14.dp
private val SectionSpacing = 4.dp
private val SectionContentSpacing = 8.dp
private val SectionTitleInset = 8.dp
private val StartupToggleDividerThickness = 0.5.dp
private val StartupToggleDividerHorizontalInset = 0.dp
private val StartupSettingsCardElevation = 2.dp
private val LowMemoryNoticePadding = 12.dp
private val LowMemoryNoticeContentSpacing = 10.dp
private val LowMemoryNoticeTextSpacing = 4.dp
private val LowMemoryNoticeIconSize = 24.dp
private const val SplitButtonWeight = 1f

private const val LOW_MEMORY_THRESHOLD_BYTES = 8L * 1024L * 1024L * 1024L
