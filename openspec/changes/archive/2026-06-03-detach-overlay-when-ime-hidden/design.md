## Context

Verbally's overlay visibility policy already uses the input method window as the source of truth: IME visible means the bubble is eligible to show, and IME hidden means the bubble should hide. The recent visual polish changed the hidden implementation from `removeViewImmediate()` to alpha plus `FLAG_NOT_TOUCHABLE`, which prevents touch blocking but keeps a `TYPE_APPLICATION_OVERLAY` window attached.

That attached transparent root can still trigger Android's system "displaying over other apps" indicator even when the user cannot see or use the bubble.

## Goals / Non-Goals

**Goals:**
- Remove the overlay root from `WindowManager` when the bubble is not needed because the IME is hidden or the context is sensitive.
- Keep the visible-state UI, sizes, waveform, haptics, edge anchoring, drag behavior, and active expand/collapse animation unchanged.
- Preserve quick re-attachment when the IME returns, using the saved edge and y position.

**Non-Goals:**
- Do not change dictation recording, transcription, cleanup, insertion, history, provider settings, or permission onboarding.
- Do not introduce an IME keyboard or alternate input surface.
- Do not attempt to suppress Android system overlay notifications.

## Decisions

- Use root removal for hidden/not-needed states. `FloatingDictationOverlay.hide()` should cancel animations and detach the attached root, returning session state to READY.
- Keep in-place layout animation only for visible state transitions such as READY to RECORDING and RECORDING to READY while the overlay remains attached.
- Keep `OverlayVisibilityPolicy` as the IME-only gate. The policy does not need new editable-field heuristics.
- Retain touchability helpers only for visible-root safety and tests where they remain useful, but the normal hidden path should have no attached root to intercept touches.

## Risks / Trade-offs

- Re-attaching the overlay can be less visually continuous than keeping a transparent root. Mitigation: preserve the existing visible-state anchored animation and saved edge placement so recording controls still expand from the bubble after the user taps it.
- IME show/hide events can be noisy on Samsung devices. Mitigation: keep the current IME-only policy and immediate teardown path rather than adding focus heuristics.
- Removing the root resets transient UI state. Mitigation: hidden states force READY and clear waveform/repair content, which matches the previous desired behavior when the keyboard disappears.
