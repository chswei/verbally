## Why

The current floating dictation bubble only swaps icons inside a single button, which does not match the faster three-part control flow the user expects from Whispr Flow. It also leaves the overlay in a post-processing state that makes repeated dictation in the same input session unreliable.

## What Changes

- Replace the recording and processing bubble UI with a three-part control row: cancel button, center status capsule, and trailing action/progress button.
- Animate the center recording waveform and the processing progress indicator to make active states legible without text.
- Reset the overlay to its original ready bubble immediately after processing finishes so the user can start the next dictation without reopening the input method.
- Preserve existing IME-driven show/hide behavior and drag-to-edge positioning.

## Capabilities

### New Capabilities

### Modified Capabilities

- `floating-dictation-overlay`: Change the overlay control layout and require the dictation lifecycle to return to a reusable ready state after each processing run.

## Impact

- Affects `FloatingDictationOverlay`, `VerballyAccessibilityService`, and overlay resources/drawables.
- Adds overlay-focused unit coverage for reusable state transitions and coordinator behavior.
- Updates the floating dictation overlay spec and tasks for the new lifecycle contract.
