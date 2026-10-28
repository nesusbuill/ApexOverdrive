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
    var boostEnabled by remember { mutableStateOf(false) }
    
    val displayFps = if (fps > 0) fps else 240
    val displayCpu = if (cpu > 0) cpu else 32
    val displayRam = if (ram > 0) ram else 62
    val displayGpu = 48
    val displayPing = 12

    Row(Modifier.fillMaxSize().background(C.BgDeep)) {
        // CỘT TRÁI
        Column(Modifier.width(200.dp).fillMaxHeight().background(C.Surface).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GamingAvatar(40)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("APEX", color = C.TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp, lineHeight = 18.sp, fontStyle = FontStyle.Italic)
                    Text("TURBO", color = C.Gold, fontWeight = FontWeight.Black, fontSize = 18.sp, lineHeight = 18.sp, fontStyle = FontStyle.Italic)
                }
            }
            Text("More FPS · Less Lag", color = C.TextSecondary, fontSize = 9.sp, modifier = Modifier.padding(top = 8.dp))
            
            Spacer(Modifier.height(30.dp))
            
            SidebarItem(Icons.Default.Home, "Trang chủ", true) {}
            SidebarItem(Icons.Default.RocketLaunch, "Tăng tốc game", false) { onNavigate("ff") }
            SidebarItem(Icons.Default.Memory, "Hiệu suất hệ thống", false) { onNavigate("deep") }
            SidebarItem(Icons.Default.SportsEsports, "Quản lý game", false) { onNavigate("library") }
            SidebarItem(Icons.Default.Terminal, "ADB Shell", false) { onNavigate("terminal") }
            SidebarItem(Icons.Default.Settings, "Cài đặt", false) { onNavigate("settings") }
            
            // NÚT RESET ĐÃ ĐƯỢC THÊM VÀO ĐÂY
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(C.Red.copy(alpha = 0.1f)).clickable { onRestoreAll() }.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = C.Red, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Khôi phục gốc", color = C.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.weight(1f))
            
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, C.Gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).background(C.BgDeep).padding(12.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("👑 Premium", color = C.Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Mở khóa toàn bộ tính năng", color = C.TextSecondary, fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(C.Gold).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text("Nâng cấp ngay", color = C.BgDeep, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // CỘT PHẢI
        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(20.dp)) {
            
            Row(Modifier.fillMaxWidth().height(250.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                
                Column(Modifier.width(220.dp).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(C.SurfaceGlass).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = C.Gold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Hiệu suất hiện tại", color = C.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            Canvas(Modifier.fillMaxSize()) {
                                drawCircle(color = C.Border, style = Stroke(width = 6.dp.toPx()))
                                drawArc(
                                    color = C.Gold,
                                    startAngle = -90f,
                                    sweepAngle = 270f * (displayFps / 240f),
                                    useCenter = false,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$displayFps", color = C.Gold, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("FPS", color = C.TextSecondary, fontSize = 9.sp)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatProgressRow("CPU", "$displayCpu%", displayCpu / 100f)
                            StatProgressRow("GPU", "$displayGpu%", displayGpu / 100f)
                            StatProgressRow("RAM", "$displayRam%", displayRam / 100f)
                            StatProgressRow("Ping", "${displayPing}ms", 0.2f)
                        }
                    }
                    
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = C.Gold, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Hiệu suất tăng ~ 68%", color = C.Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                    val infiniteTransition = rememberInfiniteTransition(label = "radar")
                    val ringScale by infiniteTransition.animateFloat(0.9f, 1.1f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart))
                    val ringAlpha by infiniteTransition.animateFloat(0.3f, 0f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart))

                    Box(Modifier.size(220.dp).scale(ringScale).border(2.dp, C.Gold.copy(alpha = ringAlpha), CircleShape))
                    
                    Canvas(Modifier.size(200.dp)) {
                        drawCircle(brush = Brush.sweepGradient(listOf(C.Gold, C.DarkGold, C.Gold)), style = Stroke(width = 6.dp.toPx()))
                    }

                    Column(
                        Modifier.size(170.dp).clip(CircleShape).background(Brush.radialGradient(listOf(C.Surface, C.BgDeep)))
                            .clickable { onNavigate("modes") },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = C.Gold, modifier = Modifier.size(44.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("TĂNG TỐC", color = C.TextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("Bật tối ưu ngay", color = C.TextSecondary, fontSize = 10.sp)
                        Spacer(Modifier.height(12.dp))
                        Box(Modifier.size(28.dp).clip(CircleShape).background(C.Gold), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = C.BgDeep, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Column(Modifier.width(220.dp).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(C.SurfaceGlass).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = C.Gold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tùy chỉnh tăng tốc", color = C.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                    ToggleRowPremium("Tối ưu CPU & GPU", "Giảm tải, tăng hiệu suất", true)
                    ToggleRowPremium("Tắt ứng dụng nền", "Giải phóng tài nguyên", true)
                    ToggleRowPremium("Tối ưu mạng", "Giảm ping, ổn định", true)
                    ToggleRowPremium("Tăng FPS trong game", "Tối ưu đồ họa, mượt hơn", true)
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1.2f).clip(RoundedCornerShape(20.dp)).background(C.SurfaceGlass).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = C.Gold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Chế độ tăng tốc", color = C.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ModeCardPremium("Cân bằng", "Hiệu năng ổn định", Icons.Default.Balance, true, Modifier.weight(1f)) { onNavigate("modes") }
                        ModeCardPremium("Hiệu suất cao", "Tối đa FPS", Icons.Default.Speed, false, Modifier.weight(1f)) { onNavigate("modes") }
                        ModeCardPremium("Tiết kiệm pin", "Kéo dài thời gian", Icons.Default.BatterySaver, false, Modifier.weight(1f)) { onNavigate("modes") }
                    }
                }

                Column(Modifier.weight(0.8f).clip(RoundedCornerShape(20.dp)).background(C.SurfaceGlass).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = C.Gold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Tình trạng mạng", color = C.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text("● Ổn định", color = C.Green, fontSize = 10.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Ping", color = C.TextSecondary, fontSize = 10.sp); Text("${displayPing}ms", color = C.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        Column { Text("Tải xuống", color = C.TextSecondary, fontSize = 10.sp); Text("125 Mbps", color = C.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        Column { Text("Tải lên", color = C.TextSecondary, fontSize = 10.sp); Text("98 Mbps", color = C.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.SurfaceGlass).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = C.Gold, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Game phổ biến", color = C.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(TweakDatabase.GAMES) { game ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(80.dp, 100.dp).clip(RoundedCornerShape(12.dp))
                                    .background(Brush.verticalGradient(listOf(C.Surface, C.BgDeep)))
                                    .border(1.dp, C.Border, RoundedCornerShape(12.dp))
                                    .clickable { onNavigate("ff") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(game.emoji, fontSize = 36.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(game.name, color = C.TextPrimary, fontSize = 10.sp, maxLines = 1)
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(C.Surface).border(1.dp, C.Gold, RoundedCornerShape(6.dp)).clickable { onNavigate("ff") }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("👑 Tăng tốc", color = C.Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(C.SurfaceGlass).padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                FooterItem(Icons.Default.Speed, "Tăng FPS", "Trải nghiệm mượt mà")
                FooterItem(Icons.Default.Wifi, "Giảm ping", "Kết nối ổn định")
                FooterItem(Icons.Default.Memory, "Giải phóng RAM", "Hiệu suất tối đa")
                FooterItem(Icons.Default.Security, "Bảo vệ hệ thống", "An toàn, ổn định")
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
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, color = tint, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
fun StatProgressRow(label: String, value: String, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = C.TextSecondary, fontSize = 10.sp, modifier = Modifier.width(30.dp))
        Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(C.Border)) {
            Box(Modifier.fillMaxWidth(progress).height(3.dp).clip(CircleShape).background(C.Gold))
        }
        Spacer(Modifier.width(8.dp))
        Text(value, color = C.TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(35.dp))
    }
}

@Composable
fun ToggleRowPremium(title: String, subtitle: String, isChecked: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = C.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = C.TextSecondary, fontSize = 9.sp)
        }
        Switch(
            checked = isChecked, onCheckedChange = {},
            colors = SwitchDefaults.colors(
                checkedThumbColor = C.BgDeep, checkedTrackColor = C.Gold,
                uncheckedThumbColor = C.TextSecondary, uncheckedTrackColor = C.Border
            ),
            modifier = Modifier.scale(0.7f)
        )
    }
}

@Composable
fun ModeCardPremium(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (isSelected) Brush.horizontalGradient(listOf(C.Gold.copy(alpha = 0.2f), Color.Transparent)) else Brush.horizontalGradient(listOf(C.Surface, C.Surface))
    val border = if (isSelected) C.Gold else C.Border
    val tint = if (isSelected) C.Gold else C.TextSecondary
    Row(
        modifier.clip(RoundedCornerShape(12.dp)).background(bg).border(1.dp, border, RoundedCornerShape(12.dp)).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, color = if (isSelected) C.Gold else C.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(subtitle, color = C.TextSecondary, fontSize = 9.sp)
        }
    }
}

@Composable
fun FooterItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = C.Gold, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, color = C.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = C.TextSecondary, fontSize = 9.sp)
        }
    }
}
