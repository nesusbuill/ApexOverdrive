package com.apex.overdrive.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.apex.overdrive.core.ShizukuManager
import com.apex.overdrive.core.Tweak
import com.apex.overdrive.core.TweakCategory

@Composable
fun BackBar(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.clip(RoundedCornerShape(10.dp)).background(C.BgSurface)
                .border(1.dp, C.Border, RoundedCornerShape(10.dp))
                .clickable(onClick = onBack).padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("← BACK", color = C.Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
        Spacer(Modifier.width(14.dp))
        Text(title, color = C.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.3.sp)
    }
}

@Composable
fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(3.dp).height(12.dp).background(C.Cyan, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(text, color = C.TextSecondary, fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
fun MiniStat(label: String, v: Int, color: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(6.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(4.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(5.dp))
        Text("$label $v", color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
    }
}

@Composable
fun ShizukuBanner(state: ShizukuManager.State, onClick: () -> Unit) {
    val (emoji, text, color) = when (state) {
        ShizukuManager.State.Ready -> Triple("●", "SHIZUKU READY", C.Lime)
        ShizukuManager.State.PermissionRequired -> Triple("●", "GRANT SHIZUKU PERMISSION", C.Warning)
        ShizukuManager.State.NotRunning -> Triple("●", "SHIZUKU NOT RUNNING", C.Orange)
        ShizukuManager.State.NotInstalled -> Triple("●", "INSTALL SHIZUKU", C.Danger)
    }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.10f)).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, color = color, fontSize = 12.sp)
        Spacer(Modifier.width(8.dp))
        Text(text, color = color, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.8.sp)
        Spacer(Modifier.weight(1f))
        Text("→", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GamingSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, accent: Color = C.Cyan, enabled: Boolean = true) {
    val thumbX by animateFloatAsState(if (checked) 1f else 0f, spring(dampingRatio = 0.75f, stiffness = 500f), label = "thumb_x")
    val trackColor by animateColorAsState(if (checked) accent else C.Border, tween(220), label = "track_color")
    val inf = rememberInfiniteTransition(label = "glow")
    val glowAlpha by inf.animateFloat(0.5f, 0.85f, infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")

    val widthDp = 50.dp
    val heightDp = 28.dp
    val padding = 3.dp

    Box(
        Modifier.size(widthDp, heightDp).clickable(
            enabled = enabled, interactionSource = remember { MutableInteractionSource() },
            indication = null, onClick = { onCheckedChange(!checked) }
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val r = h / 2f

            if (checked) {
                drawRoundRect(
                    color = accent.copy(alpha = 0.22f * glowAlpha),
                    topLeft = Offset(-3f, -3f),
                    size = Size(w + 6f, h + 6f),
                    cornerRadius = CornerRadius(r + 3f, r + 3f)
                )
            }

            drawRoundRect(
                color = if (checked) accent.copy(alpha = 0.30f) else C.BgElevated,
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r)
            )

            drawRoundRect(
                color = trackColor.copy(alpha = if (checked) 0.9f else 0.5f),
                size = Size(w, h),
                cornerRadius = CornerRadius(r, r),
                style = Stroke(width = 1.5f)
            )

            val thumbR = (h - padding.toPx() * 2) / 2f
            val thumbCX = padding.toPx() + thumbR + (w - padding.toPx() * 2 - thumbR * 2) * thumbX
            val thumbCY = h / 2f

            drawCircle(color = Color.Black.copy(alpha = 0.25f), radius = thumbR, center = Offset(thumbCX, thumbCY + 1f))
            drawCircle(color = if (checked) Color.White else C.TextSecondary, radius = thumbR, center = Offset(thumbCX, thumbCY))

            if (checked) {
                drawCircle(color = accent.copy(alpha = 0.4f), radius = thumbR * 0.55f, center = Offset(thumbCX, thumbCY))
            }
        }
    }
}

@Composable
fun TweakRow(tweak: Tweak, isOn: Boolean, isBusy: Boolean, onToggle: (Boolean) -> Unit) {
    val accent = when (tweak.category) {
        TweakCategory.GAME -> C.Lime
        TweakCategory.DISPLAY -> C.Cyan
        TweakCategory.SYSTEM -> C.Purple
        TweakCategory.NETWORK -> C.Magenta
        TweakCategory.TOUCH -> C.Orange
        TweakCategory.CLEAN -> C.Gold
        TweakCategory.ROOT -> C.Red
    }
    val bc by animateColorAsState(if (isOn) accent.copy(alpha = 0.6f) else C.Border, tween(200), label = "bc")
    val tc by animateColorAsState(if (isOn) accent else C.TextPrimary, tween(200), label = "tc")

    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(if (isOn) accent.copy(alpha = 0.06f) else C.BgSurface)
            .border(1.dp, bc, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tweak.name, color = tc, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                if (tweak.risky) { Spacer(Modifier.width(6.dp)); Text("⚠", color = C.Red, fontSize = 10.sp) }
                if (tweak.needsRoot) { Spacer(Modifier.width(6.dp)); Text("🔒", color = C.Gold, fontSize = 10.sp) }
                if (isBusy) { Spacer(Modifier.width(6.dp)); Text("⚙", color = C.Gold, fontSize = 10.sp) }
            }
            Spacer(Modifier.height(3.dp))
            Text(tweak.desc, color = C.TextSecondary, fontSize = 10.sp)
        }
        Spacer(Modifier.width(12.dp))
        GamingSwitch(checked = isOn, onCheckedChange = onToggle, accent = accent, enabled = !isBusy)
    }
}

