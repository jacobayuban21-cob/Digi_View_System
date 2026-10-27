# Graph Report - Digi_View_System  (2026-10-16)

## Corpus Check
- 60 files · ~57,758 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 86 file(s) not represented in the graph (top: .xml 51, .bat 10, .properties 8)

## Summary
- 883 nodes · 1865 edges · 68 communities (15 shown, 53 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 71 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `8ba21218`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DigiViewLMS
- 🚀 DigiView LMS — Release Notes (v1.1.0 — Minor Feature Release)
- ApiClient
- DigiViewApi
- Override
- .getProfile
- UserEntity
- Program
- MainActivity
- SoundManager
- QuizRecordEntity
- QuizRecordDto
- QuizRecord
- list
- QuizRecordRepository
- RegisterRequest
- 🔎 Detailed Folder & File Breakdown
- ScrollableContentPanel
- LeaderboardEntryDto
- .paintInputBackground
- CorsConfig.java
- UserRepository
- AchievementEntity
- Question
- DatabaseSyncTest.java
- ApiResponse
- RoundedComboBox
- org.springframework.web.bind.annotation.GetMapping
- Achievement
- Lesson
- org.springframework.http.ResponseEntity
- DigiViewApplication.java
- KeepAliveService.java
- LeaderboardController.java
- 🌐 DigiView Learning Management System (LMS)
- AuthRequest
- MouseEvent
- QuizRecord
- .componentResized
- Animation.AnimationListener
- 🏛️ DigiView Learning Management System - Architecture & Project Structure
- FocusAdapter
- ObjectInputStream
- AuthRequest
- RegisterRequest
- User
- .windowClosed
- model/ApiResponse.java
- 🚀 How to Run Each Component
- 🚀 Easy Android Commands (Zero Setup Required)
- 2. ☁️ Setting Up Supabase & Spring Boot Backend
- ❓ Beginner FAQ & Troubleshooting
- AGENTS.md

## God Nodes (most connected - your core abstractions)
1. `DigiViewLMS` - 107 edges
2. `MainActivity` - 58 edges
3. `UserEntity` - 35 edges
4. `ApiClient` - 30 edges
5. `QuizRecordEntity` - 27 edges
6. `QuizRecord` - 26 edges
7. `UserRepository` - 24 edges
8. `UserProfileDto` - 22 edges
9. `JPanel` - 22 edges
10. `JPanel` - 22 edges

## Surprising Connections (you probably didn't know these)
- `1. Root Files & Master CLI` --references--> `DigiView`  [INFERRED]
  PROJECT_STRUCTURE.md → src/System/DigiView.java
- `MainActivity` --references--> `Lesson`  [EXTRACTED]
  android/app/src/main/java/com/digiview/lms/MainActivity.java → shared/com/digiview/core/model/Lesson.java
- `MainActivity` --references--> `Question`  [EXTRACTED]
  android/app/src/main/java/com/digiview/lms/MainActivity.java → shared/com/digiview/core/model/Question.java
- `DigiViewLMS` --references--> `Lesson`  [EXTRACTED]
  src/System/DigiView.java → shared/com/digiview/core/model/Lesson.java
- `DigiViewLMS` --references--> `Question`  [EXTRACTED]
  src/System/DigiView.java → shared/com/digiview/core/model/Question.java

## Import Cycles
- None detected.

## Communities (68 total, 53 thin omitted)

### Community 0 - "DigiViewLMS"
Cohesion: 0.05
Nodes (17): User, AnswerOption, AvatarBadge, DigiView, DigiViewLMS, java.awt.event.MouseAdapter, JPanel, JPanel (+9 more)

### Community 1 - "🚀 DigiView LMS — Release Notes (v1.1.0 — Minor Feature Release)"
Cohesion: 0.17
Nodes (12): 🎨 1. Centralized Assets & Brand Visuals, 🔀 2. Root Redirection & Symbol Aliasing (`@`), 🎵 3. High-Fidelity Audio & Touch Effects, 📱 4. Mobile & Desktop Experience Parity, ☁️ 5. Cloud Backend & Supabase Database Auto-Sync, 🔒 6. Security & Quiz Progression Hardening, 🛠️ 8. Master CLI, Diagnostics, & Build Systems, 🚀 DigiView LMS — Release Notes (v1.1.0 — Minor Feature Release) (+4 more)

### Community 2 - "ApiClient"
Cohesion: 0.12
Nodes (4): ApiClient, AuthResult, ConnectionTestResult, CustomTlsSocketFactory

### Community 3 - "DigiViewApi"
Cohesion: 0.12
Nodes (3): ApiClient, DigiViewApi, LeaderboardEntry

### Community 4 - "Override"
Cohesion: 0.16
Nodes (6): Callback<ApiResponse<User>>, Callback<ApiResponse<User>>, Callback<ApiResponse<QuizRecord>>, Callback<ApiResponse<UserProfile>>, UserEntry, UserProfile

### Community 16 - "QuizRecordRepository"
Cohesion: 0.23
Nodes (4): DataInitializer, UserController, AchievementRepository, QuizRecordRepository

### Community 18 - "🔎 Detailed Folder & File Breakdown"
Cohesion: 0.33
Nodes (6): 1. Root Files & Master CLI, 2. Configuration & Persistent Data (`config/`), 3. Automation & Packaging Scripts (`scripts/`), 4. Desktop Application (`src/System/`), 6. Android Mobile Client (`android/`), 🔎 Detailed Folder & File Breakdown

### Community 23 - ".paintInputBackground"
Cohesion: 0.16
Nodes (3): RoundedComboBox, RoundedPasswordField, RoundedTextField

### Community 26 - "UserRepository"
Cohesion: 0.18
Nodes (3): AuthController, QuizController, UserRepository

### Community 40 - "🌐 DigiView Learning Management System (LMS)"
Cohesion: 0.22
Nodes (9): 1. 💻 Running the Desktop App (Instant - No Setup Required), 3. 📦 Sharing With Other PCs (Zero Java Required), 🌐 DigiView Learning Management System (LMS), Option A: The One-Click Launcher (Easiest), Option B: In VS Code / Antigravity IDE, Option C: In Apache NetBeans, ⚡ Quick Start: Choose What You Want to Do, 🛠️ REST API Quick Reference (+1 more)

### Community 48 - "🏛️ DigiView Learning Management System - Architecture & Project Structure"
Cohesion: 0.33
Nodes (3): 🏛️ DigiView Learning Management System - Architecture & Project Structure, 🌐 System Architecture Diagram, 📂 Visual Directory Tree

### Community 63 - "🚀 How to Run Each Component"
Cohesion: 0.40
Nodes (5): A. Run Desktop App, B. Run Backend Server (Locally), C. Deploy Backend to Render (Cloud Hosting), D. Distribute Desktop App to Other PCs, 🚀 How to Run Each Component

### Community 64 - "🚀 Easy Android Commands (Zero Setup Required)"
Cohesion: 0.40
Nodes (5): 1. The One-Click APK Builder, 2. The Android Management Toolkit, 3. Opening in Android Studio, 4. 📱 Android App Port & Easy Build Commands, 🚀 Easy Android Commands (Zero Setup Required)

### Community 65 - "2. ☁️ Setting Up Supabase & Spring Boot Backend"
Cohesion: 0.40
Nodes (5): 2. ☁️ Setting Up Supabase & Spring Boot Backend, Step 1: Create Your Supabase Database (Free), Step 2: Configure Your Credentials Securely, Step 3: Start the Backend Server Locally, Step 4: Deploying to Render (Free 24/7 Cloud Hosting)

### Community 66 - "❓ Beginner FAQ & Troubleshooting"
Cohesion: 0.50
Nodes (4): ❓ Beginner FAQ & Troubleshooting, "Does the desktop app break if the server is off?", "What if my classmate opens this in NetBeans with Java 8?", "Where do I see full documentation on every file?"

## Knowledge Gaps
- **53 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DigiViewLMS` connect `DigiViewLMS` to `Lesson`, `Override`, `ScrollableContentPanel`, `DigiView.java`, `.paintInputBackground`, `Question`?**
  _High betweenness centrality (0.310) - this node is a cross-community bridge._
- **Should `DigiViewLMS` be split into smaller, more focused modules?**
  _Cohesion score 0.05428796223446106 - nodes in this community are weakly interconnected._
- **Why does `MainActivity` connect `MainActivity` to `Lesson`, `Override`, `SoundManager`, `MainActivity.java`, `.showThemedAlertDialog`, `Question`, `.switchPage`?**
  _High betweenness centrality (0.107) - this node is a cross-community bridge._
- **Should `ApiClient` be split into smaller, more focused modules?**
  _Cohesion score 0.11517165005537099 - nodes in this community are weakly interconnected._
- **Why does `ApiClient` connect `ApiClient` to `System/ApiClient.java`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Should `DigiViewApi` be split into smaller, more focused modules?**
  _Cohesion score 0.11692307692307692 - nodes in this community are weakly interconnected._
- **Should `.getProfile` be split into smaller, more focused modules?**
  _Cohesion score 0.10153846153846154 - nodes in this community are weakly interconnected._