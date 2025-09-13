/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025 SpoilerRules
 *
 * Server List Explorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Server List Explorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Server List Explorer.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.spoiligaming.explorer.ui.widgets

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocalization
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.AnnotatedString
import com.spoiligaming.explorer.util.ClipboardUtils
import com.spoiligaming.explorer.util.OSUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.Method
import kotlin.math.abs

@Composable
internal fun HackedSelectionContainer(
    modifier: Modifier = Modifier,
    onSelectedChange: (Boolean) -> Unit = {},
    onAllSelectedChange: (Boolean) -> Unit = {},
    onSelectionTextChange: (String?) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val registrarClass = rememberCls("androidx.compose.foundation.text.selection.SelectionRegistrarImpl")
    val managerClass = rememberCls("androidx.compose.foundation.text.selection.SelectionManager")

    val registrar =
        remember {
            registrarClass.getDeclaredConstructor().newInstance().also {
                logger.debug { "Selection registrar created (${registrarClass.name})" }
            }
        }

    val manager =
        remember {
            managerClass.getDeclaredConstructor(registrarClass).newInstance(registrar).also {
                logger.debug { "Selection manager created (${managerClass.name})" }
            }
        }

    var isAllSelected by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    val selectedTextGetter =
        remember(managerClass) {
            SelectionReflection
                .findZeroArgMethodContaining(managerClass, "SelectedText")
                ?.apply { isAccessible = true }
        }

    val clearSelectionMethod = remember(managerClass) { SelectionReflection.findClearSelection(managerClass) }
    val selectAllMethod = remember(managerClass) { SelectionReflection.findSelectAll(managerClass) }
    val managerModifier = SelectionReflection.managerModifier(manager, managerClass)

    SideEffect {
        managerClass.getDeclaredField("focusRequester").apply { isAccessible = true }.set(manager, focusRequester)

        val setOnSelectionChange =
            (managerClass.methods + managerClass.declaredMethods)
                .firstOrNull { it.name == "setOnSelectionChange" && it.parameterTypes.size == 1 }
                ?: error(
                    "Could not find SelectionManager.setOnSelectionChange. " +
                        "Compose internals may have changed. " +
                        "Update this integration or pin a compatible version.",
                )

        val callback: (Any?) -> Unit = { selectionObj ->
            val selectedText =
                when (val raw = selectedTextGetter!!.invoke(manager)) {
                    is CharSequence -> raw.toString()
                    null -> null
                    else ->
                        raw.javaClass
                            .getDeclaredField("text")
                            .apply { isAccessible = true }
                            .get(raw) as? String
                            ?: raw.toString()
                }

            val collapsed = SelectionReflection.isCollapsed(selectionObj)
            val hasSelection = !collapsed
            val allSelected = SelectionRuntime.isAllSelected(manager, managerClass)

            logger.trace {
                "Selection changed " +
                    "hasSelection=$hasSelection " +
                    "allSelected=$allSelected " +
                    "length=${selectedText?.length ?: 0}"
            }

            onSelectedChange(hasSelection)
            onAllSelectedChange(allSelected)
            onSelectionTextChange(selectedText)
            isAllSelected = allSelected
        }

        setOnSelectionChange.apply { isAccessible = true }.invoke(manager, callback)
    }

    val keyHandlingModifier =
        modifier.onKeyEvent { e ->
            if (e.type != KeyEventType.KeyDown) return@onKeyEvent false

            when (e.key) {
                Key.Escape -> {
                    clearSelectionMethod!!.invoke(manager)
                    focusManager.clearFocus(force = true)
                    true
                }

                Key.A -> {
                    val primaryPressed =
                        (OSUtils.isMacOS && e.isMetaPressed) || (!OSUtils.isMacOS && e.isCtrlPressed)
                    if (!primaryPressed) return@onKeyEvent false

                    selectAllMethod!!.invoke(manager)
                    true
                }

                else -> false
            }
        }

    PlatformHooks.attach(manager, managerClass)

    val localRegistrar = remember { ComposeLookup.resolveLocalSelectionRegistrar() }
    val charAdjustment = rememberSelectionAdjustmentInstance("Character")
    val viewConfig = LocalViewConfiguration.current
    val updateSelectionMethod = remember { SelectionReflection.findUpdateSelectionMethod(managerClass) }

    val pointerDragModifier =
        Modifier.pointerInput(manager) {
            awaitEachGesture {
                val down = awaitFirstDown()
                val start = down.position
                var prev = start
                val pid = down.id
                var started = false

                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.first { it.id == pid }
                    val pos = change.position

                    if (!started) {
                        if ((pos - start).getDistance() > viewConfig.touchSlop) {
                            SelectionReflection.invokeUpdateSelection(
                                updateSelectionMethod,
                                start,
                                start,
                                true,
                                charAdjustment,
                                manager,
                            )
                            SelectionReflection.invokeUpdateSelection(
                                updateSelectionMethod,
                                start,
                                start,
                                false,
                                charAdjustment,
                                manager,
                            )
                            started = true
                            change.consume()
                            prev = pos
                        }
                    } else {
                        SelectionReflection.invokeUpdateSelection(
                            updateSelectionMethod,
                            pos,
                            prev,
                            false,
                            charAdjustment,
                            manager,
                        )
                        prev = pos
                        change.consume()
                    }

                    if (event.changes.all { !it.pressed }) break
                }
            }
        }

    Box(
        modifier =
            keyHandlingModifier
                .focusRequester(focusRequester)
                .then(pointerDragModifier)
                .then(managerModifier),
    ) {
        SelectableContextMenuArea(manager, isAllSelected = isAllSelected) {
            CompositionLocalProvider(localRegistrar provides registrar) {
                content()
            }
        }
    }
}

