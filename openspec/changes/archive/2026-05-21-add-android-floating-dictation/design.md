## Context

The repository starts empty. Verbally will be a new native Android app for Android 14+ that targets internal testing first. The core user experience is system-wide dictation through a floating overlay, not an IME keyboard: when an editable text field is focused, a bubble appears; the user records speech, taps a checkmark, and the app pastes cleaned text into the current cursor position.

The app must use user-supplied provider keys only. Audio is sent directly from the device to OpenAI for transcription, and transcript text is sent directly to the selected cleanup provider. The app does not operate a backend or account system.

## Goals / Non-Goals

**Goals:**

- Provide a working Android 14+ debug build with Traditional Chinese onboarding, settings, and history.
- Use AccessibilityService plus overlay windows to detect editable fields and show the dictation bubble.
- Use OpenAI `audio/transcriptions` for speech-to-text and OpenAI or Gemini for transcript cleanup.
- Paste cleaned text at the active cursor using clipboard plus accessibility paste, with best-effort clipboard restoration.
- Store encrypted API keys and a local latest-100 dictation history.
- Cover provider request construction, cleanup prompt behavior, paste fallback, and history retention with unit tests.

**Non-Goals:**

- No IME keyboard, realtime partial transcription, backend proxy, accounts, payments, cloud sync, local speech model, or Play Store production hardening in v1.
- No meeting diarization, file transcription import, command mode, or custom prompt marketplace.

## Decisions

1. Use Kotlin, Compose, AGP 9.0.1, Gradle 9.1.0, `compileSdk 36`, `targetSdk 36`, and `minSdk 34`.
   - Rationale: Android 14+ keeps the permission and background-service matrix smaller while supporting the requested modern device focus.
   - Alternative considered: Android 10+ for broader reach, rejected for v1 to reduce compatibility work.

2. Use AccessibilityService and overlay windows rather than an IME.
   - Rationale: the requested interaction matches Wispr Flow Android's floating bubble and avoids keyboard switching friction.
   - Alternative considered: custom IME, rejected for v1 because it changes the user's keyboard workflow and adds another integration surface.

3. Use clipboard plus `ACTION_PASTE` as the primary insertion path.
   - Rationale: paste is the most cursor-respecting cross-app mechanism available to an accessibility overlay. `ACTION_SET_TEXT` tends to replace the field and move the cursor to the end.
   - Alternative considered: direct node text mutation, retained only as a later research item.

4. Keep provider integration in small interfaces.
   - Rationale: transcription and cleanup have different providers and failure modes. Interfaces keep tests cheap and make future provider changes localized.
   - Alternative considered: a single network client, rejected because it couples audio upload, text cleanup, and model settings.

5. Store history locally with a hard cap of 100 entries.
   - Rationale: history is needed to recover failed paste operations and re-use recent dictations, while the cap keeps privacy and storage simple.
   - Alternative considered: no history, rejected because lost dictations are a core failure mode for this product.

## Risks / Trade-offs

- Accessibility permission is sensitive and may face Play Store review scrutiny -> keep permission copy explicit, limit data access to focused editable fields, and treat internal testing as the first release channel.
- Some apps may block paste actions or expose custom text fields poorly -> preserve cleaned text on clipboard and show a manual paste fallback.
- Clipboard restoration is best effort on modern Android -> disclose the paste mechanism in settings and avoid storing clipboard contents.
- Direct provider calls expose user API keys on device -> store keys encrypted and never log request headers or audio/transcript content.
- Large audio recordings can fail provider limits or drain battery -> enforce a recording duration limit and clear temporary audio after each attempt.

## Migration Plan

This is a new app, so no data migration is required. Internal testers install the debug APK, grant microphone, overlay, and accessibility permissions, enter BYOK credentials, and validate supported apps. Rollback is uninstalling the app, which removes local settings and history.

## Open Questions

None for v1. Later releases can evaluate an IME, realtime transcription, local models, custom cleanup styles, and Play Store production policy refinements.
