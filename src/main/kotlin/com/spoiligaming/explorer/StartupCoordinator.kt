package com.spoiligaming.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.spoiligaming.explorer.server.ContemporaryServerEntryListData
import com.spoiligaming.explorer.server.ServerFileHandler
import com.spoiligaming.explorer.server.ServerFileValidationResult
import com.spoiligaming.explorer.ui.MapleColorPalette
import com.spoiligaming.explorer.ui.components.WindowHeaderView
import com.spoiligaming.explorer.ui.navigation.NavigationComponent
import com.spoiligaming.explorer.ui.navigation.NavigationController
import com.spoiligaming.explorer.ui.navigation.Screen
import com.spoiligaming.explorer.ui.state.DialogController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object StartupCoordinator {
    private var isServerDataLoaded by mutableStateOf(false)
    private var shouldShowFilePickerDialog by mutableStateOf(false)

    @Composable
    fun Coordinate() {
        WindowHeaderView(isServerDataLoaded)
        Box(modifier = Modifier.fillMaxSize()) {
            if (shouldShowFilePickerDialog) {
                DialogController.showServerFilePickerDialog(false)
            } else {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        LaunchedEffect(isServerDataLoaded) { loadServerData() }

        if (isServerDataLoaded) {
            DisplayMainContent()
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MapleColorPalette.accent)
            }
        }
    }

    @Composable
    private fun DisplayMainContent() {
        ContemporaryServerEntryListData.initialize(
            mutableStateListOf(*ServerFileHandler.loadNames().toTypedArray()),
            mutableStateListOf(*ServerFileHandler.loadAddresses().toTypedArray()),
        )
        NavigationComponent()
        NavigationController.navigateTo(Screen.Main)
    }

    private suspend fun loadServerData() =
        withContext(Dispatchers.IO) {
            isServerDataLoaded =
                (
                    ServerFileHandler.initializeServerFileLocation() ==
                        ServerFileValidationResult.VALID
                )

            if (!isServerDataLoaded) {
                shouldShowFilePickerDialog = true
            }
        }

    fun retryLoad() = CoroutineScope(Dispatchers.IO).launch { isServerDataLoaded = false }
}
