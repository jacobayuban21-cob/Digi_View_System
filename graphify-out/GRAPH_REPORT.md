# Graph Report - Digi_View_System  (2026-10-27)

## Corpus Check
- 60 files · ~58,550 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 86 file(s) not represented in the graph (top: .xml 51, .bat 10, .properties 8)

## Summary
- 890 nodes · 1862 edges · 62 communities (10 shown, 52 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 71 edges (avg confidence: 0.86)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d155d33a`
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
- .run
- ScrollableContentPanel
- LeaderboardEntryDto
- Override
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
- AuthRequest
- MouseEvent
- QuizRecord
- .componentResized
- Animation.AnimationListener
- FocusAdapter
- ObjectInputStream
- AuthRequest
- RegisterRequest
- User
- .windowClosed
- model/ApiResponse.java
- AGENTS.md

## God Nodes (most connected - your core abstractions)
1. `DigiViewLMS` - 107 edges
2. `MainActivity` - 58 edges
3. `UserEntity` - 35 edges
4. `ApiClient` - 31 edges
5. `QuizRecordEntity` - 27 edges
6. `UserRepository` - 24 edges
7. `QuizRecord` - 23 edges
8. `JPanel` - 22 edges
9. `JPanel` - 22 edges
10. `UserProfileDto` - 22 edges

## Surprising Connections (you probably didn't know these)
- `1. Root Files & Master CLI` --references--> `DigiView`  [INFERRED]
  PROJECT_STRUCTURE.md → src/System/DigiView.java
- `MainActivity` --references--> `Question`  [EXTRACTED]
  android/app/src/main/java/com/digiview/lms/MainActivity.java → shared/com/digiview/core/model/Question.java
- `MainActivity` --references--> `Lesson`  [EXTRACTED]
  android/app/src/main/java/com/digiview/lms/MainActivity.java → shared/com/digiview/core/model/Lesson.java
- `QuizRecordRepository` --references--> `QuizRecordEntity`  [EXTRACTED]
  backend/src/main/java/com/digiview/repository/QuizRecordRepository.java → backend/src/main/java/com/digiview/entity/QuizRecordEntity.java
- `UserProfileDto` --references--> `QuizRecordDto`  [EXTRACTED]
  backend/src/main/java/com/digiview/dto/UserProfileDto.java → backend/src/main/java/com/digiview/dto/QuizRecordDto.java

## Import Cycles
- None detected.

## Communities (62 total, 52 thin omitted)

### Community 0 - "DigiViewLMS"
Cohesion: 0.05
Nodes (17): AnswerOption, AvatarBadge, DigiView, DigiViewLMS, java.awt.event.MouseAdapter, JPanel, JPanel, GradientPanel (+9 more)

### Community 1 - "🚀 DigiView LMS — Release Notes (v1.1.0 — Minor Feature Release)"
Cohesion: 0.04
Nodes (45): A. Run Desktop App, B. Run Backend Server (Locally), C. Deploy Backend to Render (Cloud Hosting), D. Distribute Desktop App to Other PCs, 🏛️ DigiView Learning Management System - Architecture & Project Structure, 🚀 How to Run Each Component, 🌐 System Architecture Diagram, 📂 Visual Directory Tree (+37 more)

### Community 2 - "ApiClient"
Cohesion: 0.11
Nodes (4): ApiClient, AuthResult, ConnectionTestResult, CustomTlsSocketFactory

### Community 3 - "DigiViewApi"
Cohesion: 0.12
Nodes (3): ApiClient, DigiViewApi, LeaderboardEntry

### Community 4 - "Override"
Cohesion: 0.16
Nodes (6): Callback<ApiResponse<User>>, Callback<ApiResponse<User>>, Callback<ApiResponse<QuizRecord>>, Callback<ApiResponse<UserProfile>>, UserEntry, UserProfile

### Community 14 - "QuizRecord"
Cohesion: 0.10
Nodes (3): AchievementRegistry, QuizRecord, User

### Community 16 - "QuizRecordRepository"
Cohesion: 0.24
Nodes (4): DataInitializer, UserController, AchievementRepository, QuizRecordRepository

### Community 18 - ".run"
Cohesion: 0.15
Nodes (6): 1. Root Files & Master CLI, 2. Configuration & Persistent Data (`config/`), 3. Automation & Packaging Scripts (`scripts/`), 4. Desktop Application (`src/System/`), 6. Android Mobile Client (`android/`), 🔎 Detailed Folder & File Breakdown

### Community 26 - "UserRepository"
Cohesion: 0.20
Nodes (3): AuthController, QuizController, UserRepository

### Community 32 - "RoundedComboBox"
Cohesion: 0.17
Nodes (3): RoundedComboBox, DefaultListCellRenderer, javax.swing.plaf.basic.BasicComboBoxUI

## Knowledge Gaps
- **52 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DigiViewLMS` connect `DigiViewLMS` to `RoundedComboBox`, `.paintComponent`, `ScrollableContentPanel`, `DigiView.java`, `Override`?**
  _High betweenness centrality (0.302) - this node is a cross-community bridge._
- **Should `DigiViewLMS` be split into smaller, more focused modules?**
  _Cohesion score 0.05320634920634921 - nodes in this community are weakly interconnected._
- **Why does `MainActivity` connect `MainActivity` to `Lesson`, `Override`, `SoundManager`, `MainActivity.java`, `.showThemedAlertDialog`, `Question`, `.switchPage`?**
  _High betweenness centrality (0.099) - this node is a cross-community bridge._
- **Should `🚀 DigiView LMS — Release Notes (v1.1.0 — Minor Feature Release)` be split into smaller, more focused modules?**
  _Cohesion score 0.041666666666666664 - nodes in this community are weakly interconnected._
- **Why does `ApiClient` connect `ApiClient` to `System/ApiClient.java`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Should `ApiClient` be split into smaller, more focused modules?**
  _Cohesion score 0.11205073995771671 - nodes in this community are weakly interconnected._
- **Should `DigiViewApi` be split into smaller, more focused modules?**
  _Cohesion score 0.11692307692307692 - nodes in this community are weakly interconnected._