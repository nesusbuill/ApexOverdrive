package com.apex.overdrive.ui.screens

import androidx.compose.foundation.background
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
import com.apex.overdrive.MainActivity
import com.apex.overdrive.core.*
import com.apex.overdrive.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BenchmarkScreen(
    szState: ShizukuManager.State,
    detectedGame: String?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var baseline by remember { mutableStateOf<Pair<Int, Float>?>(null) }
    var boosted by remember { mutableStateOf<Pair<Int, Float>?>(null) }
    var msg by remember { mutableStateOf("") }

    suspend fun measureFps(seconds: Int, onTick: (Int) -> Unit): Pair<Int, Float> {
        val samples = mutableListOf<Int>()
        val janks = mutableListOf<Float>()
        repeat(seconds) { s ->
            if (detectedGame != null) {
                val (f, j) = FpsReader.snapshot(detectedGame)
                if (f > 0) samples.add(f)
                if (j >= 0) janks.add(j)
            }
            onTick(s + 1)
            delay(1000)
        }
        return (samples.average().takeIf { !it.isNaN() }?.toInt() ?: 0) to
            (janks.average().takeIf { !it.isNaN() }?.toFloat() ?: 0f)
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Gold)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("BENCHMARK", onBack)
            Spacer(Modifier.height(16.dp))

            if (detectedGame == null) {
                Text("⚠ Launch game before testing", color = C.Red, fontSize = 12.sp)
                Spacer(Modifier.height(30.dp))
                return@Column
            }

            val gameName = TweakDatabase.gameByName(detectedGame)?.name ?: detectedGame
            Text("Game: $gameName", color = C.Lime,
                fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))

            Text("// STEP 1 — BASELINE (30s)",
                color = C.TextSecondary, fontSize = 11.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            NeonButton(
                if (running) "⏱ ${progress}/30s" else "▶ MEASURE BASELINE",
                C.Cyan,
                {
                    scope.launch {
                        running = true; progress = 0; msg = ""
                        baseline = measureFps(30) { progress = it }
                        running = false
                        msg = "✓ Baseline: ${baseline?.first} FPS"
                    }
                },
                enabled = !running && szState == ShizukuManager.State.Ready)

            if (baseline != null) {
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(C.BgElevated).padding(14.dp)) {
                    Column {
                        Text("BASELINE", color = C.Cyan, fontSize = 11.sp)
                        Text("${baseline!!.first} FPS · ${baseline!!.second.toInt()}% jank",
                            color = C.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("// STEP 2 — BOOST + RE-MEASURE",
                color = C.TextSecondary, fontSize = 11.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            NeonButton(
                if (running) "⏱ ${progress}/30s" else "⚡ BOOST + MEASURE 30s",
                C.Magenta,
                {
                    scope.launch {
                        running = true; progress = 0
                        val ctx = MainActivity.globalCtx
                        if (ctx != null) {
                            TweakEngine(ctx).boostGame(detectedGame)
                        }
                        boosted = measureFps(30) { progress = it }
                        running = false
                        msg = "✓ Boosted: ${boosted?.first} FPS"
                    }
                },
                enabled = !running && szState == ShizukuManager.State.Ready && baseline != null)

            if (boosted != null && baseline != null) {
                Spacer(Modifier.height(12.dp))
                val diff = boosted!!.first - baseline!!.first
                val pct = if (baseline!!.first > 0) (diff * 100 / baseline!!.first) else 0
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(if (diff > 0) C.Lime.copy(alpha = 0.2f)
                        else C.Red.copy(alpha = 0.2f))
                    .padding(14.dp)) {
                    Column {
                        Text("RESULT", color = C.Gold, fontSize = 11.sp)
                        Text(if (diff >= 0) "+$diff FPS (+$pct%)" else "$diff FPS ($pct%)",
                            color = if (diff >= 0) C.Lime else C.Red,
                            fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Baseline: ${baseline!!.first} → Boosted: ${boosted!!.first}",
                            color = C.TextPrimary, fontSize = 12.sp)
                    }
                }
            }

            if (msg.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(msg, color = C.Lime, fontSize = 11.sp)
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}
