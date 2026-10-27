package com.apex.overdrive.core

import android.content.Context
import android.os.BatteryManager
import java.io.File

class SystemMonitor(private val appContext: Context? = null) {
    private var lastTotal = 0L
    private var lastIdle = 0L

    fun cpu(): Int {
        return try {
            val line = File("/proc/stat").readLines()
                .firstOrNull { it.startsWith("cpu ") } ?: return 0
            val p = line.split(Regex("\\s+")).filter { it.isNotBlank() }
                .drop(1).mapNotNull { it.toLongOrNull() }
            if (p.size < 5) return 0
            val idle = p[3] + p[4]
            val total = p.sum()
            val dt = total - lastTotal
            val di = idle - lastIdle
            lastTotal = total; lastIdle = idle
            if (dt <= 0) 0 else (((dt - di) * 100) / dt).toInt().coerceIn(0, 100)
        } catch (_: Throwable) { 0 }
    }

    fun ram(): Int {
        return try {
            val lines = File("/proc/meminfo").readLines()
            val total = lines.firstOrNull { it.startsWith("MemTotal:") }
                ?.split(Regex("\\s+"))?.getOrNull(1)?.toLongOrNull() ?: return 0
            val avail = lines.firstOrNull { it.startsWith("MemAvailable:") }
                ?.split(Regex("\\s+"))?.getOrNull(1)?.toLongOrNull() ?: return 0
            if (total == 0L) 0
            else (((total - avail) * 100) / total).toInt().coerceIn(0, 100)
        } catch (_: Throwable) { 0 }
    }

    fun temp(): Float {
        return try {
            val zones = File("/sys/class/thermal")
                .listFiles { f -> f.name.startsWith("thermal_zone") } ?: return -1f
            var max = 0f
            zones.forEach { z ->
                val t = try {
                    File(z, "temp").readText().trim().toFloat()
                } catch (_: Throwable) { 0f }
                val c = if (t > 1000) t / 1000f else t
                if (c in 20f..110f && c > max) max = c
            }
            if (max > 0f) max else -1f
        } catch (_: Throwable) { -1f }
    }

    fun gpuFreqMhz(): Int {
        return try {
            listOf(
                "/sys/class/kgsl/kgsl-3d0/gpuclk",
                "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
                "/sys/class/devfreq/gpufreq/cur_freq"
            ).forEach { path ->
                val f = File(path)
                if (f.exists()) {
                    val v = f.readText().trim().toLongOrNull() ?: return@forEach
                    return (if (v > 10_000_000) v / 1_000_000 else v / 1000).toInt()
                }
            }
            -1
        } catch (_: Throwable) { -1 }
    }

    fun battery(): Pair<Int, Float> {
        val ctx = appContext ?: return 0 to 0f
        return try {
            val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val lvl = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val t = ctx.registerReceiver(null,
                android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
                ?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            lvl to (t / 10f)
        } catch (_: Throwable) { 0 to 0f }
    }

    suspend fun currentForegroundPackage(): String? {
        val r = ShellExecutor.execute("dumpsys window | grep mCurrentFocus")
        if (!r.ok) return null
        val regex = Regex("""u0\s+([\w.]+)/[\w.]+""")
        return regex.find(r.output)?.groupValues?.getOrNull(1)
    }
}
