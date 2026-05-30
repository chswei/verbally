## Why

First-time users can complete permissions and API setup but still fail their first dictation without a clear, visible next step. The Home setup also saves keys without confirming whether the selected provider accepts them.

## What Changes

- Add API key test actions to the Home transcription and text-processing setup panels.
- Show user-visible dictation completion, fallback, and error messages from the floating service instead of relying on accessibility content descriptions only.
- Localize the Traditional Chinese style labels as `正式` and `口語`.
- Add confirmation before deleting individual history entries.
- Update README usage notes so provider options and insertion behavior match the current app.
- Do not add a Home permission checklist, do not change snippet expansion behavior, and do not change dictionary copy in this change.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `local-history-and-settings`: Home provider setup gains per-panel API key testing, and history-entry deletions require confirmation.
- `floating-dictation-overlay`: Clipboard fallback and provider errors are visible to users, while successful direct insertion stays silent.
- `app-style-profiles`: Traditional Chinese style controls show `正式`/`口語` while preserving Formal/Casual semantics.

## Impact

- Affected code: `MainActivity.kt`, provider/client support, accessibility service feedback, localized string resources, focused unit and Compose tests.
- Affected docs: `README.md`.
- Network impact: API test buttons make lightweight provider calls only when the user taps the test action.
