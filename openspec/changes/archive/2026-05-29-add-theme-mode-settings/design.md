## Context

The app-level Compose theme is currently a single `lightColorScheme` in `MainActivity.kt`. Settings are already loaded from `AppSettings` and saved through `EncryptedSettingsRepository`. The main `首頁` content is API setup, while the user-facing app `設定` entry lives in the hamburger drawer.

## Goals / Non-Goals

**Goals:**
- Add a visible Traditional Chinese appearance setting under the hamburger drawer's `設定` entry with `跟隨系統`, `淺色`, and `深色` choices.
- Persist the selected mode in the existing local settings store.
- Apply the selected mode to the app-wide Material theme so every main-app destination changes together.
- Keep the implementation small and testable without adding dependencies.

**Non-Goals:**
- Changing floating bubble overlay colors or runtime behavior.
- Adding Android dynamic color, custom per-screen palettes, or theme scheduling.
- Changing provider settings, history retention, dictionary, snippets, or style-profile behavior.

## Decisions

### Store theme mode in `AppSettings`

Add an `AppThemeMode` enum with `SYSTEM`, `LIGHT`, and `DARK`, then add `themeMode` to `AppSettings`. `EncryptedSettingsRepository` will read and write the enum name with a safe fallback to `SYSTEM`.

Alternative considered: a separate preferences repository. Rejected because this is a normal app setting and the existing repository already owns app settings persistence.

### Resolve the active color scheme inside `VerballyTheme`

`VerballyTheme` will accept `AppThemeMode` and use Compose's system dark-mode signal only when the setting is `SYSTEM`. It will choose between dedicated light and dark Material color schemes while preserving existing typography and shapes.

Alternative considered: rely only on Android XML day/night resources. Rejected because the visible app surface is Compose and already uses Material 3 color schemes directly.

### Add appearance controls to the drawer settings screen

The app shell will track whether the drawer settings screen is open. The hamburger drawer's `設定` item will show an `AppSettingsScreenContent` surface for appearance controls, while `首頁` continues to show only API setup. The control will use Material radio-button rows so users can see all three mutually exclusive modes and tap once to switch.

Alternative considered: a dropdown selector. Rejected because the desired interaction is a visible single-choice group, and theme mode should not require opening a menu.

### Save and apply theme changes immediately

The drawer Settings screen will save the selected theme mode as soon as the user taps a radio option. The root `MainActivity` state is updated at the same time, so the Material theme changes immediately across the app.

Alternative considered: keep a separate save button. Rejected because appearance mode is a preference toggle-style choice; requiring a second save step makes the interaction feel broken.

## Risks / Trade-offs

- **Immediate save could feel too chatty if paired with toast** -> Save silently on selection and reserve toast copy for explicit API settings saves.
- **Dark palette contrast could regress readability** -> Use Material roles with explicit dark colors and verify through Compose tests plus emulator build.
- **The current code uses Home as the settings target** -> Split drawer settings display from Home content and add shell tests that verify `設定` opens appearance settings.
