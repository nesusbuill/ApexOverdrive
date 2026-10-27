package com.apex.overdrive.core

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

class ShizukuManager(private val context: Context) {
    sealed class State {
        data object NotInstalled : State()
        data object NotRunning : State()
        data object PermissionRequired : State()
        data object Ready : State()
    }

    private val _state = MutableStateFlow<State>(State.NotInstalled)
    val state: StateFlow<State> = _state.asStateFlow()

    private val onBinder = Shizuku.OnBinderReceivedListener { refresh() }
    private val onDead = Shizuku.OnBinderDeadListener { refresh() }
    private val onPerm = Shizuku.OnRequestPermissionResultListener { _, _ -> refresh() }

    fun register() {
        try {
            Shizuku.addBinderReceivedListenerSticky(onBinder)
            Shizuku.addBinderDeadListener(onDead)
            Shizuku.addRequestPermissionResultListener(onPerm)
        } catch (_: Throwable) {}
        refresh()
    }

    fun unregister() {
        try {
            Shizuku.removeBinderReceivedListener(onBinder)
            Shizuku.removeBinderDeadListener(onDead)
            Shizuku.removeRequestPermissionResultListener(onPerm)
        } catch (_: Throwable) {}
    }

    fun refresh() {
        _state.value = try {
            when {
                !installed() -> State.NotInstalled
                !Shizuku.pingBinder() -> State.NotRunning
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> State.Ready
                else -> State.PermissionRequired
            }
        } catch (_: Throwable) { State.NotInstalled }
    }

    fun requestPermission() {
        try {
            if (Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                Shizuku.requestPermission(0)
        } catch (_: Throwable) {}
    }

    fun openApp() {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("moe.shizuku.privileged.api://")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (_: Throwable) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=moe.shizuku.privileged.api")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Throwable) {}
        }
    }

    fun openDownload() {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://github.com/RikkaApps/Shizuku/releases")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (_: Throwable) {}
    }

    private fun installed(): Boolean = try {
        context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
        true
    } catch (_: Throwable) { false }
}
