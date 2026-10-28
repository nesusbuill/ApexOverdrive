package com.apex.overdrive.ui.screens

import androidx.compose.foundation.background
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
import com.apex.overdrive.core.AppSettings
import com.apex.overdrive.ui.*

@Composable
fun SettingsScreen(
    current: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        AnimatedBackground(C.Cyan)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            BackBar("SETTINGS", onBack)
            Spacer(Modifier.height(20.dp))

            SectionLabel("AUTO-BOOST")
            ToggleRowSwitch("Auto-Boost on game launch", current.autoBoost) {
                onUpdate { s -> s.copy(autoBoost = it) }
            }
            ToggleRowSwitch("Auto-Restore on game close", current.autoRestore) {
                onUpdate { s -> s.copy(autoRestore = it) }
            }
            ToggleRowSwitch("Auto-DND while gaming", current.autoDnd) {
                onUpdate { s -> s.copy(autoDnd = it) }
            }
            ToggleRowSwitch("Auto-Brightness MAX", current.autoBrightness) {
                onUpdate { s -> s.copy(autoBrightness = it) }
            }
            ToggleRowSwitch("Vibration on boost", current.vibrationOnBoost) {
                onUpdate { s -> s.copy(vibrationOnBoost = it) }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("SAFETY")
            ToggleRowSwitch("Safe Mode (skip risky tweaks)", current.safeMode) {
                onUpdate { s -> s.copy(safeMode = it) }
            }
            ToggleRowSwitch("Thermal Guard", current.thermalGuard) {
                onUpdate { s -> s.copy(thermalGuard = it) }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("OVERLAY")
            ToggleRowSwitch("FPS Overlay during game", current.fpsOverlay) {
                onUpdate { s -> s.copy(fpsOverlay = it) }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("PRESETS")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("balanced", "turbo", "beast").forEach { mode ->
                    val sel = current.defaultMode == mode
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (sel) C.Cyan.copy(alpha = 0.3f) else C.BgSurface)
                        .clickable { onUpdate { s -> s.copy(defaultMode = mode) } }
                        .padding(10.dp),
                        contentAlignment = Alignment.Center) {
                        Text(mode.uppercase(), color = if (sel) C.Cyan else C.TextSecondary,
                            fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionLabel("DATA")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NeonButton("📤 EXPORT", C.Lime, onExport, modifier = Modifier.weight(1f))
                NeonButton("📥 IMPORT", C.Gold, onImport, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun ToggleRowSwitch(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Brush.horizontalGradient(colors = listOf(C.BgSurface, C.BgElevated)))
        .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = C.TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        GamingSwitch(checked, onToggle, C.Cyan)
    }
}
