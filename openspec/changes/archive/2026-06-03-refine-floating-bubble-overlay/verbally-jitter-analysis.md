## Verbally Jitter Follow-Up

After applying the measured Wispr outer geometry, the connected Samsung device
still showed visible position jitter during expand and collapse.

### Evidence

Frame sampling during a single Verbally bubble tap showed the overlay window
right edge stayed fixed, but the app sent many per-frame window relayouts:

- Idle frame: `[819,434][1058,673]`, requested size `239 x 239 px`.
- Expanded frame: `[350,434][1058,673]`, requested size `708 x 239 px`.
- During the transition, logcat showed repeated overlay surface relayouts such
  as `req=(299,239)`, `req=(356,239)`, `req=(414,239)`, and so on.

This means the geometry target was correct, but animating overlay-window width
with per-frame `updateViewLayout` caused the system to resize the overlay
surface repeatedly. On this device that presents as visual position jitter.

### Applied Fix

Keep the measured Wispr geometry and right-edge anchoring, but stop the per-frame
overlay-window resize. Verbally now applies the target window geometry in one
`updateViewLayout` call, matching the black-box evidence that Wispr switches
between the measured idle and expanded main-window sizes without observable
overshoot or bouncing intermediate states.

Tap haptics remain enabled.
