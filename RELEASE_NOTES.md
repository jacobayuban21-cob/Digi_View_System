# 🚀 DigiView LMS — Release Notes (v1.0.0 — Initial Release)

**Release Date:** October 14, 2026  
**Version:** `1.0.0` (Initial Production Release)  
**Branch:** `dev`  
**Live Backend Service:** [https://digi-view-system.onrender.com](https://digi-view-system.onrender.com)  
**Distribution Artifacts:**
- 📦 **Desktop Portable ZIP:** `dist/DigiView-Portable.zip` (Self-contained JRE + Standalone JAR)
- ☕ **Desktop Runnable JAR:** `dist/DigiViewSystem.jar`
- 📱 **Android Package:** `dist/DigiView-Android.apk`

---

## 🌟 Executive Summary

The **DigiView LMS v1.0.0 Initial Release** establishes the complete educational learning ecosystem across Desktop, Mobile, and Cloud. Both the **Desktop Swing application** and the **Android mobile app** feature 100% visual and functional parity—including shared audio engines, unified dark-mode themes, automatic database sync with Supabase PostgreSQL, official logo integration, and a universal **Root Redirection system** allowing symbol-based path aliasing (`@`).

---

## 🎨 1. Centralized Assets & Brand Visuals

* **Unified `public/` Folder:**
  * Consolidated all sound and branding assets under the project root [`public/`](public/):
    * [`public/Icons/`](public/Icons/): `Favicon.ico`, `Icon.ico`, `Icon.jpg`, and `Splash-icon.png`.
    * [`public/sounds/`](public/sounds/): Ambient music (`freesound_community-relax-32200.mp3/.wav`), selection click (`mixkit-select-click-1109.wav`), and key type effect (`mixkit-single-key-type-2533.wav`).
* **Android Application Identity:**
  * Official app name updated to **DigiView** (launcher label & title).
  * High-res launcher and round icon mapped to `@drawable/ic_launcher_dv` (`Splash-icon.png`).
  * Replaced all text-based `"DV"` badge placeholders in [`activity_main.xml`](android/app/src/main/res/layout/activity_main.xml) and [`layout_auth.xml`](android/app/src/main/res/layout/layout_auth.xml) with the official crisp logo image.
* **Desktop Swing Visuals:**
  * Dynamic `loadAppIcon()` sets high-resolution application and taskbar icons.
  * Enhanced `LogoBadge` component to render the rounded, anti-aliased official logo image directly in the sidebar navigation and authentication cards.
* **Backend Server & Web Dashboard:**
  * Added dedicated [`favicon.ico`](backend/src/main/resources/static/favicon.ico) and `<link rel="icon">` tags to the interactive system dashboard.
  * Replaced the header emoji globe in [`index.html`](backend/src/main/resources/static/index.html) with the official DigiView splash logo.
  * Implemented an explicit `/favicon.ico` REST endpoint in [`HealthController.java`](backend/src/main/java/com/digiview/controller/HealthController.java) returning `image/x-icon`.

---

## 🔀 2. Root Redirection & Symbol Aliasing (`@`)

A universal path-resolution and routing system has been implemented across the entire stack, enabling any symbol alias (especially `@`) to reference the root directory:

* **Web & Backend Routing:**
  * Navigating to `/@`, `/@/`, `/~`, or `/root` immediately redirects to the root landing dashboard (`/`).
  * Sub-paths prefixed with symbols (e.g. `/@/health`, `/@/api/health`) automatically redirect to their clean counterparts (`/health`, `/api/health`).
  * Resource handlers map `/@/**`, `/public/**`, `/Icons/**`, and `/sounds/**` directly to the project's root `public/` directory.
* **Desktop Path Resolver:**
  * Implemented `DigiViewLMS.resolveRoot(path)` and `resolveRootStream(path)`:
    ```java
    File audio = DigiViewLMS.resolveRoot("@/public/sounds/mixkit-select-click-1109.wav");
    InputStream icon = DigiViewLMS.resolveRootStream("@/public/Icons/Splash-icon.png");
    ```
* **API Client (`ApiClient.java`):**
  * Added `redirectRootUrl(endpoint)` so endpoints can be referenced with root symbols (e.g. `ApiClient.redirectRootUrl("@/api/auth/login")`).
* **Unified Master CLI (`digiview.bat`):**
  * Entering `digiview @`, `digiview ~`, or `digiview /@` directly launches the main interactive menu.

---

## 🎵 3. High-Fidelity Audio & Touch Effects

* **Relaxing Ambient Music Auto-Play:**
  * Automatically plays soothing background study music (`freesound_community-relax-32200`) on app startup on both Desktop and Android.
  * Includes persistent toggle controls in both interfaces (sidebar button on Desktop, audio switch on Mobile).
* **Consistent Tap & Click Feedback:**
  * **Desktop:** Preloads audio bytes into a pool of 6 audio clips to eliminate audio latency during rapid button presses.
  * **Mobile:** Utilizes Android's `SoundPool` with an instant fallback `MediaPlayer` and subtle haptic vibration, resolving previous touch-effect dropouts.
  * Keyboard typing sounds integrated with text inputs.

---

## 📱 4. Mobile & Desktop Experience Parity

* **Full 25-Question Quizzes on Mobile:**
  * Ported the comprehensive 25-question bank (Easy, Medium, Hard) from Desktop to the Android quiz engine.
* **Active Answer Selection Indicators:**
  * Selecting an option on Android now illuminates the answer card with a high-contrast cyber blue gradient, vibrant cyan border glow, and check icon indicator matching the desktop feel.
* **Consistent Confirm & Alert Dialogs:**
  * Modern dark-themed, glassmorphic confirmation dialogs for both Desktop Swing and Android (`dialog_confirm.xml`).
  * Centered positioning, keyboard navigation (`Enter` to confirm, `Escape` to cancel), and tactile sound effects.

---

## ☁️ 5. Cloud Backend & Supabase Database Auto-Sync

* **Live Cloud Deployment:**
  * Default API URL set to the active Render cloud service:  
    `https://digi-view-system.onrender.com`
* **Automated Data Persistence:**
  * Removed the need for manual server saving—user profiles, quiz history records, and unlocked achievements automatically synchronize with the database on action completion.
* **Backend Architecture Improvements:**
  * Database initialization seeder ([`DataInitializer.java`](backend/src/main/java/com/digiview/config/DataInitializer.java)) ensuring schema compliance.
  * Cleaned Hibernate dialect configuration and tuned HikariCP connection pool settings.
  * Integrated Keep-Alive heartbeat service to ensure rapid Render server responsiveness.

---

## 🛠️ 6. Master CLI & Build Systems

* **The Unified CLI (`digiview.bat`):**
  * Interactive menu and one-line terminal commands:
    ```cmd
    digiview run        # Launch Desktop Swing LMS
    digiview server     # Start Spring Boot Server (:8085)
    digiview apk        # Build Android APK to dist\
    digiview install    # Install APK onto connected Android device
    digiview mobile     # Build, install, and launch on Android
    digiview dist       # Build all distributions (JAR, ZIP, APK)
    digiview health     # Query live server telemetry & database status
    ```
* **Release Artifacts Generated:**
  * Standalone Portable Release: `dist/DigiView-Portable.zip` (includes native `DigiView.exe` launcher with embedded icon, `DigiViewSystem.jar`, and trimmed portable JRE).
  * Android Release Build: `dist/DigiView-Android.apk` (assembled and verified).

---

## 🔒 6. Security & Quiz Progression Hardening

* **Removal of Default Login Autofill:**
  * Completely removed the `student / admin123` quick-fill helper and prompt from mobile and desktop authentication screens.
  * Ensures students and instructors perform authentic credential-based entry.
* **Strict Quiz Difficulty Unlocking (70% Passing Threshold):**
  * **Beginner / Easy:** Open by default for all learners.
  * **Intermediate / Medium:** Locked until the learner scores at least **70%** on the Beginner / Easy Quiz.
  * **Advanced / Hard:** Locked until the learner scores at least **70%** on the Intermediate / Medium Quiz.
  * Visual lock badges and clear guidance are shown across both Desktop and Android interfaces. Attempting to enter locked difficulties triggers a themed informative alert.
* **Native Windows Portable Executable (`DigiView.exe`):**
  * Replaced the `.bat` console launcher with a compiled native Windows GUI application (`DigiView.exe`).
  * Embeds the official DigiView icon directly into the executable file and launches silently without opening a terminal window.
* **Backend Cloud Resilience on Render:**
  * Configured explicit PostgreSQL dialect (`org.hibernate.dialect.PostgreSQLDialect`) to prevent dialect detection crashes during startup.
  * Configured HikariCP pool resilience (`initialization-fail-timeout=-1`, `prepareThreshold=0`, `keepalive-time=30000`) for seamless compatibility with Supabase's transaction poolers.

---

## 📋 Git Commit Log (v1.0.0 — Initial Release)

| Commit | Summary |
| :--- | :--- |
| `fdb4021` | Apply official image logos to Android, Desktop UI, and favicon to backend server |
| `0758eb1` | Implement root redirection with symbol alias `@` across web, desktop, and mobile |
| `0959dcc` | Integrate public sounds and icons across desktop, packager, and Android launcher |
| `d68610e` | Automated release synchronization |
| `0e37786` | Configure default cloud backend URL to live deployed Render server |
| `0e2746c` | Fix touch click effects consistency and auto-play background music on Mobile and Desktop |
| `b712374` | Fix desktop confirm dialog placement, replicate 25 quiz questions, add selection indicators |
| `53a075d` | Resolve achievement entity seeding compilation error in DataInitializer |
| `94db269` | Add custom dark-themed confirmation and alert dialogs for Desktop and Mobile |
| `c507842` | Add database DataInitializer seeder and auto-sync user to Supabase on login |
| `b71b0cb` | Clean up Hibernate dialect and open-in-view warnings in backend |
| `542a716` | Rename Android app to DigiView |

---

*DigiView Learning Management System — Developed by the DigiView Team.*
