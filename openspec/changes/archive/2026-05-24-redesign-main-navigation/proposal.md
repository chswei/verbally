## Why

The current main shell treats history and settings as the primary destinations, but the desired app model is closer to Whispr Flow: a branded home surface with setup controls, separate local-data sections, and a hamburger menu for secondary app settings.

## What Changes

- Redesign the main app shell with a top app header showing a hamburger action and the Verbally app name.
- Make Home the default destination and place API setup entry points there.
- Add bottom navigation destinations for Home, Dictionary, Snippets, and History.
- Move secondary settings and permission recovery into a hamburger menu instead of a bottom tab.
- Add Dictionary and Snippets placeholder pages with search fields and add actions.
- Keep History as a bottom navigation destination with search, per-entry actions, and clear-all.

## Capabilities

### New Capabilities
- `main-navigation-shell`: Branded top app shell, bottom navigation destinations, and hamburger menu behavior.
- `dictionary-and-snippets`: Local placeholder surfaces for future dictionary words and snippets.

### Modified Capabilities
- `local-history-and-settings`: API settings become the Home surface, while history remains a first-class local-data page.

## Impact

- Affects `app/src/main/java/com/verbally/app/MainActivity.kt`.
- Extends Compose instrumentation coverage in `app/src/androidTest/java/com/verbally/app/MainActivitySettingsScreenTest.kt`.
- No backend, provider, overlay, accessibility, or storage dependency changes.
