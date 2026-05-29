## Why

The main app UI has a solid structure, but emulator review shows a few rough edges: the drawer copy overlaps conceptually with Home API setup, dark mode leaves system bar icons with poor contrast, and some actions use overly heavy visual treatment. This change refreshes the Material 3 visual system while keeping Verbally quiet, work-focused, and easy to scan.

## What Changes

- Keep Verbally's deep blue as the brand anchor, but rebalance light and dark Material color roles with restrained teal/mint and lavender support colors.
- Ensure app-selected light and dark modes update system bar icon contrast so status/navigation UI remains readable.
- Rework the drawer as a low-frequency management area with separate `設定` and support-oriented permission troubleshooting entries.
- Clarify drawer copy so `設定` means app appearance/preferences and Home remains the API setup destination.
- Reduce visual noise in history by hiding the clear-history action when there is nothing to clear and keeping destructive actions visually distinct but less dominant.
- Tighten reusable UI surfaces and action treatments so cards, fields, buttons, empty states, and drawer items feel consistent.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `main-navigation-shell`: drawer content and visual hierarchy are refined, including a dedicated permissions/troubleshooting entry separate from app settings.
- `app-appearance-mode`: selected theme mode must apply readable system bar icon contrast and use the refreshed Material 3 color roles.
- `local-history-and-settings`: history clear actions should only appear when relevant and should use appropriate destructive-action emphasis.

## Impact

- Affected code: `app/src/main/java/com/verbally/app/MainActivity.kt`, `app/src/androidTest/java/com/verbally/app/MainActivitySettingsScreenTest.kt`, and theme styles/resources if needed.
- No backend, provider, data-model, or permission-scope changes.
- Validation will include OpenSpec strict validation, focused Compose instrumentation tests, unit tests, debug build, and emulator screenshot review.