private object SelectionReflection {
    private const val INT_BIT_WIDTH = 32
    private const val LOW_32_MASK = 0xFFFF_FFFFL

    private const val UPDATE_SELECTION_PARAM_COUNT = 4
    private const val POS_PARAM_INDEX = 0
    private const val PREV_PARAM_INDEX = 1
    private const val IS_START_PARAM_INDEX = 2
    private const val ADJ_PARAM_INDEX = 3

    private fun Class<*>.allMethods() = methods.asSequence() + declaredMethods.asSequence()

    fun findZeroArgMethodContaining(
        cls: Class<*>,
        token: String,
    ) = cls.allMethods().firstOrNull {
        it.parameterCount == ZERO_PARAMETER_COUNT && it.name.contains(token, ignoreCase = true)
    }

    fun findClearSelection(cls: Class<*>) =
        cls
            .allMethods()
            .firstOrNull { m ->
                m.parameterCount == ZERO_PARAMETER_COUNT &&
                    arrayOf("clearSelection", "deselect", "onRelease").any { n ->
                        m.name.equals(n, ignoreCase = true) || m.name.contains(n, ignoreCase = true)
                    }
            }?.apply { isAccessible = true }

    fun findSelectAll(cls: Class<*>) =
        cls
            .allMethods()
            .firstOrNull {
                it.parameterCount == ZERO_PARAMETER_COUNT &&
                    it.name.contains("selectAll", ignoreCase = true)
            }?.apply { isAccessible = true }

    fun findUpdateSelectionMethod(cls: Class<*>): Method {
        val adjCls = Class.forName("androidx.compose.foundation.text.selection.SelectionAdjustment")
        return cls
            .allMethods()
            .first {
                it.name.startsWith("updateSelection") &&
                    it.parameterTypes.size == UPDATE_SELECTION_PARAM_COUNT &&
                    it.parameterTypes[POS_PARAM_INDEX] == Long::class.javaPrimitiveType &&
                    it.parameterTypes[PREV_PARAM_INDEX] == Long::class.javaPrimitiveType &&
                    it.parameterTypes[IS_START_PARAM_INDEX] == Boolean::class.javaPrimitiveType &&
                    adjCls.isAssignableFrom(it.parameterTypes[ADJ_PARAM_INDEX])
            }.apply { isAccessible = true }
    }

    fun managerModifier(
        manager: Any,
        cls: Class<*>,
    ) = cls.getDeclaredMethod("getModifier").apply { isAccessible = true }.invoke(manager) as Modifier

