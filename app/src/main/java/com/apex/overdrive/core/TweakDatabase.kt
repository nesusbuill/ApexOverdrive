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
        GameProfile("com.dts.freefireth", "Free Fire", "🔥", 0xFF10B981),
        GameProfile("com.dts.freefiremax", "FF MAX", "🔥", 0xFFEF4444),
        GameProfile("com.tencent.ig", "PUBG Mobile", "🎯", 0xFFF59E0B),
        GameProfile("com.vng.pubgmobile", "PUBG VN", "🎯", 0xFFFBBF24),
        GameProfile("com.activision.callofduty.shooter", "CODM", "💂", 0xFF00D9FF),
        GameProfile("com.garena.game.codm", "CODM VN", "💂", 0xFF00D9FF),
        GameProfile("com.mobile.legends", "MLBB", "⚔️", 0xFFA855F7),
        GameProfile("com.garena.game.kgvn", "Arena of Valor", "⚔️", 0xFFEC4899),
        GameProfile("com.riotgames.league.wildrift", "LoL Wild Rift", "🏆", 0xFFFBBF24),
        GameProfile("com.miHoYo.GenshinImpact", "Genshin Impact", "🌟", 0xFF00D9FF),
        GameProfile("com.tencent.tmgp.cf", "CrossFire", "🎯", 0xFFEF4444)
    )
    fun gameByName(pkg: String) = GAMES.firstOrNull { it.pkg == pkg }

    fun gameBoost(pkg: String): List<Tweak> = listOf(
        Tweak("${pkg}_mode", "GAME MODE PERFORMANCE", "Android 12+ Game Mode API",
            NativeBridge.cmd(NativeBridge.Id.GAME_MODE, "$pkg"),
            "cmd game mode standard $pkg", TweakCategory.GAME),
        Tweak("${pkg}_120", "LOCK 120 FPS", "Preferred refresh rate 120Hz",
            NativeBridge.cmd(NativeBridge.Id.REFRESH, "$pkg"),
            "cmd game set-preferred-refresh-rate 60 $pkg", TweakCategory.GAME),
        Tweak("${pkg}_compile", "AOT COMPILE", "Pre-compile game for faster load",
            NativeBridge.cmd(NativeBridge.Id.COMPILE, "$pkg"),
            "cmd package compile -m verify $pkg", TweakCategory.GAME),
        Tweak("${pkg}_driver", "GAME DRIVER", "Opt-in to optimized Game Driver",
            NativeBridge.cmd(NativeBridge.Id.GAME_DRIVER, "$pkg"),
            "settings delete global game_driver_opt_in_apps", TweakCategory.GAME),
        Tweak("${pkg}_wl", "BATTERY WHITELIST", "Exclude from battery optimization",
            "cmd deviceidle whitelist +$pkg", "cmd deviceidle whitelist -$pkg", TweakCategory.GAME),
        Tweak("${pkg}_keep", "KEEP IN RAM", "Prevent kill",
            "am set-inactive $pkg false", null, TweakCategory.GAME)
    )

    val SYSTEM_TWEAKS = listOf(
        Tweak("phantom_max", "MAX PHANTOM PROCESSES", "Set max virtual process limit",
            NativeBridge.cmd(NativeBridge.Id.PHANTOM, "2147483647"),
            "device_config delete activity_manager max_phantom_processes", TweakCategory.SYSTEM),
        Tweak("cached_max", "MAX CACHED PROCESSES", "Cache up to 2048",
            "device_config put activity_manager max_cached_processes 2048",
            "device_config delete activity_manager max_cached_processes", TweakCategory.SYSTEM),
        Tweak("anim_off", "DISABLE ANIMATIONS", "Animation scales to 0",
            "settings put global window_animation_scale 0.0\nsettings put global transition_animation_scale 0.0\nsettings put global animator_duration_scale 0.0",
            "settings put global window_animation_scale 1.0\nsettings put global transition_animation_scale 1.0\nsettings put global animator_duration_scale 1.0",
            TweakCategory.DISPLAY),
        Tweak("refresh_120", "PEAK 120Hz", "Peak refresh 120",
            "settings put system peak_refresh_rate 120.0",
            "settings put system peak_refresh_rate 60.0", TweakCategory.DISPLAY),
        Tweak("min_refresh_90", "MIN 90Hz", "Min refresh 90",
            "settings put system min_refresh_rate 90.0",
            "settings delete system min_refresh_rate", TweakCategory.DISPLAY),
        Tweak("auto_temp", "AUTO TEMP CONTROL", "Auto thermal (A12+)",
            "settings put secure game_auto_temp_control_enabled 1",
            "settings put secure game_auto_temp_control_enabled 0", TweakCategory.SYSTEM),
        Tweak("hwui_vk", "HWUI SKIAVK", "Vulkan renderer",
            "setprop debug.hwui.renderer skiavk",
            "setprop debug.hwui.renderer opengl", TweakCategory.DISPLAY),
        Tweak("sf_latch", "SF LATCH UNSIGNALED", "SurfaceFlinger latch",
            "setprop debug.sf.latch_unsignaled 1",
            "setprop debug.sf.latch_unsignaled 0", TweakCategory.DISPLAY),
        Tweak("miui_pk", "BLOCK POWERKEEPER", "Block MIUI killer",
            NativeBridge.cmd(NativeBridge.Id.MIUI_PK, "RUN_IN_BACKGROUND deny\nappops set com.miui.powerkeeper GET_USAGE_STATS deny"),
            "appops set com.miui.powerkeeper RUN_IN_BACKGROUND allow\nappops set com.miui.powerkeeper GET_USAGE_STATS allow",
            TweakCategory.SYSTEM),
        Tweak("athena", "BLOCK ATHENA", "Block ColorOS killer",
            NativeBridge.cmd(NativeBridge.Id.ATHENA, "RUN_IN_BACKGROUND deny"),
            "appops set com.coloros.athena RUN_IN_BACKGROUND allow", TweakCategory.SYSTEM)
    )

    val TOUCH_TWEAKS = listOf(
        Tweak("polling_120", "TOUCH POLLING 120Hz", "Sampling 120Hz",
            "settings put secure touch_polling_rate 120",
            "settings put secure touch_polling_rate 60", TweakCategory.TOUCH),
        Tweak("pointer_7", "POINTER SPEED 7", "Increase pointer speed",
            "settings put system pointer_speed 7",
            "settings put system pointer_speed 0", TweakCategory.TOUCH),
        Tweak("long_200", "LONG PRESS 200ms", "Faster long-press",
            "settings put secure long_press_timeout 200",
            "settings put secure long_press_timeout 500", TweakCategory.TOUCH),
        Tweak("multi_200", "MULTI PRESS 200ms", "Faster multi-touch",
            "settings put secure multi_press_timeout 200",
            "settings put secure multi_press_timeout 300", TweakCategory.TOUCH),
        Tweak("haptic_off", "DISABLE HAPTIC", "Fix aim shake",
            NativeBridge.cmd(NativeBridge.Id.HAPTIC, "0"),
            "settings put system haptic_feedback_enabled 1", TweakCategory.TOUCH),
        Tweak("events_300", "MAX EVENTS 300/s", "Events per second",
            "settings put secure windowsmgr.max_events_per_sec 300",
            "settings put secure windowsmgr.max_events_per_sec 120", TweakCategory.TOUCH)
    )

    val NETWORK_TWEAKS = listOf(
        Tweak("dns_flush", "FLUSH DNS", "Clear DNS cache",
            "ndc resolver flushdefaultif", null, TweakCategory.NETWORK),
        Tweak("captive_off", "DISABLE CAPTIVE PORTAL", "Skip captive check",
            "settings put global captive_portal_detection_enabled 0",
            "settings put global captive_portal_detection_enabled 1", TweakCategory.NETWORK),
        Tweak("dns_off", "DISABLE PRIVATE DNS", "Disable DoT",
            "settings put global private_dns_mode off",
            "settings put global private_dns_mode opportunistic", TweakCategory.NETWORK)
    )

    val DOWNSCALE_TWEAKS = listOf(
        Tweak("res_720", "DOWNSCALE 720p", "Reduce res for GPU",
            "wm size 720x1280\nwm density 240",
            "wm size reset\nwm density reset", TweakCategory.DISPLAY, true),
        Tweak("res_540", "DOWNSCALE 540p", "Aggressive reduction",
            "wm size 540x960\nwm density 160",
            "wm size reset\nwm density reset", TweakCategory.DISPLAY, true)
    )

    val ROOT_TWEAKS = listOf(
        Tweak("root_cpu", "CPU PERFORMANCE", "Max CPU freq",
            "for p in /sys/devices/system/cpu/cpufreq/policy*; do echo performance > \$p/scaling_governor 2>/dev/null; done",
            "for p in /sys/devices/system/cpu/cpufreq/policy*; do echo schedutil > \$p/scaling_governor 2>/dev/null; done",
            TweakCategory.ROOT, true, true),
        Tweak("root_gpu", "GPU PERFORMANCE", "Max GPU freq",
            "echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null",
            "echo msm-adreno-tz > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null",
            TweakCategory.ROOT, true, true),
        Tweak("root_mglru", "ENABLE MGLRU", "Multi-gen LRU",
            "echo y > /sys/kernel/mm/lru_gen/enabled",
            "echo n > /sys/kernel/mm/lru_gen/enabled", TweakCategory.ROOT, false, true),
        Tweak("root_swappiness", "SWAPPINESS 40", "Reduce swap",
            "echo 40 > /proc/sys/vm/swappiness",
            "echo 60 > /proc/sys/vm/swappiness", TweakCategory.ROOT, false, true),
        Tweak("root_io", "I/O NONE", "Skip IO scheduler",
            "for d in /sys/block/sd* /sys/block/mmcblk*; do echo none > \$d/queue/scheduler 2>/dev/null; done",
            "for d in /sys/block/mmcblk*; do echo mq-deadline > \$d/queue/scheduler 2>/dev/null; done",
            TweakCategory.ROOT, false, true),
        Tweak("root_tcp", "TCP LOW LATENCY", "Optimize TCP",
            "echo 0 > /proc/sys/net/ipv4/tcp_slow_start_after_idle\necho 1 > /proc/sys/net/ipv4/tcp_tw_reuse\necho 1 > /proc/sys/net/ipv4/tcp_low_latency",
            "echo 1 > /proc/sys/net/ipv4/tcp_slow_start_after_idle\necho 0 > /proc/sys/net/ipv4/tcp_tw_reuse\necho 0 > /proc/sys/net/ipv4/tcp_low_latency",
            TweakCategory.ROOT, false, true),
        Tweak("root_thermal", "DISABLE THERMAL", "Disable throttle",
            "setprop persist.sys.thermal.throttle 0",
            "setprop persist.sys.thermal.throttle 1", TweakCategory.ROOT, true, true)
    )

    val AIM_FIX_TWEAKS = listOf(
        Tweak("aim_haptic_off", "DISABLE HAPTIC", "Fix aim shake",
            NativeBridge.cmd(NativeBridge.Id.HAPTIC, "0"),
            "settings put system haptic_feedback_enabled 1", TweakCategory.TOUCH),
        Tweak("aim_pressure", "REDUCE TOUCH PRESSURE", "Pressure 0.001",
            "settings put global touch.pressure.scale 0.001",
            "settings put global touch.pressure.scale 1.0", TweakCategory.TOUCH),
        Tweak("aim_events_300", "MAX EVENTS 300/s", "Events per second",
            "settings put secure windowsmgr.max_events_per_sec 300",
            "settings put secure windowsmgr.max_events_per_sec 120", TweakCategory.TOUCH),
        Tweak("aim_long_press", "LONG PRESS 200ms", "Faster long-press",
            "settings put secure long_press_timeout 200",
            "settings put secure long_press_timeout 500", TweakCategory.TOUCH),
        Tweak("aim_multi_press", "MULTI PRESS 200ms", "Faster multi-touch",
            "settings put secure multi_press_timeout 200",
            "settings put secure multi_press_timeout 300", TweakCategory.TOUCH),
        Tweak("aim_pointer_slow", "POINTER SPEED 0", "Prevent over-aim",
            "settings put system pointer_speed 0",
            "settings put system pointer_speed 7", TweakCategory.TOUCH),
        Tweak("aim_gesture_low", "GESTURE SENSITIVITY 5", "Reduce shake",
            "settings put system touch.gesture_sensitivity 5",
            "settings put system touch.gesture_sensitivity 10", TweakCategory.TOUCH),
        Tweak("aim_polling_240", "TOUCH POLLING 240Hz", "Sampling 240Hz",
            "settings put secure touch_polling_rate 240",
            "settings put secure touch_polling_rate 120", TweakCategory.TOUCH, true),
        Tweak("aim_polling_120", "TOUCH POLLING 120Hz", "Sampling 120Hz",
            "settings put secure touch_polling_rate 120",
            "settings put secure touch_polling_rate 60", TweakCategory.TOUCH),
        Tweak("aim_ff_compile", "PRE-COMPILE FF", "AOT FF",
            NativeBridge.cmd(NativeBridge.Id.COMPILE, "com.dts.freefireth"),
            "cmd package compile -m verify com.dts.freefireth", TweakCategory.GAME),
        Tweak("aim_ff_mode", "GAME MODE FF", "Game Mode FF",
            NativeBridge.cmd(NativeBridge.Id.GAME_MODE, "com.dts.freefireth"),
            "cmd game mode standard com.dts.freefireth", TweakCategory.GAME)
    )

    fun aimFixPreset(): List<Tweak> = listOf(
        AIM_FIX_TWEAKS[0], AIM_FIX_TWEAKS[1], AIM_FIX_TWEAKS[2],
        AIM_FIX_TWEAKS[3], AIM_FIX_TWEAKS[4], AIM_FIX_TWEAKS[8],
        AIM_FIX_TWEAKS[9], AIM_FIX_TWEAKS[10])

    fun aimFixPresetAggressive(): List<Tweak> = aimFixPreset() + listOf(
        AIM_FIX_TWEAKS[5], AIM_FIX_TWEAKS[6], AIM_FIX_TWEAKS[7])

    fun forMode(id: String, withRoot: Boolean = false): List<Tweak> = when (id) {
        "balanced" -> SYSTEM_TWEAKS.take(3) + TOUCH_TWEAKS.take(2)
        "turbo" -> SYSTEM_TWEAKS + TOUCH_TWEAKS + NETWORK_TWEAKS
        "beast" -> SYSTEM_TWEAKS + TOUCH_TWEAKS + NETWORK_TWEAKS +
            DOWNSCALE_TWEAKS.take(1) +
            (if (withRoot) ROOT_TWEAKS.take(4) else emptyList())
        "root_max" -> SYSTEM_TWEAKS + TOUCH_TWEAKS + ROOT_TWEAKS
        else -> emptyList()
    }

    val ALL: List<Tweak> by lazy {
        (GAMES.flatMap { gameBoost(it.pkg) } + SYSTEM_TWEAKS + TOUCH_TWEAKS +
            NETWORK_TWEAKS + DOWNSCALE_TWEAKS + ROOT_TWEAKS + AIM_FIX_TWEAKS)
            .distinctBy { it.id }
    }
    fun byId(id: String) = ALL.firstOrNull { it.id == id }
}
