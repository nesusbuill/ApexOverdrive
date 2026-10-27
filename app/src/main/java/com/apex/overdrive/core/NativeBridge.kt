package com.apex.overdrive.core

object NativeBridge {
    init { System.loadLibrary("apexnative") }

    external fun getCommand(id: Int): String
    external fun isDebuggerAttached(): Boolean
    external fun isFridaDetected(): Boolean
    external fun verifyIntegrity(sigHash: String): Boolean

    object Id {
        const val GAME_DRIVER = 0
        const val GAME_MODE = 1
        const val COMPILE = 2
        const val REFRESH = 3
        const val MIUI_PK = 4
        const val ATHENA = 5
        const val HAPTIC = 6
        const val PHANTOM = 7
    }

    fun cmd(id: Int, suffix: String = ""): String = getCommand(id) + suffix
}
