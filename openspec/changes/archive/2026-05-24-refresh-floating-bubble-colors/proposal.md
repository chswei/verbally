## Why

The floating dictation bubble currently uses a dark blue idle background and mixed grayscale active-control colors that lose contrast in dark mode and no longer feel aligned with Verbally's main visual language. The user wants the bubble flow itself, including the expanded three-part controls, to use a clearer white-and-brand-blue treatment without changing the app launcher icon.

## What Changes

- Recolor the ready bubble from dark blue with a white waveform to a white surface with a Verbally blue waveform.
- Recolor the expanded recording and processing controls so their surfaces, icons, waveform, and spinner consistently follow the same Verbally overlay palette.
- Extract the shared deep blue into a reusable Android color resource so the overlay no longer hardcodes that brand color inline.

## Capabilities

### Modified Capabilities

- `floating-dictation-overlay`: Update the ready, recording, and processing bubble palette to use a consistent Verbally-branded color system with stronger contrast in dark mode.

## Impact

- Affects overlay rendering in `FloatingDictationOverlay`.
- Adds overlay color defaults coverage so the palette contract stays stable.
- Updates the floating overlay spec and delivery flow, including debug APK installation on the approved device.
