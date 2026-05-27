## Context

The main app already has a `字典` bottom-navigation destination, but it only shows a placeholder and a toast. Cleanup requests currently use the saved cleanup prompt plus raw transcript; they do not receive any user vocabulary context.

Verbally is local-first and BYOK-only. Dictionary data should stay on device, follow the existing repository pattern used by history/settings, and avoid adding backend, account, or sync behavior.

## Goals / Non-Goals

**Goals:**

- Provide a real local Dictionary screen where users can add, edit, delete, and search vocabulary.
- Store each entry with a required term and optional note so users can capture proper nouns, spelling preferences, or short disambiguation hints.
- Make cleanup prompt construction include dictionary entries in both OpenAI and Gemini cleanup requests.
- Keep the UI Traditional Chinese and consistent with the existing Compose app shell.

**Non-Goals:**

- No cloud sync, accounts, remote provider-key proxy, import/export, or dictionary sharing.
- No automatic dictionary extraction from dictation history.
- No Snippets CRUD in this change.
- No transcription API customization beyond cleanup prompt context.

## Decisions

1. **Use a dedicated local dictionary repository.**
   - Create a `DictionaryRepository` with `list`, `search`, `save`, and `delete`, plus an in-memory implementation for tests and a SharedPreferences-backed implementation for the app.
   - Rationale: this matches the existing history repository pattern, keeps the feature testable, and avoids schema/database dependencies for a small local list.
   - Alternative considered: DataStore or Room. Those are stronger for larger schemas, but they add dependency and migration work that is not needed for a compact vocabulary list.

2. **Model entries as `term` plus optional `note`.**
   - `term` is the exact word, phrase, name, or preferred spelling the user wants preserved.
   - `note` is optional context such as pronunciation, meaning, or preferred capitalization.
   - Rationale: one required field keeps adding fast, while the note gives cleanup enough context for ambiguous terms.

3. **Pass dictionary entries through `CleanupPromptFactory`.**
   - The coordinator reads dictionary entries before cleanup, then gives them to the cleanup clients through the existing cleanup prompt path.
   - The prompt factory appends a compact `使用者字典` section before the raw transcript.
   - Rationale: both OpenAI and Gemini already route through `CleanupPromptFactory`, so this keeps provider behavior consistent.

4. **Bound prompt context and local storage.**
   - Persist up to the latest 200 entries.
   - Include up to 100 entries in cleanup context, ordered as the user sees them.
   - Rationale: this prevents runaway prompt size while keeping normal personal dictionaries fully represented.

5. **Keep Dictionary UI in the existing main-app Compose surface.**
   - Replace the placeholder with a searchable list, add/edit dialog, delete action, and honest empty states.
   - Rationale: the current `MainActivity.kt` already owns the bottom destinations, and this keeps the first implementation focused.

## Risks / Trade-offs

- Prompt context can grow and increase cleanup token use → cap the entries included in cleanup.
- A term-only dictionary can still be ambiguous → optional notes give users a low-friction way to disambiguate.
- SharedPreferences JSON is not ideal for large datasets → cap entries and keep the repository boundary so storage can be swapped later.
- Dictionary entries may not always improve transcription if the raw transcript is far from the intended word → apply the dictionary during cleanup, where the model can still use context and notes.

## Migration Plan

- Existing users have no dictionary data; the screen starts empty.
- The previous placeholder UI is replaced in place, with no data migration.
- Rollback removes the new repository and returns the Dictionary destination to placeholder behavior; stored dictionary JSON can remain ignored by older code.

## Open Questions

None for the first implementation. Future work can decide whether to add import/export, per-entry enable toggles, or automatic suggestions from history.
