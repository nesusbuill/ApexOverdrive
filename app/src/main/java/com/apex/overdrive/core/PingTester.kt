package com.apex.overdrive.core

object PingTester {
    data class Result(
        val host: String,
        val avgMs: Int,
        val minMs: Int,
        val maxMs: Int,
        val lossPct: Float
    )

    suspend fun test(host: String, count: Int = 10): Result {
        val r = ShellExecutor.execute("ping -c $count -W 2 $host", 30)
        if (!r.ok) return Result(host, -1, -1, -1, 100f)
        val times = Regex("""time=(\d+\.?\d*)""").findAll(r.output)
            .mapNotNull { it.groupValues[1].toFloatOrNull() }.toList()
        val loss = Regex("""(\d+)% packet loss""").find(r.output)
            ?.groupValues?.getOrNull(1)?.toFloatOrNull() ?: 0f
        if (times.isEmpty()) return Result(host, -1, -1, -1, loss)
        return Result(
            host = host,
            avgMs = times.average().toInt(),
            minMs = times.min().toInt(),
            maxMs = times.max().toInt(),
            lossPct = loss
        )
    }
}
