# 健身记录 App 设计文档

## 概述

一款面向个人的 Android 健身记录应用，帮助用户记录力量训练数据、追踪进度、管理训练计划和身体数据。所有数据存储在本地。

## 设计语言

**高级简约 (Premium Minimal)** 设计风格：

### 设计令牌系统

所有视觉属性统一通过设计令牌管理，确保全局一致性：

| 令牌 | 默认值 | 用途 |
|------|--------|------|
| `--primary` | #6C63FF (紫罗兰) | 主色，按钮、导航指示器、活动态 |
| `--primary-light` | #8B83FF | 主色浅色变体 |
| `--primary-bg` | #f0eeff | 主色背景态，标签、图标容器 |
| `--bg` | #f8f8fc | 页面背景 |
| `--card-bg` | #ffffff | 卡片背景 |
| `--text` | #1a1a2e | 主文本 |
| `--text-secondary` | #8e8ea0 | 次要文本 |
| `--text-tertiary` | #b0b0c0 | 第三级文本 |
| `--border` | #eeeef4 | 边框、分隔线 |
| `--danger` | #ff6b6b | 删除/危险操作 |
| `--success` | #2ecc71 | 完成态 |
| `--radius` | 14px | 卡片圆角 |
| `--radius-sm` | 10px | 小组件圆角 |

### 字体层级

- `section-title`: 17px Bold, letter-spacing -0.3px
- `stat-value`: 26px Bold, letter-spacing -0.5px
- `card-title`: 15px Semibold, letter-spacing -0.2px
- `card-sub`: 11px, text-secondary
- `stat-label`: 11px, text-secondary

### 间距规范

- 页面内容: 18px 左右内边距
- 卡片内填充: 16px
- 卡片间距: 10px
- 内容区顶部: 4px margin + 14px 标题区

### 主题颜色 (8 套预设)

用户可在设置中选择 8 套主题色，全局实时切换：

