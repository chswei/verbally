## 1. Visibility Policy

- [x] 1.1 Add a testable overlay visibility policy that separates show, hide, and keep decisions.
- [x] 1.2 Cover LINE-style passive editable focus, editable field clicks, keyboard activation, stale System UI focus, and non-editable windows with unit tests.

## 2. Accessibility Integration

- [x] 2.1 Wire `VerballyAccessibilityService` to the visibility policy.
- [x] 2.2 Expose the overlay shown state to the service without changing recording behavior.
- [x] 2.3 Include the accessibility event types required for click, focus, content, text, and keyboard-driven activation flows.

## 3. Verification

- [x] 3.1 Run `openspec validate --all --strict`.
- [x] 3.2 Run `./gradlew testDebugUnitTest`.
- [x] 3.3 Run `./gradlew assembleDebug`.
- [x] 3.4 Install the debug APK on the Samsung S23 and manually verify LINE and Google Search widget behavior.
