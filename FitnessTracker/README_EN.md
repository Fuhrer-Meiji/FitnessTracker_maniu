<div align="right">
  <a href="README.md">🇨🇳 中文</a> | <strong>🇬🇧 English</strong>
</div>

# 🏋️‍♂️ MaNiu (马牛) - Strength Training & Workout Tracker

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Room Database](https://img.shields.io/badge/Room%20DB-Offline%20First-34A853?style=for-the-badge&logo=sqlite&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2F%20Clean-FF6F00?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**Purely Personal · Forever Free · Zero Ads & Offline · Instant Training**

*A minimalist, hardcore, offline-first Android strength training and workout tracker built with Jetpack Compose.*

---

</div>

## 📖 Philosophy

Tired of annoying pop-up ads, bloated social feeds, and expensive subscription models in mainstream fitness apps?

**MaNiu** is built upon the simplest belief: **Lifting weights should be pure and distraction-free**.
* 🚀 **Instant Launch**: No onboarding fluff or tutorials. Open the app and log your very first set in milliseconds.
* 🛡️ **100% Offline-First**: All workout logs are stored locally on your device in SQLite (Room DB). Zero cloud dependencies, zero privacy leaks, and lightning-fast responsiveness.
* 🎨 **Minimalist Aesthetics**: Crafted with sleek dark-mode aesthetics inspired by premium productivity tools, complete with micro-animations and intuitive card layouts.

---

## ✨ Key Features

### ⏱️ 1. Millisecond Rest Timer & Silent Background Recovery
* **Scientific Timing**: Automatically triggers an inter-set rest timer upon logging a set. Customizable durations for heavy compound lifts and isolation exercises.
* **Silent Guardian**: Built on a robust timestamp-based algorithm. Even if you switch apps or the system kills the background process, opening MaNiu instantly restores your timer and draft without missing a beat.

### 📈 2. Multi-Mode Progress Charts & Estimated 1RM
* **Estimated 1RM**: Automatically calculates your One Rep Max potential using classic sports science formulas to visualize true strength progression.
* **Three Chart Modes**: Seamlessly toggle between **Max Weight**, **Estimated 1RM**, and **Total Volume**.
* **Muscle Radar Chart**: Diagnose training volume distribution across chest, back, legs, shoulders, and arms to identify weak points.

### 📅 3. Workout Calendar & Muscle Heatmap
* Track your training consistency just like viewing GitHub commit logs!
* Visualize monthly workout frequency and streaks. Tap any date to review the complete log, set details, and weight breakthroughs for that session.

### 🏋️‍♂️ 4. Custom Exercise Library
* **Unlimited Expansion**: Easily add custom exercises with optional English icon identifiers (e.g., `squat`, `bench_press`). Smart auto-completion if left blank.
* **Multi-Dimensional Filtering**: Categorized by **Body Part** (Chest/Back/Legs/Shoulders/Arms/Core), **Equipment** (Barbell/Dumbbell/Cable/Machine/Bodyweight), and **Log Type** (Weight×Reps/Reps Only/Time Only).

### 🧮 5. Smart Barbell Plate Calculator
* Going for a PR and tired of math? Use the built-in plate calculator!
* Set your target weight and bar weight to instantly get the exact plate-loading breakdown per side.

---

## 🚀 Getting Started

### 📱 Method 1: Ready-to-Use APK (Recommended for Lifters!)
**No programming knowledge or IDE installation required!**
1. Navigate to the **`APK安装包/`** folder in this repository (or check the **Releases** sidebar on GitHub).
2. Download the latest installation file **`马牛-v1.0.0-release.apk`** to your Android device.
3. Install and start logging your heavy sets instantly!

### 💻 Method 2: Build from Source (For Developers)
#### Requirements
* **Android Studio**: Koala / Ladybug or newer
* **JDK**: JDK 17+
* **Gradle**: 8.0+
* **Min SDK**: Android 8.0 (API 26+)
* **Target SDK**: Android 14+ (API 34+)

#### Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/Fuhrer-Meiji/FitnessTracker_maniu.git
   cd FitnessTracker_maniu/FitnessTracker
   ```
2. Open the `FitnessTracker` directory in Android Studio.
3. Wait for Gradle sync to complete.
4. Connect your Android device or emulator and hit **Run** ▶️!
5. To generate a release APK manually:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🛠️ Tech Stack & Architecture

Built with modern Android industry standards following the **MVVM / Clean Architecture** principles:

| Module | Technologies / Tools | Description |
| :--- | :--- | :--- |
| **Language** | [Kotlin 2.0+](https://kotlinlang.org/) | 100% Kotlin utilizing Coroutines & Flow for async data streaming |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) | Modern declarative UI design with Material Design 3 and Dark Mode |
| **Persistence** | [Room DB + KSP](https://developer.android.com/training/data-storage/room) | Strongly-typed local SQLite database with millisecond CRUD & relational queries |
| **State & Routing** | ViewModel + StateFlow + Navigation | Reactive Unidirectional Data Flow (UDF) for smooth navigation & modals |
| **Visualization** | Custom Compose Canvas | Custom low-level Canvas rendering for smooth trend lines, gradients, and radar charts |

---

## 🤝 Contributing & Feedback

This is an open-source project created out of pure passion for lifting and coding. Issues and PRs are welcome!
* If this app helps you add 5kg to your bench press, please leave a shining **⭐️ Star** on this repo!

---

<div align="center">
  <p>Made with 💪 and ❤️ by <b>Fuhrer-Meiji</b> & Antigravity</p>
  <p><i>"The Iron never lies to you."</i></p>
</div>
