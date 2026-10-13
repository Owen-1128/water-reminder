# 喝水提醒 · GitHub 部署与调试完整教程

> 目标：把你本地的 `WaterReminderApp` 项目推到 GitHub，用 **GitHub Actions 云端自动打包** 生成可安装的 `app-debug.apk`，并安装到手机。全程**不需要在本机安装 JDK / Android SDK**。
> 本文同时覆盖「部署流程」与「常见报错调试」，照着做即可一次成功。

---

## 0. 为什么要走 GitHub 云端打包

本地实测网络限速严重（JDK 约 30KB/s，180MB 要下一个多小时），现装工具链不现实。而 GitHub 提供的 Linux 构建机（Runner）已经联网且带宽充足，我们把「编译 APK」这件事交给它：

- ✅ 零本地安装：不用装 JDK 17、Android SDK、Gradle。
- ✅ 快：云端分钟级完成，本机只需 `git push`。
- ✅ 可复现：任何人 `fork` 你的仓库都能一键出包。
- ✅ 免费额度：公开仓库（Public）的 Actions 完全免费。

本项目已经内置好打包脚本：`.github/workflows/build-apk.yml`，你只需要把代码推上去、点一下运行即可。

---

## 1. 前置准备（5 分钟）

| 工具 | 用途 | 获取方式 |
| --- | --- | --- |
| GitHub 账号 | 托管代码 + 云端构建 | https://github.com 注册 |
| Git | 把本地代码推到 GitHub | Windows：https://git-scm.com 下载安装；Mac：`brew install git` |
| 一台安卓手机 | 安装测试 APK | 系统 ≥ Android 8.0（API 26） |

**验证 Git 已安装**（命令行 / Git Bash 执行）：

```bash
git --version
# 正常会打印：git version 2.x.x
```

> 你的项目目录 `WaterReminderApp` 已经包含完整的 Android 工程与 `.github/workflows/build-apk.yml`，无需额外写代码。

---

## 2. 在 GitHub 上新建仓库

1. 登录 GitHub，右上角 **「+」→ New repository**。
2. **Repository name**：建议 `water-reminder`（也可任意）。
3. **Visibility**：选 **Public**（Actions 免费；选 Private 也行，但私有仓库 Actions 有额度限制）。
4. **⚠️ 不要勾选** “Initialize this repository with a README”（因为本地已有 README，勾了会导致首次 push 冲突）。
5. 点击 **Create repository**。
6. 创建成功后，页面会显示一个仓库地址，形如：
   ```
   https://github.com/<你的用户名>/water-reminder.git
   ```
   复制它，下一步要用。

---

## 3. 把本地代码推送到 GitHub

### 3.1 先加一个 `.gitignore`（很重要）

在 `WaterReminderApp` 目录下新建 `.gitignore` 文件，内容如下。它能避免把构建垃圾、IDE 缓存、密钥提交进仓库（**不要提交 `local.properties`、keystore 等含本地路径/密钥的文件**）：

```gitignore
# ===== Android 构建产物 =====
*.apk
*.aar
*.ap_
*.dex
*.class
bin/
gen/
out/
.gradle/
local.properties
app/build/
build/
captures/

# ===== IDE =====
*.iml
.idea/
.DS_Store

# ===== 预览原型（非安卓工程必需，可保留可忽略） =====
# preview.html
# ui-proposals/
```

> 提示：`preview.html` 和 `ui-proposals/` 是给电脑看的 HTML 高保真预览，不影响 APK 构建。你可以保留它们作为设计稿，也可以取消注释忽略掉，让仓库更「纯安卓」。

### 3.2 执行推送命令

打开命令行，**进入项目目录**（`WaterReminderApp`），依次执行：

```bash
# 进入项目根目录（请替换成你的实际路径）
cd C:/Users/11272553/WorkBuddy/2026-10-12-10-52-33/WaterReminderApp

# 1) 初始化本地仓库（若已 init 会提示，忽略即可）
git init

# 2) 添加所有文件
git add .

# 3) 提交
git commit -m "feat: 喝水提醒 App 初始版本（云端打包）"

# 4) 设置默认分支名为 main
git branch -M main

# 5) 关联远程仓库（替换成你在第 2 步复制的地址）
git remote add origin https://github.com/<你的用户名>/water-reminder.git

# 6) 推送
git push -u origin main
```

