package com.apex.overdrive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.Progress
import com.apex.overdrive.core.ShizukuManager
import com.apex.overdrive.core.TweakDatabase
import com.apex.overdrive.ui.*

@Composable
fun FFBoostScreen(
    szState: ShizukuManager.State,
    applied: Set<String>,
    busy: Boolean,
    applyMsg: String?,
    progress: Progress,
    onBack: () -> Unit,
    onBoostGame: (String) -> Unit,
    onUnboostGame: (String) -> Unit
) {
    val ready = szState == ShizukuManager.State.Ready && !busy
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Lime)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("GAME BOOST", onBack)
            Spacer(Modifier.height(16.dp))
            Text("// ${TweakDatabase.GAMES.size} HOT GAMES",
                color = C.TextSecondary, fontSize = 11.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(10.dp))

            if (busy) {
                ProgressBar(progress.current, progress.total, progress.currentName)
                Spacer(Modifier.height(10.dp))
            }
            if (applyMsg != null && !busy) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(C.Lime.copy(alpha = 0.2f)).padding(10.dp)) {
                    Text(applyMsg, color = C.Lime, fontSize = 12.sp)
                }
                Spacer(Modifier.height(10.dp))
            }

            TweakDatabase.GAMES.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { g ->
                        val accent = C.fromLong(g.accent)
                        val isBoosted = applied.any { it.startsWith("${g.pkg}_") }
                        Box(Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(
                                accent.copy(alpha = if (isBoosted) 0.35f else 0.10f),
                                C.BgElevated, C.BgSurface)))
                            .border(if (isBoosted) 2.dp else 1.dp,
                                if (isBoosted) accent else C.Border, RoundedCornerShape(16.dp))
                            .clickable(enabled = ready) {
                                if (isBoosted) onUnboostGame(g.pkg) else onBoostGame(g.pkg)
                            }
                            .padding(14.dp)) {
                            Column(Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(g.emoji, fontSize = 28.sp)
                                    Spacer(Modifier.weight(1f))
                                    if (isBoosted) Box(Modifier.size(8.dp)
                                        .background(C.Lime, RoundedCornerShape(4.dp)))
                                }
                                Column {
                                    Text(g.name, color = accent,
                                        fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(if (isBoosted) "✓ BOOSTED" else "TAP TO BOOST",
                                        color = if (isBoosted) C.Lime else C.TextSecondary,
                                        fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(20.dp))
            Text("// Android 12+: Game Mode API", color = C.TextSecondary, fontSize = 10.sp)
            Text("// Android 8-11: AOT compile fallback", color = C.TextSecondary, fontSize = 10.sp)
            Spacer(Modifier.height(60.dp))
        }
    }
}
