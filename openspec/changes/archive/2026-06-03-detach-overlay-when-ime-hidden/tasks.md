## 1. Tests

- [x] 1.1 Add or update focused overlay lifecycle tests that express hidden states as detached roots, not retained transparent roots.
- [x] 1.2 Keep IME-only visibility policy tests passing without adding editable-focus heuristics.

## 2. Implementation

- [x] 2.1 Change `FloatingDictationOverlay.hide()` to remove the attached root for normal hidden states while preserving state cleanup.
- [x] 2.2 Keep visible-state expand/collapse animation, haptics, sizing, waveform, and drag behavior unchanged.

## 3. Validation

- [x] 3.1 Run focused unit tests for overlay visibility/lifecycle behavior.
- [x] 3.2 Run `openspec validate --all --strict`, `./gradlew testDebugUnitTest`, `./gradlew assembleDebug`, and `git diff --check`.
