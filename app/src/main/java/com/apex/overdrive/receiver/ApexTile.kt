package com.apex.overdrive.receiver

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.apex.overdrive.core.*
import kotlinx.coroutines.*

@RequiresApi(Build.VERSION_CODES.N)
class ApexTile : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        val mode = SettingsStore(this).settings.value.defaultMode
        qsTile?.state = Tile.STATE_ACTIVE
        qsTile?.label = "Boosting..."
        qsTile?.updateTile()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                TweakEngine(applicationContext).applyBatch(
                    TweakDatabase.forMode(mode, ShellExecutor.hasRoot())
                )
            } catch (_: Throwable) {}
            withContext(Dispatchers.Main) {
                qsTile?.label = "Apex Boost"
                qsTile?.state = Tile.STATE_ACTIVE
                qsTile?.updateTile()
            }
        }
    }

    private fun updateTile() {
        val sz = ShizukuManager(applicationContext)
        sz.register()
        val ready = sz.state.value == ShizukuManager.State.Ready
        qsTile?.apply {
            state = if (ready) Tile.STATE_INACTIVE else Tile.STATE_UNAVAILABLE
            label = if (ready) "Apex Boost" else "Shizuku off"
            updateTile()
        }
    }
}
