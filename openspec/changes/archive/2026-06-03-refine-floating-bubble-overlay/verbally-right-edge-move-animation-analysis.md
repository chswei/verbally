## Verbally Right-Edge Move Animation Follow-Up

After the single-layout-update jitter fix, the right-edge transition still did
not visually match the desired Wispr-like morph. Device evidence showed the
remaining issue was not app-side per-frame resizing: WindowManager could still
attach a platform move animation to the application-overlay surface when the
right-edge window changed from the idle `239 x 239 px` frame to the active
`708 x 239 px` frame.

### Evidence

- Idle right-edge frame before the stable-frame fix:
  `[819,499][1058,738]`, requested size `239 x 239 px`.
- Active right-edge frame:
  `[350,499][1058,738]`, requested size `708 x 239 px`.
- Collapse dumpsys evidence showed a `ContainerAnimator` moving the Verbally
  overlay surface from the active-left position back to the idle-left position.

That platform-level surface movement made newly inserted active controls appear
to slide from the screen edge, even though the final geometry and right edge were
correct.

### Applied Fix

For edge-anchored ready, recording, and processing states, Verbally now keeps the
same `708 x 239 px` outer motion frame. Right-edge idle content is aligned to the
trailing side of that frame and unfolds leftward; left-edge idle content is
aligned to the leading side of that frame and unfolds rightward. Tapping the
bubble no longer changes the overlay window's x position or width on either
edge.

Drag snapping and drag bounds now use the visible bubble frame rather than the
wider invisible motion frame, so moving the idle bubble can reach both screen
edges according to what the user is actually dragging.
