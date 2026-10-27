package com.apex.overdrive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.GameLibrary
import com.apex.overdrive.core.TweakDatabase
import com.apex.overdrive.ui.*

@Composable
fun GameLibraryScreen(lib: GameLibrary, onBack: () -> Unit) {
    var tracked by remember { mutableStateOf(lib.tracked()) }
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Lime)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("GAME LIBRARY", onBack)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonButton("✓ ALL", C.Lime, {
                    tracked = lib.setAll(true)
                }, modifier = Modifier.weight(1f))
                NeonButton("✕ NONE", C.Red, {
                    tracked = lib.setAll(false)
                }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            TweakDatabase.GAMES.forEach { g ->
                val accent = C.fromLong(g.accent)
                val isOn = g.pkg in tracked
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(
                        if (isOn) accent.copy(alpha = 0.15f) else C.BgSurface,
                        C.BgElevated)))
                    .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(g.emoji, fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(g.name, color = if (isOn) accent else C.TextPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(g.pkg, color = C.TextSecondary, fontSize = 9.sp)
                    }
                    GamingSwitch(isOn, {
                        tracked = lib.toggle(g.pkg)
                    }, accent)
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
