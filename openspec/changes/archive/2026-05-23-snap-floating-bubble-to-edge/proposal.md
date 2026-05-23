## Why

The floating dictation bubble can now be moved, but leaving it at any horizontal position can still obscure editable content. A magnetic left/right edge snap gives the bubble a more familiar mobile assistive-control feel while keeping it out of the center of the screen.

## What Changes

- Allow the bubble to move freely while the user drags it.
- When the user releases the bubble, snap it to the nearest left or right screen edge with a small margin.
- Preserve the released vertical position so the bubble stays at the user's chosen height.
- Persist the snapped side and vertical position until the user drags the bubble again.
- Match the reference bubble feel with a 20dp edge margin and a compact rounded-square icon surface.
- Replace text labels in the bubble with state symbols and use the same reference-style waveform mark for the app icon and idle floating bubble.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `floating-dictation-overlay`: adds edge snapping behavior for user-positioned floating bubbles.

## Impact

- `FloatingDictationOverlay.kt`: drag release handling, screen-width-aware snap position calculation, persisted position loading, icon-based rounded-square bubble UI.
- `OverlayPositionMemory.kt`: position model changes from arbitrary x/y coordinates to side-aware snapped positions.
- Launcher icon vector resources: modern waveform mark shared with the bubble.
- Unit tests for snap decision and position memory behavior.
