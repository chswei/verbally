## Context

The overlay already funnels all tap interactions through `bindDragAndClick` and `DragTouchListener`. That makes the touch listener the right seam for haptics because it can distinguish between a tap and a drag without changing the recording state machine.

## Decisions

### Use `View.performHapticFeedback(...)`

The overlay will rely on the platform's built-in view haptic API instead of talking directly to `Vibrator`. This keeps the implementation small, avoids extra API/version branching, and maps naturally to UI tap feedback.

### Fire haptics only on click-path interactions

The drag touch listener will trigger haptic feedback only when a gesture resolves to a click action. Dragging the bubble will continue to move silently.
