package com.example.waterreminder

import android.content.Context
import android.content.SharedPreferences

/** 简单的本地开关偏好存储。 */
object Prefs {
    private const val NAME = "water_prefs"
    private const val KEY_ENABLED = "enabled"

    private fun sp(context: Context): SharedPreferences =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = sp(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, value: Boolean) {
        sp(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }
}