推送成功后，刷新 GitHub 仓库页面，能看到 `app/`、`gradle/`、`build.gradle`、`.github/` 等文件都在了。

> **Windows 认证说明**：`git push` 可能弹出账号密码框。GitHub 已不支持密码登录，需要：
> - 用 **Personal Access Token（PAT）** 当作密码（GitHub → Settings → Developer settings → Personal access tokens → 勾选 `repo` 权限生成）；或
> - 安装 **GitHub CLI**（`gh auth login`）后推送更顺滑。
> 若提示 `Support for password authentication was removed`，就是账号密码方式被拒，改用上面两种之一。

---

## 4. 触发云端打包（GitHub Actions）

我们内置的 `build-apk.yml` 有两种触发方式，满足其一即可自动开始构建：

- ✅ **推送代码到 `main` / `master` 分支**（第 3 步的 `git push` 已经触发了一次）。
- ✅ **手动点击运行**（适合反复重打包）。

### 4.1 手动触发步骤

1. 进入你的仓库，点击顶部 **Actions** 标签。
2. 左侧应能看到工作流 **「Build APK」**（名字来自 `build-apk.yml` 里的 `name`）。
3. 点击它 → 右侧 **「Run workflow」** → 确认分支 `main` → **Run workflow**。
4. 页面会出现一个黄色圆点（进行中），点进去可看实时日志。

### 4.2 这个工作流每一步在干什么（对照 `build-apk.yml`）

| 步骤 | 动作 | 说明 |
| --- | --- | --- |
| 检出代码 | `actions/checkout@v4` | 把仓库代码下载到构建机 |
| 配置 JDK 17 | `setup-java@v4` (temurin, 17) | 本项目 **必须用 JDK 17**（AGP 8.2.2 要求） |
| 配置 Android SDK | `android-actions/setup-android@v3` | 安装 `android-34` 平台、`build-tools;34.0.0`、`platform-tools` |
| 接受 SDK 许可 | `sdkmanager --licenses` | **关键保险步骤**：避免构建因许可协议未接受而失败 |
| 安装 Gradle 8.5 | 手动下载解压 | 固定 Gradle 8.5（与 `gradle-wrapper.properties` 一致），不用 wrapper 下载，规避慢速 |
| 构建 Debug APK | `gradle assembleDebug --no-daemon` | 真正编译，产出 `app/build/outputs/apk/debug/app-debug.apk` |
| 上传产物 | `upload-artifact@v4` | 把 APK 打包成名为 `water-reminder-apk` 的 Artifact 供下载 |

> 正常运行约 **2–5 分钟**。看到绿色对勾 ✅ 即成功。

---

## 5. 下载并安装 APK

1. 在 Actions 运行结果页，向下滚动到 **「Artifacts」** 区域。
2. 点击 **`water-reminder-apk`** 下载（是个 zip）。
3. 解压得到 **`app-debug.apk`**。
4. 把 APK 传到手机，任选其一：
   - 数据线连接电脑 → 拷贝到手机存储 → 文件管理器点击安装；
   - 用微信/钉钉/邮件/网盘 发给自己 → 手机端下载。
5. 手机安装时若提示「允许来自此来源的应用」，点**允许**（debug 包自带签名，可直接侧载）。
6. 打开 App：
   - 打开 **「启用喝水提醒」** 开关；
   - Android 13+ 会弹 **通知授权**，务必点**允许**；
   - 点 **「发送测试通知」** 立即验证能否收到推送。

---

## 6. 调试与常见问题（重点）

下面按「仓库类 / 构建类 / 运行类」分类，列出**最高频的报错与解决办法**。

### A. 推送与仓库类

**❌ `error: remote origin already exists.`**
> 仓库已关联过远程地址。解决：先移除再添加，或直接改地址。
```bash
git remote remove origin
git remote add origin https://github.com/<用户名>/water-reminder.git
# 或改地址：git remote set-url origin <新地址>
```

