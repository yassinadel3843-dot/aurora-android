package com.aurora.workspace.theme

import android.content.Context
import com.aurora.workspace.R

/**
 * Five hand-tuned visual themes. Every theme exposes the same set of custom
 * attributes (backgrounds, glass surfaces, strokes, accents) so the whole UI
 * re-skins instantly without duplicating a single layout.
 */
object ThemeManager {

    private const val PREFS = "aurora_ui"
    private const val KEY_THEME = "theme"

    const val NIGHT = "night"
    const val GLASS = "glass"
    const val DAY = "day"
    const val EMERALD = "emerald"
    const val SUNSET = "sunset"

    val keys = listOf(NIGHT, GLASS, DAY, EMERALD, SUNSET)

    private fun prefs(context: Context) =
        (context.applicationContext ?: context)
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun current(context: Context): String =
        prefs(context).getString(KEY_THEME, NIGHT) ?: NIGHT

    fun set(context: Context, key: String) {
        prefs(context).edit().putString(KEY_THEME, key).apply()
    }

    fun styleRes(context: Context): Int = when (current(context)) {
        GLASS -> R.style.Theme_Aurora_Glass
        DAY -> R.style.Theme_Aurora_Day
        EMERALD -> R.style.Theme_Aurora_Emerald
        SUNSET -> R.style.Theme_Aurora_Sunset
        else -> R.style.Theme_Aurora_NightSky
    }

    fun isLight(context: Context): Boolean = current(context) == DAY

    fun labelRes(key: String): Int = when (key) {
        GLASS -> R.string.theme_glass
        DAY -> R.string.theme_day
        EMERALD -> R.string.theme_emerald
        SUNSET -> R.string.theme_sunset
        else -> R.string.theme_night
    }
}
