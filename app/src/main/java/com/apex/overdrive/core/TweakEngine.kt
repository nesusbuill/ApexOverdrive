package com.apex.overdrive.core

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class Progress(val current: Int = 0, val total: Int = 0, val currentName: String = "")

class TweakEngine(context: Context) {
    private val prefs = context.getSharedPreferences("apex_state_v6", Context.MODE_PRIVATE)
    private val _applied = MutableStateFlow<Set<String>>(loadApplied())
    val applied: StateFlow<Set<String>> = _applied.asStateFlow()
    private val _progress = MutableStateFlow(Progress())
    val progress: StateFlow<Progress> = _progress.asStateFlow()

    private fun loadApplied(): Set<String> = try {
        val arr = JSONArray(prefs.getString("applied_json", "[]") ?: "[]")
        (0 until arr.length()).map { arr.getString(it) }.toSet()
    } catch (_: Throwable) { emptySet() }

    private fun persist(set: Set<String>) {
        prefs.edit().putString("applied_json",
            JSONArray().apply { set.forEach { put(it) } }.toString()).apply()
        _applied.value = set
    }

    private fun saveRestore(id: String, cmd: String?) {
        if (cmd == null) return
        try {
            val obj = JSONObject(prefs.getString("restores_json", "{}") ?: "{}").put(id, cmd)
            prefs.edit().putString("restores_json", obj.toString()).apply()
        } catch (_: Throwable) {}
    }

    private fun getRestore(id: String): String? = try {
        JSONObject(prefs.getString("restores_json", "{}") ?: "{}")
            .optString(id, "").ifBlank { null }
    } catch (_: Throwable) { null }

    private suspend fun execWithRetry(cmd: String, needRoot: Boolean, retries: Int = 3): ShellExecutor.Result {
        var last = ShellExecutor.Result("", -1)
        repeat(retries) { i ->
            last = ShellExecutor.executeSmart(cmd, needRoot, 20)
            if (last.ok) return last
            if (i < retries - 1) delay(500)
        }
        return last
    }

    suspend fun enable(t: Tweak): ShellExecutor.Result = withContext(Dispatchers.IO) {
        val r = execWithRetry(t.command, t.needsRoot)
        if (r.ok) { saveRestore(t.id, t.restore); persist(_applied.value + t.id) }
        r
    }

    suspend fun disable(t: Tweak): ShellExecutor.Result = withContext(Dispatchers.IO) {
        val restore = getRestore(t.id) ?: t.restore
        val r = if (restore != null) execWithRetry(restore, t.needsRoot)
                else ShellExecutor.Result("INFO: No restore", 0)
        if (restore == null || r.ok) persist(_applied.value - t.id)
        r
    }

    suspend fun applyBatch(tweaks: List<Tweak>): Pair<Int, Int> = withContext(Dispatchers.IO) {
        var ok = 0; var fail = 0; var cur = _applied.value
        _progress.value = Progress(0, tweaks.size, "Starting...")
        tweaks.forEachIndexed { i, t ->
            _progress.value = Progress(i + 1, tweaks.size, t.name)
            val r = execWithRetry(t.command, t.needsRoot)
            if (r.ok) { saveRestore(t.id, t.restore); cur = cur + t.id; ok++ } else fail++
        }
        persist(cur)
        _progress.value = Progress()
        ok to fail
    }

    suspend fun restoreAll(): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val all = _applied.value; var ok = 0; var fail = 0; var cur = _applied.value
        _progress.value = Progress(0, all.size, "Restoring...")
        all.forEachIndexed { i, id ->
            TweakDatabase.byId(id)?.let { t ->
                _progress.value = Progress(i + 1, all.size, "Restore ${t.name}")
                getRestore(id)?.let { if (execWithRetry(it, t.needsRoot).ok) ok++ else fail++ }
            }
            cur = cur - id
        }
        persist(cur)
        _progress.value = Progress()
        ok to fail
    }

    fun saveSnapshot(label: String): Boolean = try {
        val arr = JSONArray(prefs.getString("snapshots", "[]") ?: "[]")
        val snap = JSONObject().apply {
            put("id", System.currentTimeMillis())
            put("label", label)
            put("applied", JSONArray().apply { _applied.value.forEach { put(it) } })
            put("restores", prefs.getString("restores_json", "{}"))
        }
        val newArr = JSONArray().apply { put(snap) }
        for (i in 0 until minOf(arr.length(), 9)) newArr.put(arr.getJSONObject(i))
        prefs.edit().putString("snapshots", newArr.toString()).apply()
        true
    } catch (_: Throwable) { false }

    suspend fun boostGame(pkg: String): Pair<Int, Int> =
        applyBatch(TweakDatabase.gameBoost(pkg) +
            TweakDatabase.SYSTEM_TWEAKS + TweakDatabase.TOUCH_TWEAKS.take(3))

    suspend fun unboostGame(pkg: String): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val g = TweakDatabase.gameBoost(pkg)
        var ok = 0; var fail = 0; var cur = _applied.value
        g.forEach { t ->
            getRestore(t.id)?.let { if (execWithRetry(it, false).ok) ok++ else fail++ }
            cur = cur - t.id
        }
        persist(cur); ok to fail
    }
}
