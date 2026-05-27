## Why

Verbally already exposes a `片段` destination, but it is still a placeholder and cannot reduce repetitive dictation. Users need Wispr Flow-style static snippet expansion so spoken trigger phrases such as `我的地址` can deterministically become saved text such as a full address or report template.

## What Changes

- Turn the Snippets destination into a real local CRUD surface for static snippets.
- Store each snippet locally with a required trigger phrase and required expansion text.
- Search snippets by trigger or expansion text.
- During dictation, replace saved trigger phrases in the cleaned text with their exact saved expansion before insertion and history persistence.
- Keep snippet expansion deterministic and static: no AI rewriting, no dynamic variables, no cloud sync, and no template filling in this first version.
- Preserve existing dictionary behavior and the current BYOK/local-first product constraints.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `dictionary-and-snippets`: Snippets becomes a real local feature with add, edit, delete, search, and deterministic trigger expansion.

## Impact

- App shell and Compose UI in `app/src/main/java/com/verbally/app/MainActivity.kt`.
- New local snippet model/repository following the existing dictionary repository pattern.
- `VerballyContainer` and `DictationCoordinator` wiring so dictation can load snippets before insertion.
- Unit tests for repository behavior and expansion matching.
- Compose instrumentation tests for Snippets CRUD/search UI.
