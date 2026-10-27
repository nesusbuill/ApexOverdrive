package com.apex.overdrive.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.*
import com.apex.overdrive.ui.*

@Composable
fun DeepTuneScreen(
    szState: ShizukuManager.State, applied: Set<String>,
    busy: Set<String>, appliedCount: Int,
    onBack: () -> Unit, onToggle: (Tweak, Boolean) -> Unit, onRestoreAll: () -> Unit
) {
    val hasRoot = ShellExecutor.hasRoot()
    val tabs = if (hasRoot) listOf("SYSTEM", "TOUCH", "NETWORK", "DOWNSCALE", "ROOT 🔒")
               else listOf("SYSTEM", "TOUCH", "NETWORK", "DOWNSCALE")
    var tab by remember { mutableIntStateOf(0) }
    val tweaks = when (tab) {
        0 -> TweakDatabase.SYSTEM_TWEAKS
        1 -> TweakDatabase.TOUCH_TWEAKS
        2 -> TweakDatabase.NETWORK_TWEAKS
        3 -> TweakDatabase.DOWNSCALE_TWEAKS
        4 -> TweakDatabase.ROOT_TWEAKS
        else -> emptyList()
    }
    val ready = szState == ShizukuManager.State.Ready

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Purple)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("DEEP TUNING", onBack)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tabs.forEachIndexed { i, name ->
                    Box(Modifier.clip(RoundedCornerShape(8.dp))
                        .background(if (tab == i) C.Purple.copy(alpha = 0.4f) else C.BgSurface)
                        .clickable { tab = i }
                        .padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(name, color = if (tab == i) C.Purple else C.TextSecondary,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            tweaks.forEach { t ->
                TweakRow(t, applied.contains(t.id), busy.contains(t.id) || !ready) {
                    on -> onToggle(t, on)
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(20.dp))
            RestoreButton(
                enabled = ready,
                hasActive = appliedCount > 0,
                onClick = onRestoreAll
            )
            Spacer(Modifier.height(30.dp))
        }
    }
}
