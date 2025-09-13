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

package com.spoiligaming.explorer.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spoiligaming.explorer.ui.t
import org.jetbrains.compose.resources.stringResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.cd_dialog_icon
import server_list_explorer.ui.generated.resources.dialog_cancel_button

internal class DialogSpec {
    var title = ""
    var icon: ImageVector? = null
    var supportText: AnnotatedString? = null
    var body: (@Composable () -> Unit)? = null

    var properties = DialogProperties()
    var modifier: Modifier = Modifier

    internal var acceptButton: DialogButton? = null
    internal var cancelButton: DialogButton? = null
    internal var extraButton: DialogButton? = null

    fun title(value: String) {
        title = value
    }

    fun icon(value: ImageVector?) {
        icon = value
    }

    fun supportText(value: String?) {
        supportText = value?.let { AnnotatedString(it) }
    }

    fun supportText(value: AnnotatedString?) {
        supportText = value
    }

    fun body(content: @Composable () -> Unit) {
        body = content
    }

    fun accept(button: DialogButton) {
        acceptButton = button
    }

    fun cancel(button: DialogButton) {
        cancelButton = button
    }

    fun extra(button: DialogButton) {
        extraButton = button
    }
}

@JvmInline
internal value class DialogButton(
    private val data: DialogButtonData,
) {
    val text: String get() = data.text
    val onClick: () -> Unit get() = data.onClick
    val isProminent: Boolean get() = data.isProminent
    val isEnabled: Boolean get() = data.enabled
}

internal data class DialogButtonData(
    val text: String,
    val onClick: () -> Unit,
    val isProminent: Boolean = false,
    val enabled: Boolean = true,
)

internal val String.prominent: ProminentLabel
    get() = ProminentLabel(this)

@JvmInline
internal value class ProminentLabel(
    val text: String,
)

internal infix fun ProminentLabel.onClick(action: () -> Unit) =
    DialogButton(DialogButtonData(text, action, isProminent = true))

internal fun ProminentLabel.onClick(
    enabled: Boolean = true,
    action: () -> Unit,
): DialogButton = DialogButton(DialogButtonData(text, action, isProminent = true, enabled = enabled))

internal infix fun String.onClick(action: () -> Unit): DialogButton =
    DialogButton(DialogButtonData(this, action, isProminent = false))

internal fun String.onClick(
    enabled: Boolean = true,
    action: () -> Unit,
): DialogButton = DialogButton(DialogButtonData(this, action, isProminent = false, enabled = enabled))

@Composable
private fun DialogActionButton(button: DialogButton) =
    if (button.isProminent) {
        Button(
            modifier =
                Modifier.pointerHoverIcon(
                    if (button.isEnabled) PointerIcon.Hand else PointerIcon.Default,
                ),
            onClick = button.onClick,
            enabled = button.isEnabled,
        ) { Text(button.text, maxLines = 1, softWrap = true) }
    } else {
        TextButton(
            modifier =
                Modifier.pointerHoverIcon(
                    if (button.isEnabled) PointerIcon.Hand else PointerIcon.Default,
                ),
            onClick = button.onClick,
            enabled = button.isEnabled,
        ) { Text(button.text, maxLines = 1, softWrap = true) }
    }

@Composable
internal fun ExpressiveDialog(
    properties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit = {},
    exitOnEsc: Boolean = true,
    specBuilder: DialogSpec.() -> Unit,
) {
    val spec =
        DialogSpec().apply {
            this.properties = properties
            specBuilder()
        }

    val transitionState =
        remember {
            MutableTransitionState(false).apply { targetState = true }
        }

    if (transitionState.currentState || transitionState.targetState) {
        Dialog(
            onDismissRequest = { transitionState.targetState = false },
            properties = spec.properties,
        ) {
            val consumeEscape =
                if (!exitOnEsc) {
                    Modifier.onPreviewKeyEvent { event ->
                        event.type == KeyEventType.KeyUp && event.key == Key.Escape
                    }
                } else {
                    Modifier
                }

            AnimatedVisibility(
                visibleState = transitionState,
                modifier =
                    Modifier
                        .focusable()
                        .then(consumeEscape),
                enter =
                    fadeIn(EnterFadeSpec) +
                        scaleIn(
                            animationSpec = EnterScaleSpec,
                            initialScale = DIALOG_SCALE_IN_INITIAL,
                        ),
                exit =
                    fadeOut(ExitFadeSpec) +
                        scaleOut(
                            animationSpec = ExitScaleSpec,
                            targetScale = DIALOG_SCALE_OUT_TARGET,
                        ),
            ) {
                InternalDialogContent(spec)
            }
        }
    }

    LaunchedEffect(transitionState.currentState, transitionState.targetState) {
        if (!transitionState.currentState && !transitionState.targetState) {
            onDismissRequest()
        }
    }
}

