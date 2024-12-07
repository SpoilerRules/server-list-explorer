package com.spoiligaming.explorer

import com.spoiligaming.explorer.utils.MacUtility
import com.spoiligaming.logging.Logger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.io.File
import java.nio.file.Paths

@Serializable
data class SettingsGeneral(
    var renderApi: String =
        if (hostOs.isMacOS && MacUtility.isMetalSupportedOnMac()) {
            "Metal"
        } else {
            "Software Compat"
        },
    var scrollBarVisibility: String = "Right Side",
    var controlPanelPosition: String = "Top",
    var serverFilePath: String? = getDefaultServerFilePath(),
) {
    companion object {
        private fun getDefaultServerFilePath(): String? =
            System.getenv("APPDATA")?.let {
                Paths.get(it, ".minecraft", "servers.dat").toString()
            } ?: Paths.get(
                System.getProperty("user.home"),
                when (hostOs) {
                    OS.MacOS -> "Library/Application Support/minecraft/servers.dat"
                    else -> ".minecraft/servers.dat"
                },
            ).takeIf { it.toFile().exists() }?.toString()
    }
}

@Serializable
data class SettingsTheme(
    var showScrollbarBackground: Boolean = true,
    var shortcutsInContextMenu: Boolean = true,
    var iconifiedDialogOptions: Boolean = false,
    var windowScale: String = "100%",
    var accentColor: String = "#E85D9B",
    var menuColor: String = "#404040",
    var controlColor: String = "#4C4C4C",
    var secondaryControlColor: String = "#595959",
    var tertiaryControlColor: String = "#3F3F3F",
    var secondaryColor: String = "#727272",
    var tertiaryColor: String = "#282828",
    var quaternaryColor: String = "#343434",
    var textColor: String = "#FFFFFF",
    var fadedTextColor: String = "#CCCCCC",
)

@Serializable
data class SettingsAdvanced(
    var serverFileMonitoring: Boolean = true,
    var isServerFileCompressed: Boolean = false,
    var compressServerFile: Boolean = false,
)

@Serializable
data class PropertiesWindow(
    var previousScale: String = "100%",
    var wasPreviousScaleResizable: Boolean = false,
    var isMaximized: Boolean = false,
    var currentWindowSize: Pair<Int, Int>? = null,
)

@Serializable
data class ConfigurationHandler(
    @SerialName("General") var generalSettings: SettingsGeneral = SettingsGeneral(),
    @SerialName("Theme") var themeSettings: SettingsTheme = SettingsTheme(),
    @SerialName("Advanced") var advancedSettings: SettingsAdvanced = SettingsAdvanced(),
    @SerialName("WindowProperties") var windowProperties: PropertiesWindow = PropertiesWindow(),
) {
    companion object {
        private var configFactoryInstance: ConfigurationHandler? = null
        private val configFile = File("server-list-explorer_configuration.json")
        private val jsonFormatter =
            Json {
                encodeDefaults = true
                prettyPrint = true
            }

        fun getInstance(): ConfigurationHandler {
            configFactoryInstance =
                configFactoryInstance
                    ?: if (configFile.exists() && configFile.length() != 250L) {
                        jsonFormatter.decodeFromString(serializer(), configFile.readText())
                    } else {
                        ConfigurationHandler().also {
                            runCatching {
                                configFile.writeText(
                                    jsonFormatter.encodeToString(serializer(), it),
                                )
                            }
                                .onFailure { error ->
                                    Logger.printError(
                                        "Failed to create configuration file: ${error.message}",
                                    )
                                }
                                .onSuccess { Logger.printSuccess("Created configuration file.") }
                        }
                    }
            return configFactoryInstance!!
        }

        fun updateValue(updateFunction: ConfigurationHandler.() -> Unit) =
            getInstance().apply(updateFunction).also {
                configFile.writeText(jsonFormatter.encodeToString(serializer(), it))
            }
    }
}
