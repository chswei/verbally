## Context

Verbally already has a floating overlay lifecycle driven by IME visibility, a ready bubble, a recording control row, processing feedback, drag-to-edge anchoring, haptics, and live waveform amplitude. The remaining gap is interaction quality: visibility currently removes the overlay root, some state transitions replace the window root, and the recording row is less refined than the observed Wispr Flow reference.

The change must preserve Verbally's product identity. Wispr Flow can inform shape, motion, proportions, and waveform detail, but colors must remain based on Verbally brand resources and the existing translucent overlay palette.

## Goals / Non-Goals

**Goals:**
- Keep an attached overlay root when hiding and showing, while making hidden overlays non-touchable.
- Make the idle bubble a larger-corner floating square that uses Verbally brand color and translucent surface treatment.
- Make recording expand from the right edge into a three-part row sized to the observed Wispr Flow proportions.
- Use `WindowManager.updateViewLayout` for animated width and x-position transitions instead of remove/add root transitions, and base any further motion changes on black-box Wispr Flow measurements.
- Refine the recording waveform with fewer, wider Wispr-like bars, comfortable capsule side inset, and restrained live amplitude response.

**Non-Goals:**
- Copy Wispr Flow colors, logo, assets, or an unverified secondary button.
- Change transcription, cleanup, insertion, history, settings, provider routing, or audio capture architecture.
- Add new dictation modes, translation mode, or snippet behavior.

## Decisions

### Preserve the overlay root for normal visibility changes

`FloatingDictationOverlay.show()` will attach the root once when overlay permission exists. Normal `hide()` calls will animate or set alpha to hidden and update window flags so the invisible surface does not receive touch. `dispose()` remains responsible for removing the view when the accessibility service is destroyed or the overlay object is no longer valid.

Alternative considered: continue using `removeViewImmediate()` for every hide. This is simple and was previously useful for latency, but it prevents continuity and forces the next show to rebuild the window root.

### Separate visibility from touchability

The overlay needs two independent concepts: visual alpha and touch interception. Visible states use a touchable overlay; hidden states use alpha `0f` plus `FLAG_NOT_TOUCHABLE`. This keeps the retained surface fast without creating an invisible blocker over the user's keyboard or app.

Alternative considered: only set alpha to `0f`. Rejected because transparent overlay windows can still intercept touches.

### Keep a stable right-edge motion frame for state transitions

Ready, recording, and processing states update the existing root content. On either screen edge, the overlay keeps the same outer active-width motion frame across ready, recording, and processing so the edge-adjacent bubble and window surface stay fixed while child controls reveal from the original bubble position. Right-edge content aligns to the frame end and unfolds leftward; left-edge content aligns to the frame start and unfolds rightward. This avoids the platform move animation observed on the connected Samsung device when an application-overlay window changes width and x at the same time. Repair behavior can stay compact.

Alternative considered: rebuild the root on ready-to-recording and processing-to-ready transitions. Rejected because the user explicitly wants continuous updateViewLayout animation.

Alternative considered: keep resizing the right-edge overlay window in a single `updateViewLayout` call. Rejected after device evidence showed WindowManager can still animate the surface move, making the controls appear to slide from the screen edge even when app-side per-frame resizing is removed.

### Keep Verbally colors while borrowing shape language

Idle and active surfaces will continue using `overlay_surface_frosted`, `overlay_brand_blue_translucent`, `overlay_on_brand`, and `verbally_brand_blue`. Shape and depth can move closer to Wispr Flow through larger rounded-square corners, soft elevation, thinner icon sizing, and a refined capsule.

Alternative considered: add a purple Wispr-style accent. Rejected because it conflicts with the user's explicit brand-color requirement.

### Refine waveform rendering inside the existing custom view

`RecordingWaveformView` will keep receiving the existing smoothed level from `VerballyAccessibilityService`, but its drawing model will use fewer wider marks, comfortable side inset, and subtle phase offsets. This avoids touching audio capture and keeps the visual change isolated.

Alternative considered: introduce a new audio meter or realtime waveform data model. Rejected because this change is visual refinement, not a recording pipeline change.

## Risks / Trade-offs

- [Invisible overlay intercepts touch] -> Always pair hidden alpha with `FLAG_NOT_TOUCHABLE` and test the flag update path.
- [Retained root outlives permission or service state] -> Keep `dispose()` as the teardown path and remove the root if overlay permission disappears.
- [Edge expansion drifts, clips, or blocks dragging] -> Keep a stable active-width motion frame on both edges, align content to the active edge, and test the policy and drag geometry helpers against the visible bubble bounds rather than the invisible outer frame.
- [Animation feels good in unit tests but poor on-device] -> Validate on the connected Android device after build, while keeping the run install-only unless broader device testing is explicitly requested.
- [Closer waveform detail harms readability] -> Keep the waveform brand-colored, restrained, and high-contrast against the capsule.
