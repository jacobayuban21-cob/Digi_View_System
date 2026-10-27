# 🌐 DigiView Learning Management System (LMS)

[![Java](https://img.shields.io/badge/Java-8%20to%2021+-orange?logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-Supabase%20PostgreSQL-3ECF8E?logo=supabase)](https://supabase.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Android-blue)](https://github.com)

Welcome to **DigiView LMS**! A complete, modern educational application featuring an **interactive desktop LMS**, a **Spring Boot REST backend**, a **Supabase PostgreSQL database**, and an **Android mobile port** with matching dark-mode aesthetics and animations.

---

## 🆕 Latest Changes (v1.1.1)

The latest maintenance update improves local development and release
packaging documentation:

- Combined backend startup under the single `digiview server` command.
- The server now starts on `0.0.0.0:8085`, supporting both localhost and LAN
  clients.
- Added LAN URL output and desktop LAN server selection.
- Documented every `dist\` package and JAR, including missing-artifact
  behavior.
- Added [CHANGELOG.md](CHANGELOG.md), [RELEASE_LOG.md](RELEASE_LOG.md), and
  the v1.1.1 entry in [RELEASE_NOTES.md](RELEASE_NOTES.md).

---

## ⚡ Quick Start: Choose What You Want to Do

```
 ┌─────────────────────────────────────────────────────────────┐
 │                    WHAT DO YOU WANT TO DO?                  │
 ├──────────────────────────────┬──────────────────────────────┤
 │ 👑 ALL COMMANDS & MENU       │ ➡️ Run digiview.bat          │
 │ 🎮 Just play / test desktop  │ ➡️ digiview run (or scripts\run.bat) │
 │ ⚙️ Run Backend + Database    │ ➡️ digiview server           │
 │ 📱 Build Android APK         │ ➡️ digiview apk              │
 │ 📲 Install on Android Phone  │ ➡️ digiview install          │
 │ 📦 Package All Releases      │ ➡️ digiview dist             │
 │ 🩺 Check Server Health       │ ➡️ digiview health           │
 └──────────────────────────────┴──────────────────────────────┘
```

---

## 👑 The Unified Master CLI (`digiview`)

Manage the entire project, launch desktop & backend servers, compile APKs, and install to connected devices using a single unified command:

```cmd
:: Run with no arguments to open the interactive menu:
digiview

:: Or run direct one-liners from anywhere in the project:
digiview run        # Launch Desktop Swing GUI
digiview server     # Start local backend for localhost and LAN clients (:8085)
digiview apk        # Build Android APK to dist\DigiView-Android.apk
digiview install    # Install APK onto connected Android phone or emulator
digiview mobile     # Build, install, and launch on Android device
digiview dist       # Package ALL distributions (JAR, Portable ZIP, Android APK)
digiview health     # Check live server health, memory, and database status
digiview devices    # List connected Android devices and active emulators
digiview logs       # Stream live Android logcat output
digiview clean      # Clean temporary build caches (bin, dist, gradle)
digiview exit       # Exit CLI
```

---

## 1. 💻 Running the Desktop App (Instant - No Setup Required)

The desktop app works completely offline out-of-the-box!

### Option A: The One-Click Launcher (Easiest)
1. Run `digiview run` in your terminal or double-click [`scripts/run.bat`](scripts/run.bat).
2. The Swing window will open immediately!

### Option B: In VS Code / Antigravity IDE
1. Open [`src/System/DigiView.java`](src/System/DigiView.java).
2. Press **`F5`** on your keyboard (or click the **Run** button above line 29).

### Option C: In Apache NetBeans
1. In NetBeans, click **File** &rarr; **Open Project** &rarr; select `Digi_View_System`.
2. Press **`F6`** (or right-click the project and choose **Run**).
   > *Note: We configured the project for universal Java 8–21+ bytecode, so it compiles in NetBeans on any computer.*

---

## 2. ☁️ Setting Up Supabase & Spring Boot Backend

Follow these 3 simple steps to connect the app to the cloud database:

### Step 1: Create Your Supabase Database (Free)
1. Go to [supabase.com](https://supabase.com) and sign in.
2. Click **New Project** and name it `digiview`.
3. Copy your database password (you will need it in Step 2).
4. Once the project is ready, click **SQL Editor** on the left menu.
5. Open [`backend/schema.sql`](backend/schema.sql) from this project, copy all the SQL code, paste it into Supabase, and click **Run**.
   *(This creates the `users`, `quiz_records`, and `user_achievements` tables automatically!)*

---

### Step 2: Configure Your Credentials Securely
1. In the `backend/` folder, find the file:
   [`backend/.env.example`](backend/.env.example)
2. We already created [`backend/.env`](backend/.env) for you. Open it and replace:
   ```env
   SUPABASE_DB_URL=jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres?sslmode=require
   SUPABASE_DB_USER=postgres
   SUPABASE_DB_PASSWORD=YOUR_ACTUAL_PASSWORD_HERE
   PORT=8085
   ```
   > 🔒 **Security Notice:** The `.env` file is in `.gitignore`, which means Git will **never** upload your password to GitHub!

---

### Step 3: Start the Backend Server Locally
From the repository root, run:

```cmd
digiview server
```

You can also double-click [`backend/run-backend.bat`](backend/run-backend.bat).
The server command binds Spring Boot to `0.0.0.0:8085`, so the same server is
available from this computer through `localhost` and from other devices on the
same network through the displayed LAN IPv4 address.

You will see:
```text
===================================================
  DigiView - Spring Boot & Supabase Backend Server
===================================================
[INFO] Loading database credentials from .env...
...
Started DigiViewApplication in 2.5 seconds (process running at port 8085)
```
Your backend is now live at `http://localhost:8085`.

For a different computer or a physical Android phone, use the LAN URL printed
by the command, for example `http://192.168.1.10:8085`. Enter that URL in the
desktop app's **LAN (:8085)** server preset or in the Android app's **SERVER
BASE URL** field. Android emulators should use `http://10.0.2.2:8085/`.
If Windows Firewall blocks the connection, run `digiview firewall` as
Administrator and allow TCP port 8085.

#### Physical Android phone local-server setup

1. Rebuild and install the Android APK after local-server changes:
   ```cmd
   digiview apk
   digiview install
   ```
2. Start the backend with `digiview server`.
3. On the phone's DigiView login screen, tap the **Server** bar. It is now
   visible and lets you enter, test, and save a server URL.
4. Enter the laptop's IPv4 address, not `localhost`:
   ```text
   http://192.168.1.11:8085/
   ```
   Replace `192.168.1.11` with the address printed by `digiview server`.
5. Tap **Test Server**, confirm `Server is online (HTTP 200)`, then tap
   **Save**.

If the laptop is sharing its internet connection through Windows Mobile
Hotspot, use the laptop's **Mobile Hotspot adapter address** (commonly
`192.168.137.1`) instead of its normal Wi-Fi address. The phone must be
connected to that hotspot, and Windows Firewall must allow inbound TCP port
8085. A phone and laptop being connected to the same Wi-Fi name is not
enough if the router has client isolation enabled.

The Android login and registration screens also provide show/hide password
controls. Registration rejects an existing username instead of silently
creating a suffixed username; use a different username when the server
reports that the account is already taken.

---

### Step 4: Deploying to Render (Free 24/7 Cloud Hosting)

The backend is pre-configured for **Render** via [`render.yaml`](render.yaml) and [`backend/Dockerfile`](backend/Dockerfile):

1. Go to [render.com](https://render.com) and sign in.
2. Click **New +** &rarr; select **Blueprint** (or **Web Service**).
3. Connect your GitHub repository (`Digi_View_System`).
4. Render automatically detects [`render.yaml`](render.yaml), sets the Docker runtime, and configures the `/health` endpoint.
5. In the **Environment Variables** prompt, fill in your Supabase credentials:
   * `SUPABASE_DB_URL` &rarr; Your Supabase JDBC URL
   * `SUPABASE_DB_USER` &rarr; `postgres`
   * `SUPABASE_DB_PASSWORD` &rarr; Your Supabase database password
6. Click **Apply / Deploy**.
7. Render will build the lightweight container and launch your backend with free SSL at:
   🌐 `https://digi-view-system.onrender.com`

> 💡 **Tip:** Render monitors [`/health`](/health) automatically. When accessed in your web browser, `/` and `/health` deliver an interactive **Cyber Health Dashboard** featuring live JVM telemetry, database connection monitoring, and a retro **Cyber Dino Arcade Runner** minigame!

---

## 3. 📦 Sharing With Other PCs (Zero Java Required)

If you need to send the desktop app to a classmate, teacher, or another computer that **does not have Java installed** (or has an outdated Java version like Java 8):

1. Run `digiview dist` or double-click [`scripts/build-release.bat`](scripts/build-release.bat).
2. It generates a self-contained bundle at:
   📁 **`dist\DigiView-Portable.zip`** *(~22 MB)*
3. Send this ZIP file to anyone.
4. They just unzip it and double-click **`DigiView.bat`**.
   * It uses its own bundled, portable Java 17 engine.
   * **Zero installation and zero administrator rights required!**

### What is inside `dist\`?

Run `digiview dist` to build the desktop, Android, and backend release
artifacts. The output directory can contain the following packages:

| Package | What it contains and does | How it is used |
| :--- | :--- | :--- |
| `dist\DigiViewSystem.jar` | The runnable desktop Swing application. It contains the compiled desktop classes, icons, and sound resources. It is built for Java 8 bytecode and can run on newer Java versions. | Run with `java -jar dist\DigiViewSystem.jar` from a machine with a compatible Java runtime, or use it through the portable bundle. |
| `dist\DigiView-Portable\` | The unpacked Windows desktop bundle. It normally contains `DigiView.exe`, `DigiView.bat`, `DigiView-Debug.bat`, `DigiViewSystem.jar`, application resources/configuration, and a bundled `jre\` directory when `jlink` succeeds. | Double-click `DigiView.exe` for the normal GUI, or `DigiView-Debug.bat` to stream runtime logs. |
| `dist\DigiView-Portable.zip` | A compressed copy of the `DigiView-Portable\` directory for sharing with another Windows computer. | Unzip it first, then run `DigiView.exe` or `DigiView.bat`; do not try to execute the JAR directly from inside the ZIP. |
| `dist\DigiView-Android.apk` | The compiled Android application package produced from `android/`. It contains the mobile UI and Android client code, but not the Spring Boot server or Supabase database. | Install with `digiview install` or `adb install -r dist\DigiView-Android.apk`. |
| `dist\DigiView-Backend.jar` | The packaged Spring Boot production backend copied from `backend\build\libs\app.jar`. It contains the REST API and server health dashboard, and requires Java 17+ plus the configured Supabase environment variables. | Run with `java -jar dist\DigiView-Backend.jar`; set `PORT` and the `SUPABASE_DB_*` variables first. |

`backend\build\libs\app.jar` is the backend's Gradle build output. It is the
source file used to create `dist\DigiView-Backend.jar`; it is not the desktop
application JAR. The local development command `digiview server` uses Gradle
`bootRun`, so it does not require either backend JAR to already exist.

#### What happens when a package or JAR is missing?

* **`DigiViewSystem.jar` missing from the portable folder:** `DigiView.exe`
  displays a **File Missing** error and exits. `DigiView-Debug.bat` reports a
  Java classpath/file error instead of opening the desktop app. Re-run
  `digiview dist` or `scripts\build-release.bat` to rebuild it.
* **Bundled `jre\` missing:** the portable launcher tries to use a Java
  installation from `JAVA_HOME`, `JRE_HOME`, the Windows registry, standard
  Java installation folders, or `PATH`. If no compatible JVM is found, it
  displays a **Java Not Found** message and exits. The JAR itself is still
  usable on a machine with Java installed.
* **`DigiView-Portable.zip` missing:** the already-unpacked
  `dist\DigiView-Portable\` folder can still run. The ZIP is only the sharing
  archive and must be rebuilt if a new copy needs to be distributed.
* **`DigiView-Android.apk` missing:** `digiview install` stops with an APK-not-found
  error. `digiview mobile` or `digiview apk` builds the APK first; if the
  Android SDK/build fails, no installation or launch occurs.
* **`DigiView-Backend.jar` or `backend\build\libs\app.jar` missing:** desktop
  and Android clients can still use the cloud backend or an already-running
  local server, but the packaged backend cannot be started from that missing
  file. Run `digiview dist` or the backend Gradle `bootJar` task to recreate it.
  `digiview server` remains available because it starts the backend with
  `bootRun`.

The desktop source workflow is different from the packaged workflow:
`digiview run` compiles/launches the source project through
[`scripts/run.bat`](scripts/run.bat), so it does not need
`dist\DigiViewSystem.jar`. The portable executable and direct JAR workflow do
need the packaged desktop JAR described above.

---

## 4. 📱 Android App Port & Easy Build Commands

The native Android port is located in [`android/`](android/) and offers **100% feature and visual parity** with the desktop Swing application:
* **Identical Dark Design Tokens:** Matching deep blue palette (`#050F23`, `#071120`, `#0E1B30`, `#38BDF8`, `#2563EB`, `#22C55E`, `#EF4444`).
* **Complete 5-Tab Navigation:**
  * 📊 **Dashboard:** Hero banner, live stats (XP, Level, Modules, Attempts), and quick launch actions.
  * 📚 **Lessons:** 7 interactive HTML modules with pop-up syntax viewers and monospace code blocks.
  * 🎯 **Quiz Hub & Runner:** 3 difficulty tiers (Beginner, Intermediate, Advanced), live 5-hearts system, option card micro-animations, answer locks, and instant result cards.
  * 👤 **Profile:** Cyan-glow avatar, 2x2 account detail tiles, and level progress bar.
  * 📜 **History:** Past quiz attempts with status badges (`PASSED`, `FAILED`, `PERFECT`).
* **Offline-First & Cloud Sync:** Persists instantly to Android `SharedPreferences` and syncs asynchronously via Retrofit to the Spring Boot backend (`http://10.0.2.2:8085/` for an emulator, the LAN URL printed by `digiview server` for a physical phone, or your Render cloud URL).

---

### 🚀 Easy Android Commands (Zero Setup Required)

We created high-level automated scripts so you never have to remember complex Gradle flags or ADB commands:

#### 1. The One-Click APK Builder
Run `digiview apk` or double-click [`scripts/build-android.bat`](scripts/build-android.bat).
* Automatically detects your Android SDK and Java installation.
* Automatically generates `local.properties`.
* Compiles the Android project with Gradle.
* Packages the final APK into:
  📁 **`dist\DigiView-Android.apk`**

#### 2. The Android Management Toolkit
Run `digiview mobile` or double-click [`scripts/manage-android.bat`](scripts/manage-android.bat) (or run it in your terminal).

It offers an interactive menu, or you can run single-word commands:

| Command | What it does |
| :--- | :--- |
| `digiview apk` *(or `scripts\manage-android.bat build`)* | Compiles the Android app to `dist\DigiView-Android.apk` |
| `digiview mobile` *(or `scripts\manage-android.bat run`)* | Builds APK, installs it to your phone/emulator, and launches it automatically |
| `digiview install` *(or `scripts\manage-android.bat install`)* | Installs the existing `dist\DigiView-Android.apk` onto your connected device |
| `digiview devices` *(or `scripts\manage-android.bat devices`)* | Shows all connected Android phones or active emulators (`adb devices -l`) |
| `digiview logs` *(or `scripts\manage-android.bat logs`)* | Streams live app debug logs and crash diagnostics in real time (`adb logcat`) |
| `digiview clean` *(or `scripts\manage-android.bat clean`)* | Cleans Android Gradle caches and temporary build folders |

#### 3. Opening in Android Studio
1. Open **Android Studio**.
2. Select **Open an Existing Project** &rarr; select the `android/` folder inside `Digi_View_System`.
3. Click **Run ▶** to launch in the emulator or on your phone!

---

## 🛠️ REST API Quick Reference

When the Spring Boot backend is running, these endpoints are available:

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new student account |
| `POST` | `/api/auth/login` | Login with username and password |
| `GET` | `/api/users/{username}` | Fetch student profile, XP, and history |
| `POST` | `/api/quiz/record` | Save a completed or exited quiz attempt |
| `GET` | `/api/quiz/history/{username}` | Retrieve all attempts for a student |
| `GET` | `/api/leaderboard` | View top 50 students ranked by XP |

---

## ❓ Beginner FAQ & Troubleshooting

### "What if my classmate opens this in NetBeans with Java 8?"
* It will work! We wrote the source code in universal Java 8 syntax (`Arrays.asList`, `Collectors.toList()`, standard `switch`) and set `javac.source=1.8` in `nbproject/project.properties`.

### "Does the desktop app break if the server is off?"
* **No.** The desktop application has an **offline-first** design. If the server is offline, it automatically saves everything locally to `digiview-data.ser`. When the server is online, it syncs in the background with zero lag.

### "Where do I see full documentation on every file?"
* Check out the [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) file for a detailed diagram and file-by-file guide.
