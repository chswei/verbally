## Why

The floating dictation bubble should feel faster and more polished when the keyboard appears, disappears, and enters recording. The current overlay removes and rebuilds window roots for visibility and state changes, which makes the interaction less like a continuous floating control.

## What Changes

- Keep the overlay root attached while the bubble is eligible, using alpha and touchability updates to show or hide it without leaving an invisible touch blocker.
- Restyle the idle bubble as a floating rounded-square control with larger corners, softer elevation, translucent surfaces, and Verbally brand colors.
- Change the recording state into a right-edge-expanding three-part control row with cancel, refined waveform capsule, and confirm controls sized to the observed Wispr Flow proportions.
- Animate state transitions on the retained overlay root, using a stable right-edge outer motion frame for ready/recording/processing so platform window move animations do not make controls slide in from the screen edge.
- Refine the recording waveform to use fewer, wider Wispr-like animated bars with restrained amplitude response while preserving Verbally brand colors.
- Preserve the existing recording, transcription, cleanup, insertion, and history pipeline.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `floating-dictation-overlay`: refine overlay visibility, idle styling, recording controls, and waveform requirements.

## Impact

- Affected overlay code:
  - `app/src/main/java/com/verbally/app/overlay/FloatingDictationOverlay.kt`
  - `app/src/main/java/com/verbally/app/overlay/OverlayAnimatedViews.kt`
  - `app/src/main/java/com/verbally/app/overlay/OverlayRootTransitionPolicy.kt`
  - `app/src/main/java/com/verbally/app/overlay/OverlayVisualDefaults.kt`
  - `app/src/main/java/com/verbally/app/overlay/OverlayColorDefaults.kt`
- Affected service code may include overlay show/hide call sites in `VerballyAccessibilityService.kt`, without changing dictation orchestration.
- Affected tests:
  - overlay visual defaults
  - overlay root transition policy
  - overlay show/hide and touchability behavior where feasible
- No backend, provider, transcription, cleanup, insertion, or storage changes.
