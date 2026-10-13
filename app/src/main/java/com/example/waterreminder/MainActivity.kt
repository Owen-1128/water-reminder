package com.example.waterreminder

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.waterreminder.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.ensureChannel(this)
        if (Prefs.isEnabled(this)) {
            WaterReminderScheduler.scheduleAll(this)
        }

        setupToggle()
        setupButtons()
        refreshAll()
    }

    private fun setupToggle() {
        binding.switchReminder.isChecked = Prefs.isEnabled(this)
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            Prefs.setEnabled(this, isChecked)
            if (isChecked) {
                ensureNotificationPermission()
                WaterReminderScheduler.scheduleAll(this)
                Toast.makeText(this, "喝水提醒已开启", Toast.LENGTH_SHORT).show()
            } else {
                WaterReminderScheduler.cancelAll(this)
                Toast.makeText(this, "喝水提醒已关闭", Toast.LENGTH_SHORT).show()
            }
            refreshNext()
        }
    }

    private fun setupButtons() {
        binding.btnAddCup.setOnClickListener {
            WaterStore.addCups(this, WaterStore.today(), 1)
            refreshAll()
            Toast.makeText(this, "已记录 1 杯", Toast.LENGTH_SHORT).show()
        }

        // 手动输入今日已喝水量（累加）
        binding.btnManual.setOnClickListener { showManualDialog() }

        // 打卡
        binding.btnCheckIn.setOnClickListener {
            if (WaterStore.isCheckedIn(this, WaterStore.today())) {
                Toast.makeText(this, "今日已打卡", Toast.LENGTH_SHORT).show()
            } else {
                WaterStore.checkInToday(this)
                refreshAll()
                Toast.makeText(this, "打卡成功", Toast.LENGTH_SHORT).show()
            }
        }

        // 查看记录（独立页面）
        binding.btnRecords.setOnClickListener {
            startActivity(android.content.Intent(this, RecordsActivity::class.java))
        }

        binding.btnTest.setOnClickListener {
            ensureNotificationPermission()
            NotificationHelper.showWaterNotification(this)
            Toast.makeText(this, "已发送一条测试通知", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showManualDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "输入杯数（将累加至今日）"
            textSize = 16f
        }
        AlertDialog.Builder(this)
            .setTitle("手动输入今日已喝水量")
            .setMessage("输入本次喝了几杯（1 杯约 250ml），将累加至今日总量")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val n = input.text.toString().toIntOrNull() ?: 0
                if (n > 0) {
                    WaterStore.addCups(this, WaterStore.today(), n)
                    refreshAll()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /** 统一刷新入口：所有模块各自读取最新数据渲染，互不直接调用。 */
    private fun refreshAll() {
        val cups = WaterStore.getCups(this, WaterStore.today())
        val goal = WaterStore.getGoal(this)
        val percent = (cups.toFloat() / goal * 100).toInt()

        binding.ring.setProgress(cups.toFloat() / goal)
        binding.tvCups.text = cups.toString()
        binding.tvGoal.text = "/ $goal 杯"
        binding.tvPercent.text = "已完成 $percent%"

        val week = WaterStore.getWeekCounts(this)
        binding.weekBar.setData(week, goal, WaterStore.todayWeekIndex())
        binding.tvStreak.text = "连续喝水 ${WaterStore.streak(this)} 天"

        val checkedIn = WaterStore.isCheckedIn(this, WaterStore.today())
        binding.tvCheckInStatus.text = if (checkedIn) "今日打卡：已打卡" else "今日打卡：未打卡"

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvQuote.text = QuotesProvider.quoteFor(hour)

        refreshNext()
    }

    private fun refreshNext() {
        binding.tvNext.text = if (Prefs.isEnabled(this)) {
            "下次提醒：${WaterReminderScheduler.nextReminderTime()}"
        } else {
            "提醒已关闭"
        }
    }

    /** Android 13+ 需要用户授权通知权限，否则收不到推送。 */
    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}
