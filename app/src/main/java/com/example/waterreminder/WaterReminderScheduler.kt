package com.example.waterreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 喝水提醒调度器。
 *
 * 策略：为工作日（周一至周五）的 9:00 ~ 18:00 每小时单独设置一个「精确闹钟」。
 * 每次闹钟触发后，会立即把同一小时「下一个工作日」的闹钟重新排好，
 * 因而天然地跳过周末，并且在设备重启/应用更新后由 BootReceiver 重新播种。
 */
object WaterReminderScheduler {

    const val HOUR_START = 9
    const val HOUR_END = 18
    private const val ACTION = "com.example.waterreminder.HOUR_ALARM"

    /** 为所有小时（9~18）排程。 */
    fun scheduleAll(context: Context) {
        for (hour in HOUR_START..HOUR_END) {
            scheduleHour(context, hour)
        }
    }

    /** 取消所有已排程的喝水闹钟。 */
    fun cancelAll(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        for (hour in HOUR_START..HOUR_END) {
            am.cancel(buildPendingIntent(context, hour))
        }
    }

    /** 为指定小时排程到「下一个工作日的该时刻」。 */
    fun scheduleHour(context: Context, hour: Int) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val trigger = nextWeekdayAtHour(hour) ?: return
        val pi = buildPendingIntent(context, hour)
        try {
            // USE_EXACT_ALARM 权限使得精确闹钟无需运行时授权
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        } catch (e: SecurityException) {
            // 极端情况下回退到非精确闹钟，保证至少能提醒
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        }
    }

    /** 计算下一个工作日的 hour:00（若当天已过则顺延，自动跳过周末）。 */
    fun nextWeekdayAtHour(hour: Int): Long? {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        var guard = 0
        while (isWeekend(cal)) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
            if (++guard > 14) return null
        }
        return cal.timeInMillis
    }

    /** 返回最近的「下次提醒」可读时间，用于界面展示。 */
    fun nextReminderTime(): String {
        var min = Long.MAX_VALUE
        for (h in HOUR_START..HOUR_END) {
            val t = nextWeekdayAtHour(h) ?: continue
            if (t < min) min = t
        }
        if (min == Long.MAX_VALUE) return "—"
        val sdf = SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA)
        return sdf.format(java.util.Date(min))
    }

    private fun isWeekend(cal: Calendar): Boolean {
        val d = cal.get(Calendar.DAY_OF_WEEK)
        return d == Calendar.SATURDAY || d == Calendar.SUNDAY
    }

    private fun buildPendingIntent(context: Context, hour: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION
            putExtra("hour", hour)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, hour, intent, flags)
    }
}
