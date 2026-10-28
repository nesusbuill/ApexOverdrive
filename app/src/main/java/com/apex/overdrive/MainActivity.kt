package com.apex.overdrive

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.apex.overdrive.core.*
import com.apex.overdrive.service.AutoBoostService
import com.apex.overdrive.service.BoostService
import com.apex.overdrive.ui.ApexTheme
import com.apex.overdrive.ui.screens.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object { @Volatile var globalCtx: Context? = null }

    private lateinit var shizuku: ShizukuManager
    private lateinit var engine: TweakEngine
    private lateinit var monitor: SystemMonitor
    private lateinit var settingsStore: SettingsStore
    private val notifPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        hideSystemUI()

        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this).setTitle("Cần quyền Overlay")
                .setMessage("Để chạy tính năng Game Turbo, cần cấp quyền Hiển thị trên ứng dụng khác.")
                .setPositiveButton("Cấp quyền") { _, _ ->
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }.show()
        }

        globalCtx = applicationContext
        shizuku = ShizukuManager(this)
        engine = TweakEngine(this)
        monitor = SystemMonitor(this)
        settingsStore = SettingsStore(this)
        
        shizuku.register()

        if (Build.VERSION.SDK_INT >= 33) notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)

        try {
            val svc = Intent(this, BoostService::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc) else startService(svc)
            val autoSvc = Intent(this, AutoBoostService::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(autoSvc) else startService(autoSvc)
        } catch (_: Throwable) {}

        setContent {
            ApexTheme {
                var screen by remember { mutableStateOf("home") }
                var cpu by remember { mutableIntStateOf(0) }
                var ram by remember { mutableIntStateOf(0) }
                var temp by remember { mutableFloatStateOf(-1f) }
                var fps by remember { mutableIntStateOf(0) }
                var ping by remember { mutableIntStateOf(-1) }
                var jitter by remember { mutableIntStateOf(-1) }
                var detectedGame by remember { mutableStateOf<String?>(null) }
                val fpsHist = remember { mutableStateListOf<Float>() }
                val tempHist = remember { mutableStateListOf<Float>() }
                val szState by shizuku.state.collectAsState()
                val applied by engine.applied.collectAsState()
                val settings by settingsStore.settings.collectAsState()

                LaunchedEffect(Unit) {
                    while (true) {
                        cpu = monitor.cpu(); ram = monitor.ram(); temp = monitor.temp()
                        if (temp > 0) {
                            tempHist.add(temp)
                            if (tempHist.size > 60) tempHist.removeAt(0)
                        }
                        val pr = NetworkOptimizer.measurePing()
                        ping = pr.pingMs; jitter = pr.jitterMs
                        delay(3000)
                    }
                }

                LaunchedEffect(detectedGame) {
                    while (true) {
                        detectedGame?.let { pkg ->
                            val f = FpsReader.readFps(pkg); fps = f
                            if (f > 0) {
                                fpsHist.add(f.toFloat())
                                if (fpsHist.size > 60) fpsHist.removeAt(0)
                            }
                        }
                        delay(1500)
                    }
                }

                LaunchedEffect(szState) {
                    while (true) {
                        if (szState == ShizukuManager.State.Ready) {
                            val pkg = monitor.currentForegroundPackage()?.takeIf { p ->
                                TweakDatabase.GAMES.any { it.pkg == p }
                            }
                            if (pkg != detectedGame) {
                                detectedGame = pkg
                            }
                        }
                        delay(3000)
                    }
                }

                fun launchGame(pkg: String) {
                    val intent = packageManager.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "Game chưa được cài đặt!", Toast.LENGTH_SHORT).show()
                    }
                }

                suspend fun restoreAllNow() {
                    engine.restoreAll()
                    // Reset đề phòng kẹt độ phân giải cũ
                    ShellExecutor.execute("wm size reset\nwm density reset")
                    TweakDatabase.GAMES.forEach { 
                        if (Build.VERSION.SDK_INT >= 31) {
                            ShellExecutor.execute("cmd game set-downscale-factor ${it.pkg} 1.0")
                        }
                    }
                    Toast.makeText(this@MainActivity, "✓ Đã khôi phục cài đặt gốc", Toast.LENGTH_SHORT).show()
                }

                AnimatedContent(targetState = screen, label = "nav") { s ->
                    when (s) {
                        "home" -> HomeScreen(
                            cpu, ram, temp, fps, ping, jitter, fpsHist.toList(), tempHist.toList(),
                            szState, applied.size, detectedGame,
                            settings = settings,
                            isDownscaleSupported = Build.VERSION.SDK_INT >= 31,
                            onNavigate = { screen = it },
                            onRestoreAll = { lifecycleScope.launch { restoreAllNow() } },
                            onLaunchGame = { pkg -> launchGame(pkg) },
                            onUpdateSettings = { newSettings -> settingsStore.update { newSettings } }
                        )
                        "settings" -> SettingsScreen(settings, onUpdate = { b -> settingsStore.update(b) }, onExport = {}, onImport = {}, onBack = { screen = "home" })
                        "terminal" -> TerminalScreen(szState, onBack = { screen = "home" })
                        else -> HomeScreen(
                            cpu, ram, temp, fps, ping, jitter, fpsHist.toList(), tempHist.toList(),
                            szState, applied.size, detectedGame, settings,
                            isDownscaleSupported = Build.VERSION.SDK_INT >= 31,
                            onNavigate = { screen = it },
                            onRestoreAll = { lifecycleScope.launch { restoreAllNow() } },
                            onLaunchGame = { pkg -> launchGame(pkg) },
                            onUpdateSettings = { newSettings -> settingsStore.update { newSettings } }
                        )
                    }
                }
            }
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
}
