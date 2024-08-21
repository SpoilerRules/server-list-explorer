package com.spoiligaming.explorer.ui.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spoiligaming.explorer.server.BackupController
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.explorer.ui.widgets.DisabledMapleButton
import com.spoiligaming.explorer.ui.widgets.MapleButton
import com.spoiligaming.explorer.ui.widgets.MergedInfoText

@Preview
@Composable
fun FileElement(
    backupData: BackupController.BackupEntry,
    onDelete: () -> Unit,
) = Surface(
    color = MapleColorPalette.menu,
    shape = RoundedCornerShape(12.dp),
    shadowElevation = 2.dp,
) {
    Box(
        modifier = Modifier.padding(10.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MergedInfoText(
                    "Creation On: ",
                    16.sp,
                    backupData.formattedDate,
                    16.sp,
                    MapleColorPalette.fadedText,
                )
                MergedInfoText(
                    "Last Modified: ",
                    16.sp,
                    backupData.lastAccessTime,
                    16.sp,
                    MapleColorPalette.fadedText,
                )
                MergedInfoText(
                    "File Size: ",
                    16.sp,
                    backupData.formattedFileSize,
                    16.sp,
                    MapleColorPalette.fadedText,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.height(26.dp),
            ) {
                MapleButton(
                    modifier =
                        Modifier
                            .weight(1f),
                    text = "Restore",
                ) {
                    DialogController.showFileBackupRestoreConfirmationDialog(
                        backupData.fileName,
                    )
                }
                DisabledMapleButton(
                    modifier = Modifier.weight(1f).height(26.dp),
                    text = "Preview",
                    hoverTooltipText =
                        "This feature is under consideration for future updates." +
                            "\n\nIts availability will depend on project growth and user demand.",
                )
                MapleButton(
                    modifier =
                        Modifier
                            .weight(1f),
                    text = "Delete",
                ) {
                    DialogController.showFileBackupDeletionConfirmationDialog(
                        backupData.fileName,
                        onDelete,
                    )
                }
            }
        }
    }
}
