package com.aurora.workspace.theme

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Full Arabic / English support with automatic RTL layout direction.
 * The chosen language is persisted and applied through attachBaseContext,
 * so every screen (including dialogs) is rendered in the right direction.
 */
object LocaleHelper {

    private const val PREFS = "aurora_ui"
    private const val KEY_LANG = "language"

    const val SYSTEM = "system"
    const val ENGLISH = "en"
    const val ARABIC = "ar"

    private fun prefs(context: Context) =
        // During Application.attachBaseContext the application context is not
        // attached yet, so fall back to the base context we were given.
        (context.applicationContext ?: context)
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun current(context: Context): String =
        prefs(context).getString(KEY_LANG, SYSTEM) ?: SYSTEM

    fun set(context: Context, language: String) {
        prefs(context).edit().putString(KEY_LANG, language).apply()
    }

    fun wrap(base: Context): Context {
        val language = current(base)
        val locale = when (language) {
            ARABIC -> Locale.forLanguageTag("ar")
            ENGLISH -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }
}
