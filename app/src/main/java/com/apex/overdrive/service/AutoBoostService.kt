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
    private var isBoosting = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        monitor = SystemMonitor(this)
        engine = TweakEngine(this)
        settingsStore = SettingsStore(this)
        createChannel()
        startForeground(2001, buildNotif("Zero-Touch đang theo dõi..."))
        scope.launch { watchLoop() }
    }

    private suspend fun watchLoop() {
        while (currentCoroutineContext().isActive) {
            try {
                val cfg = settingsStore.settings.value
                val currentPkg = monitor.currentForegroundPackage()
                val isGame = currentPkg != null && TweakDatabase.GAMES.any { it.pkg == currentPkg }

                if (!cfg.autoBoost) {
                    if (isBoosting) restoreAll(currentPkg)
                    delay(3000)
                    continue
                }

                if (isGame && !isBoosting) {
                    isBoosting = true
                    updateNotif("🎮 Đã phát hiện Game. Đang tối ưu...")
                    engine.boostGame(currentPkg!!)
                    ShellExecutor.execute("settings put global zen_mode 1")
                    
                    if (cfg.reduceGraphics && Build.VERSION.SDK_INT >= 31) {
                        ShellExecutor.execute("cmd game set-downscale-factor $currentPkg 0.75")
                    }
                } else if (!isGame && isBoosting) {
                    delay(2000)
                    val checkAgain = monitor.currentForegroundPackage()
                    if (checkAgain == null || TweakDatabase.GAMES.none { it.pkg == checkAgain }) {
                        restoreAll(currentPkg)
                    }
                }
            } catch (_: Throwable) {}
            delay(3000)
        }
    }

    private suspend fun restoreAll(lastPkg: String?) {
        isBoosting = false
        engine.restoreAll()
        ShellExecutor.execute("settings put global zen_mode 0")
        
        if (lastPkg != null && Build.VERSION.SDK_INT >= 31) {
            ShellExecutor.execute("cmd game set-downscale-factor $lastPkg 1.0")
        }
        
        updateNotif("Zero-Touch đang theo dõi...")
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel("apex_auto", "Auto Boost", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotif(text: String): Notification =
        NotificationCompat.Builder(this, "apex_auto")
            .setContentTitle("APEX ZERO-TOUCH").setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage).setOngoing(true).build()

    private fun updateNotif(text: String) {
        try { (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(2001, buildNotif(text)) } catch (_: Throwable) {}
    }

    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
