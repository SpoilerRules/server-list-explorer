package com.spoiligaming.explorer.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.StartupCoordinator
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.server.ServerFileValidationResult
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.dialogs.dialog.MapleDialogBase
import com.spoiligaming.explorer.ui.fonts.FontFactory
import com.spoiligaming.explorer.ui.widgets.MapleButton
import com.spoiligaming.explorer.ui.widgets.MapleButtonHeight
import com.spoiligaming.explorer.ui.widgets.MapleButtonWidth
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import java.io.File

@Composable
fun MapleServerFilePickerDialog(
    isUserRequested: Boolean,
    onDismiss: () -> Unit,
) {
    MapleDialogBase(
        isFullPopup = true,
        heightType = 0,
        isCloseable = isUserRequested,
        onDismiss = onDismiss,
    ) {
        var hasFailedOnce by remember { mutableStateOf(false) }

        val filePicker =
            rememberFilePickerLauncher(
                type = PickerType.File(extensions = listOf("dat")),
                mode = PickerMode.Single,
                title = "Select server list file",
            ) { file ->
                if (file != null) {
                    val validationResult = ServerFileHandler.validateFile(File(file.path!!))

                    if (validationResult == ServerFileValidationResult.VALID) {
                        hasFailedOnce = false
                        ConfigurationHandler.updateValue {
                            generalSettings.serverFilePath = file.path.toString()
                        }
                        onDismiss()
                        StartupCoordinator.retryLoad()
                    } else {
                        hasFailedOnce = true
                    }
                } else {
                    hasFailedOnce = true
                }
            }

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(
                text =
                    buildAnnotatedString {
                        pushStyle(SpanStyle(color = MapleColorPalette.text))
                        append(
                            if (ConfigurationHandler.getInstance().generalSettings.serverFilePath ==
                                "Replace with correct path to server.dat file." || hasFailedOnce
                            ) {
                                "Unable to load "
                            } else {
                                "Please select "
                            },
                        )
                        pushStyle(SpanStyle(color = MapleColorPalette.accent))
                        append("server.dat")
                        pop()
                        append(
                            if (ConfigurationHandler.getInstance().generalSettings.serverFilePath ==
                                "Replace with correct path to server.dat file." || hasFailedOnce
                            ) {
                                " file.\nPlease verify the file path and try again."
                            } else {
                                " file."
                            },
                        )
                    },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style =
                    TextStyle(
                        fontFamily = FontFactory.comfortaaMedium,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                    ),
                textAlign = TextAlign.Center,
                color = MapleColorPalette.text,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )

            Box(
                modifier =
                    Modifier.padding(
                        bottom =
                            when (isUserRequested) {
                                true -> 70.dp
                                false -> 40.dp
                            },
                    )
                        .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                // Error thrown by this will be fixed by JetBrains in next compose MP update:
                // https://youtrack.jetbrains.com/issue/COMPT-5064/Clicking-a-clickable-component-inside-another-disabled-clickable-component-throws-IllegalStateException
                MapleButton(
                    width = MapleButtonWidth.FILL_MAX.width,
                    height = MapleButtonHeight.ORIGINAL.height,
                    text = "Select server.dat file",
                ) {
                    filePicker.launch()
                }
            }
        }
    }
}
