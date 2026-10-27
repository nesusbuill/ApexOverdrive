package com.apex.overdrive.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.ShellExecutor
import com.apex.overdrive.ui.*

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val hasRoot = ShellExecutor.hasRoot()
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Purple)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("ABOUT", onBack)
            Spacer(Modifier.height(20.dp))
            Text("APEX OVERDRIVE v6.0",
                color = C.TextPrimary, fontWeight = FontWeight.Bold,
                fontSize = 20.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text("Deep Game Performance Shell", color = C.Cyan, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text(if (hasRoot) "🔒 ROOT DETECTED — Full power unlocked"
                else "🔓 User mode — Shizuku only",
                color = if (hasRoot) C.Red else C.Gold,
                fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))

            Info("SHIZUKU GUIDE", listOf(
                "1. Install Shizuku (Play Store / GitHub)",
                "2. Enable Wireless Debugging",
                "3. Open Shizuku → Pair → Start Service",
                "4. Return to app → tap GRANT",
                "5. Done! All tweaks will work"))

            Spacer(Modifier.height(20.dp))
            Info("TWEAK TIERS", listOf(
                "• 🟢 SAFE: settings, cmd game, cmd package",
                "• 🟡 RISKY: setprop, wm size (backup first)",
                "• 🔴 ROOT: sysfs, /proc, persist (kernel)"))

            Spacer(Modifier.height(20.dp))
            Info("11 HOT GAMES", listOf(
                "• FF, FF MAX, PUBG, PUBG VN, CODM",
                "• CODM VN, MLBB, Arena of Valor, LoL WR",
                "• Genshin Impact, CrossFire"))

            Spacer(Modifier.height(20.dp))
            Info("NOTES", listOf(
                "• ⚠ = risky tweak",
                "• Downscale needs backup (wm size)",
                "• Restore before ranked matches",
                "• FF anti-cheat may flag"))

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Info(title: String, items: List<String>) {
    Text(title, color = C.Magenta, fontWeight = FontWeight.Bold,
        fontSize = 13.sp, letterSpacing = 1.5.sp)
    Spacer(Modifier.height(8.dp))
    items.forEach {
        Text(it, color = C.TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
    }
}
