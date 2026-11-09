package com.aurora.workspace.util

import android.content.Context
import com.aurora.workspace.R
import com.aurora.workspace.data.Store

/**
 * Maps a task priority key to its accent color and localized label.
 *
 * Priorities reuse the existing theme attributes so every one of the five
 * themes gets a sensible set of colors without adding new attributes:
 * LOW -> accent, MEDIUM -> accent2, HIGH -> danger, NONE -> dim text.
 */
object Priority {

    fun color(context: Context, key: String): Int = when (key) {
        Store.PRIORITY_LOW -> context.colorAttr(R.attr.auroraAccent)
        Store.PRIORITY_MEDIUM -> context.colorAttr(R.attr.auroraAccent2)
        Store.PRIORITY_HIGH -> context.colorAttr(R.attr.auroraDanger)
        else -> context.colorAttr(R.attr.auroraTextDim)
    }

    fun labelRes(key: String): Int = when (key) {
        Store.PRIORITY_LOW -> R.string.priority_low
        Store.PRIORITY_MEDIUM -> R.string.priority_medium
        Store.PRIORITY_HIGH -> R.string.priority_high
        else -> R.string.priority_none
    }
}
