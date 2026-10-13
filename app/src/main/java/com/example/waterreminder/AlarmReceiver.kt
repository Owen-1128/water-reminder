package com.example.waterreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * 闹钟接收器：每小时触发一次。
 * - 仅在工作日（周一~周五）且小时落在 9~18 内才弹出通知；
 * - 触发后立即重新排程同一小时的「下一个工作日」，使链条永续。
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra("hour", -1)
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val isWeekday = day != Calendar.SATURDAY && day != Calendar.SUNDAY

        if (isWeekday && hour in WaterReminderScheduler.HOUR_START..WaterReminderScheduler.HOUR_END) {
            NotificationHelper.showWaterNotification(context)
        }

        // 继续排程下一次（自动跳过周末）
        WaterReminderScheduler.scheduleHour(context, hour)
    }
}
