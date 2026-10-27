package com.apex.overdrive.core

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("apex_profiles", Context.MODE_PRIVATE)

    fun saveProfile(pkg: String, ids: Set<String>) {
        try {
            val obj = JSONObject(prefs.getString("profiles", "{}") ?: "{}")
            obj.put(pkg, JSONArray().apply { ids.forEach { put(it) } })
            prefs.edit().putString("profiles", obj.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun loadProfile(pkg: String): Set<String> {
        return try {
            val obj = JSONObject(prefs.getString("profiles", "{}") ?: "{}")
            val arr = obj.optJSONArray(pkg) ?: return emptySet()
            (0 until arr.length()).map { arr.getString(it) }.toSet()
        } catch (_: Throwable) { emptySet() }
    }

    fun deleteProfile(pkg: String) {
        try {
            val obj = JSONObject(prefs.getString("profiles", "{}") ?: "{}")
            obj.remove(pkg)
            prefs.edit().putString("profiles", obj.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun listProfiles(): Map<String, Set<String>> {
        return try {
            val obj = JSONObject(prefs.getString("profiles", "{}") ?: "{}")
            obj.keys().asSequence().associateWith { k ->
                val arr = obj.getJSONArray(k)
                (0 until arr.length()).map { arr.getString(it) }.toSet()
            }
        } catch (_: Throwable) { emptyMap() }
    }
}
