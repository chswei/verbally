## Why

The floating dictation bubble currently appears from a mix of editable-field focus, click, and input-method signals, which can make it appear or remain visible at the wrong time when Android accessibility focus is stale. The bubble should instead track the IME window directly so it appears only while the keyboard is visible.

## What Changes

- Show the floating dictation bubble when the accessibility service observes an input-method window.
- Hide the floating dictation bubble when the input-method window is no longer observed.
- Remove editable-click and focused-editable heuristics from bubble visibility decisions.
- Preserve the existing recording, confirmation, cancellation, transcription, cleanup, paste, and history behavior.

## Capabilities

### New Capabilities

### Modified Capabilities
- `floating-dictation-overlay`: Bubble visibility changes from editable-field activation heuristics to IME-window-only visibility.

## Impact

- Affected code: `OverlayVisibilityPolicy`, `VerballyAccessibilityService`, and existing overlay visibility unit tests.
- No new runtime permissions, dependencies, backend services, or provider API changes.
