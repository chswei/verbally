## Context

Verbally already has a `片段` bottom-navigation destination, but it only renders a searchable empty state and a "coming soon" toast. The app is local-first and BYOK-only, and the recent Dictionary implementation established a lightweight repository pattern with in-memory test storage and SharedPreferences-backed production storage.

The first Snippets version should match Wispr Flow's core static text expansion behavior: users save a trigger phrase and exact expansion text, then dictation output replaces that trigger before text is inserted into the target field. Sensitive or exact content such as addresses should not be rewritten by the cleanup model.

## Goals / Non-Goals

**Goals:**

- Provide a real local Snippets screen where users can add, edit, delete, and search snippets.
- Model snippets as a required trigger phrase plus required expansion text.
- Apply saved snippets deterministically after cleanup and before insertion/history persistence.
- Keep visible UI copy in Traditional Chinese and consistent with the existing Dictionary surface.
- Keep the first version static and predictable.

**Non-Goals:**

- No cloud sync, account system, import/export, team snippets, or remote storage.
- No dynamic variables such as current date.
- No AI template filling or prompt-like snippet execution.
- No separate command mode or snippet picker in the floating overlay.
- No changes to transcription provider APIs beyond local post-processing.

## Decisions

1. **Use a dedicated local snippet repository.**
   - Create a `SnippetRepository` with `list`, `search`, `save`, and `delete`, plus in-memory and SharedPreferences-backed implementations.
   - Rationale: this mirrors Dictionary, keeps tests simple, and avoids adding Room/DataStore migration work for a compact local list.
   - Alternative considered: reuse Dictionary storage with a type flag. That risks mixing two concepts whose product semantics differ: Dictionary guides cleanup, Snippets replace text.

2. **Model snippets as `trigger` plus `expansion`.**
   - `trigger` is the exact phrase the user says during dictation, such as `我的地址` or `放射科報告模板`.
   - `expansion` is the exact text inserted in its place, including multiline templates.
   - Rationale: the model directly matches the user's requested first version and stays deterministic.

3. **Make trigger uniqueness local and case-insensitive.**
   - Saving a snippet with the same normalized trigger updates/replaces the previous trigger instead of creating ambiguous duplicate expansion behavior.
   - Rationale: two snippets with the same spoken phrase cannot both expand predictably.

4. **Apply snippets after cleanup, before insertion and history.**
   - The coordinator loads snippets, receives the cleaned transcript, expands static snippets, inserts the expanded text, and stores the expanded text in history.
   - Rationale: cleanup can still polish dictation around the trigger phrase, while exact saved expansions are protected from model rewriting.
   - Alternative considered: include snippets in the cleanup prompt. That would let the model decide expansion and could alter sensitive content, so it is not appropriate for first-version static snippets.

5. **Use deterministic phrase replacement with whole-trigger behavior.**
   - If the cleaned text is only the trigger plus trailing punctuation, replace the entire text with the expansion.
   - If the trigger appears inside longer text, replace matching trigger text in place.
   - Expansion preserves the saved casing and formatting.
   - Rationale: this follows the Wispr Flow mental model while remaining simple enough to test.

6. **Bound local storage.**
   - Keep up to 200 snippets, ordered newest/updated first.
   - Rationale: matches Dictionary's lightweight storage cap and avoids unbounded SharedPreferences payloads.

## Risks / Trade-offs

- Common triggers may expand unexpectedly -> UI copy and empty-state guidance should encourage specific triggers such as `我的住家地址` instead of `地址`.
- Post-cleanup matching may miss a trigger if cleanup changes the phrase -> keep matching deterministic for predictability; Dictionary can help preserve trigger wording if needed.
- Exact replacement inside longer text can affect every occurrence of a trigger -> require unique triggers and document static expansion as exact text replacement.
- SharedPreferences JSON is not ideal for large template libraries -> acceptable for the first local-only version with a 200 item cap.

## Migration Plan

- Existing users have no snippet data, so production storage starts empty.
- Existing placeholder UI is replaced by real CRUD behavior.
- Rollback is safe: removing the repository wiring leaves stored SharedPreferences data unused but local to the app.
