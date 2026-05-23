## Context

The overlay currently stores a specific x/y coordinate after the user drags the bubble. This allows arbitrary horizontal placement, but the bubble can remain in the middle of the screen and obscure editable content. The requested behavior is closer to common floating controls: free movement while dragging, then magnetic snapping to the nearest horizontal edge when released.

## Goals / Non-Goals

**Goals:**

- Snap the bubble to the nearest left or right edge on drag release.
- Preserve the user's chosen vertical placement.
- Persist the snapped side and vertical position across future overlay shows.
- Match the reference visual rhythm with a 20dp side margin and a 48dp rounded-square bubble.
- Use iconography instead of text inside the bubble, with the idle bubble sharing the app launcher reference-style waveform mark.
- Keep tap-to-start, tap-to-confirm, and long-press-cancel behavior intact.

**Non-Goals:**

- Adding animated spring motion.
- Adding a settings screen for bubble position.
- Changing when the overlay appears or hides.

## Decisions

- Use a side-aware position model rather than persisting arbitrary x/y coordinates. This keeps the stored state aligned with the product rule: the bubble belongs on either the left or right edge.
- Decide the snap side from the bubble center relative to the screen midpoint at release time. This matches user expectation when dragging past the center line.
- Store edge margin in pixels derived from a 20dp design token at runtime. This matches the reference screenshot's left-side spacing more closely than the previous 24dp margin.
- Render the overlay as a `FrameLayout` with a rounded rectangle background and centered `ImageView` symbols. This keeps the touch handling on one view while removing text from the bubble.
- Use the provided reference-style waveform mark for both the idle bubble and launcher foreground: a dark navy rounded square, a white flowing waveform, and a white vertical pill on the right. Recording, processing, success, and error states use white symbols so the recording lifecycle remains legible without text.
- Keep drag-time movement immediate through `WindowManager.updateViewLayout`; only the release path applies snapping and persistence.

## Risks / Trade-offs

- The bubble may overlap system cutouts or transient UI near the top/bottom because this change only constrains horizontal placement. → Clamp y to non-negative for now and rely on manual repositioning for vertical conflicts.
- Screen width can change after rotation or multi-window changes. → Recompute the snapped x from current metrics whenever the overlay is shown and when a drag is released.
- Without animation, the snap is immediate rather than springy. → Prefer predictable behavior first; animation can be added later without changing the spec.
- Removing text from transient success/error messages reduces visible feedback detail. → Keep state-specific symbols and content descriptions; richer message display can be handled separately.
