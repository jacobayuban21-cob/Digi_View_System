# DigiView LMS Changelog

All notable project changes are recorded here. Dates use the project release
calendar in `YYYY-MM-DD` format.

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
