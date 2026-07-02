# 🏋️‍♂️ PureLift (FitnessTracker)

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Room Database](https://img.shields.io/badge/Room%20DB-Offline%20First-34A853?style=for-the-badge&logo=sqlite&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2F%20Clean-FF6F00?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**纯粹自用 · 永久免费 · 零广告不联网 · 开屏立刻开练**

*一款为硬核力量训练者量身打造的极简 Android 举铁打卡与数据监控平台*
<br />
*A minimalist, offline-first strength training & workout tracker built with Jetpack Compose.*

---

</div>

## 📖 理念宣言 (Philosophy)

厌倦了市面上各大健身软件烦人的开屏广告、复杂臃肿的社交信息流、以及高昂的会员订阅付费？

**PureLift** 秉持着最朴素的信仰：**举铁是一件纯粹的事**。
* 🚀 **开屏即练**：没有任何多余引导和废话，点开 App 即可开始记录今天的第一组重量与次数。
* 🛡️ **本地优先 (Offline-First)**：数据 100% 存储于您设备本地的 SQLite (Room) 数据库中，无需联网，无隐私泄露风险，毫秒级响应。
* 🎨 **极简美学 (Modern UI)**：借鉴高端学习与效率工具的深色系沉浸式视觉排版，搭配灵动微动效与卡片交互，让记录训练如同艺术创作。

---

## ✨ 核心功能亮点 (Key Features)

### ⏱️ 1. 毫秒级组间休息倒计时 & 智能后台恢复
* **科学计时**：每组训练打卡后自动触发组间休息倒计时，支持针对大重量复合动作与孤立动作自定义休息时长。
* **静默守护**：基于时间戳算法（Timestamp-based）设计，即便倒计时期间将 App 切换至后台或意外杀后台，重新打开时依然能静默精准恢复剩余倒计时与未保存草稿，训练不中断。

### 📈 2. 多维度进阶图表与估算 1RM 趋势
* **估算 1RM (One Rep Max)**：根据经典公式自动计算每一组训练的力量极限潜能，清晰展示您的绝对力量成长曲线。
* **三模图表切换**：支持在 **单次最大重量 (Max Weight)**、**估算 1RM (Estimated 1RM)**、**单次训练总容量 (Total Volume)** 三种核心维度之间自由切换。
* **核心肌群雷达图**：直观展示胸、背、腿、肩、臂等不同部位的训练容量分配，帮您精准诊断偏科与短板。

### 📅 3. 热力日历与肌肉打卡看板 (Workout Calendar & Heatmap)
* 像查看 GitHub 代码提交记录一样查看您的**健身热力图**！
* 直观追踪每月举铁频次与连续训练天数，点击任意日期即可回顾当天的完整训练清单、组数细节及配重突破。

### 🏋️‍♂️ 4. 自定义动作库管理 (Exercise Library)
* **自由扩展**：支持批量添加自定义训练动作，支持选填英文图标标识（如 `squat`、`bench_press`），未填时系统能够智能补全。
* **多维筛选**：动作按**身体部位**（胸/背/腿/肩/手/核心）、**训练器材**（杠铃/哑铃/绳索/固定器械/自重）、**记录模式**（重量×次数/仅次数/仅计时）分类分组，一目了然。

### 🧮 5. 杠铃片智能配重辅助 (Plate Calculator)
* 冲极限重量不知如何装片？内建实用杠铃片计算器！
* 设定目标总重量与空杆重量，一键计算单侧需要挂载的各规格杠铃片数量，告别心算心累。

---

## 🛠️ 技术架构与现代开发栈 (Tech Stack)

本项目完全采用现代 Android 顶级开发标准构建，遵循 **MVVM / Clean Architecture** 分层规范，保证极致流畅与高可维护性：

| 技术模块 | 使用的技术与工具 | 说明 |
| :--- | :--- | :--- |
| **开发语言** | [Kotlin 2.0+](https://kotlinlang.org/) | 100% Kotlin 编写，充分发挥协程 (Coroutines) & Flow 的异步流威力 |
| **UI 框架** | [Jetpack Compose](https://developer.android.com/jetpack/compose) | 现代声明式 UI，搭配 Material Design 3 设计系统与深色模式 |
| **本地持久化** | [Room DB + KSP](https://developer.android.com/training/data-storage/room) | 强类型映射本地 SQLite 数据库，实现毫秒级增删改查与联表复杂统计 |
| **状态与路由** | ViewModel + StateFlow + Navigation | 响应式单向数据流 (UDF)，流畅进行页面级与模态框交互 |
| **图表可视化** | Custom Compose Canvas | 基于原质化底层 Canvas 深度定制绘制炫酷趋势折线图、阴影渐变与雷达图 |

---

## 🚀 快速开始 (Getting Started)

### 📱 方式一：免编译开箱即用（推荐广大健身铁友，开屏即练！）
**不需要懂任何代码编程或安装繁琐的开发软件！**
1. 在本仓库文件列表中，点击进入 **`APK安装包/`** 文件夹（或点击网页最侧边栏的 **Releases 发行版**）。
2. 下载最新版本安装包 **`PureLift-v1.0.0-release.apk`** 到您的安卓手机。
3. 点击直接安装，开屏立刻开始热血举铁打卡！

### 💻 方式二：开发者源码编译与构建
#### 环境要求
* **Android Studio**: Koala / Ladybug 或更高版本
* **JDK**: JDK 17+
* **Gradle**: 8.0+
* **Min SDK**: Android 8.0 (API 26+)
* **Target SDK**: Android 14+ (API 34+)

#### 本地编译步骤
1. 克隆本仓库到本地：
   ```bash
   git clone https://github.com/Fuhrer-Meiji/FitnessTracker_maniu.git
   cd FitnessTracker_maniu/FitnessTracker
   ```
2. 使用 Android Studio 打开 `FitnessTracker` 目录。
3. 等待 Gradle 依赖同步完成。
4. 连接您的 Android 手机或开启模拟器，点击绿色 **Run** ▶️ 按钮即可马上开练！
5. 如需生成发布版 APK，可执行：
   ```bash
   ./gradlew assembleDebug
   ```
   生成的 APK 文件位于 `app/build/outputs/apk/debug/app-debug.apk`。

---

## 🤝 贡献与反馈 (Contributing & Feedback)

本项目为开发者自用纯净开源版本。欢迎热爱健身与编程的小伙伴随时提交 Issues 讨论训练心得、交互细节优化或提出 PR！
* 如果这个项目能帮您在健身房里多举起 5 公斤，请给仓库点一颗亮晶晶的 **⭐️ Star**！

---

<div align="center">
  <p>Made with 💪 and ❤️ by <b>Fuhrer-Meiji</b> & Antigravity</p>
  <p><i>"The Iron never lies to you."</i></p>
</div>
