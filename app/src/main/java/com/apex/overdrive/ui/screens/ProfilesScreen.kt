package com.apex.overdrive.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.apex.overdrive.core.ProfileStore
import com.apex.overdrive.core.TweakDatabase
import com.apex.overdrive.ui.*

@Composable
fun ProfilesScreen(
    store: ProfileStore,
    onBack: () -> Unit,
    onApplyProfile: (String, Set<String>) -> Unit
) {
    var profiles by remember { mutableStateOf(store.listProfiles()) }
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Cyan)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("CUSTOM PROFILES", onBack)
            Spacer(Modifier.height(16.dp))
            Text("// One profile per game", color = C.TextSecondary,
                fontSize = 11.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))

            TweakDatabase.GAMES.forEach { g ->
                val accent = C.fromLong(g.accent)
                val profile = profiles[g.pkg]
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(
                        accent.copy(alpha = if (profile != null) 0.25f else 0.08f),
                        C.BgElevated)))
                    .border(if (profile != null) 2.dp else 1.dp,
                        if (profile != null) accent else C.Border, RoundedCornerShape(14.dp))
                    .clickable {
                        val ids = profile ?: TweakDatabase.gameBoost(g.pkg).map { it.id }.toSet()
                        onApplyProfile(g.pkg, ids)
                    }
                    .padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(g.emoji, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(g.name, color = accent,
                                fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                if (profile != null) "${profile.size} tweaks · Custom"
                                else "Default (${TweakDatabase.gameBoost(g.pkg).size} tweaks)",
                                color = C.TextSecondary, fontSize = 10.sp)
                        }
                        Text(if (profile != null) "▶" else "+", color = accent, fontSize = 20.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
