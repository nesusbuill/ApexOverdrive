package com.apex.overdrive.service

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import com.apex.overdrive.core.*
import kotlinx.coroutines.*

class FpsOverlay : Service() {
    private lateinit var wm: WindowManager
    private lateinit var view: TextView
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val monitor = SystemMonitor()
    private var lastX = 0
    private var lastY = 0
    private var ltX = 0f
    private var ltY = 0f
    private var startTime = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        view = TextView(this).apply {
            setTextColor(Color.parseColor("#10B981"))
            textSize = 12f
            setPadding(18, 8, 18, 8)
            setBackgroundColor(Color.parseColor("#CC0A0E1A"))
            text = "FPS -- · --°C"
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30; y = 300
        }

        view.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = lp.x; lastY = lp.y
                    ltX = e.rawX; ltY = e.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    lp.x = lastX + (e.rawX - ltX).toInt()
                    lp.y = lastY + (e.rawY - ltY).toInt()
                    try { wm.updateViewLayout(view, lp) } catch (_: Throwable) {}
                    true
                }
                else -> false
            }
        }

        try { wm.addView(view, lp) } catch (_: Throwable) {}
        startTime = System.currentTimeMillis()

        scope.launch {
            while (isActive) {
                try {
                    val pkg = monitor.currentForegroundPackage()
                        ?.takeIf { p -> TweakDatabase.GAMES.any { it.pkg == p } }
                    if (pkg == null) {
                        view.text = "No game"
                        view.setTextColor(Color.parseColor("#8B94A8"))
                    } else {
                        val fps = FpsReader.readFps(pkg)
                        val t = monitor.temp()
                        val sec = (System.currentTimeMillis() - startTime) / 1000
                        val mm = sec / 60
                        val ss = sec % 60
                        view.text = "FPS $fps · ${if (t > 0) t.toInt() else "--"}°C · %02d:%02d".format(mm, ss)
                        view.setTextColor(when {
                            fps >= 60 -> Color.parseColor("#10B981")
                            fps >= 30 -> Color.parseColor("#FBBF24")
                            else -> Color.parseColor("#EF4444")
                        })
                    }
                } catch (_: Throwable) {}
                delay(1000)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        try { if (::view.isInitialized) wm.removeView(view) } catch (_: Throwable) {}
        super.onDestroy()
    }
}
