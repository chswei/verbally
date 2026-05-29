## Why

Verbally currently uses one light Material color scheme, so the app cannot respect users who prefer dark mode or system-driven appearance. Adding an appearance mode in Settings makes this a normal app preference instead of a hidden platform assumption.

## What Changes

- Add an appearance section inside the `設定` screen opened from the hamburger drawer, with three radio-button theme modes: follow system, light, and dark.
- Persist the selected appearance mode locally as soon as the user taps a radio option.
- Apply the selected mode to the main Compose Material theme so Home, Settings, History, Dictionary, Snippets, and Style screens switch together.
- Keep visible UI copy in Traditional Chinese.

## Capabilities

### New Capabilities
- `app-appearance-mode`: Covers the user-facing appearance mode setting, local persistence, and global app theme application.

### Modified Capabilities

## Impact

- Affected code: `MainActivity.kt`, `AppSettings.kt`, `EncryptedSettingsRepository.kt`, Settings/shell UI tests, and theme-related unit tests.
- No backend, account, sync, or provider-key behavior changes.
- No new runtime permissions.
