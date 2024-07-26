package com.spoiligaming.explorer.ui.screens.settings

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BasicTooltipState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.SettingsViewModel
import com.spoiligaming.explorer.ui.components.MapleColorInformation
import com.spoiligaming.explorer.ui.components.MapleColorPicker
import com.spoiligaming.explorer.ui.widgets.DropdownMenuWithLabel
import com.spoiligaming.explorer.ui.widgets.LabeledMapleToggleSwitch
import com.spoiligaming.explorer.ui.widgets.MergedText
import com.spoiligaming.explorer.utils.ColorPaletteUtility
import com.spoiligaming.explorer.windowFrame
import kotlinx.coroutines.launch
import java.awt.Dimension

private val colorPickerStates =
    mutableStateMapOf(
        "Accent" to false,
        "Menu" to false,
        "Control" to false,
        "SecondaryControl" to false,
        "Secondary" to false,
        "Tertiary" to false,
        "Quaternary" to false,
    )

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsTheme() {
    val coroutineScope = rememberCoroutineScope()

    val tooltipStates =
        remember {
            mutableStateMapOf(
                "Accent" to BasicTooltipState(),
                "Menu" to BasicTooltipState(),
                "Control" to BasicTooltipState(),
                "SecondaryControl" to BasicTooltipState(),
                "Secondary" to BasicTooltipState(),
                "Tertiary" to BasicTooltipState(),
                "Quaternary" to BasicTooltipState(),
            )
        }

    val colorPaletteMap =
        mutableMapOf(
            "Accent" to MapleColorPalette.accent,
            "Menu" to MapleColorPalette.menu,
            "Control" to MapleColorPalette.control,
            "SecondaryControl" to MapleColorPalette.secondaryControl,
            "Secondary" to MapleColorPalette.secondary,
            "Tertiary" to MapleColorPalette.tertiary,
            "Quaternary" to MapleColorPalette.quaternary,
        )

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                DropdownMenuWithLabel(
                    label = "Window scale (not recommended)",
                    currentValue = ConfigurationHandler.getInstance().themeSettings.windowScale,
                    options = listOf("150%", "125%", "100%"),
                ) { newValue ->
                    val newScale =
                        when (newValue) {
                            "150%" -> 1.5f
                            "125%" -> 1.25f
                            "100%" -> 1f
                            else ->
                                throw IllegalArgumentException(
                                    "Invalid window scale value: $newValue",
                                )
                        }
                    ConfigurationHandler.updateValue {
                        themeSettings.windowScale = newValue
                        windowFrame.size =
                            Dimension((800 * newScale).toInt(), (600 * newScale).toInt())
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val colorKeys =
                listOf(
                    "Accent",
                    "Menu",
                    "Control",
                    "SecondaryControl",
                    "Secondary",
                    "Tertiary",
                    "Quaternary",
                )
            colorKeys.forEach { key ->
                MergedText("$key colour", MapleColorPalette.text, FontWeight.Light, 2.49988774.dp) {
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
    displayColorPicker("Accent", MapleColorPalette.accent, MapleColorPalette.defaultAccent) {
            newValue ->
        MapleColorPalette.accent = newValue
    }
    displayColorPicker("Menu", MapleColorPalette.menu, MapleColorPalette.defaultMenu) { newValue ->
        MapleColorPalette.menu = newValue
    }
    displayColorPicker("Control", MapleColorPalette.control, MapleColorPalette.defaultControl) {
            newValue ->
        MapleColorPalette.control = newValue
    }
    displayColorPicker(
        "SecondaryControl",
        MapleColorPalette.secondaryControl,
        MapleColorPalette.defaultSecondaryControl,
    ) { newValue ->
        MapleColorPalette.secondaryControl = newValue
    }
    displayColorPicker(
        "Secondary",
        MapleColorPalette.secondary,
        MapleColorPalette.defaultSecondary,
    ) { newValue ->
        MapleColorPalette.secondary = newValue
    }
    displayColorPicker("Tertiary", MapleColorPalette.tertiary, MapleColorPalette.defaultTertiary) {
            newValue ->
        MapleColorPalette.tertiary = newValue
    }
    displayColorPicker(
        "Quaternary",
        MapleColorPalette.quaternary,
        MapleColorPalette.defaultQuaternary,
    ) { newValue,
        ->
        MapleColorPalette.quaternary = newValue
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
            "$key colour",
            color,
            defaultColor,
            { newValue ->
                ConfigurationHandler.updateValue {
                    when (key) {
                        "Accent" ->
                            themeSettings.accentColor =
                                ColorPaletteUtility.getColorAsString(newValue)
                        "Menu" ->
                            themeSettings.menuColor = ColorPaletteUtility.getColorAsString(newValue)
                        "Control" ->
                            themeSettings.controlColor =
                                ColorPaletteUtility.getColorAsString(newValue)
                        "SecondaryControl" ->
                            themeSettings.secondaryControlColor =
                                ColorPaletteUtility.getColorAsString(newValue)
                        "Secondary" ->
                            themeSettings.secondaryColor =
                                ColorPaletteUtility.getColorAsString(newValue)
                        "Tertiary" ->
                            themeSettings.tertiaryColor =
                                ColorPaletteUtility.getColorAsString(newValue)
                        "Quaternary" ->
                            themeSettings.quaternaryColor =
                                ColorPaletteUtility.getColorAsString(newValue)
                    }
                    updateColor(newValue)
                }
            },
        ) {
            colorPickerStates[key] = false
        }
    }
}
