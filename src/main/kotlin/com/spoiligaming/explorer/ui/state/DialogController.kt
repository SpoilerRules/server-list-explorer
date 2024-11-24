package com.spoiligaming.explorer.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.spoiligaming.explorer.isBackupRestoreInProgress
import com.spoiligaming.explorer.server.BackupController
import com.spoiligaming.explorer.server.ContemporaryServerEntryListData
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.ui.dialogs.MapleConfirmationDialog
import com.spoiligaming.explorer.ui.dialogs.MapleIndexMoveDialog
import com.spoiligaming.explorer.ui.dialogs.MapleInformationDialog
import com.spoiligaming.explorer.ui.dialogs.MapleServerEntryCreationDialog
import com.spoiligaming.explorer.ui.dialogs.MapleServerEntryValueReplacementDialog
import com.spoiligaming.explorer.ui.dialogs.MapleServerFilePickerDialog
import com.spoiligaming.explorer.ui.dialogs.ValueReplacementType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed class DialogData {
    data class ValueReplacement(
        val serverName: String,
        val serverAddress: String,
        val serverIcon: ImageBitmap?,
        val serverIconRaw: String?,
        val serverPositionInList: Int,
        val type: ValueReplacementType,
        val onConfirm: (String) -> Unit,
    ) : DialogData()

    data class IconRefreshCompleted(val duration: Long, val successRatio: Pair<Int, Int>) :
        DialogData()

    data class DeletionConfirmation(val serverPosition: Int, val serverName: String) : DialogData()

    data class ServerFilePicker(val closeable: Boolean) : DialogData()

    data object WipeConfirmation : DialogData()

    data class BackupRestoreConfirmation(val backupFileName: String) : DialogData()

    data class BackupDeletionConfirmation(
        val backupFileName: String,
        val onDelete: () -> Unit,
    ) : DialogData()

    class BackupWipeConfirmation(val onWipe: () -> Unit) : DialogData()

    data object ForceEncodeConfirmation : DialogData()

    data object EntryCreationDialog : DialogData()

    data object IconRefreshStarted : DialogData()

    data object ExternalModification : DialogData()

    data object IconDecodeFailure : DialogData()

    data object MoveServerByIndex : DialogData()
}

