package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.LauncherScreen
import com.example.ui.components.MacosDock
import com.example.ui.screens.ConsoleScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HephaistosTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
            val ecosystemApps by viewModel.ecosystemApps.collectAsStateWithLifecycle()
            val allInstalledApps by viewModel.allInstalledApps.collectAsStateWithLifecycle()
            val manualWhitelist by viewModel.manualPackages.collectAsStateWithLifecycle()
            val terminalEntries by viewModel.terminalEntries.collectAsStateWithLifecycle()

            // Handle back navigation to Home if on a sub-screen
            BackHandler(enabled = currentScreen != LauncherScreen.HOME) {
                viewModel.navigate(LauncherScreen.HOME)
            }

            HephaistosTheme(themeMode = themeMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "launcher_screen_transition"
                    ) { screen ->
                        when (screen) {
                            LauncherScreen.HOME -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    HomeScreen(
                                        ecosystemApps = ecosystemApps,
                                        onLaunchApp = { pkg ->
                                            val launched = viewModel.launchApp(pkg)
                                            if (!launched) {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Impossible de lancer $pkg (aucune activité principale)",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        onNavigate = { dest -> viewModel.navigate(dest) },
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    MacosDock(
                                        currentScreen = currentScreen,
                                        onNavigate = { dest -> viewModel.navigate(dest) },
                                        onRefresh = {
                                            viewModel.refreshApps()
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Applications de l'écosystème actualisées",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }
                            LauncherScreen.CONSOLE -> {
                                ConsoleScreen(
                                    terminalEntries = terminalEntries,
                                    ecosystemApps = ecosystemApps,
                                    onSendCommand = { cmd -> viewModel.sendCliCommand(cmd) },
                                    onClearTerminal = { viewModel.clearTerminal() },
                                    onBack = { viewModel.navigate(LauncherScreen.HOME) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            LauncherScreen.SETTINGS -> {
                                SettingsScreen(
                                    currentTheme = themeMode,
                                    allInstalledApps = allInstalledApps,
                                    manualWhitelist = manualWhitelist,
                                    onThemeChanged = { mode -> viewModel.setThemeMode(mode) },
                                    onToggleManualPackage = { pkg, enabled ->
                                        viewModel.toggleManualPackage(pkg, enabled)
                                    },
                                    onBack = { viewModel.navigate(LauncherScreen.HOME) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
