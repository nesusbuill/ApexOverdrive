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
        GameProfile("com.garena.game.kgvn", "Liên Quân Mobile", "⚔️", 0xFF00E5FF),
        GameProfile("com.roblox.client", "Roblox", "🟥", 0xFFFF1E1E),
        GameProfile("com.dts.freefireth", "Free Fire", "🔥", 0xFFFF1E1E),
        GameProfile("com.tencent.ig", "PUBG Mobile", "🎯", 0xFFF59E0B),
        GameProfile("com.miHoYo.GenshinImpact", "Genshin Impact", "🌟", 0xFFD500F9)
    )
    fun gameByName(pkg: String) = GAMES.firstOrNull { it.pkg == pkg }

    fun gameBoost(pkg: String): List<Tweak> {
        // Sử dụng lệnh an toàn của Android 12+ (Không dùng wm size)
        val downscaleTweak = Tweak(
            "${pkg}_downscale", "GIẢM ĐỒ HỌA TRONG GAME", "Hạ độ phân giải render của game",
            "cmd game set-downscale-factor $pkg 0.75",
            "cmd game set-downscale-factor $pkg 1.0",
            TweakCategory.GAME
        )

        return listOf(
            Tweak("${pkg}_mode", "GAME MODE PERF", "Ép xung hiệu năng cao",
                "cmd game mode performance $pkg", "cmd game mode standard $pkg", TweakCategory.GAME),
            Tweak("${pkg}_120", "LOCK 120 FPS", "Khóa tần số quét 120Hz",
                "cmd game set-preferred-refresh-rate 120 $pkg", "cmd game set-preferred-refresh-rate 60 $pkg", TweakCategory.GAME),
            Tweak("${pkg}_compile", "AOT COMPILE", "Biên dịch sẵn game",
                "cmd package compile -m speed-profile -f $pkg", "cmd package compile -m verify $pkg", TweakCategory.GAME),
            Tweak("${pkg}_wl", "BATTERY WHITELIST", "Loại khỏi tối ưu pin",
                "cmd deviceidle whitelist +$pkg", "cmd deviceidle whitelist -$pkg", TweakCategory.GAME),
            downscaleTweak
        )
    }

    val SYSTEM_TWEAKS = listOf(
        Tweak("anim_off", "DISABLE ANIMATIONS", "Animation scales to 0",
            "settings put global window_animation_scale 0.0\nsettings put global transition_animation_scale 0.0\nsettings put global animator_duration_scale 0.0",
            "settings put global window_animation_scale 1.0\nsettings put global transition_animation_scale 1.0\nsettings put global animator_duration_scale 1.0", TweakCategory.DISPLAY),
        Tweak("refresh_120", "PEAK 120Hz", "Peak refresh 120",
            "settings put system peak_refresh_rate 120.0", "settings put system peak_refresh_rate 60.0", TweakCategory.DISPLAY),
        Tweak("phantom_max", "MAX PHANTOM", "RAM",
            "device_config put activity_manager max_phantom_processes 2147483647", "device_config delete activity_manager max_phantom_processes", TweakCategory.SYSTEM)
    )

    fun forMode(id: String, withRoot: Boolean = false): List<Tweak> = when (id) {
        "balanced" -> SYSTEM_TWEAKS.take(2)
        "turbo" -> SYSTEM_TWEAKS
        else -> emptyList()
    }
}
