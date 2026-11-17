# DigiView LMS Changelog

All notable project changes are recorded here. Dates use the project release
calendar in `YYYY-MM-DD` format.

## [1.1.3] - 2026-11-17

### Added

- Desktop profile/history sync from server on login/session restore to improve
  desktop ↔ mobile history parity.

### Changed

- Bumped Desktop, Android, and Backend application versions to `1.1.3`.
- Incremented Android `versionCode` from `4` to `5`.

### Fixed

- Android local quiz history is now isolated per account, preventing new
  accounts from inheriting history from previously logged-in users.
- Responsive Android layout fixes for smaller screens: single-line top-bar
  version pill, tighter top-bar spacing, centered/narrower auth card,
  single-line server pill, and better bottom spacing so content is not clipped
  by the bottom navigation.

## [1.1.2] - 2026-11-15

### Added

- 1-day login session persistence on desktop and Android so users are not asked
  to sign in again until the session expires.
- Hard quiz typed-answer mode with quick-access symbol buttons for missing-code
  questions (especially for mobile input convenience).
- Heart-depletion choice dialog allowing users to **Try Again** at the same
  difficulty or **Exit to Home**.

### Changed

- Bumped Desktop, Android, and Backend application versions to `1.1.2`.
- Incremented Android `versionCode` from `3` to `4`.
- Brightened background/theme colors for both desktop and mobile interfaces.
- Locked quiz navigation during an active quiz; users must finish or exit the
  quiz before switching screens.
- Updated heart-depletion handling to use themed dialogs and reset quiz state
  correctly when returning home on Android.

## [1.1.1] - 2026-10-27

### Added

- LAN-aware local backend startup messages showing localhost and detected LAN
  URLs.
- Desktop LAN server preset for selecting the local machine's network address.
- Android server selector with LAN URL testing and persistent selection.
- Android password visibility toggles for login, registration, and confirmation.
- Desktop password visibility toggles for login, registration, and confirmation.
- Documentation for all `dist\` packages and JAR artifacts.
- Documentation describing the behavior when release artifacts are missing.
- Dedicated release documentation in `RELEASE_LOG.md`.

### Changed

- Bumped Desktop, Android, and Backend application versions to `1.1.1`.
- Incremented Android `versionCode` from `2` to `3`.
- Combined local backend startup into `digiview server`.
- Removed the duplicate `digiview local` and `digiview local-server` commands.
- Standardized local Spring Boot startup on port `8085`.
- Configured local Spring Boot startup to bind to `0.0.0.0` for localhost and
  LAN access.
- Updated Android and README instructions for emulator and physical-device
  server URLs.
- Rebuilt Android authentication flow so mobile clients can switch from the
  cloud URL to a local LAN backend.
- Registration now rejects an existing username instead of silently creating a
  suffixed account and logging into it.
- Desktop registration now follows the same duplicate-username behavior.

### Documentation

- Added the v1.1.1 summary to `README.md`.
- Added the v1.1.1 release entry to `RELEASE_NOTES.md`.

## [1.1.0] - 2026-10-15

- See [RELEASE_NOTES.md](RELEASE_NOTES.md) for the complete v1.1.0 release
  details.
