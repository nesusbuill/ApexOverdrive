package com.apex.overdrive.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val autoBoost: Boolean = true,
    val autoRestore: Boolean = true,
    val autoDnd: Boolean = true,
    val autoBrightness: Boolean = false,
    val fpsOverlay: Boolean = true,
    val thermalGuard: Boolean = true,
    val safeMode: Boolean = false,
    val defaultMode: String = "turbo",
    val vibrationOnBoost: Boolean = true
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("apex_settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun load(): AppSettings = AppSettings(
        autoBoost = prefs.getBoolean("autoBoost", true),
        autoRestore = prefs.getBoolean("autoRestore", true),
        autoDnd = prefs.getBoolean("autoDnd", true),
        autoBrightness = prefs.getBoolean("autoBrightness", false),
        fpsOverlay = prefs.getBoolean("fpsOverlay", true),
        thermalGuard = prefs.getBoolean("thermalGuard", true),
        safeMode = prefs.getBoolean("safeMode", false),
        defaultMode = prefs.getString("defaultMode", "turbo") ?: "turbo",
        vibrationOnBoost = prefs.getBoolean("vibration", true)
    )

    fun update(block: (AppSettings) -> AppSettings) {
        val n = block(_settings.value)
        _settings.value = n
        prefs.edit()
            .putBoolean("autoBoost", n.autoBoost)
            .putBoolean("autoRestore", n.autoRestore)
            .putBoolean("autoDnd", n.autoDnd)
            .putBoolean("autoBrightness", n.autoBrightness)
            .putBoolean("fpsOverlay", n.fpsOverlay)
            .putBoolean("thermalGuard", n.thermalGuard)
            .putBoolean("safeMode", n.safeMode)
            .putString("defaultMode", n.defaultMode)
            .putBoolean("vibration", n.vibrationOnBoost)
            .apply()
    }
}
