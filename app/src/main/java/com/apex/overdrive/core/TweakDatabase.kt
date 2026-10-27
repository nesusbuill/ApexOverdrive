package com.apex.overdrive.core

enum class TweakCategory { GAME, DISPLAY, SYSTEM, TOUCH, NETWORK, CLEAN, ROOT }

data class Tweak(
    val id: String, val name: String, val desc: String, val command: String,
    val restore: String? = null,
    val category: TweakCategory = TweakCategory.SYSTEM,
    val risky: Boolean = false, val needsRoot: Boolean = false
)
data class GameProfile(val pkg: String, val name: String, val emoji: String, val accent: Long)

object TweakDatabase {
    val GAMES = listOf(
        GameProfile("com.dts.freefireth", "Free Fire", "🔥", 0xFF00E5FF),
        GameProfile("com.mobile.legends", "Mobile Legends", "⚔️", 0xFFD500F9),
        GameProfile("com.tencent.ig", "PUBG Mobile", "🎯", 0xFFFFB300),
        GameProfile("com.miHoYo.GenshinImpact", "Genshin", "🌟", 0xFF00E5FF)
    )
    fun gameByName(pkg: String) = GAMES.firstOrNull { it.pkg == pkg }

    fun gameBoost(pkg: String): List<Tweak> = listOf(
        Tweak("${pkg}_mode", "GAME MODE PERF", "Native API",
            "cmd game mode performance $pkg", "cmd game mode standard $pkg", TweakCategory.GAME),
        Tweak("${pkg}_120", "LOCK 120 FPS", "Refresh rate",
            "cmd game set-preferred-refresh-rate 120 $pkg", "cmd game set-preferred-refresh-rate 60 $pkg", TweakCategory.GAME),
        Tweak("${pkg}_compile", "AOT COMPILE", "Faster load",
            "cmd package compile -m speed-profile -f $pkg", "cmd package compile -m verify $pkg", TweakCategory.GAME),
        Tweak("${pkg}_wl", "BATTERY WHITELIST", "No kill",
            "cmd deviceidle whitelist +$pkg", "cmd deviceidle whitelist -$pkg", TweakCategory.GAME)
    )

    val SYSTEM_TWEAKS = listOf(
        Tweak("anim_off", "DISABLE ANIMATIONS", "UI Speed",
            "settings put global window_animation_scale 0.0\nsettings put global transition_animation_scale 0.0\nsettings put global animator_duration_scale 0.0",
            "settings put global window_animation_scale 1.0\nsettings put global transition_animation_scale 1.0\nsettings put global animator_duration_scale 1.0", TweakCategory.DISPLAY),
        Tweak("refresh_120", "PEAK 120Hz", "Display",
            "settings put system peak_refresh_rate 120.0", "settings put system peak_refresh_rate 60.0", TweakCategory.DISPLAY),
        Tweak("hwui_vk", "HWUI SKIAVK", "Vulkan render",
            "setprop debug.hwui.renderer skiavk", "setprop debug.hwui.renderer opengl", TweakCategory.DISPLAY),
        Tweak("phantom_max", "MAX PHANTOM", "RAM",
            "device_config put activity_manager max_phantom_processes 2147483647",
            "device_config delete activity_manager max_phantom_processes", TweakCategory.SYSTEM)
    )
    val TOUCH_TWEAKS = listOf(
        Tweak("polling_120", "TOUCH POLLING", "120Hz",
            "settings put secure touch_polling_rate 120", "settings put secure touch_polling_rate 60", TweakCategory.TOUCH),
        Tweak("haptic_off", "DISABLE HAPTIC", "Aim fix",
            "settings put system haptic_feedback_enabled 0", "settings put system haptic_feedback_enabled 1", TweakCategory.TOUCH)
    )
    val NETWORK_TWEAKS = listOf(
        Tweak("dns_flush", "FLUSH DNS", "Clear cache", "ndc resolver flushdefaultif", null, TweakCategory.NETWORK)
    )
    val DOWNSCALE_TWEAKS = listOf(
        Tweak("res_720", "DOWNSCALE 720p", "Reduce GPU load",
            "wm size 720x1280\nwm density 240", "wm size reset\nwm density reset", TweakCategory.DISPLAY, true)
    )
    val ROOT_TWEAKS = listOf(
        Tweak("root_cpu", "CPU MAX", "Root Only",
            "for p in /sys/devices/system/cpu/cpufreq/policy*; do echo performance > \$p/scaling_governor 2>/dev/null; done",
            "for p in /sys/devices/system/cpu/cpufreq/policy*; do echo schedutil > \$p/scaling_governor 2>/dev/null; done",
            TweakCategory.ROOT, true, true)
    )
    val AIM_FIX_TWEAKS = listOf(
        Tweak("aim_pressure", "REDUCE PRESSURE", "Touch",
            "settings put global touch.pressure.scale 0.001", "settings put global touch.pressure.scale 1.0", TweakCategory.TOUCH),
        Tweak("aim_polling_240", "TOUCH 240Hz", "Sampling",
            "settings put secure touch_polling_rate 240", "settings put secure touch_polling_rate 120", TweakCategory.TOUCH, true)
    )
    fun aimFixPreset(): List<Tweak> = TOUCH_TWEAKS + AIM_FIX_TWEAKS
    fun aimFixPresetAggressive(): List<Tweak> = aimFixPreset()

    fun forMode(id: String, withRoot: Boolean = false): List<Tweak> = when (id) {
        "balanced" -> SYSTEM_TWEAKS.take(2) + TOUCH_TWEAKS.take(1)
        "turbo" -> SYSTEM_TWEAKS + TOUCH_TWEAKS + NETWORK_TWEAKS
        "beast" -> SYSTEM_TWEAKS + TOUCH_TWEAKS + NETWORK_TWEAKS + DOWNSCALE_TWEAKS
        "root_max" -> SYSTEM_TWEAKS + TOUCH_TWEAKS + ROOT_TWEAKS
        else -> emptyList()
    }

    val ALL: List<Tweak> by lazy {
        (GAMES.flatMap { gameBoost(it.pkg) } + SYSTEM_TWEAKS + TOUCH_TWEAKS + NETWORK_TWEAKS + DOWNSCALE_TWEAKS + ROOT_TWEAKS + AIM_FIX_TWEAKS).distinctBy { it.id }
    }
    fun byId(id: String) = ALL.firstOrNull { it.id == id }
}