@Composable
private fun InternalDialogContent(spec: DialogSpec) {
    val focusRequester = remember { FocusRequester() }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = SurfaceShadowElevation,
        modifier =
            spec.modifier
                .widthIn(min = DialogMinWidth)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent {
                    if (it.type == KeyEventType.KeyDown &&
                        (it.key == Key.Enter || it.key == Key.NumPadEnter)
                    ) {
                        val enabled = spec.acceptButton?.isEnabled ?: true
                        if (enabled) spec.acceptButton?.onClick?.invoke()
                        return@onPreviewKeyEvent enabled
                    }
                    false
                },
    ) {
        Column(
            modifier = Modifier.padding(DialogPadding),
            verticalArrangement = Arrangement.spacedBy(ContentSpacing),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(HeaderSpacing),
                horizontalAlignment =
                    if (spec.icon == null) {
                        Alignment.Start
                    } else {
                        Alignment.CenterHorizontally
                    },
            ) {
                spec.icon?.let { icon ->
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(Res.string.cd_dialog_icon, spec.title),
                        modifier = Modifier.size(IconSize),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }

                Text(
                    text = spec.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                spec.supportText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                spec.body?.invoke()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonSpacing, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                spec.extraButton?.let { button ->
                    DialogActionButton(button)
                    Spacer(Modifier.weight(SPACER_EXPAND_WEIGHT))
                }

                spec.cancelButton?.let { button ->
                    DialogActionButton(button)
                }

                spec.acceptButton?.let { button ->
                    DialogActionButton(button)
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

@Composable
internal fun DialogBuilder(
    label: String,
    icon: ImageVector? = null,
    body: (@Composable () -> Unit)? = null,
    exitOnESC: Boolean = true,
    dialogProperties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit = {},
    supportingText: String? = null,
    acceptButton: DialogButtonData,
    cancelButton: DialogButtonData =
        DialogButtonData(
            text = t(Res.string.dialog_cancel_button),
            onClick = onDismissRequest,
        ),
    extraButton: DialogButtonData? = null,
) = ExpressiveDialog(
    properties = dialogProperties,
    onDismissRequest = onDismissRequest,
    exitOnEsc = exitOnESC,
) {
    title(label)
    icon(icon)
    supportText(supportingText)
    body(body ?: {})

    accept(DialogButton(acceptButton))
    cancel(DialogButton(cancelButton))
    extraButton?.let { extra(DialogButton(it)) }
}

private const val DIALOG_FADE_DURATION_MS = 300
private const val DIALOG_SCALE_DURATION_MS = 300
private const val DIALOG_SCALE_IN_INITIAL = 0.95f
private const val DIALOG_SCALE_OUT_TARGET = 0.95f
private const val SPACER_EXPAND_WEIGHT = 1f

private val DialogEasing = FastOutSlowInEasing
private val EnterFadeSpec: FiniteAnimationSpec<Float> =
    tween(durationMillis = DIALOG_FADE_DURATION_MS, easing = DialogEasing)
private val ExitFadeSpec: FiniteAnimationSpec<Float> =
    tween(durationMillis = DIALOG_FADE_DURATION_MS, easing = DialogEasing)
private val EnterScaleSpec: FiniteAnimationSpec<Float> =
    tween(durationMillis = DIALOG_SCALE_DURATION_MS, easing = DialogEasing)
private val ExitScaleSpec: FiniteAnimationSpec<Float> =
    tween(durationMillis = DIALOG_SCALE_DURATION_MS, easing = DialogEasing)

private val DialogMinWidth = 280.dp
private val DialogPadding = 24.dp
private val HeaderSpacing = 16.dp
private val ContentSpacing = 24.dp
private val ButtonSpacing = 8.dp
private val IconSize = 24.dp
private val SurfaceShadowElevation = 6.dp
