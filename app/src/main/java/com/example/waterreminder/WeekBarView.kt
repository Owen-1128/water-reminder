package com.example.waterreminder

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * 本周饮水打卡柱状图（周一到周日，7 根）。
 * 今日高亮为主蓝，历史有记录为浅蓝，未记录为浅灰。
 */
class WeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var counts = IntArray(7)
    private var goal = 8
    private var todayIndex = 6

    private val colorToday = Color.parseColor("#2196F3")
    private val colorDone = Color.parseColor("#90CAF9")
    private val colorEmpty = Color.parseColor("#E3EAF2")

    fun setData(counts: IntArray, goal: Int, todayIndex: Int) {
        this.counts = counts
        this.goal = goal.coerceAtLeast(1)
        this.todayIndex = todayIndex
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val n = 7
        val gap = w * 0.07f
        val barW = (w - gap * (n + 1)) / n
        val maxH = h * 0.92f
        val radius = barW / 2f

        for (i in 0 until n) {
            val x = gap + i * (barW + gap)
            val frac = (counts[i].toFloat() / goal).coerceIn(0f, 1f)
            val bh = if (counts[i] > 0) maxH * frac else maxH * 0.04f
            val y = h - bh
            paint.color = when {
                i == todayIndex -> colorToday
                counts[i] > 0 -> colorDone
                else -> colorEmpty
            }
            canvas.drawRoundRect(x, y, x + barW, h, radius, radius, paint)
        }
    }
}
