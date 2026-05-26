## Why

The localized main app is functional, but the first visual pass still leaves several screens feeling uneven: typography lacks a clear hierarchy, controls do not always explain the next action, and color/spacing could better follow Android Material Design patterns. A second pass should make the app easier to understand at a glance and visually more consistent.

## What Changes

- Refine the main app Material theme with clearer typography scale, line heights, shapes, and color roles.
- Align top app bar, screen headers, forms, cards, empty states, and bottom navigation around consistent Material spacing.
- Make Home setup instructions more explicit so users can tell what to do first, next, and after saving keys.
- Improve Dictionary, Snippets, and History presentation with clearer page headers, empty-state copy, and action hierarchy.
- Preserve all existing behavior: provider settings, local history, dictionary/snippets placeholders, permissions, and floating overlay behavior do not change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `main-navigation-shell`: Refine visual hierarchy, spacing, and Material navigation styling for the main app shell.
- `local-history-and-settings`: Make Home setup and History screens clearer through Material-style typography, line heights, color roles, and aligned controls.
- `dictionary-and-snippets`: Make placeholder destinations more understandable with page headers, aligned search fields, empty states, and primary add actions.

## Impact

- Affects `app/src/main/java/com/verbally/app/MainActivity.kt` Compose UI and theme tokens.
- Updates Compose instrumentation tests for clearer visible instructions and Material-style UI states.
- Does not add dependencies, change Android permissions, alter storage behavior, or modify the floating dictation overlay.