    fun isCollapsed(sel: Any?): Boolean {
        if (sel == null) return true
        val start =
            sel.javaClass
                .getDeclaredField("start")
                .apply { isAccessible = true }
                .get(sel)
        val end =
            sel.javaClass
                .getDeclaredField("end")
                .apply { isAccessible = true }
                .get(sel)
        val sid =
            start.javaClass
                .getDeclaredField("selectableId")
                .apply { isAccessible = true }
                .getLong(start)
        val eid =
            end.javaClass
                .getDeclaredField("selectableId")
                .apply { isAccessible = true }
                .getLong(end)
        val so =
            start.javaClass
                .getDeclaredField("offset")
                .apply { isAccessible = true }
                .getInt(start)
        val eo =
            end.javaClass
                .getDeclaredField("offset")
                .apply { isAccessible = true }
                .getInt(end)
        return sid == eid && so == eo
    }

    private fun pack(o: Offset): Long {
        val xBits = o.x.toBits()
        val yBits = o.y.toBits()
        return (xBits.toLong() shl INT_BIT_WIDTH) or (yBits.toLong() and LOW_32_MASK)
    }

    fun invokeUpdateSelection(
        m: Method,
        pos: Offset,
        prev: Offset,
        isStart: Boolean,
        adj: Any,
        mgr: Any,
    ) {
        m.invoke(mgr, pack(pos), pack(prev), isStart, adj)
    }
}

private object SelectionRuntime {
    private const val ONE_PARAMETER_COUNT = 1
    private const val ZERO_LENGTH = 0

    fun isAllSelected(
        manager: Any,
        managerCls: Class<*>,
    ): Boolean {
        val registrar =
            managerCls
                .getDeclaredField("selectionRegistrar")
                .apply { isAccessible = true }
                .get(manager)!!

        val coords =
            managerCls
                .getDeclaredField("containerLayoutCoordinates")
                .apply { isAccessible = true }
                .get(manager) ?: error("Container layout coordinates are missing.")

        val sort =
            (registrar.javaClass.methods + registrar.javaClass.declaredMethods)
                .first { it.name == "sort" && it.parameterTypes.size == ONE_PARAMETER_COUNT }
                .apply { isAccessible = true }
        val selectables = sort.invoke(registrar, coords) as List<*>

        val subs =
            (registrar.javaClass.methods + registrar.javaClass.declaredMethods)
                .firstOrNull { it.name.contains("Subselection", true) && it.parameterCount == ZERO_PARAMETER_COUNT }
                ?.apply { isAccessible = true }
                ?.invoke(registrar)
                ?: registrar.javaClass.declaredFields
                    .first { it.name.contains("subselection", true) }
                    .apply { isAccessible = true }
                    .get(registrar)

        val subsGet =
            (subs.javaClass.methods + subs.javaClass.declaredMethods)
                .first {
                    it.name == "get" &&
                        it.parameterTypes.size == ONE_PARAMETER_COUNT &&
                        (
                            it.parameterTypes[0] == java.lang.Long.TYPE ||
                                it.parameterTypes[0] == java.lang.Long::class.java
                        )
                }.apply { isAccessible = true }

        for (sel in selectables) {
            if (sel == null) continue

            val textObj =
                sel.javaClass
                    .getDeclaredMethod("getText")
                    .apply { isAccessible = true }
                    .invoke(sel)

            val len =
                when (textObj) {
                    is CharSequence -> textObj.length
                    null -> 0
                    else ->
                        (
                            textObj.javaClass
                                .getDeclaredField("text")
                                .apply { isAccessible = true }
                                .get(textObj) as String
                        ).length
                }
            if (len == ZERO_LENGTH) continue

            val id =
                sel.javaClass
                    .getDeclaredMethod("getSelectableId")
                    .apply { isAccessible = true }
                    .invoke(sel) as Long

            val sub = subsGet.invoke(subs, id) ?: return false

            val start =
                sub.javaClass
                    .getDeclaredField("start")
                    .apply { isAccessible = true }
                    .get(sub)
            val end =
                sub.javaClass
                    .getDeclaredField("end")
                    .apply { isAccessible = true }
                    .get(sub)
            val so =
                start.javaClass
                    .getDeclaredField("offset")
                    .apply { isAccessible = true }
                    .getInt(start)
            val eo =
                end.javaClass
                    .getDeclaredField("offset")
                    .apply { isAccessible = true }
                    .getInt(end)

            if (abs(so - eo) != len) return false
        }

        return true
    }
}

