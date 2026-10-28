package com.apex.overdrive.core

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.File
import java.util.concurrent.TimeUnit

object ShellExecutor {
    data class Result(val output: String, val exitCode: Int) {
        val ok: Boolean get() = exitCode == 0
    }

    private var rootCache: Boolean? = null

    fun hasRoot(): Boolean {
        rootCache?.let { return it }
        val paths = listOf("/system/bin/su", "/system/xbin/su", "/sbin/su", "/data/local/xbin/su", "/su/bin/su")
        rootCache = paths.any { File(it).exists() }
        return rootCache!!
    }

    suspend fun execute(cmd: String, timeoutSec: Int = 15): Result = withContext(Dispatchers.IO) {
        if (!Shizuku.pingBinder()) return@withContext Result("ERROR: Shizuku not running", -1)
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
            return@withContext Result("ERROR: Shizuku permission denied", -1)
        runProc(arrayOf("sh", "-c", cmd), timeoutSec)
    }

    suspend fun executeRoot(cmd: String, timeoutSec: Int = 15): Result = withContext(Dispatchers.IO) {
        if (!hasRoot()) return@withContext Result("ERROR: No root access", -1)
        runProc(arrayOf("su", "-c", cmd), timeoutSec)
    }

    suspend fun executeSmart(cmd: String, needRoot: Boolean = false, timeoutSec: Int = 15): Result =
        if (needRoot) {
            if (hasRoot()) executeRoot(cmd, timeoutSec) else Result("ERROR: Root required", -1)
        } else execute(cmd, timeoutSec)

    private suspend fun runProc(argv: Array<String>, timeoutSec: Int): Result {
        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java
            )
            method.isAccessible = true
            @Suppress("DEPRECATION")
            val process = method.invoke(null, argv, null, null) as ShizukuRemoteProcess
            
            val stdout = StringBuilder()
            val stderr = StringBuilder()
            
            val t1 = Thread {
                try { process.inputStream.bufferedReader().forEachLine { stdout.appendLine(it) } } catch (_: Throwable) {}
            }
            val t2 = Thread {
                try { process.errorStream.bufferedReader().forEachLine { stderr.appendLine(it) } } catch (_: Throwable) {}
            }
            
            t1.start()
            t2.start()
            
            val exited = process.waitFor(timeoutSec.toLong(), TimeUnit.SECONDS)
            if (!exited) {
                try { process.destroy() } catch (_: Throwable) {}
                return Result("TIMEOUT after ${timeoutSec}s", -1)
            }
            
            t1.join(2000)
            t2.join(2000)
            
            val exitCode = process.exitValue()
            try { process.destroy() } catch (_: Throwable) {}
            
            val finalOutput = buildString {
                if (stdout.isNotBlank()) append(stdout.toString().trim())
                if (stderr.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append("⚠ ").append(stderr.toString().trim())
                }
                if (isEmpty()) append("✅ Lệnh chạy thành công (không có kết quả trả về).")
            }
            Result(finalOutput, exitCode)
        } catch (e: Throwable) {
            Result("❌ Lỗi thực thi: ${e.message}", -1)
        }
    }
}
