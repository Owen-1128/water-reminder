package com.example.waterreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 设备重启 / 应用更新后，重新播种所有闹钟，
 * 保证提醒在重启后依然生效。
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val handled = action == Intent.ACTION_BOOT_COMPLETED ||
                action == Intent.ACTION_MY_PACKAGE_REPLACED ||
                action == "android.intent.action.QUICKBOOT_POWERON"
        if (handled && Prefs.isEnabled(context)) {
            WaterReminderScheduler.scheduleAll(context)
        }
    }
}
