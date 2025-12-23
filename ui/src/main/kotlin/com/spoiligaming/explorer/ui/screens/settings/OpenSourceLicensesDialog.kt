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

package com.spoiligaming.explorer.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.components.rememberAutoLinkMarkdownAnnotatedString
import com.spoiligaming.explorer.ui.dialog.ExpressiveDialog
import com.spoiligaming.explorer.ui.dialog.onClick
import com.spoiligaming.explorer.ui.snackbar.SnackbarController
import com.spoiligaming.explorer.ui.snackbar.SnackbarEvent
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.ui.widgets.AppVerticalScrollbar
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.dialog_close_button
import server_list_explorer.ui.generated.resources.dialog_title_open_source_licenses
import server_list_explorer.ui.generated.resources.open_source_license_line
import server_list_explorer.ui.generated.resources.open_source_licenses_empty
import server_list_explorer.ui.generated.resources.open_source_licenses_load_error
import server_list_explorer.ui.generated.resources.open_source_licenses_loading
import server_list_explorer.ui.generated.resources.open_source_project_line
import server_list_explorer.ui.generated.resources.open_source_version_line

@Composable
internal fun OpenSourceLicensesDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
) {
    if (!visible) return

    val prefs = LocalPrefs.current

    var loadState by remember { mutableStateOf<LicenseLoadState>(LicenseLoadState.Loading) }

    val dialogTitleText = t(Res.string.dialog_title_open_source_licenses)
    val closeButtonText = t(Res.string.dialog_close_button)
    val loadingText = t(Res.string.open_source_licenses_loading)
    val emptyText = t(Res.string.open_source_licenses_empty)
    val loadErrorText = t(Res.string.open_source_licenses_load_error)

    LaunchedEffect(Unit) {
        loadState = LicenseLoadState.Loading

        val result =
            runCatching {
                val rawBytes = readLicenseReportBytes()
                LicenseJson.decodeFromString(
                    OpenSourceLicenseReport.serializer(),
                    rawBytes.decodeToString(),
                )
            }

        result
            .onSuccess { report ->
                loadState = LicenseLoadState.Ready(report)
            }.onFailure { e ->
                loadState = LicenseLoadState.Error
                logger.error(e) {
                    "Failed to load open source licenses from $LICENSE_REPORT_PATH."
                }
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = loadErrorText,
                        duration = SnackbarDuration.Short,
                    ),
                )
            }
    }

    val lazyListState = rememberLazyListState()
    val scrollbarAdapter = rememberScrollbarAdapter(lazyListState)

    ExpressiveDialog(
        onDismissRequest = onDismissRequest,
    ) {
        title(dialogTitleText)
        cancel(closeButtonText onClick onDismissRequest)
        body {
            when (val current = loadState) {
                LicenseLoadState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(LoadingSpacing),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = loadingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                LicenseLoadState.Error -> {
                    Text(
                        text = emptyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                is LicenseLoadState.Ready -> {
                    val items = current.report.displayItems

                    if (items.isEmpty()) {
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(RowSpacing),
                        ) {
                            LazyColumn(
                                state = lazyListState,
                                modifier =
                                    Modifier
                                        .weight(LIST_CONTENT_WEIGHT)
                                        .heightIn(max = DialogMaxHeight),
                                contentPadding = PaddingValues(vertical = ListVerticalPadding),
                                verticalArrangement = Arrangement.spacedBy(ListItemSpacing),
                            ) {
                                items(items) { item ->
                                    when (item) {
                                        is LicenseListItem.Header -> {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(HeaderPadding),
                                            )
                                        }

                                        is LicenseListItem.Dependency -> {
                                            LicenseRow(item.dependency)
                                        }
                                    }
                                }
                            }
                            if (lazyListState.canScrollForward || lazyListState.canScrollBackward) {
                                AppVerticalScrollbar(
                                    scrollbarAdapter,
                                    alwaysVisible = prefs.settingsScrollbarAlwaysVisible,
                                    modifier = Modifier.heightIn(max = DialogMaxHeight),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseRow(dependency: OpenSourceDependency) {
    val versionLine =
        dependency.moduleVersion?.let { version ->
            t(
                Res.string.open_source_version_line,
                version,
            )
        }
    val licenseLine =
        when {
            dependency.moduleLicense != null && dependency.moduleLicenseUrl != null ->
                t(
                    Res.string.open_source_license_line,
                    "[${dependency.moduleLicense}](${dependency.moduleLicenseUrl})",
                )
            dependency.moduleLicense != null ->
                t(
                    Res.string.open_source_license_line,
                    dependency.moduleLicense,
                )
            dependency.moduleLicenseUrl != null ->
                t(
                    Res.string.open_source_license_line,
                    dependency.moduleLicenseUrl,
                )
            else -> null
        }
    val projectLine =
        dependency.moduleUrl?.let { url ->
            t(
                Res.string.open_source_project_line,
                "[$url]($url)",
            )
        }
    val supportingText =
        listOfNotNull(versionLine, licenseLine, projectLine)
            .joinToString(separator = "\n")
            .ifBlank { null }
    val linkStyles =
        TextLinkStyles(
            style =
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                ),
            hoveredStyle =
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                ),
            pressedStyle =
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                ),
        )

    ListItem(
        modifier = Modifier.clip(MaterialTheme.shapes.large),
        headlineContent = {
            SelectionContainer {
                Text(
                    text = dependency.moduleName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = LICENSE_NAME_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        supportingContent =
            supportingText?.let {
                {
                    val annotatedText =
                        rememberAutoLinkMarkdownAnnotatedString(
                            text = it,
                            defaultLinkStyles = linkStyles,
                        )
                    SelectionContainer {
                        Text(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
    )
}

@Serializable
private data class OpenSourceLicenseReport(
    val dependencies: List<OpenSourceDependency> = emptyList(),
    val importedModules: List<ImportedModuleBundle> = emptyList(),
) {
    val displayItems
        get() =
            buildList {
                dependencies.forEach { add(LicenseListItem.Dependency(it)) }

                importedModules
                    .asSequence()
                    .filter { it.dependencies.isNotEmpty() }
                    .forEach { module ->
                        add(LicenseListItem.Header(module.moduleName))
                        module.dependencies.forEach { add(LicenseListItem.Dependency(it)) }
                    }
            }
}

@Serializable
private data class OpenSourceDependency(
    val moduleName: String,
    val moduleUrl: String? = null,
    val moduleVersion: String? = null,
    val moduleLicense: String? = null,
    val moduleLicenseUrl: String? = null,
)

@Serializable
private data class ImportedModuleBundle(
    val moduleName: String,
    val dependencies: List<OpenSourceDependency> = emptyList(),
)

private sealed class LicenseLoadState {
    object Loading : LicenseLoadState()

    object Error : LicenseLoadState()

    data class Ready(
        val report: OpenSourceLicenseReport,
    ) : LicenseLoadState()
}

private sealed class LicenseListItem {
    data class Header(
        val title: String,
    ) : LicenseListItem()

    data class Dependency(
        val dependency: OpenSourceDependency,
    ) : LicenseListItem()
}

private val LicenseJson =
    Json {
        ignoreUnknownKeys = true
    }

private const val LICENSE_REPORT_PATH = "open_source_licenses.json"
private const val LICENSE_NAME_MAX_LINES = 2

private val DialogMaxHeight = 420.dp
private val HeaderPadding = 8.dp
private val LoadingSpacing = 12.dp
private val ListItemSpacing = 8.dp
private val ListVerticalPadding = 4.dp
private const val LIST_CONTENT_WEIGHT = 1f
private val RowSpacing = 4.dp

private val logger = KotlinLogging.logger {}

private fun readLicenseReportBytes(): ByteArray {
    val loader = OpenSourceLicenseReport::class.java.classLoader
    return loader.getResourceAsStream(LICENSE_REPORT_PATH)?.use { it.readBytes() }
        ?: error("Missing resource: $LICENSE_REPORT_PATH")
}
