## 1. Settings Model And Theme Contract

- [x] 1.1 Add a failing unit test for `AppThemeMode` labels and default `AppSettings.themeMode`.
- [x] 1.2 Add `AppThemeMode` to `AppSettings` with Traditional Chinese labels and a `SYSTEM` default.

## 2. Settings UI

- [x] 2.1 Add a failing Compose test that drawer Settings shows `外觀模式`, radio choices for `跟隨系統`, `淺色`, and `深色`, and updates selected mode after choosing `深色`.
- [x] 2.2 Add the appearance mode radio group to drawer Settings and keep Home API setup free of appearance controls.

## 3. Theme Application And Persistence

- [x] 3.1 Add a failing Compose test that `VerballyTheme` uses the light scheme for light mode and the dark scheme for dark mode.
- [x] 3.2 Implement light/dark/system theme resolution and wire `MainActivity` to load `themeMode` from settings.
- [x] 3.3 Persist `themeMode` immediately through `EncryptedSettingsRepository` with safe fallback to `SYSTEM`.

## 4. Validation

- [x] 4.1 Run `openspec validate --all --strict`.
- [x] 4.2 Run `./gradlew testDebugUnitTest`.
- [x] 4.3 Run focused Settings Compose instrumentation on an Android target.
- [x] 4.4 Run `./gradlew assembleDebug`.
