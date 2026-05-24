## Context

The previous UI placed onboarding, settings, and history at the same navigation level. That was useful during early development, but it exposed setup mechanics after the app was already configured. The settings page also mixed transcription and cleanup provider fields, which made API setup feel dense.

Android permission behavior adds another constraint: microphone can use a runtime dialog, but overlay and accessibility permissions require system settings. The app must therefore keep copy honest about the extra tap and refresh state when the user returns.

## Goals / Non-Goals

**Goals:**
- Make the app feel like a normal Android app after setup: History and Settings are the primary destinations.
- Show only the next missing permission during onboarding.
- Advance automatically when the user returns from overlay or accessibility settings and the required state is now granted.
- Split API configuration into Transcribe and second-pass cleanup subpages.
- Keep Traditional Chinese copy concise and aligned with actual Android behavior.

**Non-Goals:**
- Bypassing Android restricted-settings or overlay permission behavior.
- Adding account, backend, cloud sync, or an IME keyboard.
- Changing the floating-bubble runtime behavior beyond permission readiness checks.

## Decisions

### Use a sequential permission state machine

`PermissionGuidance.nextSetupStep` derives the current setup step from microphone, overlay, and accessibility state. The UI renders only that step, with the button routed to the correct Android permission surface.

Alternative considered:
- Show all permission cards at once. Rejected because it made setup feel busier and repeated instructions that are only relevant later.

### Refresh permission state on lifecycle resume

`PermissionScreen` observes `ON_RESUME` and re-reads microphone, overlay, and accessibility state. This handles Android settings round trips where the app itself does not receive a direct activity result.

Alternative considered:
- Ask the user to press a "completed" button after each permission. Rejected because returning from Settings should be enough when Android already changed the state.

### Treat restricted accessibility settings as recovery guidance

Restricted-settings copy remains visible only on the accessibility step. The App Info action uses an outlined full-width button so it reads as an actionable recovery path without competing with the primary "open settings" action.

### Separate API setup by mental model

Settings now exposes an overview page with two API subpages: Transcribe for OpenAI speech-to-text configuration, and second-pass cleanup for provider/model cleanup configuration. This mirrors how the pipeline works and keeps each form short.

## Risks / Trade-offs

- Overlay and accessibility settings screens vary by OEM. The app can guide to Android settings and refresh on return, but it cannot guarantee a one-tap toggle on every device.
- Lifecycle refresh may run more often than strictly necessary, but the permission checks are cheap and keep the UI from going stale after system settings changes.
