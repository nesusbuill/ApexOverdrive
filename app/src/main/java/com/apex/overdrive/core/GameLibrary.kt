package com.apex.overdrive.core

import android.content.Context

class GameLibrary(context: Context) {
    private val prefs = context.getSharedPreferences("apex_library", Context.MODE_PRIVATE)

    fun tracked(): Set<String> = prefs.getStringSet("tracked", null)
        ?: TweakDatabase.GAMES.map { it.pkg }.toSet()

    fun toggle(pkg: String): Set<String> {
        val cur = tracked().toMutableSet()
        if (cur.contains(pkg)) cur.remove(pkg) else cur.add(pkg)
        prefs.edit().putStringSet("tracked", cur).apply()
        return cur
    }

    fun setAll(on: Boolean): Set<String> {
        val set = if (on) TweakDatabase.GAMES.map { it.pkg }.toSet() else emptySet()
        prefs.edit().putStringSet("tracked", set).apply()
        return set
    }
}
