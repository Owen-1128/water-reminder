package com.example.waterreminder

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.waterreminder.databinding.ActivityRecordsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RecordsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordsBinding
    private val mlPerCup = 250
    private val weekNames = arrayOf("日", "一", "二", "三", "四", "五", "六")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        refresh()
    }

    private fun refresh() {
        val today = WaterStore.today()
        val cups = WaterStore.getCups(this, today)
        val checkedIn = WaterStore.isCheckedIn(this, today)

        binding.tvTodayCups.text = cups.toString()
        binding.tvTodayMl.text = "约 ${cups * mlPerCup} ml"
        binding.tvTodayCheckIn.text = if (checkedIn) "已打卡" else "未打卡"
        binding.tvTodayCheckIn.setTextColor(
            if (checkedIn) Color.parseColor("#0D47A1") else Color.parseColor("#5A6B82")
        )

        binding.tvCupStreak.text = "${WaterStore.streak(this)} 天"
        binding.tvCheckInStreak.text = "${WaterStore.checkInStreak(this)} 天"

        // 近 30 天明细
        binding.listRecords.removeAllViews()
        val days = WaterStore.getRecentDays(this, 30)
        for (d in days) {
            binding.listRecords.addView(buildRow(d.date, d.cups, d.checkedIn))
        }
    }

    private fun buildRow(date: String, cups: Int, checkedIn: Boolean): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 12, 0, 12)
        }
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        row.layoutParams = params

        // 日期 + 星期
        val cal = Calendar.getInstance()
        try {
            cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(date)
        } catch (_: Exception) {
        }
        val wd = weekNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
        val dateTv = TextView(this).apply {
            text = "$date  周$wd"
            textSize = 14f
            setTextColor(Color.parseColor("#0F1B2D"))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        // 杯数 / 水量
        val cupTv = TextView(this).apply {
            text = "$cups 杯 · ${cups * mlPerCup}ml"
            textSize = 14f
            setTextColor(Color.parseColor("#5A6B82"))
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // 打卡标记
        val badge = TextView(this).apply {
            text = if (checkedIn) "已打卡" else "·"
            textSize = 13f
            setTextColor(
                if (checkedIn) Color.parseColor("#2196F3") else Color.parseColor("#B0BEC5")
            )
            gravity = Gravity.END
            val p = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            p.marginStart = 16
            layoutParams = p
        }

        row.addView(dateTv)
        row.addView(cupTv)
        row.addView(badge)
        return row
    }
}