**❌ `failed to push some refs` / `non-fast-forward`**
> 远程仓库有本地没有的内容（比如你勾选了 Initialize README）。解决：
```bash
git pull origin main --allow-unrelated-histories
git push -u origin main
```

**❌ `Support for password authentication was removed`**
> GitHub 不再接受账号密码。改用 **PAT（当作密码）** 或 **GitHub CLI (`gh auth login`)**。

### B. 云端构建类（Actions 标红失败）

**❌ `Could not determine the dependencies of task ... / Unsupported class file major version`**
> JDK 版本不对。本项目必须用 **JDK 17**。检查工作流里的 `java-version: 17`，不要改成 11 或 21。

**❌ `Failed to install the following SDK packages ... / License for package ... not accepted`**
> SDK 许可未接受。我们已在工作流里加了 `yes | sdkmanager --licenses` 作为保险；若仍报，确认该步骤确实执行（看日志里「接受 Android SDK 许可协议」这一步不是被跳过）。极少数情况下可把那一步的 `|| true` 去掉，让错误暴露出来定位。

**❌ `Minimum supported Gradle version is 8.5. Current version is X.X`**
> Gradle 与 AGP 不匹配。本项目 `build.gradle` 固定 AGP `8.2.2`，对应 Gradle `8.5`，工作流里也已固定下载 `gradle-8.5`。**不要混用本地 `gradlew` 的版本**——我们的工作流直接用下载好的 `gradle` 命令，绕开 wrapper，避免版本漂移。

**❌ `Could not resolve all files for configuration ...`（依赖下载失败）**
> 多为 GitHub 构建机到 Google Maven 的网络抖动。解决办法：**直接重新 Run workflow**（Action 页面右上角「Re-run jobs」），通常第二次就过。

**❌ 构建「卡住」或超时**
> 免费账号有并发/时长限制。一般 5 分钟内完成；若长时间无日志，Cancel 后重试。避免同时跑多个构建。

**❌ 找不到 APK 产物 / Artifacts 里没有 `water-reminder-apk`**
> 构建其实失败了（前面步骤红）。点开日志看是哪一步红的，对照上面 B 类条目修。产物只在**构建成功**时才上传。

**❌ 日志显示 `What went wrong: Execution failed for task ':app:compileDebugKotlin'`**
> Kotlin 代码编译错误。本项目代码已自测可编；若你改过 `.kt` 文件，检查报错行号与语法。常见：少了 `import`、括号不匹配、用了已删除的方法。

### C. 装到手机后的运行类

**❌ Android 13+ 收不到任何通知**
> 原因：未授予 `POST_NOTIFICATIONS` 权限。解决：系统设置 → 应用 → 喝水提醒 → 通知 → 设为**允许**。App 首次打开也会弹窗，务必点允许。

**❌ 提醒没按时响 / 锁屏不弹**
> 原因（国产 ROM 常见）：系统把 App 杀了或限制了后台。建议：
> - 设置 → 应用 → 喝水提醒 → **电池 / 省电策略** → 设为「**无限制 / 允许后台**」；
> - 设置 → **自启动管理** → 允许本 App 自启动（这样 `BootReceiver` 重启后还能恢复闹钟）；
> - 把 App 加入「**锁定后台**」（多任务卡片上锁）。
> 注：本 App 用的是 `USE_EXACT_ALARM` 精确闹钟，安装即授权，无需运行时弹窗；但后台存活仍受厂商 ROM 影响。

**❌ 重启手机后不再提醒**
> 已通过 `BootReceiver`（`BOOT_COMPLETED` + `MY_PACKAGE_REPLACED`）自动重新播种闹钟。若仍不响，多半是上面「自启动权限」没开，请开启自启动。

**❌ 周末也收到提醒？**
> 不会。调度逻辑 `WaterReminderScheduler.nextWeekdayAtHour()` 自动跳过周六日，只对周一至周五 9:00–18:00 排程。

