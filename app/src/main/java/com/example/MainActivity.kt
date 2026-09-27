package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.AppBottomNavBar
import com.example.ui.components.FullscreenImageViewerDialog
import com.example.ui.screens.ConfirmationScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DataListScreen
import com.example.ui.screens.DetailDataScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.InputFormScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FilterCategory
import com.example.viewmodel.Screen
import com.example.viewmodel.WarehouseViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WarehouseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: WarehouseViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val fullscreenPhotoUrl by viewModel.fullscreenPhotoUrl.collectAsState()

    val showBottomBar = currentScreen is Screen.Dashboard ||
            currentScreen is Screen.DataList ||
            currentScreen is Screen.Notifications ||
            currentScreen is Screen.Profile

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavBar(
                    currentScreen = currentScreen,
                    onSelectScreen = { screen ->
                        viewModel.navigateTo(screen, clearStack = true)
                    },
                    unreadNotifCount = notifications.size
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is Screen.Splash -> {
                        SplashScreen(
                            onSplashFinished = {
                                viewModel.navigateTo(Screen.Dashboard, clearStack = true)
                            }
                        )
                    }

                    is Screen.Login -> {
                        LoginScreen(
                            onLoginSuccess = { user ->
                                viewModel.setUser(user)
                                viewModel.navigateTo(Screen.Dashboard, clearStack = true)
                            }
                        )
                    }

                    is Screen.Dashboard -> {
                        DashboardScreen(
                            user = currentUser,
                            stats = stats,
                            recentItems = allRecords,
                            isOnline = isOnline,
                            syncState = syncState,
                            onInputClick = { viewModel.startNewInputForm() },
                            onDataListClick = { viewModel.navigateTo(Screen.DataList) },
                            onSearchFilterClick = { viewModel.navigateTo(Screen.DataList) },
                            onExportClick = { viewModel.navigateTo(Screen.Export) },
                            onItemClick = { recordId ->
                                viewModel.navigateTo(Screen.Detail(recordId))
                            },
                            onSyncClick = { viewModel.syncNow() }
                        )
                    }

                    is Screen.DataList -> {
                        DataListScreen(
                            viewModel = viewModel,
                            onItemClick = { recordId ->
                                viewModel.navigateTo(Screen.Detail(recordId))
                            },
                            onInputClick = { viewModel.startNewInputForm() }
                        )
                    }

                    is Screen.InputForm -> {
                        InputFormScreen(
                            viewModel = viewModel,
                            onNavigateBack = { viewModel.navigateBack() },
                            onProceedConfirmation = { confirmedItem ->
                                viewModel.navigateTo(Screen.ConfirmData(confirmedItem))
                            }
                        )
                    }

                    is Screen.ConfirmData -> {
                        ConfirmationScreen(
                            item = screen.item,
                            isOnline = isOnline,
                            viewModel = viewModel,
                            onNavigateBack = { viewModel.navigateBack() }
                        )
                    }

                    is Screen.Detail -> {
                        DetailDataScreen(
                            recordId = screen.recordId,
                            viewModel = viewModel,
                            user = currentUser,
                            onNavigateBack = { viewModel.navigateBack() },
                            onEditClick = { item ->
                                viewModel.startEditForm(item)
                            }
                        )
                    }

                    is Screen.EditForm -> {
                        InputFormScreen(
                            viewModel = viewModel,
                            onNavigateBack = { viewModel.navigateBack() },
                            onProceedConfirmation = { confirmedItem ->
                                viewModel.navigateTo(Screen.ConfirmData(confirmedItem))
                            }
                        )
                    }

                    is Screen.Export -> {
                        ExportScreen(
                            viewModel = viewModel,
                            onNavigateBack = { viewModel.navigateBack() }
                        )
                    }

                    is Screen.Notifications -> {
                        NotificationsScreen(viewModel = viewModel)
                    }

                    is Screen.Profile -> {
                        ProfileScreen(
                            viewModel = viewModel,
                            onLogout = {
                                viewModel.navigateTo(Screen.Login, clearStack = true)
                            }
                        )
                    }
                }
            }

            // Fullscreen Photo Viewer Dialog Overlay
            FullscreenImageViewerDialog(
                imageUrl = fullscreenPhotoUrl,
                onClose = { viewModel.closeFullscreenPhoto() }
            )
        }
    }
}
