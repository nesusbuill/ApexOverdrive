package com.apex.overdrive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.GameSession
import com.apex.overdrive.core.TweakDatabase
import com.apex.overdrive.ui.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(session: GameSession, onBack: () -> Unit) {
    val history = session.history()
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Gold)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("SESSION HISTORY", onBack)
            Spacer(Modifier.height(20.dp))
            if (history.isEmpty()) {
                Text("No sessions yet.\nPlay a game to start tracking.",
                    color = C.TextSecondary, fontSize = 13.sp)
                return@Column
            }
            history.forEach { o ->
                val pkg = o.optString("pkg")
                val game = TweakDatabase.gameByName(pkg)
                val name = game?.name ?: pkg
                val emoji = game?.emoji ?: "🎮"
                val date = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                    .format(Date(o.optLong("startedAt")))
                val duration = o.optInt("durationMin")
                val avg = o.optInt("avgFps")
                val min = o.optInt("minFps")
                val maxT = o.optDouble("maxTemp", 0.0)
                val drops = o.optInt("drops")

                Box(Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(C.BgSurface).padding(12.dp)) {
                    Column {
                        Row {
                            Text("$emoji $name", color = C.Cyan,
                                fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(Modifier.weight(1f))
                            Text(date, color = C.TextSecondary, fontSize = 10.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row {
                            InfoChip("⏱ ${duration}min")
                            Spacer(Modifier.width(6.dp))
                            InfoChip("FPS $avg", C.Lime)
                            Spacer(Modifier.width(6.dp))
                            InfoChip("Min $min", if (min < 30) C.Red else C.Gold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row {
                            InfoChip("${maxT.toInt()}°C", if (maxT > 45) C.Red else C.Lime)
                            Spacer(Modifier.width(6.dp))
                            InfoChip("$drops drops", if (drops > 3) C.Orange else C.TextSecondary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun InfoChip(text: String, color: androidx.compose.ui.graphics.Color = C.TextSecondary) {
    Box(Modifier.clip(RoundedCornerShape(6.dp))
        .background(color.copy(alpha = 0.15f))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
