package com.aurora.workspace.util

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * Screen-size adaptation helpers.
 *
 * Phones keep the exact layouts from v1.0.0. From 600dp smallest screen width
 * (large phones / tablets) the content switches to a centered fixed-width
 * column so cards, rows and the glass nav bar stop stretching across the
 * whole display. Nothing here changes colors, themes or the navigation
 * structure - it only caps widths.
 */
object ScreenFit {

    /** Smallest screen width (dp) at which tablet adaptation kicks in. */
    const val TABLET_MIN_DP = 600
}

/**
 * Caps this view's width to [maxWidthDp] (dp) and centers it inside its
 * parent whenever current screen width >= [ScreenFit.TABLET_MIN_DP] (covers
 * tablets and landscape phones). On portrait phones this is a no-op, so
 * layouts stay byte-identical to v1.0.0.
 */
fun View.fitCenteredColumn(maxWidthDp: Int) {
    if (resources.configuration.screenWidthDp < ScreenFit.TABLET_MIN_DP) return
    if (layoutParams != null) capColumn(maxWidthDp) else onFirstAttach { it.capColumn(maxWidthDp) }
}

private fun View.capColumn(maxWidthDp: Int) {
    val lp = layoutParams ?: return
    lp.width = dp(maxWidthDp.toFloat())
    when (lp) {
        is FrameLayout.LayoutParams -> lp.gravity = Gravity.CENTER_HORIZONTAL
        is LinearLayout.LayoutParams -> lp.gravity = Gravity.CENTER_HORIZONTAL
    }
    requestLayout()
}

private fun View.onFirstAttach(action: (View) -> Unit) {
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            removeOnAttachStateChangeListener(this)
            action(v)
        }

        override fun onViewDetachedFromWindow(v: View) {}
    })
}

/** Column count for list screens: 1 on phones, 2 on wide screens (inside
 *  the 720dp content column a third card would crowd its labels). */
fun ViewGroup.gridSpan(): Int =
    if (resources.configuration.screenWidthDp >= ScreenFit.TABLET_MIN_DP) 2 else 1
