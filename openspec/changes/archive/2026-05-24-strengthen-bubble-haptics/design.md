## Context

The overlay already has a good touch seam in `DragTouchListener`, but `performHapticFeedback` is not strong enough on the current Samsung device. To make the behavior reliable, the overlay should own a small vibration helper backed by `VibratorManager` on newer Android versions and `Vibrator` on older ones.

## Decisions

### Use explicit vibration APIs with short predefined durations

The overlay will trigger actual vibration effects rather than relying on view haptic hints. This gives predictable tactile output on the user's device while keeping the implementation small.

### Trigger drag haptics only on key moments

The drag gesture will vibrate once when dragging begins and once when the bubble snaps to its remembered edge. It will not vibrate continuously during movement.
