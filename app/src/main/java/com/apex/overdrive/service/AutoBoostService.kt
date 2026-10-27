package com.apex.overdrive.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.apex.overdrive.core.*
import kotlinx.coroutines.*

class AutoBoostService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var monitor: SystemMonitor
    private lateinit var engine: TweakEngine
    private lateinit var settingsStore: SettingsStore
    private lateinit var gameLib: GameLibrary
    private lateinit var session: GameSession
    private var boostedPkg: String? = null
    private var savedBrightness: Int = -1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        monitor = SystemMonitor(this)
        engine = TweakEngine(this)
        settingsStore = SettingsStore(this)
        gameLib = GameLibrary(this)
        session = GameSession(this)
        createChannel()
        startForeground(2001, buildNotif("Auto-boost monitoring..."))
        scope.launch { watchLoop() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    private suspend fun watchLoop() {
        while (currentCoroutineContext().isActive) {
            try {
                val cfg = settingsStore.settings.value
                if (!cfg.autoBoost) { delay(3000); continue }
                val fg = monitor.currentForegroundPackage()
                val tracked = gameLib.tracked()
                if (fg != null && fg in tracked && fg != boostedPkg) {
                    if (boostedPkg != null) restoreAll()
                    onGameStart(fg, cfg)
                } else if ((fg == null || fg !in tracked) && boostedPkg != null) {
                    delay(3000)
                    val still = monitor.currentForegroundPackage()
                    if (still == null || still !in tracked) onGameEnd()
                }
            } catch (_: Throwable) {}
            delay(3000)
        }
    }

    private suspend fun onGameStart(pkg: String, cfg: AppSettings) {
        boostedPkg = pkg
        session.start(pkg)
        if (cfg.safeMode) engine.applyBatch(TweakDatabase.gameBoost(pkg).filter { !it.risky })
        else engine.boostGame(pkg)
        if (cfg.autoDnd) ShellExecutor.execute("settings put global zen_mode 1")
        if (cfg.autoBrightness) {
            savedBrightness = ShellExecutor.execute("settings get system screen_brightness")
                .output.trim().toIntOrNull() ?: -1
            ShellExecutor.execute("settings put system screen_brightness 255")
        }
        if (cfg.fpsOverlay) try {
            val i = Intent(this, FpsOverlay::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
        } catch (_: Throwable) {}
        updateNotif("Playing: ${TweakDatabase.gameByName(pkg)?.name ?: pkg} · Boosted")
    }

    private suspend fun onGameEnd() {
        session.finish()
        restoreAll()
        boostedPkg = null
        updateNotif("Auto-boost monitoring...")
    }

    private suspend fun restoreAll() {
        val cfg = settingsStore.settings.value
        if (cfg.autoRestore) engine.restoreAll()
        if (cfg.autoDnd) ShellExecutor.execute("settings put global zen_mode 0")
        if (cfg.autoBrightness && savedBrightness >= 0) {
            ShellExecutor.execute("settings put system screen_brightness $savedBrightness")
            savedBrightness = -1
        }
        if (cfg.fpsOverlay) try { stopService(Intent(this, FpsOverlay::class.java)) } catch (_: Throwable) {}
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel("apex_auto", "Auto Boost", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotif(text: String): Notification =
        NotificationCompat.Builder(this, "apex_auto")
            .setContentTitle("APEX AUTO-BOOST")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()

    private fun updateNotif(text: String) {
        try {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .notify(2001, buildNotif(text))
        } catch (_: Throwable) {}
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
