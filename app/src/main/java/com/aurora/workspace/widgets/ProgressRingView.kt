package com.aurora.workspace.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.aurora.workspace.R
import com.aurora.workspace.util.colorAttr
import com.aurora.workspace.util.dp

/**
 * Circular progress ring with a soft track and a rounded accent arc.
 * Also renders the percentage in the middle so cards stay lightweight.
 */
class ProgressRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = ResourcesCompat.getFont(context, R.font.plexarabic_medium)
                ?: Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }
    private val oval = RectF()

    private var target = 0f
    private var displayed = 0f
    private var stroke = context.dp(8f).toFloat()
    private var showText = true
    private var animator: ValueAnimator? = null

    init {
        trackPaint.color = context.colorAttr(R.attr.auroraStroke)
        arcPaint.color = context.colorAttr(R.attr.auroraAccent)
        textPaint.color = context.colorAttr(R.attr.auroraText)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ProgressRingView)
            target = a.getInt(R.styleable.ProgressRingView_ringProgress, 0).toFloat()
            displayed = target
            stroke = a.getDimension(R.styleable.ProgressRingView_ringStroke, stroke)
            a.recycle()
        }
    }

    fun setProgress(value: Int, animate: Boolean = true) {
        val clamped = value.coerceIn(0, 100).toFloat()
        animator?.cancel()
        if (!animate) {
            target = clamped
            displayed = clamped
            invalidate()
            return
        }
        animator = ValueAnimator.ofFloat(displayed, clamped).apply {
            duration = 720
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                displayed = it.animatedValue as Float
                invalidate()
            }
        }
        animator?.start()
        target = clamped
    }

    fun showLabel(show: Boolean) {
        showText = show
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        trackPaint.strokeWidth = stroke
        arcPaint.strokeWidth = stroke
        trackPaint.color = context.colorAttr(R.attr.auroraStroke)
        arcPaint.color = context.colorAttr(R.attr.auroraAccent)
        textPaint.color = context.colorAttr(R.attr.auroraText)
        textPaint.textSize = measuredHeight * 0.24f
    }

    override fun onDraw(canvas: Canvas) {
        val size = minOf(width, height).toFloat()
        val inset = stroke / 2f + context.dp(2f)
        oval.set(
            (width - size) / 2f + inset,
            (height - size) / 2f + inset,
            (width + size) / 2f - inset,
            (height + size) / 2f - inset
        )
        canvas.drawArc(oval, 0f, 360f, false, trackPaint)
        if (displayed > 0f) {
            canvas.drawArc(oval, -90f, displayed * 3.6f, false, arcPaint)
        }
        if (showText) {
            val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText("${displayed.toInt()}%", width / 2f, y, textPaint)
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
