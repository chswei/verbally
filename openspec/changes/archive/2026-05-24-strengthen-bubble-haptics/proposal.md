## Why

The first haptic pass added tap feedback, but on the user's device it is too weak to notice and it does not match Whispr's drag-time tactile cues. The overlay needs stronger, more reliable vibration and should also vibrate when a drag begins and when the bubble snaps into place.

## What Changes

- Replace the view haptic approach with explicit vibration API usage for overlay interactions.
- Add short tap vibration for ready, cancel, and confirm taps.
- Add short drag-start vibration and a slightly stronger snap vibration for drag interactions.

## Capabilities

### Modified Capabilities

- `floating-dictation-overlay`: provide noticeable tap, drag-start, and snap vibration feedback for the floating bubble.