1. **紫罗兰** (#6C63FF) — 默认
2. **翡翠绿** (#2ECC71)
3. **天空蓝** (#3498DB)
4. **日落橙** (#E67E22)
5. **玫瑰红** (#E74C3C)
6. **暗夜黑** (#1a1a2e)
7. **石墨灰** (#4A4A4A)
8. **珊瑚粉** (#FF6B6B)

每个主题包含 primary/primaryLight/primaryBg 三个色值，分别用于主色、浅色变体和背景态。

### 交互反馈

- Stepper 按钮按压态：背景填满 primary 色，文字变白
- 按钮按压：scale(0.97) + opacity 微降
- 页面切换：fadeUp 动画 (0.3s ease)
- 底部导航：选中态顶部 activity indicator bar (2.5px 高，primary 色)
- 底部弹窗：slideUp 动画 (0.35s cubic-bezier)

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **数据库**: Room (SQLite)
- **架构**: MVVM (ViewModel + Repository + Room DAO)
- **导航**: Navigation Compose + Bottom Navigation
- **图表**: Vico (Compose 原生图表库)
- **构建**: Gradle + Kotlin DSL

## 导航结构

底部 3 个标签页：

| 标签 | 功能 |
|------|------|
| 🏋️ 训练 | 开始训练、查看今日/历史训练 |
| 📈 进度 | 力量趋势图表、训练频率统计 |
| ⚙️ 设置 | 身体数据、动作库管理、单位设置、数据导出 |

## 数据模型

### Exercise（训练动作）
- `id: Long` (主键)
- `name: String` — 动作名称
- `bodyPart: String` — 身体部位（胸/背/腿/肩/手臂/核心/全身）
- `recordType: String` — 记录类型（strength=重量×次数 / reps=仅次数 / duration=计时秒数）
- `iconName: String` — 卡通图标文件名
- `isPreset: Boolean` — 是否为预设动作
- `createdAt: Long` — 创建时间

### Workout（训练记录）
- `id: Long` (主键)
- `date: Long` — 训练日期（时间戳）
- `startTime: Long` — 开始时间
- `endTime: Long` — 结束时间
- `note: String?` — 备注

### WorkoutSet（训练组）
- `id: Long` (主键)
- `workoutId: Long` — 关联训练记录 (FK → Workout)
- `exerciseId: Long` — 关联动作 (FK → Exercise)
- `setNumber: Int` — 组号
- `recordType: String` — 记录类型（strength/reps/duration）
- `weight: Double?` — 重量 kg（仅 strength 类型）
- `reps: Int?` — 次数（仅 strength/reps 类型）
- `durationSeconds: Int?` — 时长秒数（仅 duration 类型）
- `restSeconds: Int?` — 组间休息（秒）

### BodyMetric（身体数据）
- `id: Long` (主键)
- `date: Long` — 记录日期
- `weight: Double?` — 体重 (kg)
- `bodyFat: Double?` — 体脂率 (%)
- `note: String?` — 备注

## 页面详细设计

### 1. 训练页 (WorkoutScreen)

**状态:**
- 未开始训练 → 显示大按钮 "开始训练" + 当月月历
- 训练中 → 显示当前正在记录的动作、组数列表、快速点按面板
- 查看历史 → 点击月历某天查看该日训练详情

**月历组件:**
- 按月切换（上/下月箭头）
- 有训练记录的日期显示彩色圆点标记
- 点击有记录的日期展开当天训练摘要

**训练流程:**
1. 点击"开始训练" → 计时开始，自动创建第一个动作卡片（默认卧推）
2. 快速点按面板录入每组：根据动作类型显示不同录入字段
   - strength：重量 stepper + 次数 stepper
   - reps：仅次数 stepper
   - duration：时长(秒) stepper
3. 点击"完成本组"将组数据添加到当前卡片
4. 点击"添加新动作" → 从动作库选择新动作 → 新卡片追加在下方，旧卡片标记为已完成
5. 每张卡片独立管理自己的组数和录入字段
6. 点击"结束训练"保存所有卡片数据到数据库

### 2. 进度页 (ProgressScreen)

- 顶部统计卡片：本周训练次数、本月训练总次数、总训练时长
- 动作选择下拉框：选择某个动作查看力量趋势
- 力量趋势折线图（Vico）：X 轴日期，Y 轴最大重量/最大预估 1RM
- 训练频率热力图：小方块展示过去 3 个月每天是否训练

### 3. 设置页 (SettingsScreen)

分区列表：
- **身体数据**: 录入/查看体重、体脂率的历史记录
- **动作库管理**: 查看所有动作，新增自定义动作，编辑/删除自定义动作
- **单位设置**: 切换 kg ↔ lb
- **导出数据**: 导出训练记录为 CSV 文件
- **关于**: 版本信息

## 预设动作库

按部位分类预设约 30+ 个常见动作，每个动作配有一个卡通风格 SVG/Vector 图标。

### 胸部
卧推、上斜卧推、下斜卧推、飞鸟、上斜飞鸟、绳索夹胸

### 背部
引体向上、杠铃划船、哑铃划船、高位下拉、坐姿划船、硬拉

### 腿部
深蹲、前蹲、腿举、腿弯举、腿屈伸、弓步蹲、罗马尼亚硬拉

### 肩部
推举、侧平举、前平举、面拉、反向飞鸟

### 手臂
二头弯举、锤式弯举、三头下压、窄距卧推、法式弯举

### 核心
卷腹、平板支撑、举腿、俄罗斯转体

## 错误与边界状态

| 场景 | 处理 |
|------|------|
| 首次使用/无数据 | 空状态提示 + 引导开始第一次训练 |
| 动作库为空 | 提示"暂无动作"，提供"从预设导入"按钮 |
| 无训练记录 | 月历无标记，进度页显示"开始你的第一次训练吧" |
| 正在训练中退出 App | 自动保存当前训练进度（草稿状态） |
| 删除训练记录 | 确认弹窗 + 级联删除关联的训练组 |

## 非功能需求

- 支持中文界面
- 重量单位支持 kg / lb 切换
- 数据导出为 CSV 格式
- 自动保存训练草稿（防止意外退出丢失数据）
