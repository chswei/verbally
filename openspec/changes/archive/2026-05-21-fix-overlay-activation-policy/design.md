## Context

Verbally uses an AccessibilityService plus system overlay to show a dictation bubble near editable fields. The baseline behavior showed the bubble whenever `rootInActiveWindow.findFocus(FOCUS_INPUT)` found an editable node. Real-device logs on a Samsung S23 showed that LINE and WhatsApp can keep an editable node focused while non-input events arrive from chat navigation, System UI, gesture overlays, and keyboards. That caused the bubble to appear before the user intentionally tapped the input box.

At the same time, some valid text-entry surfaces such as the Google Search home widget open a Google activity and input method without delivering an editable source-click event to the accessibility service.

## Goals / Non-Goals

**Goals:**

- Prevent the bubble from appearing solely because an app passively focused or retained an editable field.
- Preserve expected behavior when the user taps ordinary editable fields in chat apps and other apps.
- Support search-widget style entry points where the input method opens for a focused editable field.
- Keep the policy testable without Android framework objects.

**Non-Goals:**

- Do not add an IME keyboard.
- Do not add app-specific hard-coded behavior for LINE, WhatsApp, or Google Search.
- Do not change the recording, transcription, cleanup, paste, or history flows.

## Decisions

1. Extract overlay visibility decisions into a small `OverlayVisibilityPolicy`.
   - Rationale: Accessibility framework objects are hard to unit test directly. A pure Kotlin policy allows real log-derived scenarios to be covered by unit tests.
   - Alternative considered: Keep all logic in the service, rejected because it would encourage another untested event-condition patch.

2. Treat editable source clicks as direct activation.
   - Rationale: An editable source click is the clearest accessibility signal that the user tapped a text field.
   - Alternative considered: Treat all clicks followed by editable focus as activation, rejected because entering a LINE chat can produce same-package navigation events followed by editable focus.

3. Treat default input-method events with focused editable fields as activation.
   - Rationale: Search widgets can legitimately open the keyboard for a focused input without an editable source click. Checking the configured default input method keeps this broader path tied to actual text entry.
   - Alternative considered: Show on any focused editable content change, rejected because System UI and unrelated overlays can emit content changes while stale editable focus remains.

## Risks / Trade-offs

- Some custom input controls may not expose editable source clicks and may not open the default input method immediately -> they may require future accessibility-event logging and another narrowly specified activation path.
- Default input method lookup happens during accessibility events -> this is simple and accurate, but can be cached later if profiling shows overhead.
- The policy intentionally favors fewer false positives over showing on every possible focus edge case.
