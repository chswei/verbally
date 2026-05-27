## Why

Verbally already exposes a `字典` destination, but it is still a placeholder and cannot help dictation quality. A local personal dictionary lets users preserve common terms, proper nouns, and preferred spellings so cleanup can produce text closer to their actual usage.

## What Changes

- Replace the Dictionary placeholder with a local, searchable dictionary list.
- Let users add, edit, and delete dictionary entries from the main app.
- Store dictionary entries locally on device, with no account, backend, or sync.
- Include active dictionary entries in cleanup prompts so AI text cleanup can prefer the user's saved vocabulary.
- Preserve the existing Snippets placeholder behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `dictionary-and-snippets`: Dictionary becomes a real local vocabulary management surface while Snippets remains a placeholder.
- `ai-transcription-cleanup`: Cleanup requests include relevant dictionary vocabulary as user preference context.

## Impact

- Adds local dictionary models and repository code under `app/src/main/java/com/verbally/app/`.
- Updates `MainActivity.kt` Dictionary UI from placeholder state to searchable CRUD.
- Updates cleanup prompt construction and orchestration to read dictionary entries.
- Adds unit and Compose instrumentation coverage for repository behavior, prompt integration, and Dictionary UI interactions.
