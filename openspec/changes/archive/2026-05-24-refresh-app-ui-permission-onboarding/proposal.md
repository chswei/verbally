## Why

Verbally's first-run and settings UI was still shaped like an implementation checklist: permissions were visible as a persistent tab, API settings were crowded into one page, and Android permission flows did not always advance after the user returned from system settings. This made the app feel harder than necessary to set up, especially compared with modern Android apps that guide one permission at a time.

## What Changes

- Apply the Verbally app-icon blue as the app-wide Material theme color and simplify the main app shell to History and Settings after onboarding.
- Convert permission setup into a one-step-at-a-time Android onboarding flow for microphone, overlay, and accessibility permissions.
- Refresh permission state on app resume so returning from Android settings automatically advances to the next required step.
- Split API settings into Transcribe and second-pass cleanup subpages.
- Keep exceptional accessibility guidance available without repeating the main instructions, including a clear outlined App Info button for restricted settings recovery.

## Capabilities

### Modified Capabilities

- `floating-dictation-overlay`: Permission readiness is now guided by a sequential onboarding flow that advances as Android permissions/settings are granted.
- `local-history-and-settings`: Settings navigation now separates Transcribe and second-pass cleanup configuration while preserving local-only history/settings behavior.

## Impact

- Affects `MainActivity` Compose UI, permission-step decision logic, and UI/instrumentation tests.
- Adds lifecycle-based permission refresh for Android settings round trips.
- Keeps delivery install-only on the approved real device after emulator validation.
