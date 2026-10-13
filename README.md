# 水滴 · 喝水提醒（Water Reminder）

一个轻量的 Android 应用：在**工作日（周一至周五）09:00 – 18:00** 之间，**每小时**准时推送一条「喝水提醒」通知。
通知里不是干巴巴的「该喝水了」，而是一句**有深意的提示语**，把喝水这件小事连接到生活、自省与温柔的坚持。

---

## 功能特性

- ⏰ **精确每小时提醒**：工作日 9 点到 18 点，整点推送（共 10 次/天）。
- 🚫 **自动跳过周末**：调度逻辑只认工作日，周六日不会打扰你。
- 💧 **深意提示语**：内置 24 句围绕「水 / 健康 / 生活」的中文短句，按「日期 + 小时」稳定轮换，每天、每小时都不重样。
- 🔔 **系统通知推送**：高优先级通知渠道，息屏/锁屏也能看到。
- 🔁 **重启不丢失**：设备重启或应用更新后，自动重新播种所有闹钟。
- 📊 **饮水仪表盘界面**：采用「数据仪表盘」风格——顶部进度环显示今日饮水完成度（X/8 杯），一键「+1 杯 / + 自定义」记录，下方「本周打卡」柱状图 + 连续天数。
- 💬 **今日一句**：随界面展示当前时段对应的深意提示语。
- ✅ **每日打卡**：新增「打卡」按钮，记录当天打卡并显示「今日打卡 / 未打卡」状态与连续打卡天数。
- ✍️ **手动输入水量**：新增「手动输入」按钮，可输入今日已喝杯数（累加），方便随时补记。
- 📑 **独立记录页**：新增「查看记录」页面，展示今日已喝水量（杯 / 约 ml）、连续喝水天数、连续打卡天数，以及近 30 天每日明细（日期·杯数·打卡状态）。
- 🎛️ **开关与测试**：一键启用/关闭提醒，实时显示「下次提醒」时间，并支持发送测试通知。

---

## 项目结构

```
WaterReminderApp/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/waterreminder/
│       │   ├── MainActivity.kt          # 仪表盘主界面（进度环 / 记录 / 提醒开关 / 测试）
│       │   ├── RecordsActivity.kt        # 独立记录页（今日水量 / 连续天数 / 近 30 天明细）
│       │   ├── AlarmReceiver.kt         # 每小时触发，决定是否弹通知
│       │   ├── BootReceiver.kt          # 重启/更新后恢复提醒
│       │   ├── WaterReminderScheduler.kt # 精确闹钟调度（跳过周末）
│       │   ├── NotificationHelper.kt    # 通知渠道与展示
│       │   ├── QuotesProvider.kt        # 深意提示语文库
│       │   ├── WaterStore.kt            # 饮水记录本地存储（按日杯数 / 周数据 / 连续天数）
│       │   ├── RingView.kt              # 今日饮水进度环（自定义 View）
│       │   ├── WeekBarView.kt           # 本周打卡柱状图（自定义 View）
│       │   └── Prefs.kt                 # 本地开关存储
│       ├── res/                         # 布局、主题、图标、字符串、头部渐变
│       └── proguard-rules.pro
├── build.gradle / settings.gradle / gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── .github/workflows/build-apk.yml      # 云端一键打包（无需本地 SDK）
```

---

## 如何生成可安装的 APK

> ⚠️ **推荐用方式一（GitHub Actions 云端打包）**。本地若处于限速网络，JDK / Android SDK 下载会非常慢（实测 ~30KB/s），不建议在本机现装工具链。

### 方式一：GitHub Actions 云端打包（推荐，零安装、速度快）

1. 把本仓库推送到你自己的 GitHub 仓库。
2. 进入仓库 **Actions → Build APK → Run workflow**，点击运行。
3. 运行结束后，在 **Artifacts** 中下载 `water-reminder-apk`，里面是 `app-debug.apk`。
4. 把 APK 传到手机安装即可（debug 包自带签名，可直接侧载）。

> 全程不需要在你电脑上安装 JDK / Android SDK，编译在 GitHub 的服务器上完成。

> 📘 **想要一步步的图文 + 排错指引？** 看 [`GITHUB_DEPLOY.md`](GITHUB_DEPLOY.md) —— 含建仓、推送、触发构建、下载安装，以及「推送失败 / 构建标红 / 收不到通知」等常见问题的调试方案。

### 方式二：Android Studio 本地打包（需自装 SDK）

1. 用 **Android Studio** 打开本目录（File → Open → 选择 `WaterReminderApp`）。
2. 首次打开会提示下载 Gradle / 依赖，按提示完成同步（需要 Android SDK + JDK 17）。
3. 菜单 **Build → Build Bundle(s) / APK(s) → Build APK(s)**。
4. 编译完成后，在 `app/build/outputs/apk/debug/app-debug.apk` 拿到安装包。

---

## 使用说明

1. 安装后打开 App，打开「启用喝水提醒」开关。
2. Android 13+ 会弹出通知授权，请选择**允许**，否则收不到推送。
3. 之后每个工作日的 9:00、10:00 … 18:00 都会收到一条喝水提醒。
4. 想立刻看看效果，点「发送测试通知」即可。

---

## 兼容性

- 最低支持 Android 8.0（API 26），目标 API 34。
- 使用 `USE_EXACT_ALARM` 权限实现精确闹钟（提醒/日历类应用适用，安装即授予，无需运行时弹窗）。

---

## 想改提示语 / 时间？

- **改提示语**：编辑 `app/src/main/java/com/example/waterreminder/QuotesProvider.kt` 里的 `quotes` 列表。
- **改时间范围**：编辑 `WaterReminderScheduler.kt` 中的 `HOUR_START` / `HOUR_END` 常量（默认 9 和 18）。
- **改提醒日**：`isWeekday` 判断在 `AlarmReceiver.kt` 与 `WaterReminderScheduler.kt` 中，可扩展为自定义星期。
