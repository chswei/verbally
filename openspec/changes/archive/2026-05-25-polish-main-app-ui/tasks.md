## 1. Tests

- [x] 1.1 Update main shell Compose tests to expect Traditional Chinese navigation, drawer, and menu semantics.
- [x] 1.2 Update Dictionary and Snippets tests to expect localized search placeholders, polished empty states, and accessible add actions.
- [x] 1.3 Update Home and History tests to expect polished Traditional Chinese visible states while preserving existing actions.

## 2. Main app UI implementation

- [x] 2.1 Add vector drawables for main app shell icons.
- [x] 2.2 Localize bottom navigation, drawer, search placeholders, and add action semantics in `MainActivity.kt`.
- [x] 2.3 Polish Home API setup with branded operation panels and stable primary actions.
- [x] 2.4 Polish Dictionary, Snippets, and History presentation without adding new data behavior.

## 3. Verification and delivery

- [x] 3.1 Validate OpenSpec artifacts with `openspec validate --all --strict`.
- [x] 3.2 Run `./gradlew testDebugUnitTest`.
- [x] 3.3 Run focused Compose instrumentation on an emulator when available.
- [x] 3.4 Run `./gradlew assembleDebug`.
- [x] 3.5 Install the debug APK to the currently connected approved device if one is available, install-only.
