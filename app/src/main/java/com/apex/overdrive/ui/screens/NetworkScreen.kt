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
import com.apex.overdrive.core.*
import com.apex.overdrive.ui.*
import kotlinx.coroutines.launch

@Composable
fun NetworkScreen(szState: ShizukuManager.State, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    var pingResult by remember { mutableStateOf<PingTester.Result?>(null) }
    var dnsResults by remember { mutableStateOf<List<DnsBenchmark.DnsResult>>(emptyList()) }
    val ready = szState == ShizukuManager.State.Ready

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Magenta)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("NETWORK", onBack)
            Spacer(Modifier.height(20.dp))

            Text("// PING TEST", color = C.TextSecondary, fontSize = 11.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonButton("🌐 8.8.8.8", C.Cyan, {
                    if (ready) scope.launch {
                        running = true
                        pingResult = PingTester.test("8.8.8.8")
                        running = false
                    }
                }, enabled = ready && !running, modifier = Modifier.weight(1f))
                NeonButton("⚡ 1.1.1.1", C.Lime, {
                    if (ready) scope.launch {
                        running = true
                        pingResult = PingTester.test("1.1.1.1")
                        running = false
                    }
                }, enabled = ready && !running, modifier = Modifier.weight(1f))
            }
            if (pingResult != null && !running) {
                Spacer(Modifier.height(8.dp))
                val r = pingResult!!
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(if (r.avgMs < 50) C.Lime.copy(alpha = 0.2f)
                        else if (r.avgMs < 150) C.Gold.copy(alpha = 0.2f)
                        else C.Red.copy(alpha = 0.2f))
                    .padding(12.dp)) {
                    Column {
                        Text("${r.host}", color = C.TextPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Avg: ${r.avgMs}ms · Min: ${r.minMs} · Max: ${r.maxMs}",
                            color = C.TextSecondary, fontSize = 11.sp)
                        Text("Loss: ${r.lossPct.toInt()}%",
                            color = if (r.lossPct > 0) C.Red else C.Lime, fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("// DNS BENCHMARK", color = C.TextSecondary, fontSize = 11.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(8.dp))
            NeonButton("🔍 TEST 6 DNS", C.Magenta, {
                if (ready) scope.launch {
                    running = true
                    dnsResults = DnsBenchmark.benchmark()
                    running = false
                }
            }, enabled = ready && !running)

            if (dnsResults.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                dnsResults.forEachIndexed { i, r ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(C.BgSurface).padding(10.dp)) {
                        Text("${i + 1}.", color = C.Gold, fontSize = 12.sp,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(r.name, color = if (i == 0) C.Lime else C.TextPrimary,
                                fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(r.ip, color = C.TextSecondary, fontSize = 9.sp)
                        }
                        Text("${r.ms}ms",
                            color = if (r.ms < 50) C.Lime else if (r.ms < 150) C.Gold else C.Red,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
