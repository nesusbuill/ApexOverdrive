package com.apex.overdrive.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.apex.overdrive.MainActivity
import com.apex.overdrive.core.TweakEngine
import kotlinx.coroutines.*

class BoostService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val ch = "apex_boost"
        if (Build.VERSION.SDK_INT >= 26) {
            val c = NotificationChannel(ch, "APEX Boost", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(c)
        }

        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getBroadcast(
            this, 1,
            Intent(this, StopReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(this, ch)
            .setContentTitle("APEX OVERDRIVE")
            .setContentText("Boost running")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", stop)
            .build()

        startForeground(1001, notif)
        return START_STICKY
    }
}

class StopReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            try { TweakEngine(context).restoreAll() } catch (_: Throwable) {}
        }
        context.stopService(Intent(context, BoostService::class.java))
    }
}
