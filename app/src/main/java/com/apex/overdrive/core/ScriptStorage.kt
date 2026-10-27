package com.apex.overdrive.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ScriptStorage(context: Context) {
    private val prefs = context.getSharedPreferences("apex_scripts", Context.MODE_PRIVATE)

    data class Script(val id: Long, val name: String, val command: String)

    fun load(): List<Script> = try {
        val arr = JSONArray(prefs.getString("scripts", "[]") ?: "[]")
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Script(o.getLong("id"), o.getString("name"), o.getString("command"))
        }
    } catch (_: Throwable) { emptyList() }

    fun save(list: List<Script>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id); put("name", it.name); put("command", it.command)
            })
        }
        prefs.edit().putString("scripts", arr.toString()).apply()
    }

    fun add(name: String, cmd: String): List<Script> {
        val l = load().toMutableList()
        l.add(Script(System.currentTimeMillis(), name, cmd))
        save(l); return l
    }

    fun remove(id: Long): List<Script> {
        val l = load().filter { it.id != id }
        save(l); return l
    }
}
