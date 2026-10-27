package com.apex.overdrive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.ShizukuManager
import com.apex.overdrive.core.ShellExecutor
import com.apex.overdrive.core.TweakDatabase
import com.apex.overdrive.ui.*

@Composable
fun HomeScreen(
    cpu: Int, ram: Int, temp: Float, fps: Int, fpsHistory: List<Float>,
    szState: ShizukuManager.State, appliedCount: Int, detectedGame: String?,
    sessionTime: String, onNavigate: (String) -> Unit, onShizukuAction: () -> Unit,
    onRestoreAll: () -> Unit
) {
    val hasRoot = ShellExecutor.hasRoot()
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground()
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GamingAvatar(64)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "APEX OVERDRIVE",
                        color = C.TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("v6.0 · Refined",
                            color = C.Cyan, fontSize = 10.sp, letterSpacing = 0.5.sp)
                        if (hasRoot) {
                            Spacer(Modifier.width(6.dp))
                            Box(Modifier.size(4.dp)
                                .background(C.Red, RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(4.dp))
                            Text("ROOT", color = C.Red, fontSize = 10.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniStat("CPU", cpu, if (cpu > 70) C.Red else C.Cyan)
                MiniStat("RAM", ram, if (ram > 80) C.Orange else C.Lime)
                if (temp > 0) MiniStat("°C", temp.toInt(),
                    if (temp > 45) C.Red else C.Gold)
                if (fps > 0) MiniStat("FPS", fps,
                    if (fps >= 60) C.Lime else if (fps >= 30) C.Gold else C.Red)
            }
            Spacer(Modifier.height(12.dp))

            ShizukuBanner(szState, onShizukuAction)

            if (fpsHistory.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                LineChart(fpsHistory, C.Lime, label = "FPS · 60s")
            }

            if (detectedGame != null) {
                Spacer(Modifier.height(12.dp))
                val g = TweakDatabase.gameByName(detectedGame)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(C.Lime.copy(alpha = 0.08f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(8.dp)
                        .background(C.Lime, RoundedCornerShape(4.dp)))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Playing: ${g?.name ?: detectedGame}",
                            color = C.Lime, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (sessionTime.isNotBlank())
                            Text(sessionTime, color = C.TextSecondary, fontSize = 10.sp)
                    }
                }
            }

            if (appliedCount > 0) {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(C.Cyan.copy(alpha = 0.08f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(6.dp)
                        .background(C.Cyan, RoundedCornerShape(3.dp)))
                    Spacer(Modifier.width(8.dp))
                    Text("$appliedCount tweaks active",
                        color = C.Cyan, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(20.dp))

            SectionLabel("GAME BOOST")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridCard("🎮", "GAME BOOST", "${TweakDatabase.GAMES.size} games",
                    C.Lime, { onNavigate("ff") }, Modifier.weight(1f))
                GridCard("🎯", "AIM FIX", "Fix aim drift",
                    C.Orange, { onNavigate("aimfix") }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))

            SectionLabel("DEEP TUNE")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridCard("🛡️", "PRESETS", "4 modes",
                    C.Cyan, { onNavigate("modes") }, Modifier.weight(1f))
                GridCard("⚙️", "SYSTEM", "Global tweaks",
                    C.Purple, { onNavigate("deep") }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))

            SectionLabel("TOOLS")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridCard("📊", "STATS", "Charts + FPS",
                    C.Gold, { onNavigate("stats") }, Modifier.weight(1f))
                GridCard("⚡", "BENCHMARK", "Measure impact",
                    C.Magenta, { onNavigate("benchmark") }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridCard("💻", "TERMINAL", "ADB Shell",
                    C.Orange, { onNavigate("terminal") }, Modifier.weight(1f))
                GridCard("📖", "CHEATSHEET", "40+ commands",
                    C.Cyan, { onNavigate("cheatsheet") }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridCard("🎯", "PROFILES", "Per-game",
                    C.Purple, { onNavigate("profiles") }, Modifier.weight(1f))
                GridCard("📚", "LIBRARY", "Tracked games",
                    C.Lime, { onNavigate("library") }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridCard("📜", "HISTORY", "Sessions",
                    C.Gold, { onNavigate("history") }, Modifier.weight(1f))
                GridCard("🌐", "NETWORK", "Ping / DNS",
                    C.Magenta, { onNavigate("network") }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridCard("🌡️", "TOOLS", "8 utilities",
                    C.Red, { onNavigate("tools") }, Modifier.weight(1f))
                GridCard("⚙️", "SETTINGS", "Config",
                    C.Cyan, { onNavigate("settings") }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridCard("ℹ️", "ABOUT", "Info",
                    C.Purple, { onNavigate("about") }, Modifier.weight(1f))
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))

            NeonButton("⚡ BOOST NOW", C.Magenta,
                { onNavigate("ff") },
                enabled = szState == ShizukuManager.State.Ready)
            Spacer(Modifier.height(12.dp))

            RestoreButton(
                enabled = szState == ShizukuManager.State.Ready,
                hasActive = appliedCount > 0,
                onClick = onRestoreAll
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}
