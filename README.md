# 🌐 DigiView Learning Management System (LMS)

[![Java](https://img.shields.io/badge/Java-8%20to%2021+-orange?logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-Supabase%20PostgreSQL-3ECF8E?logo=supabase)](https://supabase.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Android-blue)](https://github.com)

Welcome to **DigiView LMS**! A complete, modern educational application featuring an **interactive desktop LMS**, a **Spring Boot REST backend**, a **Supabase PostgreSQL database**, and an **Android mobile port** with matching dark-mode aesthetics and animations.

---

## ⚡ Quick Start: Choose What You Want to Do

```
 ┌─────────────────────────────────────────────────────────────┐
 │                    WHAT DO YOU WANT TO DO?                  │
 ├──────────────────────────────┬──────────────────────────────┤
 │ 🎮 Just play / test desktop  │ ➡️ Double-click run.bat      │
 │ 📦 Send app to another PC    │ ➡️ Run build-release.bat     │
 │ ⚙️ Run Backend + Database    │ ➡️ See Section 2 below       │
 │ 📱 Open Android app          │ ➡️ Open /android in Studio   │
 └──────────────────────────────┴──────────────────────────────┘
```

---

## 1. 💻 Running the Desktop App (Instant - No Setup Required)

The desktop app works completely offline out-of-the-box!

### Option A: The One-Click Launcher (Easiest)
1. Double-click [`run.bat`](file:///c:/Users/131fgh/Desktop/Digi_View_System/run.bat) in the project folder.
2. The Swing window will open immediately!

### Option B: In VS Code / Antigravity IDE
1. Open [`src/System/DigiView.java`](file:///c:/Users/131fgh/Desktop/Digi_View_System/src/System/DigiView.java).
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
5. Open [`backend/schema.sql`](file:///c:/Users/131fgh/Desktop/Digi_View_System/backend/schema.sql) from this project, copy all the SQL code, paste it into Supabase, and click **Run**.
   *(This creates the `users`, `quiz_records`, and `user_achievements` tables automatically!)*

---

### Step 2: Configure Your Credentials Securely
1. In the `backend/` folder, find the file:
   [`backend/.env.example`](file:///c:/Users/131fgh/Desktop/Digi_View_System/backend/.env.example)
2. We already created [`backend/.env`](file:///c:/Users/131fgh/Desktop/Digi_View_System/backend/.env) for you. Open it and replace:
   ```env
   SUPABASE_DB_URL=jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres?sslmode=require
   SUPABASE_DB_USER=postgres
   SUPABASE_DB_PASSWORD=YOUR_ACTUAL_PASSWORD_HERE
   PORT=8080
   ```
   > 🔒 **Security Notice:** The `.env` file is in `.gitignore`, which means Git will **never** upload your password to GitHub!

---

### Step 3: Start the Backend Server
Double-click [`backend/run-backend.bat`](file:///c:/Users/131fgh/Desktop/Digi_View_System/backend/run-backend.bat).

You will see:
```text
===================================================
  DigiView - Spring Boot & Supabase Backend Server
===================================================
[INFO] Loading database credentials from .env...
...
Started DigiViewApplication in 2.5 seconds (process running at port 8080)
```
Your backend is now live at `http://localhost:8080`!

---

## 3. 📦 Sharing With Other PCs (Zero Java Required)

If you need to send the desktop app to a classmate, teacher, or another computer that **does not have Java installed** (or has an outdated Java version like Java 8):

1. Double-click [`build-release.bat`](file:///c:/Users/131fgh/Desktop/Digi_View_System/build-release.bat).
2. It generates a self-contained bundle at:
   📁 **`dist\DigiView-Portable.zip`** *(~22 MB)*
3. Send this ZIP file to anyone.
4. They just unzip it and double-click **`DigiView.bat`**.
   * It uses its own bundled, portable Java 17 engine.
   * **Zero installation and zero administrator rights required!**

---

## 4. 📱 Android App Port

The native Android port is located in [`android/`](file:///c:/Users/131fgh/Desktop/Digi_View_System/android):
* **Same Design Tokens:** Identical dark palette (`#050F23`, `#071120`, `#0E1B30`, `#38BDF8`, `#2563EB`, `#22C55E`, `#EF4444`).
* **Interactive Quiz:** A/B/C/D option cards, live hearts, instant answer locks, and bounce animations.
* **Pre-configured Retrofit API:** Communicates with the Spring Boot backend (`10.0.2.2:8080` for emulator, or your PC's Wi-Fi IP address for physical devices).

### To open the Android app:
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
* Check out the [PROJECT_STRUCTURE.md](file:///c:/Users/131fgh/Desktop/Digi_View_System/PROJECT_STRUCTURE.md) file for a detailed diagram and file-by-file guide.
