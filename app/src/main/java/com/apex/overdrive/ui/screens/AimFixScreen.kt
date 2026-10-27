package com.apex.overdrive.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.*
import com.apex.overdrive.ui.*

@Composable
fun AimFixScreen(
    szState: ShizukuManager.State,
    applied: Set<String>, busy: Set<String>,
    applying: Boolean, applyMsg: String?, progress: Progress,
    appliedCount: Int,
    onBack: () -> Unit,
    onToggle: (Tweak, Boolean) -> Unit,
    onApplyPreset: (List<Tweak>) -> Unit,
    onRestoreAll: () -> Unit
) {
    val ready = szState == ShizukuManager.State.Ready
    var showAggressive by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Orange)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("AIM FIX · FF", onBack)
            Spacer(Modifier.height(12.dp))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(C.Orange.copy(alpha = 0.15f))
                .border(1.dp, C.Orange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(12.dp)) {
                Column {
                    Text("🎯 FIX AIM DRIFT / SHAKE",
                        color = C.Orange, fontWeight = FontWeight.Bold,
                        fontSize = 13.sp, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Run NORMAL preset first — safe. If still shaky, try AGGRESSIVE.\n" +
                        "After applying: test in FF Training, adjust in-game sensitivity.",
                        color = C.TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
                }
            }
            Spacer(Modifier.height(16.dp))

            if (applying) {
                ProgressBar(progress.current, progress.total, progress.currentName)
                Spacer(Modifier.height(10.dp))
            }
            if (applyMsg != null && !applying) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(C.Lime.copy(alpha = 0.2f)).padding(10.dp)) {
                    Text(applyMsg, color = C.Lime, fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
            }

            PresetButton(
                title = "🟢 PRESET NORMAL (recommended)",
                subtitle = "8 tweaks · safe · no conflict",
                color = C.Lime,
                enabled = ready && !applying,
                onClick = { onApplyPreset(TweakDatabase.aimFixPreset()) }
            )
            Spacer(Modifier.height(10.dp))

            if (!showAggressive) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(C.BgSurface)
                    .clickable { showAggressive = true }
                    .padding(12.dp)) {
                    Text("⚠ EXPAND — AGGRESSIVE PRESET",
                        color = C.Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                PresetButton(
                    title = "🔴 PRESET AGGRESSIVE (experimental)",
                    subtitle = "12 tweaks · may cause strange hand lag",
                    color = C.Red,
                    enabled = ready && !applying,
                    onClick = { onApplyPreset(TweakDatabase.aimFixPresetAggressive()) }
                )
            }
            Spacer(Modifier.height(20.dp))

            SectionLabel("DETAILED TWEAKS")
            TweakDatabase.AIM_FIX_TWEAKS.forEach { t ->
                TweakRow(
                    tweak = t,
                    isOn = applied.contains(t.id),
                    isBusy = busy.contains(t.id) || !ready || applying
                ) { on -> onToggle(t, on) }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(24.dp))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(C.BgSurface).padding(14.dp)) {
                Column {
                    Text("📋 IN-GAME SENSITIVITY SETTINGS",
                        color = C.Cyan, fontWeight = FontWeight.Bold,
                        fontSize = 12.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "• General: 90-95% (no need 100%)",
                        "• Red Dot: 90%",
                        "• 2x Scope: 92%",
                        "• 4x Scope: 95%",
                        "• AWM: 45-50%",
                        "• Fire button: 45-52%",
                        "• Disable vibration in FF Settings",
                        "• System DPI: 500-520 (not higher)"
                    ).forEach {
                        Text(it, color = C.TextSecondary, fontSize = 11.sp)
                        Spacer(Modifier.height(3.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            RestoreButton(
                enabled = ready,
                hasActive = appliedCount > 0,
                onClick = onRestoreAll
            )
            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun PresetButton(
    title: String, subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    enabled: Boolean, onClick: () -> Unit
) {
    val inf = rememberInfiniteTransition(label = "preset")
    val glow by inf.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = FastOutSlowInEasing),
            RepeatMode.Reverse),
        label = "g")
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
        .background(Brush.horizontalGradient(listOf(
            color.copy(alpha = if (enabled) 0.35f else 0.15f), C.BgElevated)))
        .border(2.dp, color.copy(alpha = if (enabled) glow else 0.3f),
            RoundedCornerShape(14.dp))
        .clickable(enabled = enabled, onClick = onClick)
        .padding(14.dp)) {
        Column {
            Text(title, color = color, fontWeight = FontWeight.Bold,
                fontSize = 14.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = C.TextSecondary, fontSize = 11.sp)
        }
    }
}
