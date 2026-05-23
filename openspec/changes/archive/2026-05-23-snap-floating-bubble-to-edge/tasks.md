## 1. Position Model

- [x] 1.1 Add tests for left/right snap decisions based on bubble center and screen width.
- [x] 1.2 Update overlay position memory to persist edge side plus vertical position instead of arbitrary x/y.

## 2. Overlay Drag Behavior

- [x] 2.1 Update drag release handling to snap to the nearest edge with a density-based margin.
- [x] 2.2 Recompute saved snapped x positions when showing the overlay on the current screen width.
- [x] 2.3 Preserve existing tap, confirm, and recording long-press cancel interactions.

## 3. Validation

- [x] 3.1 Update visual defaults to match the reference margin and rounded-square bubble size.
- [x] 3.2 Replace bubble text with icon-based state symbols.
- [x] 3.3 Update the launcher foreground icon to the shared waveform mark.
- [x] 3.4 Replace the abstract cat attempt with the provided reference-style waveform mark.
- [x] 3.5 Move launcher artwork into the adaptive icon safe zone.
- [x] 3.6 Further shrink launcher foreground to avoid app drawer masking.
- [x] 3.7 Separate launcher waveform and right-side pill so they do not overlap.
- [x] 3.8 Reuse the bubble waveform path for launcher foreground so the wave connects to the pill consistently.
- [x] 3.9 Run OpenSpec strict validation.
- [x] 3.10 Run debug unit tests.
- [x] 3.11 Build and install the debug APK on the connected Android device.
- [x] 3.12 Nudge launcher foreground left to visually center the mark.
