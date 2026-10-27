package com.apex.overdrive.service

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import com.apex.overdrive.core.SystemMonitor
import kotlinx.coroutines.*

class FloatingHud : Service() {
    private lateinit var wm: WindowManager
    private lateinit var view: TextView
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val monitor = SystemMonitor()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        view = TextView(this).apply {
            setTextColor(Color.parseColor("#00D9FF"))
            textSize = 13f
            setPadding(24, 12, 24, 12)
            setBackgroundColor(Color.parseColor("#CC0A0E1A"))
            text = "Temp: --"
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 20; y = 150
        }
        try { wm.addView(view, lp) } catch (_: Throwable) {}

        scope.launch {
            while (isActive) {
                val t = monitor.temp()
                view.text = "Temp: ${if (t > 0) t.toInt() else "--"}°C"
                delay(2000)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        try { if (::view.isInitialized) wm.removeView(view) } catch (_: Throwable) {}
        super.onDestroy()
    }
}
