package com.aurora.workspace.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import com.aurora.workspace.R
import com.aurora.workspace.util.boolAttr
import com.aurora.workspace.util.colorAttr
import com.aurora.workspace.util.dp

/**
 * A translucent surface with a hairline border and a soft top sheen — the
 * core visual element of Aurora's "liquid glass" language. Works on API 21
 * without RenderEffect.
 */
class GlassCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val clipPath = Path()

    private var corner = context.dp(24f).toFloat()
    private var fillColor = context.colorAttr(R.attr.auroraSurface)
    private var strokeColor = context.colorAttr(R.attr.auroraStroke)
    private var strong = false

    init {
        setWillNotDraw(false)
        isClickable = false

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.GlassCard)
            corner = a.getDimension(R.styleable.GlassCard_glassCorner, corner)
            strong = a.getBoolean(R.styleable.GlassCard_glassStrong, false)
            if (strong) {
                fillColor = context.colorAttr(R.attr.auroraSurfaceStrong)
            }
            a.recycle()
        }

        // Soft drop shadow. This view paints its own rounded surface, so the
        // default (empty) outline would leave it with no shadow at all. The
        // provider matches the painted corner radius and the stronger variant
        // gets a deeper lift.
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, corner)
            }
        }
        clipToOutline = false
        elevation = context.dp(if (strong) 9f else 5f).toFloat()

        fillPaint.color = fillColor
        fillPaint.style = Paint.Style.FILL

        strokePaint.color = strokeColor
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = context.dp(1f).toFloat()

        sheenPaint.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // Re-read colors after a theme switch (activity is recreated, but this
        // also keeps previews honest).
        fillPaint.color = if (strong) {
            context.colorAttr(R.attr.auroraSurfaceStrong)
        } else {
            context.colorAttr(R.attr.auroraSurface)
        }
        strokePaint.color = context.colorAttr(R.attr.auroraStroke)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // A faint light band across the top quarter reads as real glass catching
        // daylight. The sheen is created with premultiplied-safe gradient colors.
        val band = (h * 0.28f).coerceAtLeast(1f)
        val isLight = context.boolAttr(R.attr.auroraIsLight)
        val startSheen = if (isLight) 0x38FFFFFF else 0x1AFFFFFF
        sheenPaint.shader = LinearGradient(
            0f, 0f, 0f, band,
            startSheen, 0x00FFFFFF,
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        val inset = strokePaint.strokeWidth / 2f
        rect.set(inset, inset, width - inset, height - inset)
        clipPath.reset()
        clipPath.addRoundRect(rect, corner, corner, Path.Direction.CW)
        canvas.save()
        canvas.clipPath(clipPath)
        canvas.drawRoundRect(rect, corner, corner, fillPaint)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), sheenPaint)
        canvas.restore()
        canvas.drawRoundRect(rect, corner, corner, strokePaint)
    }
}
