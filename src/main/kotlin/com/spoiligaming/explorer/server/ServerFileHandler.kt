package com.spoiligaming.explorer.server

import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.StartupCoordinator
import com.spoiligaming.explorer.ui.state.DialogController
import com.spoiligaming.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.querz.nbt.io.NBTUtil
import net.querz.nbt.tag.CompoundTag
import net.querz.nbt.tag.ListTag
import java.io.File
import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.attribute.FileTime
import java.util.Base64
import javax.imageio.ImageIO

object ServerFileHandler {
    private var isInternalModification = false
    private var lastModifiedTime: FileTime? = null
    private var fileWatcherJob: Job? = null

    private lateinit var serverData: ListTag<CompoundTag>

    private var serverFilePath =
        Paths.get(ConfigurationHandler.getInstance().generalSettings.serverFilePath)

    fun initializeServerFileLocation(): ServerFileValidationResult {
        val serverDatFile = File(ConfigurationHandler.getInstance().generalSettings.serverFilePath)
        serverFilePath = serverDatFile.toPath()

        val validationResult = validateFile(serverDatFile)

        when (validationResult) {
            ServerFileValidationResult.VALID -> {
                serverData =
                    (
                        NBTUtil.read(
                            serverDatFile,
                            ConfigurationHandler.getInstance()
                                .advancedSettings
                                .isServerFileCompressed,
                        )
                            .tag as CompoundTag
                    )
                        .getListTag("servers")
                        .asCompoundTagList()
                startFileMonitor()
            }
            else -> {
                Logger.printError("Current path to 'server.dat' file is invalid: $validationResult")
                ServerFileValidationResult::class
                    .sealedSubclasses
                    .firstOrNull { it.simpleName == validationResult.name }
                    ?.let { subclass ->
                        when (subclass.objectInstance) {
                            ServerFileValidationResult.INVALID_NBT -> {
                                Logger.printError(
                                    "The structure of the 'server.dat' file is invalid. Please try selecting another file from a different location, or ensure Minecraft: Java Edition has saved the file correctly.",
                                )
                                false
                            }
                            else -> {
                                Logger.printError("Error: ${subclass.simpleName} occurred.")
                                ConfigurationHandler.updateValue {
                                    generalSettings.serverFilePath =
                                        "Replace with correct path to server.dat file."
                                }
                                true
                            }
                        }
                    } ?: false
            }
        }

        return validationResult
    }

    fun validateFile(file: File?): ServerFileValidationResult =
        file
            ?.takeIf { it.exists() && it.isFile && it.name == "servers.dat" }
            ?.let {
                runCatching {
                    (NBTUtil.read(it).tag as CompoundTag)
                        .getListTag("servers")
                        ?.asCompoundTagList() != null
                }
                    .map {
                        if (it) {
                            ServerFileValidationResult.VALID
                        } else {
                            ServerFileValidationResult.INVALID_NBT
                        }
                    }
                    .getOrElse { ServerFileValidationResult.INVALID_NBT }
            }
            ?: when {
                file == null -> ServerFileValidationResult.FILE_NOT_FOUND
                !file.exists() -> ServerFileValidationResult.FILE_NOT_FOUND
                !file.isFile -> ServerFileValidationResult.NOT_A_FILE
                file.name != "servers.dat" -> ServerFileValidationResult.WRONG_FILE_NAME
                else -> ServerFileValidationResult.INVALID_NBT
            }

