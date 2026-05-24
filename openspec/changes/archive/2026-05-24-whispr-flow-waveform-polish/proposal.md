## Why

The current bubble flow works, but it still looks heavier and less refined than Whispr Flow, and the recording waveform is only a canned animation. The user wants the overlay to feel much closer to the Whispr Flow reference and to have the waveform visibly react to live voice volume.

## What Changes

- Refine the three-part recording and processing controls so their proportions, spacing, colors, and icon weight feel much closer to the Whispr Flow reference.
- Drive the recording waveform from live microphone amplitude instead of a fixed looping animation.
- Keep the existing IME-driven visibility, repeatable recording loop, and drag behavior while improving visual polish and motion quality.

## Capabilities

### New Capabilities

### Modified Capabilities

- `floating-dictation-overlay`: Refine the active control visuals and require the recording waveform to respond to live speaking amplitude.

## Impact

- Affects overlay rendering and animation in `FloatingDictationOverlay`.
- Affects recording integration in `TemporaryAudioRecorder`, `DictationCoordinator`, and `VerballyAccessibilityService`.
- Adds unit tests for live amplitude normalization/smoothing and updated overlay metrics.