**❌ 提醒时间不是整点 / 偏了几分钟**
> 检查手机**系统时间与时区**是否正确。闹钟基于设备本地时间计算。

---

## 7. 本地构建替代方案（Android Studio）

如果你之后网络好转、想在本机改代码，可以用 Android Studio：

1. 下载安装 **Android Studio**（含 SDK Manager）。
2. **File → Open** → 选择 `WaterReminderApp` 目录。
3. 首次打开会提示 **Gradle Sync**，按提示下载 SDK（`android-34`、`build-tools;34.0.0`）与依赖。
4. 若同步报许可错误，打开终端执行：
   ```bash
   sdkmanager --licenses
   # 一路输入 y 接受
   ```
5. **Build → Build Bundle(s) / APK(s) → Build APK(s)**。
6. 产物在 `app/build/outputs/apk/debug/app-debug.apk`。

> 命令行等价：`gradlew assembleDebug`（需本机已装好 SDK 并 `sdkmanager --licenses` 接受协议）。

---

## 8. 进阶：发布带签名的 Release 安装包（可选）

Debug 包能直接装，但若要上架或长期分发，需要**自己的签名**。步骤：

1. 本机生成 keystore（一次性）：
   ```bash
   keytool -genkeypair -v -keystore my-release-key.jks \
     -keyalg RSA -keysize 2048 -validity 10000 -alias waterreminder
   ```
   > ⚠️ 请保管好 `my-release-key.jks` 与密码，**不要提交进 Git**（已在 `.gitignore` 之外，建议额外忽略 `*.jks`）。
2. 在 `app/build.gradle` 的 `android { }` 内加：
   ```gradle
   signingConfigs {
       release {
           storeFile file("my-release-key.jks")
           storePassword System.getenv("KEYSTORE_PWD")
           keyAlias "waterreminder"
           keyPassword System.getenv("KEY_PWD")
       }
   }
   buildTypes {
       release {
           signingConfig signingConfigs.release
           minifyEnabled false
           proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
       }
   }
   ```
3. 在 GitHub 仓库 **Settings → Secrets → Actions** 里添加 `KEYSTORE_PWD` / `KEY_PWD` 两个密钥。
4. 把工作流里的 `gradle assembleDebug` 改成 `gradle assembleRelease`，即可云端出**签名 Release 包**。

---

## 9. 常用命令速查 & FAQ

**速查（本机命令行）：**
```bash
git status                 # 看改动
git add . && git commit -m "..."   # 提交
git push                   # 推送到 GitHub（会触发一次构建）
```

**FAQ：**

- **Q：一定要 Public 仓库吗？** 不一定，Private 也可，但免费 Actions 额度更少；教程用 Public 最省心。
- **Q：每次改代码都要手动点 Run workflow 吗？** 不用——只要 `git push` 到 `main`，工作流会**自动跑**（脚本里配了 `on: push`）。
- **Q：构建免费吗？** Public 仓库的 Linux Runner 完全免费，按月有 2000 分钟额度，打包 APK 一次约 3 分钟，绰绰有余。
- **Q：想改提醒语 / 时间怎么办？** 改完代码 `git push` 即可重新出包：
  - 改提示语：`app/src/main/java/com/example/waterreminder/QuotesProvider.kt`
  - 改时段：`WaterReminderScheduler.kt` 的 `HOUR_START` / `HOUR_END`（默认 9 / 18）
  - 改目标 API / 版本：`app/build.gradle` 的 `compileSdk` / `versionName`

---

## 附：一键部署最简清单

```
1. 装 Git，注册 GitHub 账号
2. 新建空仓库（不勾 Initialize README）
3. 项目里加 .gitignore（见 §3.1）
4. git init → add → commit → remote add → push
5. 仓库 Actions → Build APK → Run workflow
6. 下载 Artifacts 里的 water-reminder-apk → 解压 app-debug.apk
7. 传到手机安装 → 开开关 → 允许通知 → 发测试通知验证
```

完成以上 7 步，你就拥有了一个「工作日 9–18 点每小时深意喝水提醒」的可用 App。🎉
