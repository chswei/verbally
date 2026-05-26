## Context

The previous UI change localized the main app and added icon-based bottom navigation. Current screenshots show the right information, but the visual hierarchy can still be improved: Home needs clearer setup sequencing, placeholder destinations need headers, and shared typography/spacing should be explicit instead of relying on Material defaults.

## Goals / Non-Goals

**Goals:**
- Use a deliberate Material-style typography scale with readable Traditional Chinese line heights.
- Align screen content, cards, fields, and navigation using consistent spacing.
- Add concise usage guidance: Home explains the setup order, Dictionary/Snippets explain what the add action is for, and History explains what appears after dictation.
- Use color roles to distinguish primary setup, secondary setup, caution/destructive actions, and neutral empty states.
- Preserve tests that protect existing settings, history, and navigation behavior.

**Non-Goals:**
- Redesigning onboarding permissions.
- Changing floating bubble behavior or overlay visual defaults.
- Implementing real dictionary/snippets CRUD.
- Adding external design libraries or new runtime dependencies.

## Decisions

### Add app-level Material tokens in `MainActivity.kt`

`VerballyTheme` will provide a custom `Typography` and `Shapes` configuration. This keeps the refinement local to the existing Compose surface and avoids a package split while the UI remains in one file.

Alternative considered: introduce a full design-system package. Rejected because the current app surface is small and a package split would slow this polish pass.

### Make usage guidance visible, not instructional prose-heavy

Home will show a short setup hint row/chip sequence and concise copy. Dictionary, Snippets, and History will use compact page headers plus empty-state descriptions. This makes usage obvious without turning screens into documentation.

Alternative considered: add long help sections. Rejected because mobile utility screens should stay scannable.

### Use color to separate roles

The transcription panel uses the primary brand color, cleanup uses a tertiary accent, empty states use neutral text, and destructive/clear history actions remain outlined instead of filled. This helps users visually distinguish tasks without introducing a noisy palette.

### Keep behavior contracts stable

The tests will assert user-visible text and semantics for the refined UI. Fine-grained color and spacing will be verified through screenshot inspection rather than brittle pixel tests.

## Risks / Trade-offs

- **Material polish could crowd small screens** -> Keep labels short, use existing scroll containers, and run emulator screenshots.
- **New copy could imply unimplemented Dictionary/Snippets features are ready** -> Keep empty-state wording future-facing and preserve placeholder add toasts.
- **A large `MainActivity.kt` can become harder to maintain** -> Add focused helpers only where they reduce repeated layout code.
