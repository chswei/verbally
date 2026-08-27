## Why

The idle floating bubble is rendered inside a stable active-width overlay window so state transitions remain visually anchored, but the transparent portion of that window still intercepts taps on underlying apps. The overlay must preserve its existing motion behavior while allowing touches outside visible controls to pass through.

## What Changes

- Restrict the visible overlay's touchable region to the bubble or controls that are actually shown.
- Keep the application-overlay window within Android's permitted obscuring-opacity threshold so touches outside that region are trusted for pass-through delivery.
- Recompute the touchable region after state, layout, edge, and configuration changes.
- Preserve the stable edge-anchored outer frame used by ready, recording, and processing transitions.
- Add focused geometry and device-facing coverage for touch pass-through behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `floating-dictation-overlay`: Require transparent non-interactive portions of a visible overlay window to pass touches through to the underlying app.

## Impact

- Affects overlay rendering and input-region management in `FloatingDictationOverlay`.
- Adds focused overlay touch-region policy/geometry tests and instrumentation coverage where feasible.
- Uses Android platform root-surface touchable-region and maximum-obscuring-opacity APIs already available within the app's Android 14+ support range.
