/*
 * This file is part of Server List Explorer.
 * Copyright (C) 2025-2026 SpoilerRules
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

package com.spoiligaming.explorer.ui.screens.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.serverlist.bookmarks.ServerListFileBookmarksManager
import com.spoiligaming.explorer.settings.manager.UniversalSettingsManager
import com.spoiligaming.explorer.settings.manager.preferenceSettingsManager
import com.spoiligaming.explorer.settings.manager.singleplayerSettingsManager
import com.spoiligaming.explorer.settings.manager.startupSettingsManager
import com.spoiligaming.explorer.settings.model.ComputerStartupBehavior
import com.spoiligaming.explorer.settings.model.StartupSettings
import com.spoiligaming.explorer.settings.util.AppStoragePaths
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalPrefs
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalSingleplayerSettings
import com.spoiligaming.explorer.ui.com.spoiligaming.explorer.ui.LocalStartupSettings
import com.spoiligaming.explorer.ui.screens.setup.steps.LanguageSelectionStep
import com.spoiligaming.explorer.ui.screens.setup.steps.PathStep
import com.spoiligaming.explorer.ui.screens.setup.steps.StartupStep
import com.spoiligaming.explorer.ui.snackbar.SnackbarController
import com.spoiligaming.explorer.ui.snackbar.SnackbarEvent
import com.spoiligaming.explorer.ui.t
import com.spoiligaming.explorer.util.ComputerStartupRegistrationManager
import com.spoiligaming.explorer.util.OSUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import server_list_explorer.ui.generated.resources.Res
import server_list_explorer.ui.generated.resources.button_back
import server_list_explorer.ui.generated.resources.button_finish
import server_list_explorer.ui.generated.resources.button_next
import server_list_explorer.ui.generated.resources.setup_locale_save_failed
import server_list_explorer.ui.generated.resources.setup_wizard_finish_failed
import server_list_explorer.ui.generated.resources.setup_wizard_step_counter
import java.nio.file.Path
import java.util.Locale

internal enum class SetupStep(
    val order: Int,
) {
    LANGUAGE_SELECTION(0),
    STARTUP_CONFIGURATION(1),
    PATH_CONFIGURATION(2),
    ;

    companion object {
        private val orderedSteps = entries.sortedBy { it.order }
        val totalSteps = entries.size
        val firstStep = orderedSteps.first()
        val lastStep = orderedSteps.last()
    }

    fun nextStep() = orderedSteps.getOrNull(orderedSteps.indexOf(this) + 1)

    fun previousStep() = orderedSteps.getOrNull(orderedSteps.indexOf(this) - 1)

    fun isFirst() = this == firstStep

    fun isLast() = this == lastStep

    fun getStepNumber() = order + 1

    fun calculateProgress() = getStepNumber().toFloat() / totalSteps.toFloat()
}

@Stable
internal class SetupUiState(
    initialLocale: Locale,
    initialStartupSettings: StartupSettings,
    initialWorldSavesPath: Path?,
    initialServerFilePath: Path?,
) {
    var currentStep by mutableStateOf(SetupStep.firstStep)
    var isFinishing by mutableStateOf(false)

    var locale by mutableStateOf(initialLocale)
    var startupSettings by mutableStateOf(initialStartupSettings)

    var worldSavesPath by mutableStateOf(initialWorldSavesPath)
    var serverFilePath by mutableStateOf(initialServerFilePath)

    fun navigateToNext(): Boolean {
        val nextStep = currentStep.nextStep()
        return if (nextStep != null) {
            currentStep = nextStep
            true
        } else {
            false
        }
    }

    fun navigateToPrevious(): Boolean {
        val previousStep = currentStep.previousStep()
        return if (previousStep != null) {
            currentStep = previousStep
            true
        } else {
            false
        }
    }

    fun isCurrentStepValid() =
        when (currentStep) {
            SetupStep.PATH_CONFIGURATION -> worldSavesPath != null && serverFilePath != null
            SetupStep.LANGUAGE_SELECTION -> true
            SetupStep.STARTUP_CONFIGURATION -> true
        }
}

@Composable
internal fun SetupWizard(
    onFinished: () -> Unit,
    intOffsetAnimationSpec: FiniteAnimationSpec<IntOffset>,
    floatAnimationSpec: FiniteAnimationSpec<Float>,
) {
    val prefs = LocalPrefs.current
    val sp = LocalSingleplayerSettings.current
    val startupSettings = LocalStartupSettings.current
    val scope = rememberCoroutineScope()
    val activeServerListFilePath by ServerListFileBookmarksManager.activePath.collectAsState()
    val setupWizardFinishFailedMessage = t(Res.string.setup_wizard_finish_failed)
    val setupLocaleSaveFailedMessage = t(Res.string.setup_locale_save_failed)
    val supportsStartupRegistration =
        (OSUtils.isWindows || OSUtils.isDebian) && !OSUtils.isRunningOnBareJvm && !AppStoragePaths.isPortableInstall

    LaunchedEffect(Unit) {
        ServerListFileBookmarksManager.load()
    }

    val state =
        remember {
            SetupUiState(
                initialLocale = prefs.locale,
                initialStartupSettings =
                    resolveInitialSetupStartupSettings(
                        startupSettings = startupSettings,
                        supportsStartupRegistration = supportsStartupRegistration,
                    ),
                initialWorldSavesPath = sp.savesDirectory,
                initialServerFilePath = activeServerListFilePath,
            )
        }

    LaunchedEffect(activeServerListFilePath) {
        if (state.serverFilePath == null && activeServerListFilePath != null) {
            state.serverFilePath = activeServerListFilePath
        }
    }

    suspend fun persistLocaleSetting(locale: Locale) =
        persistSettingsUpdate(
            manager = preferenceSettingsManager,
            context = "locale setting",
            update = { it.copy(locale = locale) },
            isPersisted = { it.locale == locale },
        )

    fun applyLocaleSelection(locale: Locale) {
        if (state.locale == locale) {
            return
        }

        state.locale = locale
        scope.launch {
            runCatching {
                persistLocaleSetting(locale)
            }.onFailure { e ->
                logger.error(e) {
                    "Failed to apply setup wizard locale selection immediately. locale=$locale"
                }
                if (state.locale == locale) {
                    state.locale = preferenceSettingsManager.settingsFlow.value.locale
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = setupLocaleSaveFailedMessage,
                            duration = SnackbarDuration.Short,
                        ),
                    )
                }
            }
        }
    }

    suspend fun persistSetupState() =
        runCatching {
            if (prefs.locale != state.locale) {
                persistLocaleSetting(state.locale)
            }

            if (sp.savesDirectory != state.worldSavesPath) {
                persistSettingsUpdate(
                    manager = singleplayerSettingsManager,
                    context = "singleplayer saves directory setting",
                    update = { it.copy(savesDirectory = state.worldSavesPath) },
                    isPersisted = { it.savesDirectory == state.worldSavesPath },
                )
            }

            if (startupSettings.computerStartupBehavior != state.startupSettings.computerStartupBehavior) {
                ComputerStartupRegistrationManager
                    .applyBehavior(state.startupSettings.computerStartupBehavior)
                    .getOrThrow()
            }
            if (startupSettings != state.startupSettings) {
                persistSettingsUpdate(
                    manager = startupSettingsManager,
                    context = "startup settings",
                    update = { state.startupSettings },
                    isPersisted = { it == state.startupSettings },
                )
            }

            val selectedServerFilePath = state.serverFilePath
            if (selectedServerFilePath != null && selectedServerFilePath != activeServerListFilePath) {
                ServerListFileBookmarksManager.setActivePath(selectedServerFilePath)
            }
        }.onFailure { e ->
            logger.error(e) { "Failed to finalize setup wizard settings." }
            SnackbarController.sendEvent(
                SnackbarEvent(
                    message = setupWizardFinishFailedMessage,
                    duration = SnackbarDuration.Short,
                ),
            )
        }.isSuccess

    fun finalizeSetup() {
        if (state.isFinishing) {
            return
        }
        state.isFinishing = true
        scope.launch {
            try {
                if (persistSetupState()) {
                    onFinished()
                }
            } finally {
                state.isFinishing = false
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(VerticalSpacing),
        ) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = {
                        val isMovingForward = targetState.order > initialState.order

                        if (isMovingForward) {
                            slideInHorizontally(animationSpec = intOffsetAnimationSpec) { it } +
                                fadeIn(animationSpec = floatAnimationSpec) togetherWith
                                slideOutHorizontally(animationSpec = intOffsetAnimationSpec) { -it } +
                                fadeOut(animationSpec = floatAnimationSpec)
                        } else {
                            slideInHorizontally(animationSpec = intOffsetAnimationSpec) { -it } +
                                fadeIn(animationSpec = floatAnimationSpec) togetherWith
                                slideOutHorizontally(animationSpec = intOffsetAnimationSpec) { it } +
                                fadeOut(animationSpec = floatAnimationSpec)
                        }
                    },
                ) { targetStep ->
                    when (targetStep) {
                        SetupStep.LANGUAGE_SELECTION ->
                            LanguageSelectionStep(
                                state = state,
                                onLocaleSelected = ::applyLocaleSelection,
                            )
                        SetupStep.STARTUP_CONFIGURATION -> StartupStep(state = state)
                        SetupStep.PATH_CONFIGURATION -> PathStep(state = state)
                    }
                }
            }

            SetupProgressBar(state, floatAnimationSpec)
            NavigationControls(
                state = state,
                onFinished = ::finalizeSetup,
            )
        }
    }
}

@Composable
private fun SetupProgressBar(
    state: SetupUiState,
    floatAnimationSpec: FiniteAnimationSpec<Float>,
) {
    val progressTarget = state.currentStep.calculateProgress()
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = floatAnimationSpec,
        label = "SetupProgress",
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        trackColor = ProgressIndicatorDefaults.linearTrackColor,
        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
    )
}

@Composable
private fun NavigationControls(
    state: SetupUiState,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) = Box(
    modifier =
        modifier
            .fillMaxWidth()
            .padding(start = ScreenPadding, end = ScreenPadding, bottom = ScreenPadding),
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!state.currentStep.isFirst()) {
            OutlinedButton(
                onClick = { state.navigateToPrevious() },
                modifier = Modifier.pointerHoverIcon(if (!state.isFinishing) PointerIcon.Hand else PointerIcon.Default),
                enabled = !state.isFinishing,
            ) {
                Text(t(Res.string.button_back))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val isCurrentStepValid = state.isCurrentStepValid() && !state.isFinishing

        Button(
            onClick = {
                if (!state.currentStep.isLast()) {
                    state.navigateToNext()
                } else {
                    onFinished()
                }
            },
            modifier =
                Modifier.pointerHoverIcon(
                    if (isCurrentStepValid) PointerIcon.Hand else PointerIcon.Default,
                ),
            enabled = isCurrentStepValid,
        ) {
            Text(
                if (state.currentStep.isLast()) {
                    t(Res.string.button_finish)
                } else {
                    t(Res.string.button_next)
                },
            )
        }
    }
    Text(
        text =
            stringResource(
                Res.string.setup_wizard_step_counter,
                state.currentStep.getStepNumber(),
                SetupStep.totalSteps,
            ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.align(Alignment.Center),
    )
}

private fun resolveInitialSetupStartupSettings(
    startupSettings: StartupSettings,
    supportsStartupRegistration: Boolean,
) = if (
    supportsStartupRegistration &&
    startupSettings.computerStartupBehavior == ComputerStartupBehavior.DoNotStart
) {
    startupSettings.copy(
        computerStartupBehavior = ComputerStartupBehavior.StartMinimizedToSystemTray,
    )
} else {
    startupSettings
}

private suspend fun <T : Any> persistSettingsUpdate(
    manager: UniversalSettingsManager<T>,
    context: String,
    update: (T) -> T,
    isPersisted: (T) -> Boolean,
) {
    manager.updateSettings(update).join()
    check(isPersisted(manager.settingsFlow.value)) {
        "Failed to persist $context."
    }
}

private val ScreenPadding = 16.dp
private val VerticalSpacing = 16.dp

private val logger = KotlinLogging.logger {}
