package com.apex.overdrive.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.apex.overdrive.MainActivity
import com.apex.overdrive.R
import com.apex.overdrive.core.*
import kotlinx.coroutines.*

class ApexWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { mgr.updateAppWidget(it, buildViews(context)) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "APEX_BOOST_ACTION") {
            val mode = intent.getStringExtra("mode") ?: "turbo"
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    TweakEngine(context).applyBatch(
                        TweakDatabase.forMode(mode, ShellExecutor.hasRoot())
                    )
                } catch (_: Throwable) {}
            }
        }
    }

    private fun buildViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.apex_widget)
        views.setOnClickPendingIntent(
            R.id.widget_boost,
            PendingIntent.getBroadcast(
                context, 0,
                Intent(context, ApexWidget::class.java).apply {
                    action = "APEX_BOOST_ACTION"
                    putExtra("mode", "turbo")
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        views.setOnClickPendingIntent(
            R.id.widget_open,
            PendingIntent.getActivity(
                context, 1,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        return views
    }
}
