package com.spoiligaming.explorer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoiligaming.explorer.server.LiveServerEntryList
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

class StartupCoordinatorViewModel {
    var isServerDataLoaded by mutableStateOf(false)
        private set

    var shouldShowFilePickerDialog by mutableStateOf(false)
        private set

    fun verifyServerFile() {
        isServerDataLoaded = (
            ServerFileHandler.initializeServerFileLocation()
                == ServerFileValidationResult.VALID
        )
        shouldShowFilePickerDialog = !isServerDataLoaded
    }

    fun reloadServerFile() =
        CoroutineScope(Dispatchers.IO).launch {
            isServerDataLoaded = false
            verifyServerFile()
        }
}

object StartupCoordinator {
    private val viewModel = StartupCoordinatorViewModel()

    @Composable
    fun Coordinate() {
        val isServerDataLoaded by remember { derivedStateOf { viewModel.isServerDataLoaded } }
        val shouldShowFilePickerDialog by remember {
            derivedStateOf { viewModel.shouldShowFilePickerDialog }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            WindowHeaderView(isServerDataLoaded)

            Box(modifier = Modifier.fillMaxSize()) {
                if (shouldShowFilePickerDialog) {
                    DialogController.showServerFilePickerDialog(false)
                } else {
                    Content(isServerDataLoaded)
                }
            }
        }
    }

    @Composable
    private fun Content(isServerDataLoaded: Boolean) {
        LaunchedEffect(Unit) {
            viewModel.verifyServerFile()
        }

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
        val serverDataLoaded = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            // these variables should not be cached and must trigger recomposition on change, even though they may have some performance overhead
            val names = ServerFileHandler.loadNames()
            val addresses = ServerFileHandler.loadAddresses()

            LiveServerEntryList.initialize(
                mutableStateListOf(*names.toTypedArray()),
                mutableStateListOf(*addresses.toTypedArray()),
            )

            serverDataLoaded.value = true
        }

        if (serverDataLoaded.value) {
            NavigationComponent()
            NavigationController.navigateTo(Screen.Home)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MapleColorPalette.accent)
            }
        }
    }

    fun retryLoad() {
        viewModel.reloadServerFile()
    }
}
