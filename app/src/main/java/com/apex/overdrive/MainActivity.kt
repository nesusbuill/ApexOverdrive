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
import androidx.compose.animation.core.tween
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
    private lateinit var thermal: ThermalGuard
    private lateinit var session: GameSession
    private lateinit var profileStore: ProfileStore
    private lateinit var settingsStore: SettingsStore
    private lateinit var gameLib: GameLibrary
    private val notifPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        hideSystemUI()

        // Xin quyền vẽ đè (Overlay) để HUD có thể nổi lên trên game
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle("Cần quyền hiển thị")
                .setMessage("Để hiển thị thanh FPS/Nhiệt độ trên Game, vui lòng cấp quyền 'Hiển thị trên ứng dụng khác'.")
                .setPositiveButton("Cấp quyền") { _, _ ->
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivity(intent)
                }
                .setNegativeButton("Bỏ qua", null)
                .show()
        }

        if (!BuildConfig.DEBUG) {
            val integrity = IntegrityCheck.verify(this)
            if (!integrity.passed) {
                AlertDialog.Builder(this)
                    .setTitle("⚠ SECURITY")
                    .setMessage("APK has been modified.\nReason: ${integrity.reason}")
                    .setCancelable(false)
                    .setPositiveButton("EXIT") { _, _ -> finish() }
                    .show()
                return
            }
        }

        globalCtx = applicationContext
        shizuku = ShizukuManager(this)
        engine = TweakEngine(this)
        monitor = SystemMonitor(this)
        session = GameSession(this)
        profileStore = ProfileStore(this)
        settingsStore = SettingsStore(this)
        gameLib = GameLibrary(this)
        thermal = ThermalGuard(monitor) { _, suggested ->
            lifecycleScope.launch {
                engine.applyBatch(TweakDatabase.forMode(suggested, ShellExecutor.hasRoot()))
            }
        }
        shizuku.register()
        thermal.start(lifecycleScope)

        if (Build.VERSION.SDK_INT >= 33) notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)

        try {
            val svc = Intent(this, BoostService::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc) else startService(svc)
        } catch (_: Throwable) {}

        setContent {
            ApexTheme {
                var screen by remember { mutableStateOf("home") }
                var cpu by remember { mutableIntStateOf(0) }
                var ram by remember { mutableIntStateOf(0) }
                var temp by remember { mutableFloatStateOf(-1f) }
                var fps by remember { mutableIntStateOf(0) }
                var applying by remember { mutableStateOf(false) }
                var applyMsg by remember { mutableStateOf<String?>(null) }
                var detectedGame by remember { mutableStateOf<String?>(null) }
                var sessionTime by remember { mutableStateOf("") }
                val fpsHist = remember { mutableStateListOf<Float>() }
                val ramHist = remember { mutableStateListOf<Float>() }
                val tempHist = remember { mutableStateListOf<Float>() }
                val busy = remember { mutableStateMapOf<String, Boolean>() }
                val szState by shizuku.state.collectAsState()
                val applied by engine.applied.collectAsState()
                val progress by engine.progress.collectAsState()
                val settings by settingsStore.settings.collectAsState()

                LaunchedEffect(Unit) {
                    while (true) {
                        cpu = monitor.cpu(); ram = monitor.ram(); temp = monitor.temp()
                        val (b, bt) = monitor.battery()
                        ramHist.add(ram.toFloat()); if (ramHist.size > 60) ramHist.removeAt(0)
                        if (temp > 0) { tempHist.add(temp); if (tempHist.size > 60) tempHist.removeAt(0) }
                        delay(2000)
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
                        delay(2000)
                    }
                }

                LaunchedEffect(szState) {
                    while (true) {
                        if (szState == ShizukuManager.State.Ready) {
                            val pkg = monitor.currentForegroundPackage()?.takeIf { p ->
                                TweakDatabase.GAMES.any { it.pkg == p }
                            }
                            if (pkg != detectedGame) {
                                if (detectedGame != null) session.finish()
                                detectedGame = pkg
                                if (pkg != null) session.start(pkg)
                            }
                        }
                        delay(3000)
                    }
                }

                // HÀM MỞ GAME TỰ ĐỘNG
                fun launchGame(pkg: String) {
                    val intent = packageManager.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        // Bật FPS Overlay khi vào game
                        if (settings.fpsOverlay) {
                            try {
                                val overlayIntent = Intent(this@MainActivity, com.apex.overdrive.service.FpsOverlay::class.java)
                                if (Build.VERSION.SDK_INT >= 26) startForegroundService(overlayIntent) else startService(overlayIntent)
                            } catch (_: Throwable) {}
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Game chưa được cài đặt!", Toast.LENGTH_SHORT).show()
                    }
                }

                fun handleShizuku() = when (szState) {
                    ShizukuManager.State.PermissionRequired -> shizuku.requestPermission()
                    ShizukuManager.State.NotRunning -> shizuku.openApp()
                    ShizukuManager.State.NotInstalled -> shizuku.openDownload()
                    else -> {}
                }

                suspend fun applyMode(id: String) {
                    applying = true; applyMsg = null
                    engine.saveSnapshot("Before $id")
                    val r = if (id == "restore") engine.restoreAll()
                    else engine.applyBatch(TweakDatabase.forMode(id, ShellExecutor.hasRoot()))
                    applyMsg = "✓ ${r.first} OK · ✗ ${r.second} fail"
                    applying = false
                }

                suspend fun boostGameAndLaunch(pkg: String) {
                    applying = true; applyMsg = null
                    engine.saveSnapshot("Before boost $pkg")
                    // Áp dụng tinh chỉnh
                    engine.boostGame(pkg)
                    applyMsg = "🎮 Đang mở ${TweakDatabase.gameByName(pkg)?.name ?: pkg}..."
                    applying = false
                    // Mở game
                    launchGame(pkg)
                }

                suspend fun restoreAllNow() {
                    applying = true; applyMsg = null
                    val r = engine.restoreAll()
                    applyMsg = "✓ Đã khôi phục ${r.first} tinh chỉnh"
                    applying = false
                }

                AnimatedContent(targetState = screen, label = "nav") { s ->
                    when (s) {
                        "home" -> HomeScreen(cpu, ram, temp, fps, fpsHist.toList(), szState,
                            applied.size, detectedGame, sessionTime,
                            onNavigate = { screen = it },
                            onShizukuAction = { handleShizuku() },
                            onRestoreAll = { lifecycleScope.launch { restoreAllNow() } },
                            onLaunchGame = { pkg -> lifecycleScope.launch { boostGameAndLaunch(pkg) } } // Truyền hàm mở game
                        )
                        "modes" -> ModesScreen(szState, applying, applyMsg, progress, applied.size, onBack = { screen = "home" }, onApply = { m -> lifecycleScope.launch { applyMode(m) } }, onRestoreAll = { lifecycleScope.launch { restoreAllNow() } })
                        "ff" -> FFBoostScreen(szState, applied, applying, applyMsg, progress, onBack = { screen = "home" }, onBoostGame = { pkg -> lifecycleScope.launch { boostGameAndLaunch(pkg) } }, onUnboostGame = {})
                        "deep" -> DeepTuneScreen(szState, applied, busy.keys, applied.size, onBack = { screen = "home" }, onToggle = { t, on -> }, onRestoreAll = { lifecycleScope.launch { restoreAllNow() } })
                        "settings" -> SettingsScreen(settings, onUpdate = { b -> settingsStore.update(b) }, onExport = {}, onImport = {}, onBack = { screen = "home" })
                        "terminal" -> TerminalScreen(szState, onBack = { screen = "home" })
                        else -> HomeScreen(cpu, ram, temp, fps, fpsHist.toList(), szState, applied.size, detectedGame, sessionTime, { screen = it }, { handleShizuku() }, { lifecycleScope.launch { restoreAllNow() } }, { pkg -> lifecycleScope.launch { boostGameAndLaunch(pkg) } })
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

    override fun onDestroy() {
        thermal.stop(); session.finish(); shizuku.unregister(); super.onDestroy()
    }
}