private object PlatformHooks {
    fun attach(
        target: Any,
        cls: Class<*>,
    ) {
        fun setField(
            field: String,
            v: Any?,
        ) = cls.getDeclaredField(field).apply { isAccessible = true }.set(target, v)

        fun setFields(vararg pairs: Pair<String, Any?>) {
            for ((f, v) in pairs) setField(f, v)
        }

        /*
         * These helpers are only meaningful on Android and are skipped on desktop:
         * - hapticFeedback: provides vibration/haptic responses to user actions
         * - textToolbar: enables selection toolbar features like cut/copy/paste
         */

        // setFields(
        //     "hapticFeedback" to LocalHapticFeedback.current,
        //     "textToolbar" to LocalTextToolbar.current,
        // )

        // provide copy-to-clipboard support with Ctrl+C on desktop
        setField("onCopyHandler") { text: AnnotatedString -> ClipboardUtils.copy(text.text) }
    }
}

private object ComposeLookup {
    @Suppress("UNCHECKED_CAST")
    fun resolveLocalSelectionRegistrar(): ProvidableCompositionLocal<Any?> {
        val holders =
            listOf(
                "androidx.compose.foundation.text.selection.SelectionRegistrarKt",
                "androidx.compose.foundation.text.selection.SelectionKt",
                "androidx.compose.foundation.text.selection.SelectionJvmKt",
                "androidx.compose.foundation.text.selection.Selection_desktopKt",
                "androidx.compose.foundation.text.selection.Selection",
            )
        for (h in holders) {
            val c = runCatching { Class.forName(h) }.getOrNull() ?: continue
            runCatching {
                val field = c.getDeclaredField("LocalSelectionRegistrar").apply { isAccessible = true }
                logger.debug { "LocalSelectionRegistrar resolved via $h.field." }
                return field.get(null) as ProvidableCompositionLocal<Any?>
            }
            runCatching {
                logger.debug { "LocalSelectionRegistrar resolved via $h.getter." }
                return c
                    .getDeclaredMethod(
                        "getLocalSelectionRegistrar",
                    ).invoke(null) as ProvidableCompositionLocal<Any?>
            }
        }
        error(
            "Cannot resolve LocalSelectionRegistrar. " +
                "Compose internals may have changed. " +
                "Update this integration or pin a compatible version.",
        )
    }
}

@Composable
private fun SelectableContextMenuArea(
    manager: Any,
    isAllSelected: Boolean,
    content: @Composable () -> Unit,
) {
    val state = remember { ContextMenuState() }
    val copyMethod = rememberCopyMethod(manager)
    val selectAllMethod = remember(manager) { SelectionReflection.findSelectAll(manager::class.java) }
    val localization = LocalLocalization.current

    val menuItems =
        remember(copyMethod, selectAllMethod, localization, isAllSelected) {
            buildList {
                copyMethod?.let { m -> add(ContextMenuItem(localization.copy) { m.invoke(manager) }) }
                if (!isAllSelected && selectAllMethod != null) {
                    add(ContextMenuItem(localization.selectAll) { selectAllMethod.invoke(manager) })
                }
            }
        }

    ContextMenuArea(items = { menuItems }, state = state, content = content)
}

@Composable
private fun rememberCls(name: String) =
    remember(name) {
        runCatching { Class.forName(name) }
            .onFailure { e -> logger.error(e) { "Cannot load class $name." } }
            .getOrThrow()
            .also { logger.debug { "Loaded class $name." } }
    }

@Composable
private fun rememberCopyMethod(manager: Any) =
    remember(manager) {
        val all = manager::class.java.methods + manager::class.java.declaredMethods
        all.firstOrNull { it.name.equals("copy", true) && it.parameterCount == ZERO_PARAMETER_COUNT }
            ?: all.firstOrNull { it.name.startsWith("copy", true) && it.parameterCount == ZERO_PARAMETER_COUNT }
    }?.apply { isAccessible = true }

@Composable
private fun rememberSelectionAdjustmentInstance(name: String) =
    remember(name) {
        val adjCls = Class.forName("androidx.compose.foundation.text.selection.SelectionAdjustment")
        val companion = adjCls.getDeclaredField("Companion").apply { isAccessible = true }.get(null)
        val getter =
            (companion.javaClass.methods + companion.javaClass.declaredMethods)
                .first { it.parameterCount == ZERO_PARAMETER_COUNT && it.name.equals("get$name", true) }
                .apply { isAccessible = true }
        getter.invoke(companion) ?: error("SelectionAdjustment.$name is not available.")
    }

private const val ZERO_PARAMETER_COUNT = 0
private val logger = KotlinLogging.logger {}
