package com.aurora.workspace.util

import android.content.Context
import com.aurora.workspace.R
import com.aurora.workspace.theme.LocaleHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Turns a stored due label (seed data is written in English, e.g. "Nov 28")
 * into a string that follows the language the interface is currently using.
 */
object DateText {

    private val PATTERNS = listOf("MMM d, yyyy", "MMM d", "d MMM", "yyyy-MM-dd")

    fun due(context: Context, raw: String): String {
        val value = raw.trim()
        if (value.isEmpty()) return context.getString(R.string.due_soon)
        if (value.equals("soon", ignoreCase = true)) return context.getString(R.string.due_soon)

        val date = parse(value) ?: return value
        val locale = locale(context)
        val explicitYear = Regex("\\d{4}").containsMatchIn(value)
        val sameYear = SimpleDateFormat("yyyy", Locale.ENGLISH).format(date) ==
            SimpleDateFormat("yyyy", Locale.ENGLISH).format(Date())
        val pattern = if (explicitYear && !sameYear) "d MMM yyyy" else "d MMM"
        return SimpleDateFormat(pattern, locale).format(date)
    }

    private fun locale(context: Context): Locale = when (LocaleHelper.current(context)) {
        LocaleHelper.ARABIC -> Locale.forLanguageTag("ar")
        LocaleHelper.ENGLISH -> Locale.ENGLISH
        else -> Locale.getDefault()
    }

    private fun parse(value: String): Date? {
        for (pattern in PATTERNS) {
            try {
                val date = SimpleDateFormat(pattern, Locale.ENGLISH).parse(value)
                if (date != null) return date
            } catch (_: Exception) {
                // try the next pattern
            }
        }
        return null
    }
}
