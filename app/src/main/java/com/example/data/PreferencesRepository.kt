package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hephaistos_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _manualPackages = MutableStateFlow(loadManualPackages())
    val manualPackages: StateFlow<Set<String>> = _manualPackages.asStateFlow()

    private fun loadThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadManualPackages(): Set<String> {
        return prefs.getStringSet(KEY_MANUAL_PACKAGES, emptySet()) ?: emptySet()
    }

    fun toggleManualPackage(packageName: String, enabled: Boolean) {
        val current = _manualPackages.value.toMutableSet()
        if (enabled) {
            current.add(packageName)
        } else {
            current.remove(packageName)
        }
        prefs.edit().putStringSet(KEY_MANUAL_PACKAGES, current).apply()
        _manualPackages.value = current
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_MANUAL_PACKAGES = "manual_packages"
    }
}
