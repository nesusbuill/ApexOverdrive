package com.apex.overdrive.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.ui.*

@Composable
fun StatsScreen(
    cpu: Int, ram: Int, temp: Float, fps: Int,
    fpsHistory: List<Float>, ramHistory: List<Float>, tempHistory: List<Float>,
    battery: Int, batteryTemp: Float, gpuMhz: Int, onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Gold)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("STATS", onBack)
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()) {
                StatRing("CPU", cpu.toFloat(), if (cpu > 70) C.Red else C.Cyan)
                StatRing("RAM", ram.toFloat(), if (ram > 80) C.Orange else C.Lime)
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()) {
                if (fps > 0) StatRing("FPS", (fps / 120f * 100f).coerceIn(0f, 100f),
                    if (fps >= 60) C.Lime else if (fps >= 30) C.Gold else C.Red)
                if (temp > 0) StatRing("TEMP", (temp / 100f * 100f).coerceIn(0f, 100f),
                    if (temp > 45) C.Red else C.Gold)
            }
            if (battery > 0) {
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()) {
                    StatRing("PIN", battery.toFloat(), if (battery < 20) C.Red else C.Lime)
                    StatRing("PIN°C", (batteryTemp / 60f * 100f).coerceIn(0f, 100f),
                        if (batteryTemp > 40) C.Red else C.Cyan)
                }
            }

            Spacer(Modifier.height(24.dp))
            if (fpsHistory.isNotEmpty()) {
                LineChart(fpsHistory, C.Lime, label = "FPS HISTORY")
                Spacer(Modifier.height(12.dp))
            }
            if (ramHistory.isNotEmpty()) {
                LineChart(ramHistory, C.Magenta, label = "RAM %")
                Spacer(Modifier.height(12.dp))
            }
            if (tempHistory.isNotEmpty()) {
                LineChart(tempHistory, C.Red, label = "TEMP °C")
            }

            Spacer(Modifier.height(20.dp))
            Text("GPU: ${if (gpuMhz > 0) "${gpuMhz}MHz" else "N/A"}",
                color = C.Cyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Battery: $battery% · ${batteryTemp.toInt()}°C",
                color = C.Lime, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(60.dp))
        }
    }
}
