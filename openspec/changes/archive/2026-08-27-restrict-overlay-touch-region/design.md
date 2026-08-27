## Context

`FloatingDictationOverlay` keeps a 222dp-wide outer window for ready, recording, and processing states even though the idle bubble is only 48dp wide. The stable window avoids a Samsung `WindowManager` surface-move animation that previously made edge-anchored transitions slide or jitter, but Android currently treats the entire transparent window rectangle as touchable.

Verbally targets Android 14 and newer. Android exposes `AttachedSurfaceControl.setTouchableRegion` from API 33, allowing the retained visual surface to use a smaller input region without changing window geometry.

The first implementation restricted the input region but left `WindowManager.LayoutParams.alpha` at `1.0`. Real-device verification showed Android rejecting touches through the non-touchable part of the application overlay and displaying a touch-compatibility warning. Android 12+ treats `TYPE_APPLICATION_OVERLAY` as untrusted for pass-through unless its window-level alpha is at or below the system's maximum obscuring opacity.

## Goals / Non-Goals

**Goals:**

- Allow taps outside visible overlay controls to reach the underlying app.
- Preserve the current stable outer frame, left/right anchoring, drag behavior, and child reveal animation.
- Keep touch-region calculations deterministic and covered by focused tests.
- Refresh the region whenever rendered controls or their layout can change.

**Non-Goals:**

- Redesign the bubble or active control row.
- Change recording, processing, insertion, permission, or overlay-visibility behavior.
- Replace the retained overlay root or reintroduce window-width animation.

## Decisions

### Set a root-surface touchable region independent of window bounds

After the overlay root and its current content are laid out, obtain the root `AttachedSurfaceControl` and set a `Region` expressed in window-local coordinates. Ready and repair states use the visible bubble bounds. Recording and processing states use the visible three-part row bounds. Transparent outer padding and unused portions of the stable motion frame remain outside the region and pass touches through.

Alternative considered: shrink the ready window and expand it for active states. Rejected because prior Samsung device evidence showed platform-level surface movement when overlay width and position changed.

Alternative considered: split visual and interactive content across multiple overlay windows. Rejected because it complicates drag positioning, state transitions, accessibility, and lifecycle cleanup without providing a better input contract.

### Pair the region with Android's allowed window-level opacity

Read `InputManager.getMaximumObscuringOpacityForTouch()` and assign the resulting value, clamped to the valid window-alpha range, to `WindowManager.LayoutParams.alpha`. This satisfies Android's pass-through security requirement for the single application-overlay window while keeping the root view and child reveal alpha independent.

Alternative considered: keep `LayoutParams.alpha = 1.0` because the rendered child backgrounds are already translucent. Rejected because Android's untrusted-touch decision uses the window-level layout alpha, not the per-pixel or child drawable alpha.

Alternative considered: shrink the idle window so no overlay surface exists beside the bubble. Rejected as the primary approach because it reintroduces the Samsung platform window-move animation that the stable frame was designed to avoid. It remains a fallback if an OEM does not honor the documented opacity rule.

### Register one visible interaction surface per rendered state

Rendering records the view whose visible bounds define the state interaction surface: the idle/repair bubble or the active row. Individual child listeners continue to own tap and drag semantics. A single row region keeps coordinate updates stable while its children run the short reveal animation.

The registered surface is cleared and replaced during every render. Region application is posted until after attachment/layout, and it is requested again after content, edge, or configuration changes.

### Keep geometry normalization separate from Android surface effects

A small pure-Kotlin geometry helper will clip candidate bounds to the root window and discard empty rectangles. Unit tests will cover left- and right-aligned idle bounds, active-row bounds, clipping, and empty input. `FloatingDictationOverlay` will remain responsible for translating descendant view bounds into root coordinates and applying the resulting Android `Region`.

### Treat missing root-surface state conservatively

The root surface is unavailable before attachment. Region updates will therefore be scheduled after the view is attached and laid out, and stale callbacks will verify that the root is still current. Removing the overlay root remains the definitive cleanup path.

## Risks / Trade-offs

- [A region is applied before descendant bounds are valid] → Post the update after attachment/layout and ignore zero-sized candidates.
- [State replacement briefly retains an older region] → Schedule a region refresh as part of every render and keep the prior region no larger than the previously visible controls.
- [OEM behavior differs despite the public API] → Add emulator instrumentation where feasible and perform install-only verification on the connected phone, leaving final cross-app tapping to the user.
- [Window alpha makes existing translucent surfaces visually lighter] → Use the system-provided maximum rather than an unnecessarily lower value and verify the result on the existing phone.
- [A future visible control is added without updating the region surface] → Centralize registration in bubble/control construction and cover each overlay state in tests.
