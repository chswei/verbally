## Why

Whispr Flow gives tactile feedback when the user taps the ready bubble, cancel control, and confirm control. Verbally currently has no matching haptic response, which makes the overlay feel less responsive even when the visual behavior is correct.

## What Changes

- Add light haptic feedback when the user taps the ready bubble.
- Add the same light haptic feedback when the user taps cancel or confirm in the recording controls.
- Keep drag behavior unchanged so haptics fire only on real taps, not while moving the bubble.

## Capabilities

### Modified Capabilities

- `floating-dictation-overlay`: provide tactile feedback for ready, cancel, and confirm taps.
