package com.spoiligaming.explorer

import androidx.compose.ui.graphics.Color
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.utils.toHex
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
    var controlPanelPosition: String = "Top",
    var serverFilePath: String =
        Paths.get(System.getenv("APPDATA"), ".minecraft", "servers.dat").toString(),
)

@Serializable
data class SettingsTheme(
    var showScrollbarBackground: Boolean = true,
    var shortcutsInContextMenu: Boolean = true,
    var iconifiedDialogOptions: Boolean = false,
    var windowScale: String = "100%",
    var accentColor: String = formatColor(MapleColorPalette.defaultAccent),
    var secondaryColor: String = formatColor(MapleColorPalette.defaultSecondary),
    var secondaryControlColor: String = formatColor(MapleColorPalette.defaultSecondaryControl),
    var controlColor: String = formatColor(MapleColorPalette.defaultControl),
    var tertiaryControlColor: String = formatColor(MapleColorPalette.defaultTertiaryControl),
    var menuColor: String = formatColor(MapleColorPalette.defaultMenu),
    var quaternaryColor: String = formatColor(MapleColorPalette.defaultQuaternary),
    var tertiaryColor: String = formatColor(MapleColorPalette.defaultTertiary),
) {
    companion object {
        private fun formatColor(color: Color): String = "#" + color.toHex().dropLast(2)
    }
}

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
