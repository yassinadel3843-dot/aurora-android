package com.aurora.workspace.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import com.aurora.workspace.R
import com.aurora.workspace.util.boolAttr
import com.aurora.workspace.util.colorAttr
import com.aurora.workspace.util.withAlpha
import kotlin.random.Random

/**
 * The signature backdrop: a deep vertical gradient with two soft aurora glows
 * and a quiet field of stars. Purely canvas drawn, so it costs a single view
 * on every screen and respects every theme.
 */
class AuroraBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.DITHER_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var bg1 = 0
    private var bg2 = 0
    private var glow1 = 0
    private var glow2 = 0
    private var isLight = false

    private var backgroundShader: Shader? = null
    private var glowShader1: Shader? = null
    private var glowShader2: Shader? = null
    private var stars: Array<FloatArray> = emptyArray()

    init {
        bg1 = context.colorAttr(R.attr.auroraBg1)
        bg2 = context.colorAttr(R.attr.auroraBg2)
        glow1 = context.colorAttr(R.attr.auroraGlow1)
        glow2 = context.colorAttr(R.attr.auroraGlow2)
        isLight = context.boolAttr(R.attr.auroraIsLight)
        starPaint.color = Color.WHITE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        backgroundShader = LinearGradient(
            0f, 0f, w * 0.35f, h.toFloat(),
            bg1, bg2, Shader.TileMode.CLAMP
        )

        val glowAlpha1 = if (isLight) 55 else 120
        val glowAlpha2 = if (isLight) 45 else 95

        glowShader1 = RadialGradient(
            w * 0.86f, h * 0.06f, w * 0.95f,
            intArrayOf(glow1.withAlpha(glowAlpha1), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        glowShader2 = RadialGradient(
            w * 0.04f, h * 0.66f, w * 0.85f,
            intArrayOf(glow2.withAlpha(glowAlpha2), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )

        val random = Random(21)
        val count = if (isLight) 18 else 54
        stars = Array(count) {
            floatArrayOf(
                random.nextFloat() * w,
                random.nextFloat() * h,
                0.5f + random.nextFloat() * 2.0f,
                0.18f + random.nextFloat() * 0.55f
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        backgroundShader?.let {
            bgPaint.shader = it
            canvas.drawRect(0f, 0f, w, h, bgPaint)
        } ?: run {
            bgPaint.shader = null
            bgPaint.color = bg1
            canvas.drawRect(0f, 0f, w, h, bgPaint)
        }

        glowShader1?.let {
            glowPaint.shader = it
            canvas.drawRect(0f, 0f, w, h * 0.62f, glowPaint)
        }
        glowShader2?.let {
            glowPaint.shader = it
            canvas.drawRect(0f, h * 0.42f, w, h, glowPaint)
        }

        val time = System.currentTimeMillis() * 0.0018f
        for (i in stars.indices) {
            val star = stars[i]
            val pulse = kotlin.math.sin(time + i * 1.731f) * 0.28f + 0.72f
            starPaint.alpha = (star[3] * pulse * 255f).coerceIn(0f, 255f).toInt()
            canvas.drawCircle(star[0], star[1], star[2], starPaint)
        }

        if (isAttachedToWindow) {
            postInvalidateOnAnimation()
        }
    }
}
