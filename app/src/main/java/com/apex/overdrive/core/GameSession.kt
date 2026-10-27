package com.apex.overdrive.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class GameSession(private val context: Context) {
    private val prefs = context.getSharedPreferences("apex_sessions", Context.MODE_PRIVATE)
    private var current: Session? = null

    data class Sample(val t: Long, val fps: Int, val temp: Float, val ram: Int)

    data class Session(
        val pkg: String,
        val startedAt: Long,
        val samples: MutableList<Sample> = mutableListOf()
    ) {
        fun avgFps(): Int = samples.filter { it.fps > 0 }.map { it.fps }.average()
            .takeIf { !it.isNaN() }?.toInt() ?: 0
        fun maxTemp(): Float = samples.filter { it.temp > 0 }.maxOfOrNull { it.temp } ?: 0f
        fun minFps(): Int = samples.filter { it.fps > 0 }.minOfOrNull { it.fps } ?: 0
        fun durationMin(): Int = ((System.currentTimeMillis() - startedAt) / 60000L).toInt()
        fun dropCount(): Int = samples.count { it.fps in 1..30 }
    }

    fun start(pkg: String) {
        current = Session(pkg, System.currentTimeMillis())
    }

    fun sample(fps: Int, temp: Float, ram: Int) {
        current?.samples?.add(Sample(System.currentTimeMillis(), fps, temp, ram))
    }

    fun isActive(): Boolean = current != null
    fun currentPkg(): String? = current?.pkg

    fun finish(): Session? {
        val s = current ?: return null
        current = null
        if (s.samples.isEmpty()) return null
        try {
            val arr = JSONArray(prefs.getString("history", "[]") ?: "[]")
            val obj = JSONObject().apply {
                put("pkg", s.pkg)
                put("startedAt", s.startedAt)
                put("endedAt", System.currentTimeMillis())
                put("avgFps", s.avgFps())
                put("minFps", s.minFps())
                put("maxTemp", s.maxTemp())
                put("drops", s.dropCount())
                put("durationMin", s.durationMin())
            }
            val newArr = JSONArray().apply { put(obj) }
            for (i in 0 until minOf(arr.length(), 19)) newArr.put(arr.getJSONObject(i))
            prefs.edit().putString("history", newArr.toString()).apply()
        } catch (_: Throwable) {}
        return s
    }

    fun history(): List<JSONObject> = try {
        val arr = JSONArray(prefs.getString("history", "[]") ?: "[]")
        (0 until arr.length()).map { arr.getJSONObject(it) }
    } catch (_: Throwable) { emptyList() }
}
