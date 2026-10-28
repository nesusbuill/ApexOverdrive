package com.apex.overdrive

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
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

        // 1. Ép xoay ngang
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        // 2. Ẩn thanh hệ thống
        hideSystemUI()

        // Anti-Tamper Check
        if (!BuildConfig.DEBUG) {
            val integrity = IntegrityCheck.verify(this)
            if (!integrity.passed) {
                AlertDialog.Builder(this)
                    .setTitle("⚠ SECURITY")
                    .setMessage("APK has been modified.\nReason: ${integrity.reason}\n\nPlease reinstall from official source.")
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

        if (Build.VERSION.SDK_INT >= 33)
            notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)

        try {
            val svc = Intent(this, BoostService::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(svc) else startService(svc)
        } catch (_: Throwable) {}
        try {
            val auto = Intent(this, AutoBoostService::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(auto) else startService(auto)
        } catch (_: Throwable) {}

        setContent {
            ApexTheme {
                var screen by remember { mutableStateOf("home") }
                var cpu by remember { mutableIntStateOf(0) }
                var ram by remember { mutableIntStateOf(0) }
                var temp by remember { mutableFloatStateOf(-1f) }
                var fps by remember { mutableIntStateOf(0) }
                var gpuMhz by remember { mutableIntStateOf(-1) }
                var battery by remember { mutableIntStateOf(0) }
                var batteryTemp by remember { mutableFloatStateOf(0f) }
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
                        gpuMhz = monitor.gpuFreqMhz()
                        val (b, bt) = monitor.battery()
                        battery = b; batteryTemp = bt
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

                suspend fun toggleOne(t: Tweak, on: Boolean) {
                    busy[t.id] = true
                    val r = if (on) engine.enable(t) else engine.disable(t)
                    busy.remove(t.id)
                    applyMsg = if (r.ok) "✓ ${t.name}" else "✗ ${r.output.take(60)}"
                }

                suspend fun boostGame(pkg: String, ids: Set<String>? = null) {
                    applying = true; applyMsg = null
                    engine.saveSnapshot("Before boost $pkg")
                    val r = if (ids != null) engine.applyBatch(ids.mapNotNull { TweakDatabase.byId(it) })
                    else engine.boostGame(pkg)
                    applyMsg = "🎮 ${TweakDatabase.gameByName(pkg)?.name ?: pkg}: ${r.first} OK"
                    applying = false
                }

                suspend fun unboostGame(pkg: String) {
                    applying = true; applyMsg = null
                    val r = engine.unboostGame(pkg)
                    applyMsg = "↩ ${TweakDatabase.gameByName(pkg)?.name ?: pkg}: ${r.first} OK"
                    applying = false
                }

                suspend fun restoreAllNow() {
                    applying = true; applyMsg = null
                    val r = engine.restoreAll()
                    applyMsg = "✓ Restored ${r.first} · ✗ ${r.second} failed"
                    applying = false
                }

                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 })
                            .togetherWith(fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 4 })
                    },
                    label = "nav"
                ) { s ->
                    when (s) {
                        "home" -> HomeScreen(cpu, ram, temp, fps, fpsHist.toList(), szState,
                            applied.size, detectedGame, sessionTime,
                            onNavigate = { screen = it },
                            onShizukuAction = { handleShizuku() },
                            onRestoreAll = { lifecycleScope.launch { restoreAllNow() } })
                        "modes" -> ModesScreen(szState, applying, applyMsg, progress, applied.size,
                            onBack = { screen = "home" },
                            onApply = { m -> lifecycleScope.launch { applyMode(m) } },
                            onRestoreAll = { lifecycleScope.launch { restoreAllNow() } })
                        "ff" -> FFBoostScreen(szState, applied, applying, applyMsg, progress,
                            onBack = { screen = "home" },
                            onBoostGame = { pkg -> lifecycleScope.launch { boostGame(pkg) } },
                            onUnboostGame = { pkg -> lifecycleScope.launch { unboostGame(pkg) } })
                        "aimfix" -> AimFixScreen(szState, applied, busy.keys, applying, applyMsg, progress, applied.size,
                            onBack = { screen = "home" },
                            onToggle = { t, on -> lifecycleScope.launch { toggleOne(t, on) } },
                            onApplyPreset = { tweaks ->
                                lifecycleScope.launch {
                                    applying = true; applyMsg = null
                                    engine.saveSnapshot("Before aim fix")
                                    val r = engine.applyBatch(tweaks)
                                    applyMsg = "🎯 Aim fix: ${r.first} OK"
                                    applying = false
                                }
                            },
                            onRestoreAll = { lifecycleScope.launch { restoreAllNow() } })
                        "deep" -> DeepTuneScreen(szState, applied, busy.keys, applied.size,
                            onBack = { screen = "home" },
                            onToggle = { t, on -> lifecycleScope.launch { toggleOne(t, on) } },
                            onRestoreAll = { lifecycleScope.launch { restoreAllNow() } })
                        "profiles" -> ProfilesScreen(profileStore,
                            onBack = { screen = "home" },
                            onApplyProfile = { pkg, ids -> lifecycleScope.launch { boostGame(pkg, ids) } })
                        "benchmark" -> BenchmarkScreen(szState, detectedGame, onBack = { screen = "home" })
                        "cheatsheet" -> CheatsheetScreen(onBack = { screen = "home" })
                        "settings" -> SettingsScreen(settings,
                            onUpdate = { b -> settingsStore.update(b) },
                            onExport = {}, onImport = {}, onBack = { screen = "home" })
                        "history" -> HistoryScreen(session, onBack = { screen = "home" })
                        "library" -> GameLibraryScreen(gameLib, onBack = { screen = "home" })
                        "network" -> NetworkScreen(szState, onBack = { screen = "home" })
                        "tools" -> ToolsScreen(szState, onBack = { screen = "home" })
                        "stats" -> StatsScreen(cpu, ram, temp, fps, fpsHist.toList(), ramHist.toList(),
                            tempHist.toList(), battery, batteryTemp, gpuMhz, onBack = { screen = "home" })
                        "terminal" -> TerminalScreen(szState, onBack = { screen = "home" })
                        "about" -> AboutScreen(onBack = { screen = "home" })
                        else -> HomeScreen(cpu, ram, temp, fps, fpsHist.toList(), szState,
                            applied.size, detectedGame, sessionTime,
                            onNavigate = { screen = it },
                            onShizukuAction = { handleShizuku() },
                            onRestoreAll = { lifecycleScope.launch { restoreAllNow() } })
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
        thermal.stop()
        session.finish()
        shizuku.unregister()
        super.onDestroy()
    }
}
