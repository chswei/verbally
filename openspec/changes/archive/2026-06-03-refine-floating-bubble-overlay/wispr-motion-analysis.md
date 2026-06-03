## Wispr Flow Black-Box Motion Notes

Captured on the connected Android device at 1080 x 2340 px, using Google search
with the IME open. Verbally was stopped for the reference capture so only Wispr
Flow overlays were visible.

### Evidence Files

- `app/build/wispr-clean-idle-before-recording.png`
- `app/build/wispr-after-direct-center-tap.png`
- `app/build/wispr-blackbox-sequence-2.mp4`
- `app/build/wispr-blackbox-2-contact.png`
- `app/build/wispr-blackbox-window-samples-2.jsonl`

### Window Model

Wispr Flow exposes two overlay windows during this reference flow:

- Main floating overlay window: visible, `alpha=0.8`.
- Secondary bottom overlay window: full-width bottom surface, but `alpha=0.0`
  throughout idle, expand, collapse, and the second expand.

The visible bubble and recording controls are hosted by the main overlay window.
The secondary window is present but hidden and does not participate in this
tap-to-expand interaction.

### Main Window Geometry

Observed main-window positions:

- Idle: `x=821`, `y=731`, requested size `238 x 238 px`, right edge `1059 px`.
- Expanded: `x=350`, `y=731`, requested size `709 x 238 px`, right edge `1059 px`.

Motion implication:

- The main window keeps the same height while switching states.
- The main window keeps the same top and right edge.
- Expansion is a right-edge-anchored width change from `238 px` to `709 px`.
- Collapse reverses to `238 px` while preserving the same right edge.

At the captured density, this maps to approximately:

- Outer idle frame: `75 dp`.
- Outer expanded frame: `222 dp`.
- Outer frame right margin: `7 dp`.

### Child Surface Geometry

Image-level detection on the idle screenshot found the visible idle control at
approximately `151 x 151 px`, centered inside the `238 x 238 px` main window.
The active row shows three visible controls inside the `709 x 238 px` main
window:

- Leading cancel circle.
- Center waveform capsule.
- Trailing confirm circle.

The visible controls are substantially smaller than the overlay window. The
window supplies shadow/padding space above, below, and around the controls.
Verbally should therefore add a stable motion frame around the existing visible
controls instead of making the visible control itself fill the window.

### Timing And Opacity

The sampled `dumpsys window` records show state changes between samples rather
than intermediate widths. The contact sheet and video show a fast direct switch
with no visible bounce, overshoot, or secondary element animation. The observable
model to apply is:

- One retained main overlay root.
- Constant main-window alpha.
- No haptic-driven or spring overshoot.
- Width and x-position update together, with the right edge visually fixed.
- Child controls stay vertically centered inside a constant-height frame.

### Verbally Application Constraints

Apply the observed motion model only to the overlay shell:

- Keep Verbally brand colors.
- Keep the existing cancel, waveform, confirm, processing, and repair semantics.
- Keep the dictation, cleanup, insertion, and history pipeline unchanged.
