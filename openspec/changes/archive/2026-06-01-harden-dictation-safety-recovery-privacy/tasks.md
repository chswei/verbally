## 1. Sensitive Context Hiding

- [x] 1.1 Add policy tests for password, numeric, phone, financial-app, and normal text-field visibility decisions.
- [x] 1.2 Implement a sensitive-input policy and wire it into accessibility overlay visibility routing.
- [x] 1.3 Add focused service or policy tests proving hidden sensitive contexts do not show a ready bubble.

## 2. Repair Bubble And Runtime Readiness

- [x] 2.1 Add tests for microphone, overlay, and accessibility recovery-state decisions.
- [x] 2.2 Implement repair overlay state, localized copy, and recovery actions.
- [x] 2.3 Update main app permission recovery surfaces for overlay/accessibility readiness where the overlay cannot be shown.

## 3. Local History Retention

- [x] 3.1 Add repository/settings tests for latest-100, 24-hour auto-delete, no-history mode, and cancellation of destructive changes.
- [x] 3.2 Implement history retention settings storage and repository enforcement.
- [x] 3.3 Add settings/history UI controls and localized disabled-history messaging.

## 4. Dictionary And Snippet Hardening

- [x] 4.1 Add repository tests for dictionary/snippet normalized conflict rejection.
- [x] 4.2 Add repository tests for duplicate-safe dictionary and snippet edits, including case/whitespace-only edits and duplicate loaded data.
- [x] 4.3 Implement validation results and normalized identity handling in dictionary/snippet repositories.
- [x] 4.4 Add UI validation messages for dictionary/snippet conflict and duplicate saves.

## 5. Verification And Delivery

- [x] 5.1 Run OpenSpec strict validation for all specs and this change.
- [x] 5.2 Run focused tests, unit tests, lint, and debug APK build.
- [x] 5.3 Perform code review and address critical/important findings.
- [x] 5.4 Install the latest debug APK to the approved connected phone.
