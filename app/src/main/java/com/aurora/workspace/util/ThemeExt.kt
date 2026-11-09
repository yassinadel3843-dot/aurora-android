package com.aurora.workspace.util

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes

/** Resolves a theme attribute (custom aurora colors) to a real ARGB int. */
fun Context.colorAttr(@AttrRes attr: Int): Int {
    val value = TypedValue()
    return if (theme.resolveAttribute(attr, value, true)) value.data else 0xFF4DA3FF.toInt()
}

fun Context.boolAttr(@AttrRes attr: Int): Boolean {
    val value = TypedValue()
    return theme.resolveAttribute(attr, value, true) && value.data != 0
}

fun Context.dp(value: Float): Int =
    (value * resources.displayMetrics.density + 0.5f).toInt()

fun View.dp(value: Float): Int = context.dp(value)

fun Int.withAlpha(alpha: Int): Int =
    (this and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)
