## Why

Wispr Flow's Android documentation frames snippets as static voice shortcuts that paste the saved expansion exactly, while dictionary entries are separate vocabulary hints and cannot reuse snippet triggers. Verbally already has local dictionary/snippet surfaces, but snippet persistence and expansion can still lose saved formatting or trigger inside unrelated words.

## What Changes

- Preserve snippet expansion text exactly as saved, including casing, punctuation, line breaks, and surrounding formatting whitespace.
- Keep trimming and normalizing snippet triggers for stable search, validation, and duplicate handling.
- Expand snippets only when a trigger appears as a standalone spoken cue or phrase, not as a substring inside a longer Latin-style token.
- Continue enforcing dictionary term and snippet trigger uniqueness with existing local validation.
- Do not add accounts, cloud sync, team libraries, bulk import, starred dictionary words, or Android dictionary misspelling replacement rules.

## Capabilities

### New Capabilities

### Modified Capabilities
- `dictionary-and-snippets`: tighten static snippet expansion requirements so saved expansion formatting is preserved and trigger matching avoids substring false positives.

## Impact

- Affected code: `SnippetRepository`, `SnippetExpander`, snippet coordinator flow, and focused unit tests.
- Affected specs: `dictionary-and-snippets`.
- No new dependencies, permissions, backend APIs, accounts, or provider-key changes.
