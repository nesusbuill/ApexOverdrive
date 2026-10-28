package com.apex.overdrive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    var outputLog by remember { mutableStateOf("Nhập lệnh và bấm RUN...\n") }
    var isRunning by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val ready = szState == ShizukuManager.State.Ready

    fun runCmd() {
        if (cmd.isBlank() || !ready || isRunning) return
        val commandToRun = cmd
        cmd = ""
        isRunning = true
        outputLog += "\n$ $commandToRun\n"
        
        scope.launch {
            val result = ShellExecutor.execute(commandToRun)
            outputLog += "${result.output}\n"
            isRunning = false
        }
    }

    // Tự động cuộn xuống cuối khi có log mới
    LaunchedEffect(outputLog) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Cyan)
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            BackBar("ADB TERMINAL", onBack)
            Spacer(Modifier.height(16.dp))

            // Màn hình Log
            Box(Modifier.weight(1f).fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, C.Border, RoundedCornerShape(12.dp))
                .padding(12.dp)) {
                Column(Modifier.verticalScroll(scrollState)) {
                    Text(outputLog, color = C.Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Thanh nhập lệnh
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$ ", color = C.Green, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                Box(Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(C.Surface).padding(8.dp)) {
                    BasicTextField(
                        value = cmd, onValueChange = { cmd = it },
                        textStyle = TextStyle(color = C.TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    if (cmd.isEmpty()) {
                        Text("settings get global zen_mode", color = C.TextSecondary.copy(alpha = 0.5f), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonButton(
                    text = if (isRunning) "⏳ ĐANG CHẠY..." else "▶ RUN", 
                    accent = C.Green, 
                    onClick = { runCmd() }, 
                    enabled = cmd.isNotBlank() && ready && !isRunning, 
                    modifier = Modifier.weight(1f)
                )
                NeonButton(
                    text = "✕ CLEAR", 
                    accent = C.Red, 
                    onClick = { outputLog = "Nhập lệnh và bấm RUN...\n" }, 
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
