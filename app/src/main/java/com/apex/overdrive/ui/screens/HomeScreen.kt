package com.apex.overdrive.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apex.overdrive.core.ShizukuManager
import com.apex.overdrive.core.TweakDatabase
import com.apex.overdrive.ui.*

@Composable
fun HomeScreen(
    cpu: Int, ram: Int, temp: Float, fps: Int, fpsHistory: List<Float>,
    szState: ShizukuManager.State, appliedCount: Int, detectedGame: String?,
    sessionTime: String, onNavigate: (String) -> Unit, onShizukuAction: () -> Unit,
    onRestoreAll: () -> Unit
) {
    var selectedGamePkg by remember { mutableStateOf(detectedGame ?: "com.garena.game.kgvn") }
    
    LaunchedEffect(detectedGame) {
        if (detectedGame != null) selectedGamePkg = detectedGame!!
    }

    val isLienQuan = selectedGamePkg == "com.garena.game.kgvn"
    val isRoblox = selectedGamePkg == "com.roblox.client"
    val gameName = TweakDatabase.gameByName(selectedGamePkg)?.name ?: "Liên Quân Mobile"
    
    val realFps = if (fps > 0) fps else 0
    val realCpu = cpu
    val realRam = ram

    Row(Modifier.fillMaxSize().background(C.BgDeep)) {
        // ==========================================
        // CỘT TRÁI: SIDEBAR
        // ==========================================
        Column(Modifier.width(180.dp).fillMaxHeight().background(C.Surface).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GamingAvatar(40)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("APEX", color = C.TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp, lineHeight = 16.sp, fontStyle = FontStyle.Italic)
                    Text("TURBO", color = C.Gold, fontWeight = FontWeight.Black, fontSize = 16.sp, lineHeight = 16.sp, fontStyle = FontStyle.Italic)
                }
            }
            Spacer(Modifier.height(24.dp))
            
            SidebarItem(Icons.Default.Home, "Trang chủ", true) {}
            SidebarItem(Icons.Default.RocketLaunch, "Tăng tốc game", false) { onNavigate("ff") }
            SidebarItem(Icons.Default.Memory, "Hiệu suất", false) { onNavigate("deep") }
            SidebarItem(Icons.Default.Terminal, "ADB Shell", false) { onNavigate("terminal") }
            
            Spacer(Modifier.height(12.dp))
            // NÚT RESET
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(C.Red.copy(alpha = 0.1f)).clickable { onRestoreAll() }.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = C.Red, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Khôi phục gốc", color = C.Red, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.weight(1f))
        }

        // ==========================================
        // CỘT GIỮA: DASHBOARD
        // ==========================================
        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp)) {
            
            // HÀNG 1: THÔNG SỐ & BOOST
            Row(
                Modifier.fillMaxWidth().height(210.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(C.Surface).padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = C.Gold, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Hiệu suất thực", color = C.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Spacer(Modifier.height(12.dp))
                    
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                            Canvas(Modifier.fillMaxSize()) {
                                drawCircle(color = C.Border, style = Stroke(width = 5.dp.toPx()))
                                drawArc(color = C.Gold, startAngle = -90f, sweepAngle = if (realFps > 0) 270f * (realFps / 120f) else 0f, useCenter = false, style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (realFps > 0) "$realFps" else "--", color = C.Gold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                Text("FPS", color = C.TextSecondary, fontSize = 8.sp)
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatProgressRow("CPU", "$realCpu%", realCpu / 100f)
                            StatProgressRow("RAM", "$realRam%", realRam / 100f)
                            StatProgressRow("TEMP", if (temp > 0) "${temp.toInt()}°C" else "--", if (temp > 0) (temp / 100f) else 0f)
                        }
                    }
                }

                // Nút Boost (Tự đổi Icon theo Game)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                    val infiniteTransition = rememberInfiniteTransition(label = "radar")
                    val ringScale by infiniteTransition.animateFloat(0.9f, 1.1f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart))
                    val ringAlpha by infiniteTransition.animateFloat(0.3f, 0f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart))

                    Box(Modifier.size(160.dp).scale(ringScale).border(2.dp, C.Gold.copy(alpha = ringAlpha), CircleShape))
                    Canvas(Modifier.size(140.dp)) { drawCircle(brush = Brush.sweepGradient(listOf(C.Gold, C.DarkGold, C.Gold)), style = Stroke(width = 4.dp.toPx())) }

                    Column(
                        Modifier.size(120.dp).clip(CircleShape).background(Brush.radialGradient(listOf(C.Surface, C.BgDeep))).clickable { onNavigate("ff") },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val gameIcon = when {
                            isLienQuan -> Icons.Default.Security
                            isRoblox -> Icons.Default.ViewInAr
                            else -> Icons.Default.RocketLaunch
                        }
                        Icon(gameIcon, contentDescription = null, tint = C.Gold, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("TĂNG TỐC", color = C.TextPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text(gameName, color = C.TextSecondary, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                // Bảng Toggles
                Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(C.Surface).padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = C.Gold, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tùy chỉnh", color = C.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Spacer(Modifier.height(8.dp))
                    ToggleRowPremium("Tối ưu CPU/GPU", true)
                    ToggleRowPremium("Giảm nhiệt độ", true)
                    ToggleRowPremium("Tối ưu mạng", true)
                    ToggleRowPremium("Khóa FPS", true)
                }
            }

            Spacer(Modifier.height(16.dp))

            // HÀNG 2: NÚT GIẢM ĐỒ HỌA (Hiển thị cho Liên Quân & Roblox)
            if (isLienQuan || isRoblox) {
                Row(Modifier.fillMaxWidth().height(70.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(C.Surface).border(1.dp, C.Gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).clickable { onNavigate("ff") }, contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoSizeSelectSmall, contentDescription = null, tint = C.Gold, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Giảm đồ họa 720p", color = C.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                Text("Mượt hơn, hình ảnh nét vừa", color = C.TextSecondary, fontSize = 9.sp, maxLines = 1)
                            }
                        }
                    }
                    
                    Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(C.Surface).border(1.dp, C.Gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).clickable { onNavigate("ff") }, contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = C.Gold, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Giảm đồ họa 540p", color = C.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                Text("Siêu mượt cho máy yếu", color = C.TextSecondary, fontSize = 9.sp, maxLines = 1)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // HÀNG 3: CHỌN GAME
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(C.Surface).padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = C.Gold, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Chọn Game", color = C.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(TweakDatabase.GAMES) { game ->
                        val isSelected = game.pkg == selectedGamePkg
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedGamePkg = game.pkg }
                        ) {
                            Box(
                                Modifier.size(60.dp, 75.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) C.Gold.copy(alpha = 0.2f) else C.BgDeep)
                                    .border(1.dp, if (isSelected) C.Gold else C.Border, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(game.emoji, fontSize = 28.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(game.name, color = if (isSelected) C.Gold else C.TextSecondary, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(60.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun SidebarItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg = if (isSelected) Brush.horizontalGradient(listOf(C.Gold.copy(alpha = 0.15f), Color.Transparent)) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    val tint = if (isSelected) C.Gold else C.TextSecondary
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(bg).clickable { onClick() }.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(title, color = tint, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp, maxLines = 1)
    }
    Spacer(Modifier.height(2.dp))
}

@Composable
fun StatProgressRow(label: String, value: String, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = C.TextSecondary, fontSize = 9.sp, modifier = Modifier.width(28.dp))
        Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(C.Border)) {
            Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(3.dp).clip(CircleShape).background(C.Gold))
        }
        Spacer(Modifier.width(6.dp))
        Text(value, color = C.TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
    }
}

@Composable
fun ToggleRowPremium(title: String, isChecked: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = C.TextPrimary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked, onCheckedChange = {},
            colors = SwitchDefaults.colors(checkedThumbColor = C.BgDeep, checkedTrackColor = C.Gold),
            modifier = Modifier.scale(0.6f)
        )
    }
}
