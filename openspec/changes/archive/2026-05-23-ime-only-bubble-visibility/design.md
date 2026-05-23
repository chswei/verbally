## Context

Verbally currently decides floating bubble visibility from editable click events, focused editable nodes, keyboard package events, input-method window visibility, and a small set of System UI dismissal heuristics. This gives the app several chances to show the bubble quickly, but it also means stale accessibility focus can keep the bubble visible or show it before the keyboard is actually on screen.

Android already exposes input-method windows through `AccessibilityService.windows`. For this change, that window list becomes the source of truth: if a `TYPE_INPUT_METHOD` window is present, the bubble is eligible to show; if it is absent, the bubble hides.

## Goals / Non-Goals

**Goals:**
- Make bubble visibility deterministic from IME window presence.
- Show the bubble even if focused editable metadata is missing, delayed, or stale.
- Hide the bubble immediately once the IME window is no longer visible.
- Keep recording, transcription, cleanup, paste insertion, and history unchanged.

**Non-Goals:**
- Adding a custom IME keyboard.
- Changing overlay layout, dictation controls, provider behavior, or paste insertion.
- Claiming cross-app overlay behavior is fully verified without a real Android device pass.

## Decisions

- Use `AccessibilityWindowInfo.TYPE_INPUT_METHOD` as the only visibility signal. This directly matches the requested behavior and avoids guessing from editable nodes.
- Keep the visibility policy as a small testable unit. `VerballyAccessibilityService` will continue translating Android events into `OverlayVisibilityEvent`, while the policy decides show/hide/keep.
- Remove click-memory and focused-editable activation from the policy. Alternative considered: keep editable focus as a secondary guard, but that would preserve the stale-focus failure mode this change is meant to remove.
- Keep the overlay's existing `WindowInsetsAnimation` hide callback as a best-effort fast hide path during IME dismissal animation, but correctness comes from the accessibility window state.

## Risks / Trade-offs

- IME window reporting can vary by Android build or keyboard app -> unit tests cover the policy contract, and device testing remains required before claiming complete cross-app verification.
- Bubble may appear for any visible keyboard, even if focused editable metadata is unavailable -> this is intentional and matches the IME-only rule.
- If Android delays `windows` updates, the bubble can lag that system signal -> the design avoids extra heuristics so behavior stays predictable.