    private fun startFileMonitor() {
        fileWatcherJob?.cancel()

        fileWatcherJob =
            CoroutineScope(Dispatchers.IO).launch {
                if (!ConfigurationHandler.getInstance().advancedSettings.serverFileMonitoring) {
                    return@launch fileWatcherJob?.cancel()!!
                }

                val watchService = FileSystems.getDefault().newWatchService()
                serverFilePath.parent.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)

                lastModifiedTime = Files.getLastModifiedTime(serverFilePath)

                while (isActive) {
                    val key = watchService.take()
                    val events = key.pollEvents()

                    events
                        .filterIsInstance<WatchEvent<*>>()
                        .filter { it.kind() == StandardWatchEventKinds.ENTRY_MODIFY }
                        .map { it.context() as Path }
                        .firstOrNull { it.fileName.toString() == "servers.dat" }
                        ?.let {
                            if (!isInternalModification) {
                                val currentModifiedTime = Files.getLastModifiedTime(serverFilePath)
                                if (lastModifiedTime != currentModifiedTime) {
                                    lastModifiedTime = currentModifiedTime
                                    DialogController.showExternalModificationDialog()
                                }
                            }
                        }

                    key.reset()
                    delay(1000)
                }
            }
    }

    fun loadNames() = serverData.map { it.getString("name") }

    fun loadAddresses() = serverData.map { it.getString("ip") }

    fun saveServerData() =
        runCatching {
            if (isInternalModification) return@runCatching

            val serverDataFile =
                File(ConfigurationHandler.getInstance().generalSettings.serverFilePath)

            if (!serverDataFile.exists()) {
                StartupCoordinator.retryLoad()
                return@runCatching
            }

            CompoundTag()
                .apply { put("servers", serverData) }
                .let { compoundTag ->
                    isInternalModification = true
                    NBTUtil.write(
                        compoundTag,
                        serverDataFile,
                        ConfigurationHandler.getInstance().advancedSettings.compressServerFile,
                    )
                    Logger.printSuccess("Server data successfully saved.")
                }
        }
            .onFailure { error ->
                Logger.printError("Error saving server data: ${error.localizedMessage}")
            }
            .also {
                isInternalModification = false
                lastModifiedTime = Files.getLastModifiedTime(serverFilePath)
            }

    fun wipeServerFile() =
        runCatching {
            if (!::serverData.isInitialized) {
                Logger.printSuccess("wipeServerFile: serverData is not initialized.")
                return@runCatching
            }

            serverData.clear()
            saveServerData()
            Logger.printSuccess("All server entries have been deleted.")
        }
            .onFailure { error ->
                Logger.printError("Error while wiping server entries: ${error.localizedMessage}")
            }

    fun getServerIcon(index: Int): InputStream? =
        runCatching {
            require(index in 0 until serverData.size()) {
                "getServerIcon: Invalid index $index."
            }

            serverData[index]
                .getString("icon")
                .takeIf { it.isNotEmpty() }
                ?.let { iconBase64 ->
                    Base64.getDecoder()
                        .decode(iconBase64)
                        .takeIf { it.size <= 96 * 1024 }
                        ?.inputStream()
                        ?: run {
                            Logger.printWarning(
                                "Icon size exceeds limit for server at index: $index",
                            )
                            null
                        }
                }
        }
            .getOrNull()

    fun getRawIconValue(index: Int): String? =
        if (index in 0 until serverData.size()) {
            serverData[index].getString("icon")
        } else {
            null
        }

    fun isHidden(index: Int): Boolean =
        runCatching {
            require(index in 0 until serverData.size()) {
                "isHidden: Invalid index $index, cannot retrieve hidden status."
            }
            serverData[index].getBoolean("hidden")
        }
            .getOrElse { exception ->
                Logger.printError(
                    "Error retrieving 'hidden' status at index $index: ${exception.localizedMessage}",
                )
                false
            }

    fun getAcceptedTexturesState(index: Int): Int? =
        runCatching {
            require(index in 0 until serverData.size()) {
                "getAcceptedTexturesState: Invalid index $index, cannot retrieve accepted textures state."
            }
            serverData[index].getInt("acceptedTextures")
        }
            .getOrNull()

    fun moveServerUp(index: Int) =
        runCatching {
            require(index > 0 && index < serverData.size()) {
                "moveServerUp: Invalid index $index, cannot move up."
            }

            val newIndex = index - 1
            val server = serverData.remove(index)
            serverData.add(newIndex, server)
            saveServerData()

            Logger.printSuccess("Server entry moved from index $index to index $newIndex.")
        }
            .onFailure { error ->
                Logger.printError(
                    "Error moving server up from index $index: ${error.localizedMessage}",
                )
            }

    fun moveServerDown(index: Int) =
        runCatching {
            require(index in 0 until serverData.size() - 1) {
                "moveServerDown: Invalid index $index, cannot move down."
            }

            val newIndex = index + 1
            val server = serverData.remove(index)
            serverData.add(newIndex, server)
            saveServerData()

            Logger.printSuccess("Server entry moved from index $index to index $newIndex.")
        }
            .onFailure { error ->
                Logger.printError(
                    "Error moving server down from index $index: ${error.localizedMessage}",
                )
            }

    fun deleteServer(index: Int) =
        runCatching {
            require(index in 0 until serverData.size()) {
                "deleteServer: Invalid index $index, cannot delete."
            }
            serverData.remove(index)
            saveServerData()
            Logger.printSuccess("Server entry deleted at index $index.")
        }
            .onFailure { error ->
                Logger.printError(
                    "Error deleting server at index $index: ${error.localizedMessage}",
                )
            }

    fun createServerEntry(
        acceptedTextures: Int?,
        icon: String?,
        address: String,
        name: String,
    ) = runCatching {
        val serverTag =
            CompoundTag().apply {
                acceptedTextures?.let { putInt("acceptedTextures", it) }
                putInt("hidden", 0)
                icon?.let { putString("icon", it) }
                putString("ip", address)
                putString("name", name)
            }

        serverData.add(serverData.size(), serverTag)
        saveServerData()
        Logger.printSuccess(
            "Server entry created at index ${serverData.size() - 1} with name '$name'.",
        )
    }
        .onFailure { error ->
            Logger.printError("Error creating server entry: ${error.localizedMessage}")
        }

    fun deleteServerIcon(serverIndex: Int) =
        runCatching {
            require(serverIndex in 0 until serverData.size()) {
                "deleteServerIcon: Invalid server index $serverIndex."
            }

            val serverTag = serverData[serverIndex]
            if (serverTag.remove("icon") != null) {
                saveServerData()
                Logger.printSuccess("Icon removed for server at index $serverIndex.")
            } else {
                Logger.printWarning("No icon found for server at index $serverIndex.")
            }
        }
            .onFailure { error ->
                Logger.printError(
                    "Error removing icon for server at index $serverIndex: ${error.localizedMessage}",
                )
            }

    fun modifyIcon(
        index: Int,
        newIconBase64: String,
    ) = runCatching {
        require(index in 0 until serverData.size()) {
            "modifyIcon: Invalid index $index, cannot modify icon."
        }

        val iconBytes = Base64.getDecoder().decode(newIconBase64)
        ImageIO.read(iconBytes.inputStream())
            ?: throw IllegalArgumentException(
                "Decoded image is null for server at index $index",
            )

        serverData[index].putString("icon", newIconBase64)
        saveServerData()
        Logger.printSuccess("Icon successfully updated for server at index $index.")
    }
        .onFailure { error ->
            Logger.printError(
                "Error updating icon for server at index $index: ${error.localizedMessage}",
            )
            DialogController.showIconValueDecodeFailureDialog()
        }

    fun renameServer(
        index: Int,
        newName: String,
    ) = runCatching {
        require(index in 0 until serverData.size()) {
            "renameServer: Invalid index $index, cannot rename."
        }

        serverData[index].putString("name", newName)
        saveServerData()
        Logger.printSuccess("Server entry at index $index renamed to '$newName'.")
    }
        .onFailure { error ->
            Logger.printError(
                "Error renaming server at index $index: ${error.localizedMessage}",
            )
        }

    fun changeAddress(
        index: Int,
        newIp: String,
    ) = runCatching {
        require(index in 0 until serverData.size()) {
            "changeAddress: Invalid index $index, cannot change IP."
        }

        serverData[index].putString("ip", newIp)
        saveServerData()
        Logger.printSuccess("Server address changed to '$newIp' at index $index.")
    }
        .onFailure { error ->
            Logger.printError("Error changing IP at index $index: ${error.localizedMessage}")
        }

    fun changeHidden(
        index: Int,
        hiddenValue: Int,
    ) = runCatching {
        require(index in 0 until serverData.size()) {
            "changeHidden: Invalid index $index, cannot change hidden status."
        }

        serverData[index].putInt("hidden", hiddenValue)
        saveServerData()
        Logger.printSuccess("Server hidden status changed to $hiddenValue at index $index.")
    }
        .onFailure { error ->
            Logger.printError(
                "Error changing hidden status at index $index: ${error.localizedMessage}",
            )
        }

    fun changeAcceptedTextures(
        index: Int,
        acceptedTexturesValue: Int,
    ) = runCatching {
        require(index in 0 until serverData.size()) {
            "changeAcceptedTextures: Invalid index $index, cannot change accepted textures."
        }

        serverData[index].putInt("acceptedTextures", acceptedTexturesValue)
        saveServerData()
        Logger.printSuccess(
            "Accepted textures updated to $acceptedTexturesValue at index $index.",
        )
    }
        .onFailure { error ->
            Logger.printError(
                "Error changing accepted textures at index $index: ${error.localizedMessage}",
            )
        }
}

enum class ServerFileValidationResult {
    VALID,
    FILE_NOT_FOUND,
    INVALID_NBT,
    NOT_A_FILE,
    WRONG_FILE_NAME,
}
