package com.example.waterreminder

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 饮水记录本地存储（按「日期 → 杯数」）。
 * 同时提供本周 7 天数据与连续打卡天数。
 */
object WaterStore {

    const val DEFAULT_GOAL = 8

    private const val NAME = "water_records"
    private const val KEY_GOAL = "goal"
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    private fun sp(context: Context): SharedPreferences =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun today(): String = dateFmt.format(Date())

    /** 周一到周日索引：周一=0 … 周日=6 */
    fun todayWeekIndex(): Int {
        val d = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return if (d == Calendar.SUNDAY) 6 else d - Calendar.MONDAY
    }

    fun getCups(context: Context, date: String): Int =
        sp(context).getInt("cups_$date", 0)

    fun addCups(context: Context, date: String, n: Int) {
        if (n <= 0) return
        val next = (getCups(context, date) + n).coerceAtLeast(0)
        sp(context).edit().putInt("cups_$date", next).apply()
    }

    fun getGoal(context: Context): Int =
        sp(context).getInt(KEY_GOAL, DEFAULT_GOAL).coerceAtLeast(1)

    fun setGoal(context: Context, goal: Int) {
        sp(context).edit().putInt(KEY_GOAL, goal.coerceAtLeast(1)).apply()
    }

    /** 最近 7 天（周一到周日，含今天）的杯数数组。 */
    fun getWeekCounts(context: Context): IntArray {
        val cal = Calendar.getInstance()
        // 回到本周周一
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val back = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_MONTH, -back)
        val result = IntArray(7)
        for (i in 0..6) {
            result[i] = getCups(context, dateFmt.format(cal.time))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return result
    }

    /** 连续打卡天数：从今天往前数，每天 >=1 杯则 +1，遇到 0 杯即止。 */
    fun streak(context: Context): Int {
        val cal = Calendar.getInstance()
        var count = 0
        // 若今天还没喝，从昨天起算，避免把"今天未喝"误判断签
        if (getCups(context, dateFmt.format(cal.time)) == 0) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        while (getCups(context, dateFmt.format(cal.time)) > 0) {
            count++
            cal.add(Calendar.DAY_OF_MONTH, -1)
            if (count > 365) break
        }
        return count
    }

    // ---- 打卡（每日签到） ----
    fun isCheckedIn(context: Context, date: String): Boolean =
        sp(context).getInt("checkin_$date", 0) == 1

    fun checkInToday(context: Context) {
        sp(context).edit().putInt("checkin_${today()}", 1).apply()
    }

    /** 连续打卡天数：从今天往前数，已打卡则 +1，遇未打卡即止。 */
    fun checkInStreak(context: Context): Int {
        val cal = Calendar.getInstance()
        var count = 0
        if (!isCheckedIn(context, dateFmt.format(cal.time))) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        while (isCheckedIn(context, dateFmt.format(cal.time))) {
            count++
            cal.add(Calendar.DAY_OF_MONTH, -1)
            if (count > 365) break
        }
        return count
    }

    /** 单日记录。 */
    data class DayRecord(
        val date: String,
        val cups: Int,
        val checkedIn: Boolean
    )

    /** 最近 n 天（含今天，由近到远）。 */
    fun getRecentDays(context: Context, n: Int): List<DayRecord> {
        val cal = Calendar.getInstance()
        val list = mutableListOf<DayRecord>()
        for (i in 0 until n) {
            val date = dateFmt.format(cal.time)
            list.add(DayRecord(date, getCups(context, date), isCheckedIn(context, date)))
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        return list
    }
}
