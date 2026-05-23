## Context

The current overlay is a single `FrameLayout` with one `ImageView` whose icon changes across `IDLE`, `RECORDING`, `PROCESSING`, `SUCCESS`, and `ERROR`. That shape cannot represent the requested three-part control row, and the success/error terminal states are the likely reason repeated dictation is awkward while the IME stays visible. The existing IME visibility policy and drag persistence already work well and should remain the source of truth for when the overlay is shown.

## Goals / Non-Goals

**Goals:**
- Render a Whispr Flow-style three-part control row during recording and processing.
- Keep animations local to the overlay view layer so service logic stays simple.
- Make dictation lifecycle states cycle cleanly from ready to recording to processing and back to ready.
- Preserve overlay drag behavior and IME-only visibility.

**Non-Goals:**
- Rework IME visibility policy or accessibility event sources.
- Introduce richer success/error toasts, banners, or permanent result states.
- Match Whispr Flow pixel-for-pixel beyond the requested interaction model.

## Decisions

### Replace the single-icon bubble with two view modes inside one overlay root

The overlay will keep one top-level root view so showing, hiding, and drag persistence stay unchanged. Inside that root, we will switch between:
- a compact ready bubble for the idle state
- a horizontal segmented control for recording and processing

This is simpler than maintaining multiple window overlays and lets us animate between states with local view updates.

Alternative considered:
- Separate overlay windows for idle vs active controls. Rejected because it adds teardown/recreation complexity and makes position persistence more fragile.

### Collapse terminal success/error UI into a reusable ready state

Instead of leaving the overlay in `SUCCESS` or `ERROR`, processing completion will always transition back to `READY` after recording work ends. Result messages can still be stored for accessibility content descriptions, but the visible UI returns immediately to the ready bubble.

Alternative considered:
- Keep success/error icons and auto-time them away. Rejected because the user explicitly does not want extra result prompts, and a timed terminal state still delays the next dictation.

### Keep animation responsibilities in the overlay class

The dynamic recording waveform and spinning processing indicator will be implemented as view-level animations controlled by `setState`. The accessibility service only reports lifecycle events (`start`, `cancel`, `confirm`, `processing complete`) and does not manage animation state directly.

Alternative considered:
- Compose the overlay in a separate UI framework. Rejected because the current overlay is view-based and a small custom view hierarchy is lower risk.

## Risks / Trade-offs

- [Custom animated views may be visually off from the reference] -> Keep the implementation intentionally simple and tune dimensions/colors against the provided screenshots.
- [Immediate reset to ready could hide useful failure feedback] -> Preserve failure message in accessibility content description and logs while keeping visible UI reset fast.
- [State transitions could regress cancel/confirm behavior] -> Add unit coverage around repeatable state transitions and coordinator reset paths before implementation.
