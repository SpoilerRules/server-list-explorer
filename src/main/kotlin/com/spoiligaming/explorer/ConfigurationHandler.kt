package com.spoiligaming.explorer

import com.spoiligaming.logging.Logger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths

@Serializable
data class SettingsGeneral(
    var renderApi: String = "OpenGL",
    var scrollBarVisibility: String = "Right Side",
    var serverFilePath: String =
        Paths.get(System.getenv("APPDATA"), ".minecraft", "servers.dat").toString(),
)

@Serializable
data class SettingsTheme(
    var shortcutsInContextMenu: Boolean = true,
    var iconifiedDialogOptions: Boolean = false,
    var windowScale: String = "100%",
    var accentColor: String = "#E85D9B",
    var menuColor: String = "#404040",
    var controlColor: String = "#4C4C4C",
    var secondaryControlColor: String = "#3F3F3F",
    var secondaryColor: String = "#727272",
    var tertiaryColor: String = "#282828",
    var quaternaryColor: String = "#343434",
)

@Serializable
data class SettingsAdvanced(
    var serverFileMonitoring: Boolean = true,
    var isServerFileCompressed: Boolean = false,
    var compressServerFile: Boolean = false,
)

@Serializable
data class ConfigurationHandler(
    @SerialName("General") var generalSettings: SettingsGeneral = SettingsGeneral(),
    @SerialName("Theme") var themeSettings: SettingsTheme = SettingsTheme(),
    @SerialName("Advanced") var advancedSettings: SettingsAdvanced = SettingsAdvanced(),
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
                    ?: if (configFile.exists()) {
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
