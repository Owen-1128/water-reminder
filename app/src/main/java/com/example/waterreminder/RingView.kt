package com.example.waterreminder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * 圆环进度（今日饮水完成度）。背景环为半透明白，前景环为白色进度。
 */
class RingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(70, 255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private var progress = 0f
    private val rect = RectF()

    fun setProgress(fraction: Float) {
        progress = fraction.coerceIn(0f, 1f)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        val pad = STROKE / 2f + 2f
        rect.set(pad, pad, w - pad, h - pad)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawArc(rect, 0f, 360f, false, bgPaint)
        if (progress > 0f) {
            canvas.drawArc(rect, -90f, 360f * progress, false, fgPaint)
        }
    }

    companion object {
        private const val STROKE = 12f
    }
}
