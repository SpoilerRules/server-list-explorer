package com.spoiligaming.explorer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.isBackupRestoreInProgress
import com.spoiligaming.explorer.server.BackupController
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.components.FileElement
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.explorer.ui.widgets.MapleDropdownMenu
import com.spoiligaming.explorer.ui.widgets.MapleVerticalScrollbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FileBackupSubScreen() {
    val scrollState = rememberLazyGridState()
    val configuration = ConfigurationHandler.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var backupEntries by remember { mutableStateOf(emptyList<BackupController.BackupEntry>()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentSortOrder by remember { mutableStateOf("Newest") }

    LaunchedEffect(isLoading) {
        isLoading = true
        coroutineScope.launch {
            backupEntries =
                BackupController.getBackups().let { entries ->
                    when (currentSortOrder) {
                        "Newest" ->
                            entries.sortedByDescending {
                                parseDateFromFileName(it.fileName)
                            }
                        "Oldest" ->
                            entries.sortedBy {
                                parseDateFromFileName(it.fileName)
                            }
                        "Largest Size" ->
                            entries.sortedByDescending {
                                parseSizeFromHumanReadableFormat(it.formattedFileSize)
                            }
                        "Smallest Size" ->
                            entries.sortedBy {
                                parseSizeFromHumanReadableFormat(it.formattedFileSize)
                            }
                        else -> entries
                    }
                }
            isLoading = false
        }
    }

    val shouldShowScrollbar =
        remember(
            configuration.generalSettings.scrollBarVisibility,
            configuration.themeSettings.windowScale,
        ) {
            configuration.generalSettings.scrollBarVisibility != "Disabled" &&
                when (configuration.themeSettings.windowScale) {
                    "100%", "1f", "1.0f" -> backupEntries.size > 4
                    "125%", "1.25f" -> backupEntries.size > 9
                    "150%", "1.5f" -> backupEntries.size > 12
                    else -> backupEntries.size > 9
                }
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isLoading || isBackupRestoreInProgress) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(MapleColorPalette.quaternary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        CircularProgressIndicator(
                            color = MapleColorPalette.accent,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally),
                        )
                        if (isBackupRestoreInProgress) {
                            Text(
                                text =
                                    "Backup restoration in progress." +
                                        "\nPlease wait while we complete the operation.",
                                maxLines = 2,
                                color = MapleColorPalette.fadedText,
                                style =
                                    TextStyle(
                                        fontFamily = FontFactory.comfortaaMedium,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 24.sp,
                                        textAlign = TextAlign.Center,
                                    ),
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(MapleColorPalette.quaternary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .padding(top = 10.dp, start = 10.dp, bottom = 10.dp),
                    ) {
                        TextButton(
                            onClick = {
                                DialogController.showBackupWipeConfirmationDialog {
                                    isLoading = true
                                }
                            },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MapleColorPalette.menu,
                                ),
                            shape = RoundedCornerShape(8.dp),
                            elevation =
                                ButtonDefaults.elevatedButtonElevation(
                                    defaultElevation = 8.dp,
                                ),
                            modifier =
                                Modifier
                                    .width(96.dp)
                                    .height(36.dp)
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .padding(0.dp),
                        ) {
                            Text(
                                "Wipe",
                                color = MapleColorPalette.fadedText,
                                style =
                                    TextStyle(
                                        fontFamily = FontFactory.comfortaaMedium,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                    ),
                            )
                        }
                        VerticalDivider(
                            Modifier.height(36.dp),
                            thickness = 1.dp,
                            color = MapleColorPalette.control,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Sort by: ",
                                color = MapleColorPalette.fadedText,
                                style =
                                    TextStyle(
                                        fontFamily = FontFactory.comfortaaMedium,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 16.sp,
                                    ),
                            )
                            MapleDropdownMenu(
                                true,
                                currentSortOrder,
                                options =
                                    listOf(
                                        "Newest",
                                        "Oldest",
                                        "Largest Size",
                                        "Smallest Size",
                                    ),
                            ) { selectedOption ->
                                currentSortOrder = selectedOption
                                isLoading = true
                            }
                        }
                    }
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MapleColorPalette.quaternary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.TopStart,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 280.dp),
                                state = scrollState,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier =
                                    Modifier.padding(
                                        start =
                                            if (
                                                configuration.generalSettings.scrollBarVisibility == "Left Side" &&
                                                shouldShowScrollbar
                                            ) {
                                                15.dp
                                            } else {
                                                0.dp
                                            },
                                        end =
                                            if (
                                                configuration.generalSettings.scrollBarVisibility == "Right Side" &&
                                                shouldShowScrollbar
                                            ) {
                                                15.dp
                                            } else {
                                                0.dp
                                            },
                                    ),
                            ) {
                                itemsIndexed(backupEntries) { _, backup ->
                                    FileElement(backup) {
                                        // onDelete:
                                        isLoading = true
                                    }
                                }
                            }

                            MapleVerticalScrollbar(shouldShowScrollbar, scrollState)
                        }
                    }
                }
            }

            InformationContainer()
            Spacer(Modifier)
        }
    }
}

private fun parseDateFromFileName(fileName: String): Date =
    runCatching {
        SimpleDateFormat(
            "yyyy-MM-dd'T'HH-mm-ss-SSS",
            Locale.getDefault(),
        ).parse(fileName.removeSuffix(".dat"))
    }.getOrDefault(Date(0))

private fun parseSizeFromHumanReadableFormat(sizeString: String): Long =
    with(sizeString) {
        val unitMultipliers =
            mapOf(
                "TB" to 1024L * 1024 * 1024 * 1024,
                "GB" to 1024L * 1024 * 1024,
                "MB" to 1024L * 1024,
                "KB" to 1024L,
            )

        val unit = unitMultipliers.keys.find { endsWith(it) }
        removeSuffix(unit ?: "").trim().toLong() * (unitMultipliers[unit] ?: 1L)
    }
