package com.apex.overdrive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.*
import com.apex.overdrive.ui.*

data class ModeInfo(
    val id: String, val name: String, val emoji: String,
    val desc: String, val accent: Color, val risk: Int, val count: Int
)

@Composable
fun ModesScreen(
    szState: ShizukuManager.State, applying: Boolean, applyMsg: String?,
    progress: Progress, appliedCount: Int,
    onBack: () -> Unit, onApply: (String) -> Unit, onRestoreAll: () -> Unit
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var showDanger by remember { mutableStateOf(false) }
    val hasRoot = ShellExecutor.hasRoot()

    val modes = listOf(
        ModeInfo("balanced", "BALANCED", "🛡️", "Stable · Cool", C.Cyan, 0,
            TweakDatabase.forMode("balanced").size),
        ModeInfo("turbo", "TURBO", "⚡", "High FPS · Warm", C.Magenta, 1,
            TweakDatabase.forMode("turbo").size),
        ModeInfo("beast", "BEAST", "💀", "MAX perf · Hot", C.Red, 3,
            TweakDatabase.forMode("beast", hasRoot).size),
        if (hasRoot) ModeInfo("root_max", "ROOT MAX", "🔒", "Kernel-level · Extreme",
            C.Red, 4, TweakDatabase.forMode("root_max", true).size) else null
    ).filterNotNull()

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Magenta)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("BOOST PRESETS", onBack)
            Spacer(Modifier.height(16.dp))

            if (szState != ShizukuManager.State.Ready) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(C.Red.copy(alpha = 0.2f)).padding(12.dp)) {
                    Text("⚠ SHIZUKU READY REQUIRED", color = C.Red, fontSize = 11.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            if (applying) {
                ProgressBar(progress.current, progress.total, progress.currentName)
                Spacer(Modifier.height(10.dp))
            }
            if (applyMsg != null && !applying) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(C.Lime.copy(alpha = 0.2f)).padding(10.dp)) {
                    Text(applyMsg, color = C.Lime, fontSize = 12.sp)
                }
                Spacer(Modifier.height(12.dp))
            }

            modes.forEach { m ->
                val isSel = selected == m.id
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(
                        colors = if (isSel) listOf(m.accent.copy(alpha = 0.35f), C.BgElevated)
                        else listOf(C.BgSurface, C.BgElevated)))
                    .clickable { selected = m.id }.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(m.emoji, fontSize = 40.sp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(m.name, color = m.accent,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp,
                                letterSpacing = 2.sp)
                            Text(m.desc, color = C.TextSecondary, fontSize = 12.sp)
                            Text("${m.count} tweaks", color = C.TextSecondary, fontSize = 10.sp)
                            if (m.risk > 0)
                                Text("⚠".repeat(m.risk), color = C.Red, fontSize = 12.sp)
                        }
                        if (isSel) Text("●", color = m.accent, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(20.dp))
            NeonButton(
                if (applying) "⚡ APPLYING..." else "▶ ACTIVATE",
                C.Cyan,
                {
                    val mode = selected
                    if (mode != null) {
                        val info = modes.firstOrNull { it.id == mode }
                        if (info != null && info.risk >= 2) showDanger = true
                        else onApply(mode)
                    }
                },
                enabled = selected != null && !applying &&
                    szState == ShizukuManager.State.Ready
            )
            Spacer(Modifier.height(12.dp))

            RestoreButton(
                enabled = szState == ShizukuManager.State.Ready,
                hasActive = appliedCount > 0,
                onClick = onRestoreAll
            )

            Spacer(Modifier.height(60.dp))
        }
    }

    if (showDanger && selected != null) {
        DangerDialog(
            title = "DANGER MODE",
            message = "Mode ${selected!!.uppercase()} will:\n\n" +
                "• Overclock CPU/GPU → device heats up\n" +
                "• Drain battery faster\n" +
                "• If rooted → disable thermal → risk of damage\n\n" +
                "Only use while gaming. Remember to RESTORE after.",
            onConfirm = { showDanger = false; selected?.let(onApply) },
            onDismiss = { showDanger = false }
        )
    }
}
