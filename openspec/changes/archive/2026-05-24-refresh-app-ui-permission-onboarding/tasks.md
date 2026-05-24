## 1. Permission onboarding

- [x] 1.1 Add a deterministic permission setup step model for microphone, overlay, accessibility, and complete states.
- [x] 1.2 Render only the next missing permission in onboarding with concise Traditional Chinese copy.
- [x] 1.3 Route each step to the correct Android permission or settings surface.
- [x] 1.4 Refresh permission state on app resume so returning from Android settings advances to the next step.
- [x] 1.5 Keep accessibility restricted-settings recovery available with a clearly visible App Info button.

## 2. App UI and settings

- [x] 2.1 Apply the Verbally app-icon blue as the Material app theme color.
- [x] 2.2 Replace the always-visible onboarding tab with a first-run/repair permission flow.
- [x] 2.3 Keep History and Settings as the primary post-setup destinations.
- [x] 2.4 Split API settings into Transcribe and second-pass cleanup subpages.

## 3. Verification and delivery

- [x] 3.1 Add unit and instrumentation coverage for permission step ordering, settings subpages, app shell navigation, and resume refresh.
- [x] 3.2 Run OpenSpec validation, unit tests, debug build, and targeted emulator instrumentation.
- [x] 3.3 Install the resulting debug APK on the approved real Android device without clearing data or running device tests.
