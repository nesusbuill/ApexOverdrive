package com.apex.overdrive.core

object NetworkOptimizer {
    data class PingResult(val pingMs: Int, val jitterMs: Int)

    suspend fun measurePing(): PingResult {
        val r = ShellExecutor.execute("ping -c 5 -W 2 8.8.8.8", 15)
        if (!r.ok) return PingResult(-1, -1)
        
        val times = Regex("""time=(\d+\.?\d*)""").findAll(r.output)
            .mapNotNull { it.groupValues[1].toFloatOrNull() }.toList()
            
        if (times.isEmpty()) return PingResult(-1, -1)
        val avg = times.average().toInt()
        val max = times.maxOrNull() ?: 0f
        val min = times.minOrNull() ?: 0f
        val jitter = (max - min).toInt()
        
        return PingResult(avg, jitter)
    }
}
