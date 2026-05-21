## Why

The initial editable-focus rule made the floating bubble appear too early in apps such as LINE, where entering a chat can leave an editable field focused before the user taps the input box. Tightening that rule exposed the opposite problem for Google Search widgets, where the keyboard opens from a search surface without an editable source-click event.

## What Changes

- Refine floating bubble visibility so passive editable focus alone does not show the bubble.
- Show the bubble when the user explicitly clicks an editable text field.
- Show the bubble when the active input method opens for a currently focused editable field, covering search widgets and similar entry points.
- Keep the bubble visible while the input method and System UI emit follow-up events for the same focused editable field.
- Hide the bubble when focus leaves editable fields.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `floating-dictation-overlay`: Bubble activation now distinguishes user-initiated text entry from passive or stale editable focus.

## Impact

- Updates accessibility event handling in `VerballyAccessibilityService`.
- Adds a small visibility policy model and focused unit tests.
- Expands accessibility event subscriptions to include click, text change, and content change events required by common app/search-widget flows.