@Composable
fun RestoreButton(enabled: Boolean = true, hasActive: Boolean = true, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var showConfirm by remember { mutableStateOf(false) }
    val inf = rememberInfiniteTransition(label = "restore_glow")
    val glowAlpha by inf.animateFloat(0.4f, 0.75f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")

    Box(
        modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled && hasActive) Brush.horizontalGradient(colors = listOf(C.Red.copy(alpha = 0.85f), C.Orange.copy(alpha = 0.75f)))
                else Brush.horizontalGradient(colors = listOf(C.BgElevated, C.BgElevated))
            )
            .border(1.5.dp, if (enabled && hasActive) C.Red.copy(alpha = glowAlpha) else C.Border, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled && hasActive) { showConfirm = true },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("↻", color = if (enabled && hasActive) Color.White else C.TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Text(if (hasActive) "RESTORE ALL" else "NO ACTIVE TWEAKS", color = if (enabled && hasActive) Color.White else C.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.5.sp)
        }
    }

    if (showConfirm) {
        DangerDialog(
            title = "RESTORE ALL TWEAKS?",
            message = "This will revert every active tweak back to the system default.\n\nThis action cannot be undone, but you can re-apply tweaks anytime.",
            confirmText = "RESTORE",
            onConfirm = { showConfirm = false; onClick() },
            onDismiss = { showConfirm = false }
        )
    }
}

@Composable
fun DangerDialog(title: String, message: String, confirmText: String = "CONTINUE", onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.clip(RoundedCornerShape(18.dp)).background(C.BgSurface).border(1.dp, C.Red.copy(alpha = 0.5f), RoundedCornerShape(18.dp)).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠", color = C.Red, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(title, color = C.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(message, color = C.TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(C.BgElevated).clickable(onClick = onDismiss).padding(12.dp), contentAlignment = Alignment.Center) {
                    Text("CANCEL", color = C.TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(C.Red).clickable(onClick = onConfirm).padding(12.dp), contentAlignment = Alignment.Center) {
                    Text(confirmText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ProgressBar(current: Int, total: Int, name: String) {
    if (total == 0) return
    val anim by animateFloatAsState((current.toFloat() / total).coerceIn(0f, 1f), tween(300), label = "p")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(C.BgSurface).border(1.dp, C.Border, RoundedCornerShape(10.dp)).padding(12.dp)) {
        Row {
            Text("$current / $total", color = C.Cyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            Text(name.take(32), color = C.TextSecondary, fontSize = 10.sp, maxLines = 1)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(C.Border)) {
            Box(Modifier.fillMaxWidth(anim).height(4.dp).background(Brush.horizontalGradient(colors = listOf(C.Cyan, C.Purple)), RoundedCornerShape(2.dp)))
        }
    }
}
