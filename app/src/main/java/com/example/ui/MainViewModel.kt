package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PackageManagerRepository
import com.example.data.PreferencesRepository
import com.example.ipc.HephaistosIpcManager
import com.example.model.EcosystemApp
import com.example.model.TerminalEntry
import com.example.model.ThemeMode
import com.example.ui.components.LauncherScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val packageRepo = PackageManagerRepository(application)
    private val prefsRepo = PreferencesRepository(application)

    private val _currentScreen = MutableStateFlow(LauncherScreen.HOME)
    val currentScreen: StateFlow<LauncherScreen> = _currentScreen.asStateFlow()

    private val _ecosystemApps = MutableStateFlow<List<EcosystemApp>>(emptyList())
    val ecosystemApps: StateFlow<List<EcosystemApp>> = _ecosystemApps.asStateFlow()

    private val _allInstalledApps = MutableStateFlow<List<EcosystemApp>>(emptyList())
    val allInstalledApps: StateFlow<List<EcosystemApp>> = _allInstalledApps.asStateFlow()

    val manualPackages: StateFlow<Set<String>> = prefsRepo.manualPackages
    val themeMode: StateFlow<ThemeMode> = prefsRepo.themeMode

    val terminalEntries: StateFlow<List<TerminalEntry>> = HephaistosIpcManager.terminalEntries

    init {
        // Observe manual package changes to automatically refresh ecosystem app list
        viewModelScope.launch {
            prefsRepo.manualPackages.collect { manualWhitelist ->
                loadApps(manualWhitelist)
            }
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            loadApps(prefsRepo.manualPackages.value)
        }
    }

    private suspend fun loadApps(manualWhitelist: Set<String>) {
        val eco = packageRepo.getEcosystemApps(manualWhitelist)
        _ecosystemApps.value = eco

        val all = packageRepo.getAllInstalledApps(manualWhitelist)
        _allInstalledApps.value = all
    }

    fun launchApp(packageName: String): Boolean {
        return packageRepo.launchApp(packageName)
    }

    fun navigate(screen: LauncherScreen) {
        _currentScreen.value = screen
    }

    fun setThemeMode(mode: ThemeMode) {
        prefsRepo.setThemeMode(mode)
    }

    fun toggleManualPackage(packageName: String, enabled: Boolean) {
        prefsRepo.toggleManualPackage(packageName, enabled)
    }

    fun sendCliCommand(rawCommand: String) {
        val activePackages = _ecosystemApps.value.map { it.packageName }
        HephaistosIpcManager.executeCliString(getApplication(), rawCommand, activePackages)
    }

    fun clearTerminal() {
        HephaistosIpcManager.clearLog()
    }
}
