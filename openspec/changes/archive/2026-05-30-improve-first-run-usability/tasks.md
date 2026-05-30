## 1. Provider API Key Testing

- [x] 1.1 Add failing unit tests for provider key test request construction, success, missing-key, and failure outcomes.
- [x] 1.2 Implement a lightweight provider key tester and wire it into `VerballyContainer`.
- [x] 1.3 Add Home transcription and text-processing test buttons with localized loading/result messages.
- [x] 1.4 Add/update Compose tests for the Home API test actions and result messages.

## 2. Dictation Feedback

- [x] 2.1 Add failing unit tests for routing successful dictation completion to the overlay only and failures to both the overlay and visible user message sink.
- [x] 2.2 Implement feedback routing in the accessibility service.

## 3. Safer Main-App Interactions

- [x] 3.1 Add/update Compose tests for Traditional Chinese style labels.
- [x] 3.2 Add/update Compose tests for history-entry delete confirmation.
- [x] 3.3 Implement localized style labels and history-entry delete confirmation.

## 4. Docs and Validation

- [x] 4.1 Update README to match direct insertion fallback and current provider options.
- [x] 4.2 Run OpenSpec validation, unit tests, focused Compose instrumentation where feasible, assemble the debug APK, and `git diff --check`.
