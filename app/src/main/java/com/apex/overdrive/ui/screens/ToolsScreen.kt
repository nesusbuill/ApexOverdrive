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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.ShizukuManager
import com.apex.overdrive.core.ShellExecutor
import com.apex.overdrive.ui.*
import kotlinx.coroutines.launch

data class ToolItem(val emoji: String, val name: String, val cmd: String)

val TOOLS = listOf(
    ToolItem("🧹", "DEEP CLEAN", "am kill-all"),
    ToolItem("🌐", "FLUSH DNS", "ndc resolver flushdefaultif"),
    ToolItem("🔆", "BRIGHTNESS MAX", "settings put system screen_brightness 255"),
    ToolItem("🔕", "DND ON", "settings put global zen_mode 1"),
    ToolItem("🔔", "DND OFF", "settings put global zen_mode 0"),
    ToolItem("📦", "LIST APPS", "pm list packages -3"),
    ToolItem("🌡️", "THERMAL", "cat /sys/class/thermal/thermal_zone0/temp"),
    ToolItem("🔄", "RESTART UI", "pkill -f com.android.systemui")
)

@Composable
fun ToolsScreen(szState: ShizukuManager.State, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var output by remember { mutableStateOf("") }
    val ready = szState == ShizukuManager.State.Ready

    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Orange)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("TOOLS", onBack)
            Spacer(Modifier.height(20.dp))
            TOOLS.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { tool ->
                        GridCard(tool.emoji, tool.name, "Tap to run", C.Orange, {
                            if (ready) scope.launch {
                                output = "[${tool.name}]\n${ShellExecutor.execute(tool.cmd).output}"
                            }
                        }, Modifier.weight(1f))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
            }

            if (output.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text("// OUTPUT", color = C.TextSecondary, fontSize = 11.sp, letterSpacing = 3.sp)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF000000)).padding(12.dp)) {
                    Text(output, color = C.Cyan, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}
