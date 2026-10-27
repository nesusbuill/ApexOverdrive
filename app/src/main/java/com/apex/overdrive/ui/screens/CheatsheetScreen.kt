package com.apex.overdrive.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.ui.*

data class Cheat(val name: String, val cmd: String, val tag: String)

val CHEATS = listOf(
    Cheat("Get screen size", "wm size", "SYSTEM"),
    Cheat("Get density", "wm density", "SYSTEM"),
    Cheat("Device model", "getprop ro.product.model", "SYSTEM"),
    Cheat("Battery stats", "dumpsys battery", "SYSTEM"),
    Cheat("Kill all bg", "am kill-all", "SYSTEM"),
    Cheat("List user apps", "pm list packages -3", "SYSTEM"),
    Cheat("Current focus", "dumpsys window | grep mCurrentFocus", "SYSTEM"),
    Cheat("Thermal temp", "cat /sys/class/thermal/thermal_zone0/temp", "SYSTEM"),
    Cheat("Anim off", "settings put global window_animation_scale 0", "DISPLAY"),
    Cheat("Refresh 120", "settings put system peak_refresh_rate 120.0", "DISPLAY"),
    Cheat("Refresh 60", "settings put system peak_refresh_rate 60.0", "DISPLAY"),
    Cheat("Touch polling", "settings put secure touch_polling_rate 120", "TOUCH"),
    Cheat("Pointer 7", "settings put system pointer_speed 7", "TOUCH"),
    Cheat("Long press 200", "settings put secure long_press_timeout 200", "TOUCH"),
    Cheat("Multi press 200", "settings put secure multi_press_timeout 200", "TOUCH"),
    Cheat("Haptic off", "settings put system haptic_feedback_enabled 0", "TOUCH"),
    Cheat("Events 300", "settings put secure windowsmgr.max_events_per_sec 300", "TOUCH"),
    Cheat("Flush DNS", "ndc resolver flushdefaultif", "NETWORK"),
    Cheat("Private DNS off", "settings put global private_dns_mode off", "NETWORK"),
    Cheat("Captive off", "settings put global captive_portal_detection_enabled 0", "NETWORK"),
    Cheat("Ping test", "ping -c 10 8.8.8.8", "NETWORK"),
    Cheat("Game mode PERF", "cmd game mode performance com.dts.freefireth", "GAME"),
    Cheat("Game 120fps", "cmd game set-preferred-refresh-rate 120 com.dts.freefireth", "GAME"),
    Cheat("AOT compile", "cmd package compile -m speed-profile -f com.dts.freefireth", "GAME"),
    Cheat("Game driver", "settings put global game_driver_opt_in_apps com.dts.freefireth", "GAME"),
    Cheat("Whitelist FF", "cmd deviceidle whitelist +com.dts.freefireth", "GAME"),
    Cheat("Phantom proc", "device_config put activity_manager max_phantom_processes 1024", "SYSTEM"),
    Cheat("Enable MGLRU", "echo y > /sys/kernel/mm/lru_gen/enabled", "ROOT 🔒"),
    Cheat("CPU perf", "for p in /sys/devices/system/cpu/cpufreq/policy*; do echo performance > $p/scaling_governor; done", "ROOT 🔒"),
    Cheat("Swappiness 40", "echo 40 > /proc/sys/vm/swappiness", "ROOT 🔒"),
    Cheat("IO none", "for d in /sys/block/*; do echo none > $d/queue/scheduler; done", "ROOT 🔒"),
    Cheat("TCP low lat", "echo 1 > /proc/sys/net/ipv4/tcp_low_latency", "ROOT 🔒"),
    Cheat("Dumpsys gfxinfo", "dumpsys gfxinfo com.dts.freefireth framestats", "DEBUG"),
    Cheat("Dumpsys meminfo", "dumpsys meminfo com.dts.freefireth", "DEBUG"),
    Cheat("Dumpsys battery", "dumpsys battery", "DEBUG"),
    Cheat("Logcat AM", "logcat -d -s ActivityManager", "DEBUG"),
    Cheat("Restart SysUI", "pkill -f com.android.systemui", "DEBUG"),
    Cheat("Reboot", "reboot", "DEBUG ⚠")
)

@Composable
fun CheatsheetScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var copied by remember { mutableStateOf<String?>(null) }
    var filter by remember { mutableStateOf("ALL") }
    val tabs = listOf("ALL", "SYSTEM", "DISPLAY", "TOUCH", "NETWORK", "GAME", "ROOT 🔒", "DEBUG")
    val filtered = if (filter == "ALL") CHEATS else CHEATS.filter { it.tag == filter }

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Magenta)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("CHEATSHEET", onBack)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                tabs.forEach { t ->
                    Box(Modifier.padding(2.dp).clip(RoundedCornerShape(6.dp))
                        .background(if (filter == t) C.Magenta.copy(alpha = 0.4f) else C.BgSurface)
                        .clickable { filter = t }
                        .padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(t, color = if (filter == t) C.Magenta else C.TextSecondary,
                            fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            filtered.forEach { c ->
                Box(Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(C.BgSurface)
                    .clickable {
                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("cmd", c.cmd))
                        copied = c.name
                    }
                    .padding(12.dp)) {
                    Column {
                        Row {
                            Text(c.name, color = C.Cyan,
                                fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(Modifier.weight(1f))
                            Text(c.tag, color = when {
                                c.tag.startsWith("ROOT") -> C.Red
                                c.tag == "GAME" -> C.Lime
                                c.tag == "DEBUG" -> C.Orange
                                else -> C.TextSecondary
                            }, fontSize = 9.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(c.cmd, color = C.TextSecondary, fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace)
                    }
                }
            }
            if (copied != null) {
                Spacer(Modifier.height(20.dp))
                Text("✓ Copied: $copied", color = C.Lime, fontSize = 12.sp)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
