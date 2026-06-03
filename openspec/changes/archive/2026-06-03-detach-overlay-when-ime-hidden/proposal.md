## Why

Verbally currently keeps its overlay root attached after the input method is hidden, using alpha and touchability flags to make the bubble invisible. That prevents touch interception, but Android can still treat Verbally as displaying over other apps and show the system overlay notification even when the bubble is not needed.

## What Changes

- Restore input-method-driven overlay teardown when the keyboard closes or a sensitive context hides the bubble.
- Keep the existing idle bubble size, active three-part controls, waveform styling, haptics, drag behavior, and anchored expand/collapse animation.
- Preserve in-place layout animation while the overlay is visible; only the hidden/not-needed state should detach from `WindowManager`.
- Keep the dictation, insertion, cleanup, and history pipeline unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `floating-dictation-overlay`: hidden input-method states should remove the overlay root instead of retaining an invisible attached overlay.

## Impact

- `FloatingDictationOverlay` show/hide lifecycle.
- Focused overlay visibility tests and OpenSpec requirements.
- No provider, recording, cleanup, insertion, or persistence changes.
