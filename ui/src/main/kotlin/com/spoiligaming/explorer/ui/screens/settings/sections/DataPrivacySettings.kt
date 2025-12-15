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

package com.spoiligaming.explorer.ui.screens.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.settings.manager.privacySettingsManager
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrivacySettings
import com.spoiligaming.explorer.ui.components.rememberAutoLinkMarkdownAnnotatedString
import com.spoiligaming.explorer.ui.dialog.DialogBuilder
import com.spoiligaming.explorer.ui.dialog.DialogButtonData
import com.spoiligaming.explorer.ui.screens.settings.components.SettingsSection
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.theme.linkTint
import com.spoiligaming.explorer.ui.widgets.FlexibleSettingTile
import com.spoiligaming.explorer.ui.widgets.ItemSwitch
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.setting_note_restart_required
import server_list_explorer.ui.generated.resources.setting_privacy_disable_error_reporting_desc
import server_list_explorer.ui.generated.resources.setting_privacy_disable_error_reporting_keep_on
import server_list_explorer.ui.generated.resources.setting_privacy_disable_error_reporting_title
import server_list_explorer.ui.generated.resources.setting_privacy_disable_error_reporting_turn_off
import server_list_explorer.ui.generated.resources.setting_privacy_share_usage_data
import server_list_explorer.ui.generated.resources.setting_privacy_share_usage_data_desc
import server_list_explorer.ui.generated.resources.settings_section_data_privacy

@Composable
internal fun DataPrivacySettings() {
    val privacySettings = LocalPrivacySettings.current
    var showDisableErrorReportingDialog by remember { mutableStateOf(false) }

    SettingsSection(
        header = t(Res.string.settings_section_data_privacy),
        settings =
            buildList {
                add {
                    FlexibleSettingTile(
                        title = t(Res.string.setting_privacy_share_usage_data),
                        description = "",
                        supportingContent = {
                            Column(verticalArrangement = Arrangement.spacedBy(DescriptionArrangement)) {
                                val linkStyles =
                                    TextLinkStyles(
                                        SpanStyle(
                                            color = MaterialTheme.colorScheme.linkTint,
                                            textDecoration = TextDecoration.None,
                                        ),
                                    )
                                SelectionContainer {
                                    Text(
                                        text =
                                            rememberAutoLinkMarkdownAnnotatedString(
                                                text =
                                                    t(
                                                        Res.string.setting_privacy_share_usage_data_desc,
                                                        PRIVACY_POLICY_URL,
                                                    ),
                                                defaultLinkStyles = linkStyles,
                                            ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                SelectionContainer {
                                    Text(
                                        text = "Note: ${t(Res.string.setting_note_restart_required)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = NOTE_ALPHA,
                                            ),
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            ItemSwitch(
                                isChecked = privacySettings.usageDataEnabled,
                                onCheckedChange = { newValue ->
                                    if (!newValue && privacySettings.usageDataEnabled) {
                                        showDisableErrorReportingDialog = true
                                        return@ItemSwitch
                                    }

                                    privacySettingsManager.updateSettings {
                                        it.copy(usageDataEnabled = newValue)
                                    }
                                },
                                enabled = true,
                            )
                        },
                    )
                }
            },
    )

    if (showDisableErrorReportingDialog) {
        ConfirmDisableUsageDataDialog(
            onTurnOff = {
                privacySettingsManager.updateSettings {
                    it.copy(usageDataEnabled = false)
                }
                showDisableErrorReportingDialog = false
            },
            onKeepOn = { showDisableErrorReportingDialog = false },
        )
    }
}

@Composable
private fun ConfirmDisableUsageDataDialog(
    onTurnOff: () -> Unit,
    onKeepOn: () -> Unit,
) {
    val linkStyles =
        TextLinkStyles(
            SpanStyle(
                color = MaterialTheme.colorScheme.linkTint,
                textDecoration = TextDecoration.None,
            ),
        )

    DialogBuilder(
        label = t(Res.string.setting_privacy_disable_error_reporting_title),
        onDismissRequest = onKeepOn,
        body = {
            Column(verticalArrangement = Arrangement.spacedBy(DialogDescriptionArrangement)) {
                SelectionContainer {
                    Text(
                        text =
                            rememberAutoLinkMarkdownAnnotatedString(
                                text = t(Res.string.setting_privacy_disable_error_reporting_desc, PRIVACY_POLICY_URL),
                                defaultLinkStyles = linkStyles,
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        acceptButton =
            DialogButtonData(
                text = t(Res.string.setting_privacy_disable_error_reporting_keep_on),
                isProminent = true,
                onClick = onKeepOn,
            ),
        cancelButton =
            DialogButtonData(
                text = t(Res.string.setting_privacy_disable_error_reporting_turn_off),
                onClick = onTurnOff,
            ),
    )
}

private const val PRIVACY_POLICY_URL =
    "https://github.com/SpoilerRules/server-list-explorer/blob/main/PRIVACY.md"

private const val NOTE_ALPHA = 0.7f

private val DescriptionArrangement = 4.dp

private val DialogDescriptionArrangement = 12.dp
