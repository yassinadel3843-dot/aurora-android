package com.aurora.workspace.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import android.view.View
import com.aurora.workspace.R
import com.aurora.workspace.util.colorAttr
import com.aurora.workspace.util.dp

/**
 * Initials avatar with a subtle vertical gradient, an optional accent ring,
 * and an online dot. No external image loading required.
 */
class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.dp(2f).toFloat()
        color = context.colorAttr(R.attr.auroraAccent)
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF37D67A.toInt() }
    private val dotBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.colorAttr(R.attr.auroraBg1)
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.plexarabic_medium)
                ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private var displayName = "A"
    private var baseColor = context.colorAttr(R.attr.auroraAccent)
    private var showRing = false
    private var showOnline = false
    private var gradient: Shader? = null

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.AvatarView)
            displayName = a.getString(R.styleable.AvatarView_avatarName) ?: displayName
            baseColor = a.getColor(R.styleable.AvatarView_avatarColor, baseColor)
            showRing = a.getBoolean(R.styleable.AvatarView_avatarRing, false)
            a.recycle()
        }
        updateGradient()
    }

    fun bind(name: String, color: Int, online: Boolean = false) {
        displayName = name
        baseColor = color
        showOnline = online
        updateGradient()
        invalidate()
    }

    fun setInitials(initials: String, color: Int) {
        displayName = initials
        baseColor = color
        updateGradient()
        invalidate()
    }

    fun setOnline(online: Boolean) {
        showOnline = online
        invalidate()
    }

    fun setRing(enabled: Boolean) {
        showRing = enabled
        invalidate()
    }

    private fun updateGradient() {
        val dark = darken(baseColor, 0.62f)
        gradient = LinearGradient(
            0f, 0f, 0f, dp(64f).toFloat(),
            baseColor, dark, Shader.TileMode.CLAMP
        )
    }

    private fun darken(color: Int, factor: Float): Int {
        val r = ((color shr 16 and 0xFF) * factor).toInt().coerceIn(0, 255)
        val g = ((color shr 8 and 0xFF) * factor).toInt().coerceIn(0, 255)
        val b = ((color and 0xFF) * factor).toInt().coerceIn(0, 255)
        return 0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
    }

    private fun initialsOf(name: String): String {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "A"
            parts.size == 1 -> parts[0].take(1).uppercase()
            else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        textPaint.textSize = measuredHeight * 0.38f
        updateGradient()
    }

    override fun onDraw(canvas: Canvas) {
        val size = minOf(width, height).toFloat()
        val cx = width / 2f
        val cy = height / 2f
        val radius = size / 2f - (if (showRing) ringPaint.strokeWidth else 0f) / 2f - dp(1f)

        fillPaint.shader = gradient
        canvas.drawCircle(cx, cy, radius, fillPaint)

        if (showRing) {
            canvas.drawCircle(cx, cy, radius + ringPaint.strokeWidth / 2f, ringPaint)
        }

        val baseline = cy - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(initialsOf(displayName), cx, baseline, textPaint)

        if (showOnline) {
            val dotRadius = size * 0.13f
            val dx = cx + radius * 0.66f
            val dy = cy + radius * 0.66f
            canvas.drawCircle(dx, dy, dotRadius + dp(3f), dotBorder)
            canvas.drawCircle(dx, dy, dotRadius, dotPaint)
        }
    }
}
