package com.spoiligaming.explorer.ui.screens.settings

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BasicTooltipState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import com.spoiligaming.explorer.ConfigurationHandler
import com.spoiligaming.explorer.isWindowMaximized
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.SettingsViewModel
import com.spoiligaming.explorer.ui.components.MapleColorInformation
import com.spoiligaming.explorer.ui.components.MapleColorPicker
import com.spoiligaming.explorer.ui.widgets.DropdownMenuWithLabel
import com.spoiligaming.explorer.ui.widgets.LabeledMapleToggleSwitch
import com.spoiligaming.explorer.ui.widgets.MergedText
import com.spoiligaming.explorer.utils.ColorPaletteUtility
import com.spoiligaming.explorer.utils.WindowUtility
import com.spoiligaming.explorer.utils.WindowUtility.centerOnScreen
import com.spoiligaming.explorer.windowFrame
import kotlinx.coroutines.launch
import java.awt.Dimension

val colorKeys =
    listOf(
        "Accent",
        "Secondary",
        "Secondary Control",
        "Control",
        "Menu",
        "Tertiary Control",
        "Quaternary",
        "Tertiary",
    )

private val colorPickerStates =
    mutableStateMapOf<String, Boolean>().apply {
        colorKeys.forEach { key -> this[key] = false }
    }

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsTheme() {
    val coroutineScope = rememberCoroutineScope()

    val tooltipStates =
        remember {
            colorKeys.associateWith { BasicTooltipState() }
        }

    val colorPaletteMap by remember {
        derivedStateOf {
            colorKeys.zip(
                listOf(
                    MapleColorPalette.accent,
                    MapleColorPalette.secondary,
                    MapleColorPalette.secondaryControl,
                    MapleColorPalette.control,
                    MapleColorPalette.menu,
                    MapleColorPalette.tertiaryControl,
                    MapleColorPalette.quaternary,
                    MapleColorPalette.tertiary,
                ),
            ).toMap()
        }
    }

    tooltipStates.forEach { (key, tooltipState) ->
        LaunchedEffect(colorPickerStates[key]) {
            if (colorPickerStates[key] == true) {
                tooltipState.show()
            } else {
                tooltipState.dismiss()
            }
        }
    }

    LabeledMapleToggleSwitch(
        "Show Scrollbar Background",
        ConfigurationHandler.getInstance().themeSettings.showScrollbarBackground,
    ) { newValue ->
        ConfigurationHandler.updateValue { themeSettings.showScrollbarBackground = newValue }
    }

    LabeledMapleToggleSwitch(
        "Show Shortcuts in Context Menu",
        ConfigurationHandler.getInstance().themeSettings.shortcutsInContextMenu,
    ) { newValue ->
        ConfigurationHandler.updateValue { themeSettings.shortcutsInContextMenu = newValue }
        SettingsViewModel.displayShortcutsInContextMenu = newValue
    }

    LabeledMapleToggleSwitch(
        "Experimental Iconified Dialog Options",
        ConfigurationHandler.getInstance().themeSettings.iconifiedDialogOptions,
    ) { newValue ->
        ConfigurationHandler.updateValue { themeSettings.iconifiedDialogOptions = newValue }
        SettingsViewModel.experimentalIconifiedDialogOptions = newValue
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(contentAlignment = Alignment.Center) {
            DropdownMenuWithLabel(
                label = "Window scale",
                currentValue =
                    run {
                        when (
                            val currentScale =
                                ConfigurationHandler.getInstance().themeSettings.windowScale
                        ) {
                            "Resizable" -> "Resizable (unstable)"
                            "Maximized" -> "Maximized (unstable)"
                            else -> currentScale
                        }
                    },
                options =
                    listOf(
                        "150%",
                        "125%",
                        "100%",
                        "Resizable (unstable)",
                        "Maximized (unstable)",
                    ),
            ) { newValue ->
                val processedValue =
                    when {
                        newValue.contains("Resizable (unstable)") -> "Resizable"
                        newValue.contains("Maximized (unstable)") -> "Maximized"
                        else -> newValue
                    }

                val currentScale = ConfigurationHandler.getInstance().themeSettings.windowScale

                if (processedValue in listOf("Resizable", "Maximized") &&
                    currentScale in WindowUtility.windowScaleMapping.keys
                ) {
                    ConfigurationHandler.updateValue {
                        windowProperties.previousScale = currentScale
                    }
                }

                windowFrame.isResizable = processedValue == "Resizable"
                isWindowMaximized = processedValue == "Maximized"
                ConfigurationHandler.updateValue {
                    windowProperties.wasPreviousScaleResizable = processedValue == "Resizable"
                }

                ConfigurationHandler.updateValue {
                    themeSettings.windowScale = processedValue

                    when (processedValue) {
                        "Maximized" -> {
                            if (currentScale != "Resizable") {
                                windowProperties.previousScale = currentScale
                                if (currentScale != "Maximized") {
                                    windowProperties.wasPreviousScaleResizable = false
                                }
                            } else {
                                windowProperties.wasPreviousScaleResizable = true
                            }
                            WindowUtility.maximizeWindow()
                        }
                        "Resizable" ->
                            windowFrame.apply {
                                size =
                                    ConfigurationHandler.getInstance().let { config ->
                                        config.themeSettings.windowScale.let { scale ->
                                            WindowUtility.windowScaleMapping[scale]
                                                ?: WindowUtility.windowScaleMapping[
                                                    config.windowProperties.previousScale,
                                                ]
                                                ?: scale.toFloatOrNull()
                                                ?: 1f
                                        }.let { windowScale ->
                                            config.windowProperties.currentWindowSize?.let {
                                                    (width, height) ->
                                                width to height
                                            } ?: (800 * windowScale).toInt().let { width ->
                                                (600 * windowScale).toInt().let { height ->
                                                    width to height
                                                }
                                            }
                                        }.let { Dimension(it.first, it.second) }
                                    }
                                centerOnScreen()
                            }
                        in WindowUtility.windowScaleMapping.keys -> {
                            val newScale = WindowUtility.windowScaleMapping[processedValue]!!

                            ConfigurationHandler.updateValue {
                                windowProperties.wasPreviousScaleResizable = false
                            }

                            windowFrame.apply {
                                size = Dimension((800 * newScale).toInt(), (600 * newScale).toInt())
                                centerOnScreen()
                            }
                        }
                        else -> throw IllegalArgumentException(
                            "Invalid window scale value: $processedValue",
                        )
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            colorKeys.forEach { key ->
                MergedText("$key color", MapleColorPalette.text, FontWeight.Light, 2.49988774.dp) {
                    Box(
                        modifier =
                            Modifier.width(25.dp)
                                .height(25.dp)
                                .background(
                                    color =
                                        mutableStateOf(colorPaletteMap[key]).value
                                            ?: Color.Transparent,
                                    shape = RoundedCornerShape(4.dp),
                                )
                                .onClick { colorPickerStates[key] = true }
                                .onPointerEvent(PointerEventType.Enter) {
                                    coroutineScope.launch { tooltipStates[key]?.show() }
                                }
                                .onPointerEvent(PointerEventType.Exit) {
                                    tooltipStates[key]?.dismiss()
                                },
                    ) {
                        BasicTooltipBox(
                            tooltip = {
                                MapleColorInformation(colorPaletteMap[key] ?: Color.Transparent)
                            },
                            positionProvider =
                                object : PopupPositionProvider {
                                    override fun calculatePosition(
                                        anchorBounds: IntRect,
                                        windowSize: IntSize,
                                        layoutDirection: LayoutDirection,
                                        popupContentSize: IntSize,
                                    ): IntOffset =
                                        IntOffset(
                                            x = anchorBounds.left + 30,
                                            y = anchorBounds.bottom + 20,
                                        )
                                },
                            state = tooltipStates[key] ?: BasicTooltipState(),
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsThemeOuter() {
    displayColorPicker(
        "Accent",
        MapleColorPalette.accent,
        MapleColorPalette.defaultAccent,
    ) { newValue ->
        MapleColorPalette.accent = newValue
    }
    displayColorPicker(
        "Secondary",
        MapleColorPalette.secondary,
        MapleColorPalette.defaultSecondary,
    ) { newValue ->
        MapleColorPalette.secondary = newValue
    }
    displayColorPicker(
        "Secondary Control",
        MapleColorPalette.secondaryControl,
        MapleColorPalette.defaultSecondaryControl,
    ) { newValue ->
        MapleColorPalette.secondaryControl = newValue
    }
    displayColorPicker(
        "Control",
        MapleColorPalette.control,
        MapleColorPalette.defaultControl,
    ) { newValue ->
        MapleColorPalette.control = newValue
    }
    displayColorPicker("Menu", MapleColorPalette.menu, MapleColorPalette.defaultMenu) { newValue ->
        MapleColorPalette.menu = newValue
    }
    displayColorPicker(
        "Tertiary Control",
        MapleColorPalette.tertiaryControl,
        MapleColorPalette.defaultTertiaryControl,
    ) { newValue ->
        MapleColorPalette.tertiaryControl = newValue
    }
    displayColorPicker(
        "Quaternary",
        MapleColorPalette.quaternary,
        MapleColorPalette.defaultQuaternary,
    ) { newValue ->
        MapleColorPalette.quaternary = newValue
    }
    displayColorPicker(
        "Tertiary",
        MapleColorPalette.tertiary,
        MapleColorPalette.defaultTertiary,
    ) { newValue ->
        MapleColorPalette.tertiary = newValue
    }
}

@Composable
private fun displayColorPicker(
    key: String,
    color: Color,
    defaultColor: Color,
    updateColor: (Color) -> Unit,
) {
    if (colorPickerStates[key] == true) {
        MapleColorPicker(
            "$key color",
            color,
            defaultColor,
            { newValue ->
                ConfigurationHandler.updateValue {
                    themeSettings =
                        when (key) {
                            "Accent" ->
                                themeSettings.copy(
                                    accentColor =
                                        ColorPaletteUtility.getColorAsString(
                                            newValue,
                                        ),
                                )
                            "Secondary" ->
                                themeSettings.copy(
                                    secondaryColor =
                                        ColorPaletteUtility.getColorAsString(
                                            newValue,
                                        ),
                                )
                            "Secondary Control" ->
                                themeSettings.copy(
                                    secondaryControlColor =
                                        ColorPaletteUtility.getColorAsString(
                                            newValue,
                                        ),
                                )
                            "Control" ->
                                themeSettings.copy(
                                    controlColor =
                                        ColorPaletteUtility.getColorAsString(
                                            newValue,
                                        ),
                                )
                            "Tertiary Control" ->
                                themeSettings.copy(
                                    tertiaryControlColor =
                                        ColorPaletteUtility.getColorAsString(
                                            newValue,
                                        ),
                                )
                            "Menu" ->
                                themeSettings.copy(
                                    menuColor =
                                        ColorPaletteUtility.getColorAsString(
                                            newValue,
                                        ),
                                )
                            "Quaternary" ->
                                themeSettings.copy(
                                    quaternaryColor =
                                        ColorPaletteUtility.getColorAsString(
                                            newValue,
                                        ),
                                )
                            "Tertiary" ->
                                themeSettings.copy(
                                    tertiaryColor =
                                        ColorPaletteUtility.getColorAsString(
                                            newValue,
                                        ),
                                )
                            else -> throw IllegalArgumentException("Unexpected color key: $key")
                        }
                    updateColor(newValue)
                }
            },
        ) {
            colorPickerStates[key] = false
        }
    }
}
