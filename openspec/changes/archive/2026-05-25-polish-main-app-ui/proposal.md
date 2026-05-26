## Why

Verbally's main app UI still exposes several prototype-era labels and controls such as English navigation text, letter markers, and generic empty states. The app should feel like a finished Traditional Chinese Android utility while preserving the existing setup, history, dictionary, and snippets workflows.

## What Changes

- Localize the main app shell visible labels to Traditional Chinese, including bottom navigation, drawer settings, search placeholders, and add actions.
- Replace prototype text markers in the top bar and bottom navigation with app-like icon treatments and accessible Traditional Chinese descriptions.
- Polish the Home API settings surface so transcription and text-processing setup read as clean, branded operation panels rather than plain form cards.
- Polish History, Dictionary, and Snippets surfaces with clearer empty states, better action placement, and consistent card/list styling.
- Preserve existing product behavior: Home remains the default destination, Dictionary/Snippets remain searchable/addable placeholders, History remains local and capped, and the floating overlay runtime is out of scope.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `main-navigation-shell`: Main app navigation and drawer labels become Traditional Chinese and use polished app-shell controls instead of prototype markers.
- `dictionary-and-snippets`: Dictionary and snippets placeholder surfaces use Traditional Chinese search/add copy and more complete empty states.
- `local-history-and-settings`: Home API setup and History retain existing behavior while using polished Traditional Chinese operation panels and list presentation.

## Impact

- Affects `app/src/main/java/com/verbally/app/MainActivity.kt` Compose UI.
- Adds or adjusts Android vector drawables used by the main app shell.
- Updates Compose instrumentation tests for localized labels, search placeholders, and polished visible states.
- Does not change provider APIs, local storage, overlay behavior, permissions behavior, or history persistence semantics.
