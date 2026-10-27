package com.apex.overdrive.core

import kotlinx.coroutines.delay

object FpsReader {
    suspend fun readFps(pkg: String): Int = try {
        val r1 = ShellExecutor.execute("dumpsys gfxinfo $pkg | grep 'Total frames'")
        if (!r1.ok) return 0
        val f1 = parseTotal(r1.output) ?: return 0
        delay(1000)
        val r2 = ShellExecutor.execute("dumpsys gfxinfo $pkg | grep 'Total frames'")
        if (!r2.ok) return 0
        val f2 = parseTotal(r2.output) ?: return 0
        (f2 - f1).coerceIn(0, 240)
    } catch (_: Throwable) { 0 }

    suspend fun readJank(pkg: String): Float = try {
        val r = ShellExecutor.execute("dumpsys gfxinfo $pkg | grep 'Janky frames'")
        if (!r.ok) return -1f
        Regex("""\((\d+\.?\d*)%\)""").find(r.output)
            ?.groupValues?.getOrNull(1)?.toFloatOrNull() ?: -1f
    } catch (_: Throwable) { -1f }

    suspend fun snapshot(pkg: String): Pair<Int, Float> = readFps(pkg) to readJank(pkg)

    private fun parseTotal(text: String): Int? =
        Regex("""Total frames rendered:\s*(\d+)""").find(text)
            ?.groupValues?.getOrNull(1)?.toIntOrNull()
}
