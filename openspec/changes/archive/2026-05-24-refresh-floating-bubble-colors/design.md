## Context

The current floating overlay gets its colors from a mix of hardcoded hex strings inside `FloatingDictationOverlay` and fixed vector colors inside the waveform/check/close assets. That makes the idle and active states feel inconsistent, and it makes future palette adjustments harder than they need to be.

## Goals / Non-Goals

**Goals:**
- Give the idle bubble a high-contrast white surface with a brand-blue waveform.
- Make the three-part active controls feel like one intentional Verbally palette instead of a blend of unrelated grays and dark fills.
- Centralize the shared brand blue so the overlay stops duplicating the same deep-blue hex value.

**Non-Goals:**
- Changing the app launcher icon or any non-overlay screen.
- Redesigning overlay layout, motion, or recording behavior beyond the color treatment.
- Introducing a full app-wide theming refactor.

## Decisions

### Add overlay palette defaults backed by Android color resources

The overlay will use a small Kotlin defaults object that points at named color resources for each visual role. This keeps the palette explicit in code, makes unit assertions easy, and avoids scattering raw hex strings through the overlay implementation.

Alternative considered:
- Keep using inline hex strings and only replace their values. Rejected because it preserves the same maintenance problem that caused the palette drift.

### Tint vector icons at usage sites instead of creating duplicate overlay-only assets

The ready waveform, cancel icon, and confirm icon will be tinted at the `ImageView` level. This keeps the drawable set small while still allowing the idle and active states to reuse the same shapes with different colors.

Alternative considered:
- Fork separate drawable XML files for each overlay state. Rejected because the shapes already work and only the colors are changing.

### Push the active controls toward a white-surface, blue-accent hierarchy

The center capsule and secondary controls will use white or near-white surfaces, while the primary confirm button will keep a solid brand-blue fill with a white checkmark. The recording waveform and processing spinner accents will switch to the same brand-blue family so the row reads as a coherent Verbally component.

## Risks / Trade-offs

- [System tinting can affect vector stroke/fill uniformly] -> Acceptable here because each icon should render as a single-color mark in the overlay.
- [Introducing named palette roles can feel slightly indirect for a small file] -> Worth it because this overlay already has multiple states and custom-drawn components sharing the same brand colors.
