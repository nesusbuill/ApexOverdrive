package com.apex.overdrive.core

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.apex.overdrive.BuildConfig
import java.security.MessageDigest

object IntegrityCheck {
    data class Result(val passed: Boolean, val reason: String = "")

    private val EXPECTED_SIG_HASH: String
        get() = try {
            val f = BuildConfig::class.java.getField("APEX_SIG_HASH")
            f.get(null) as? String ?: "0000000000000000"
        } catch (_: Throwable) { "0000000000000000" }

    fun verify(context: Context): Result {
        // ⚠️ ĐÃ TẮT KIỂM TRA DEBUGGER VÀ FRIDA ĐỂ TRÁNH BẮT NHẦM TRÊN MÁY THẬT
        // if (NativeBridge.isDebuggerAttached()) return Result(false, "Debugger detected")
        // if (NativeBridge.isFridaDetected()) return Result(false, "Frida detected")
        
        // Chỉ kiểm tra xem APK có bị sửa (đổi Sign) hay không
        val sig = getSigHash(context) ?: return Result(false, "No signature")
        val expected = EXPECTED_SIG_HASH
        
        // Nếu là bản build từ GitHub, expected sẽ là "0000000000000000" (bỏ qua check)
        if (expected != "0000000000000000" && !NativeBridge.verifyIntegrity(sig))
            return Result(false, "APK signature mismatch")
            
        return Result(true)
    }

    private fun getSigHash(context: Context): String? {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName
            val sigs = if (Build.VERSION.SDK_INT >= 28) {
                pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES).signatures
            }
            if (sigs.isNullOrEmpty()) return null
            val md = MessageDigest.getInstance("SHA-256")
            md.update(sigs[0].toByteArray())
            md.digest().take(8).joinToString("") { "%02x".format(it) }
        } catch (_: Throwable) { null }
    }
}
