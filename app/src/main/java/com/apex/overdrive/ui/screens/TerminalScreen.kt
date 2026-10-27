package com.apex.overdrive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.ShizukuManager
import com.apex.overdrive.core.ShellExecutor
import com.apex.overdrive.ui.*
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(szState: ShizukuManager.State, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var cmd by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(listOf<String>()) }
    val ready = szState == ShizukuManager.State.Ready

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Cyan)
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            BackBar("TERMINAL", onBack)
            Spacer(Modifier.height(16.dp))

            Box(Modifier.weight(1f).fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF000000))
                .border(1.dp, C.Border, RoundedCornerShape(12.dp))
                .padding(12.dp).verticalScroll(rememberScrollState())) {
                Column {
                    if (history.isEmpty()) {
                        Text("// ADB shell ready\n// Type command and press RUN",
                            color = C.TextSecondary, fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace)
                    } else {
                        history.forEach { line ->
                            Text(line,
                                color = if (line.startsWith("$")) C.Lime else C.Cyan,
                                fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$ ", color = C.Lime, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                Box(Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(C.BgSurface).padding(8.dp)) {
                    BasicTextField(
                        value = cmd, onValueChange = { cmd = it },
                        textStyle = TextStyle(color = C.Cyan, fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    if (cmd.isEmpty()) {
                        Text("wm size", color = C.TextSecondary.copy(alpha = 0.5f),
                            fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonButton("▶ RUN", C.Lime, {
                    if (cmd.isNotBlank() && ready) {
                        val c = cmd
                        history = history + "$ $c"
                        cmd = ""
                        scope.launch {
                            history = history + ShellExecutor.execute(c).output
                        }
                    }
                }, enabled = cmd.isNotBlank() && ready, modifier = Modifier.weight(1f))
                NeonButton("✕ CLEAR", C.Red, {
                    history = emptyList(); cmd = ""
                }, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))
            Text("// Quick:", color = C.TextSecondary, fontSize = 10.sp)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("wm size", "getprop", "pm list packages -3").forEach { q ->
                    Box(Modifier.clip(RoundedCornerShape(6.dp))
                        .background(C.BgSurface)
                        .clickable { cmd = q }
                        .padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(q, color = C.Cyan, fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace)
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}