object DialogController {
    private var currentDialogData by mutableStateOf<DialogData?>(null)
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    @Composable
    fun RenderDialog() =
        currentDialogData?.let { dialogData ->
            when (dialogData) {
                is DialogData.ValueReplacement -> {
                    MapleServerEntryValueReplacementDialog(
                        serverName = dialogData.serverName,
                        serverAddress = dialogData.serverAddress,
                        serverIcon = dialogData.serverIcon,
                        type = dialogData.type,
                        onConfirm = {
                            when (dialogData.type) {
                                ValueReplacementType.NAME ->
                                    ContemporaryServerEntryListData.updateServerName(
                                        dialogData.serverPositionInList,
                                        dialogData.serverName,
                                    )
                                ValueReplacementType.ADDRESS ->
                                    ContemporaryServerEntryListData.updateServerAddress(
                                        dialogData.serverPositionInList,
                                        dialogData.serverAddress,
                                    )
                                ValueReplacementType.ICON ->
                                    ContemporaryServerEntryListData.updateServerIcon(
                                        dialogData.serverPositionInList,
                                        dialogData.serverIconRaw,
                                    )
                            }
                            dialogData.onConfirm(it)
                            currentDialogData = null
                        },
                        onDismiss = { currentDialogData = null },
                    )
                }
                is DialogData.DeletionConfirmation -> {
                    MapleConfirmationDialog(
                        title = "Confirm Server Deletion",
                        description =
                            "Do you want to delete the server entry named ${dialogData.serverName}?" +
                                "\nThis action is irreversible.",
                        onAccept = {
                            ContemporaryServerEntryListData.deleteServer(dialogData.serverPosition)
                            currentDialogData = null
                        },
                        onDismiss = { currentDialogData = null },
                    )
                }
                is DialogData.IconRefreshCompleted -> {
                    MapleInformationDialog(
                        title = "Icon Refresh Complete",
                        description =
                            "The process took ${
                                buildString {
                                    val totalSeconds = remember { dialogData.duration / 1000 }
                                    val (hours, minutes, remainingSeconds) =
                                        remember {
                                            listOf(
                                                totalSeconds / 3600,
                                                (totalSeconds % 3600) / 60,
                                                totalSeconds % 60,
                                            )
                                        }

                                    if (hours > 0) {
                                        append(
                                            "$hours hour${if (hours > 1) "s" else ""} ",
                                        )
                                    }
                                    if (minutes > 0) {
                                        append(
                                            "$minutes minute${if (minutes > 1) "s" else ""} ",
                                        )
                                    }
                                    if (remainingSeconds > 0 || (hours == 0L && minutes == 0L)) {
                                        append(
                                            "$remainingSeconds second${if (remainingSeconds != 1L) "s" else ""}",
                                        )
                                    }
                                }.trim()
                            }.\n${dialogData.successRatio.first} out of ${dialogData.successRatio.second} icons were successfully refreshed.",
                        onDismiss = { currentDialogData = null },
                    )
                }
                is DialogData.ServerFilePicker -> {
                    MapleServerFilePickerDialog(dialogData.closeable) { currentDialogData = null }
                }
                DialogData.ExternalModification -> {
                    MapleInformationDialog(
                        title = "External Modification Detected",
                        description =
                            "The 'server.dat' file has been modified externally. Please reload the server list to synchronize with these updates.",
                        onDismiss = { currentDialogData = null },
                    )
                }
                DialogData.IconRefreshStarted -> {
                    MapleInformationDialog(
                        title = "Icon Refresh Started",
                        description =
                            "The server icon refresh process has begun.\nPlease avoid moving or deleting servers during this time.\nYou will be notified when the process is complete.",
                        onDismiss = { currentDialogData = null },
                    )
                }
                DialogData.WipeConfirmation -> {
                    MapleConfirmationDialog(
                        title = "Are you sure about wiping your server.dat file?",
                        description = "This action is irrecoverable and permanent.",
                        onAccept = {
                            ContemporaryServerEntryListData.wipeServerFile()
                            currentDialogData = null
                        },
                        onDismiss = { currentDialogData = null },
                    )
                }
                DialogData.ForceEncodeConfirmation -> {
                    MapleConfirmationDialog(
                        title = "Are you sure about forcefully saving to server.dat?",
                        description =
                            "This action is irreversible and may cause unexpected issues.\nYour data is automatically saved during regular actions.",
                        onAccept = {
                            ServerFileHandler.saveServerData()
                            currentDialogData = null
                        },
                        onDismiss = { currentDialogData = null },
                    )
                }
                DialogData.EntryCreationDialog -> {
                    MapleServerEntryCreationDialog(
                        onAccept = { serverData ->
                            ContemporaryServerEntryListData.createServer(
                                null,
                                serverData.third,
                                serverData.second,
                                serverData.first,
                            )
                            currentDialogData = null
                        },
                        onDismiss = { currentDialogData = null },
                    )
                }
                DialogData.IconDecodeFailure -> {
                    MapleInformationDialog(
                        title = "Failed to Update Server Icon",
                        description =
                            "The Base64 value provided for the server icon is invalid.\nConsequently, the server icon will remain unchanged.",
                        onDismiss = { currentDialogData = null },
                    )
                }
                is DialogData.BackupRestoreConfirmation -> {
                    MapleConfirmationDialog(
                        title = "Restore Backup",
                        description = "Do you want to revert to a previous version of the server list?\nThis action will replace the current list with the selected backup.\nYou can return to the current list if needed.",
                        onAccept = {
                            isBackupRestoreInProgress = true
                            scope.launch {
                                BackupController.restoreBackup(dialogData.backupFileName)
                            }
                            currentDialogData = null
                        },
                        onDismiss = { currentDialogData = null },
                    )
                }
                is DialogData.BackupWipeConfirmation -> {
                    MapleConfirmationDialog(
                        title = "Confirm Backup Wipe",
                        description = "This action will permanently delete all your backup files.\nYou won't be able to recover them after this action.\n\nAre you sure you want to proceed?",
                        onAccept = {
                            scope.launch {
                                BackupController.wipeBackup()
                                dialogData.onWipe()
                            }
                            currentDialogData = null
                        },
                        onDismiss = { currentDialogData = null },
                    )
                }
                is DialogData.BackupDeletionConfirmation -> {
                    MapleConfirmationDialog(
                        title = "Delete Backup File",
                        description = "Are you sure you want to permanently delete this backup file?\nThis action cannot be undone.",
                        onAccept = {
                            scope.launch {
                                BackupController.deleteBackup(dialogData.backupFileName)
                                dialogData.onDelete()
                            }
                            currentDialogData = null
                        },
                        onDismiss = { currentDialogData = null },
                    )
                }
                DialogData.MoveServerByIndex -> {
                    MapleIndexMoveDialog { currentDialogData = null }
                }
            }
        }

    fun showValueReplacementDialog(
        serverName: String,
        serverAddress: String,
        serverIcon: ImageBitmap?,
        serverIconRaw: String?,
        serverPositionInList: Int,
        type: ValueReplacementType,
        onConfirm: (String) -> Unit,
    ) {
        currentDialogData =
            DialogData.ValueReplacement(
                serverName,
                serverAddress,
                serverIcon,
                serverIconRaw,
                serverPositionInList,
                type,
                onConfirm,
            )
    }

    fun showExternalModificationDialog() {
        currentDialogData = DialogData.ExternalModification
    }

    fun showDeletionConfirmationDialog(
        serverPosition: Int,
        serverName: String,
    ) {
        currentDialogData = DialogData.DeletionConfirmation(serverPosition, serverName)
    }

    fun showIconRefreshStartedDialog() {
        currentDialogData = DialogData.IconRefreshStarted
    }

    fun showIconRefreshCompletedDialog(
        duration: Long,
        successRatio: Pair<Int, Int>,
    ) {
        currentDialogData = DialogData.IconRefreshCompleted(duration, successRatio)
    }

    fun showServerFilePickerDialog(closeable: Boolean) {
        currentDialogData = DialogData.ServerFilePicker(closeable)
    }

    fun showWipeConfirmationDialog() {
        currentDialogData = DialogData.WipeConfirmation
    }

    fun showForceEncodeConfirmationDialog() {
        currentDialogData = DialogData.ForceEncodeConfirmation
    }

    fun showServerEntryCreationDialog() {
        currentDialogData = DialogData.EntryCreationDialog
    }

    fun showIconValueDecodeFailureDialog() {
        currentDialogData = DialogData.IconDecodeFailure
    }

    fun showFileBackupRestoreConfirmationDialog(backupFileName: String) {
        currentDialogData = DialogData.BackupRestoreConfirmation(backupFileName)
    }

    fun showFileBackupDeletionConfirmationDialog(
        backupFileName: String,
        onDelete: () -> Unit,
    ) {
        currentDialogData = DialogData.BackupDeletionConfirmation(backupFileName, onDelete)
    }

    fun showBackupWipeConfirmationDialog(onWipe: () -> Unit) {
        currentDialogData = DialogData.BackupWipeConfirmation(onWipe)
    }

    fun showMoveServerByIndexDialog() {
        currentDialogData = DialogData.MoveServerByIndex
    }
}
