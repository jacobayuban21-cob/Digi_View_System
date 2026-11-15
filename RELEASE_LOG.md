# DigiView LMS Release Log

This log summarizes shipped releases and the operational impact of each
release. For the detailed feature history, see
[RELEASE_NOTES.md](RELEASE_NOTES.md). For itemized changes, see
[CHANGELOG.md](CHANGELOG.md).

## v1.1.2 - Quiz UX and Session Persistence

**Release date:** 2026-11-15  
**Release type:** Minor UI and quiz-experience update

### Release highlights

1. **Brighter interface theme**
   - Adjusted desktop and mobile background/theme colors for improved
     readability.

2. **Locked quiz flow**
   - While a quiz is active, navigation away is blocked until the user finishes
     or exits the quiz.

3. **Improved heart-depletion handling**
   - When hearts run out, users can choose **Try Again** (same difficulty) or
     **Exit to Home** via themed confirmation dialogs.
   - Android quiz hub state resets correctly after exiting to home.

4. **Hard quiz typed input**
   - Hard/Advanced quiz questions now require typing the missing code instead of
     multiple-choice selection.
   - Quick-access symbol buttons were added to make mobile code entry easier.

5. **1-day login sessions**
   - Desktop and Android now persist login sessions for approximately 24 hours,
     reducing repeated login prompts until session expiry or logout.

### Version alignment

- Desktop UI/runtime logs: `v1.1.2`
- Android: version name `1.1.2`, version code `4`
- Backend Gradle artifacts: `1.1.2`

## v1.1.1 - Local Server and Distribution Documentation

**Release date:** 2026-10-27  
**Release type:** Minor maintenance and developer-experience release

### Release highlights

1. **One backend command**
   - Use `digiview server` to start the local Spring Boot backend.
   - The previous duplicate `digiview local` and `digiview local-server`
     commands have been removed.

2. **Localhost and LAN support**
   - The backend listens on `0.0.0.0:8085`.
   - Desktop clients can use `http://localhost:8085`.
   - Other computers and physical Android devices can use the displayed LAN
     URL, such as `http://192.168.1.10:8085`.
   - Android emulators continue to use `http://10.0.2.2:8085/`.

3. **Release artifact guidance**
   - README documentation now explains `DigiViewSystem.jar`,
     `DigiView-Portable`, `DigiView-Portable.zip`,
     `DigiView-Android.apk`, `DigiView-Backend.jar`, and
     `backend\build\libs\app.jar`.
   - Missing JAR, APK, ZIP, or bundled-JRE behavior is documented with
     recovery commands.

4. **Application version alignment**
   - Desktop UI and runtime logs now report v1.1.1.
   - Android reports version name `1.1.1` with version code `3`.
   - Backend Gradle artifacts now use version `1.1.1`.

5. **Android local-server configuration**
   - The Android login screen now exposes the current server URL.
   - Users can test and save a laptop LAN URL without editing desktop
     configuration files.

6. **Authentication behavior**
   - Duplicate usernames now show an explicit registration error.
   - Android login and registration password fields include show/hide controls.

### Verification

- CLI help confirms the unified `server` command.
- Backend tests passed with Gradle.
- Updated desktop API client compilation passed.
- Batch labels and documentation references were checked.

## v1.1.0 - 2026-10-15

See [RELEASE_NOTES.md](RELEASE_NOTES.md) for the full release history and
feature details.
