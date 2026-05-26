## Context

The current main app is functional but still feels like an early Compose scaffold. Several high-visibility labels are English (`Home`, `Dictionary`, `Snippets`, `History`, `Settings`, `Search`), bottom navigation icons are placeholder characters, and the empty states are plain centered text. Recent overlay polish made the floating bubble feel more intentional; the main app now needs the same finished-product treatment while keeping the existing workflows stable.

## Goals / Non-Goals

**Goals:**
- Make the main app shell read as a Traditional Chinese app: `首頁`, `字典`, `片段`, `歷史`, `設定`, and `搜尋`.
- Replace prototype shell markers with simple app-like icon resources and accessible content descriptions.
- Make Home API setup feel like two branded operation panels for `語音轉錄` and `文字處理`.
- Make Dictionary and Snippets empty states clearer without pretending those features are implemented.
- Make History easier to scan while preserving search, copy, delete, clear confirmation, and latest-100 messaging.

**Non-Goals:**
- Changing floating bubble visibility, recording, waveform, haptics, or overlay colors.
- Adding real dictionary/snippets data models or editing flows.
- Changing provider configuration semantics, encrypted storage, or history retention.
- Adding a new navigation architecture or external UI dependency.

## Decisions

### Localize shell labels in place

The existing `AppDestination` enum will carry Traditional Chinese labels. This keeps the current app shell and tests focused while avoiding a larger navigation rewrite. Drawer copy will also use `設定` instead of `Settings`.

Alternative considered: keep English destination labels for product style. Rejected because the user explicitly clarified that the app should be localized.

### Use local vector drawables for shell icons

The app will add small vector resources for the main app shell rather than introducing a new material-icons dependency. Compose can render them with `painterResource`, keeping build impact low and matching the repo's existing XML drawable style.

Alternative considered: keep text markers like `D` and `S`. Rejected because those markers make the UI feel like a prototype.

### Keep visual polish inside existing composables

`MainActivity.kt` is already the home for the main app Compose UI and has instrumentation tests around its screen content. The implementation will add small reusable helpers for polished cards, icon badges, and empty states, but will not split files during this change.

Alternative considered: move UI components to a new package. Rejected for this narrow polish pass because behavior and state remain local to the existing screens.

### Preserve current behavior while improving presentation

Tests will assert visible Traditional Chinese labels, absence of old English shell labels, localized search placeholders, and the presence of polished empty-state and history affordances. Visual details such as color and spacing will remain implementation details unless they affect user-visible copy or semantics.

## Risks / Trade-offs

- **Tests tied to old labels will fail** -> Update instrumentation tests first and watch them fail before implementation.
- **Chinese labels may crowd bottom navigation** -> Use short labels (`首頁`, `字典`, `片段`, `歷史`) and stable icon sizes.
- **Empty states could imply unavailable features are ready** -> Keep copy honest and continue showing add actions as existing placeholder actions.
- **MainActivity is large** -> Add only small helpers needed for this pass and avoid unrelated refactors.
