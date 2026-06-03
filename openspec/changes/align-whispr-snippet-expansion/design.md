## Context

Verbally already stores local dictionary entries and snippets, validates dictionary/snippet key conflicts, feeds dictionary terms into cleanup prompts, and applies snippets after cleanup before insertion. Public Wispr Flow Android documentation describes the same product split: dictionary entries teach vocabulary, while snippets are static voice shortcuts whose spoken cue inserts the saved text. The connected Samsung phone has Wispr Flow `1.9.1` installed; black-box UI inspection confirmed Android bottom tabs for `Dictionary` and `Snippets`, search fields, floating add buttons, a dictionary `Add word` dialog with one word field, and a snippet `Add snippet` dialog with `Snippet` and `Expansion` fields.

The current Verbally snippet path trims expansion text during repository normalization and again before expansion. That can remove user-saved leading/trailing newlines or spacing from templates. The current regex also replaces trigger text anywhere it appears, which can fire inside a longer Latin-style word instead of acting like a spoken cue.

## Goals / Non-Goals

**Goals:**
- Preserve snippet expansion text exactly as saved when dictation expands a trigger.
- Continue trimming snippet triggers so search, validation, duplicate checks, and conflict checks stay stable.
- Keep snippet expansion deterministic and local.
- Avoid substring false positives for Latin-style tokens while still supporting Chinese/Japanese/Korean trigger phrases inside natural sentence text.

**Non-Goals:**
- No cloud sync, accounts, shared/team snippets, or bulk import.
- No Android dictionary misspelling replacement rules.
- No automatic learning from user edits.
- No changes to transcription, cleanup provider APIs, or insertion mechanics.

## Decisions

1. Preserve expansion text at persistence and expansion time.
   The repository will trim only the trigger. It will reject blank expansions by checking `expansion.trim().isNotEmpty()`, but it will store the expansion string as entered. `SnippetExpander` will likewise avoid trimming expansion text. This matches the static-template behavior users expect from a snippet.

2. Keep trigger identity normalized separately from expansion content.
   Trigger keys remain trimmed and case-insensitive through `normalizedLocalEntryKey`. This preserves the existing duplicate-safe edit behavior and dictionary/snippet conflict checks.

3. Match triggers only at sensible token boundaries.
   `SnippetExpander` will continue preferring longer triggers first, but regex replacement will require the character before and after a match not to be a Latin letter or digit. This prevents `api` from expanding inside `rapidapi` while allowing Chinese phrase matches such as `請寄到我的地址。`.

4. Apply formatting-preserving behavior before insertion and history.
   `DictationCoordinator` already expands snippets before insertion and history persistence. No orchestration change is required once the repository and expander preserve expansion content.

## Risks / Trade-offs

- [Risk] Keeping surrounding expansion whitespace can preserve accidental spaces typed by the user. -> Mitigation: the UI still requires a non-blank expansion, and this is preferable for templates where blank lines are intentional.
- [Risk] Boundary detection is heuristic for multilingual text. -> Mitigation: limit boundary blocking to Latin letters and digits so CJK phrase matching remains useful.
- [Risk] Existing saved snippets may already have had formatting trimmed at save time. -> Mitigation: no migration can restore lost whitespace, but future edits/saves will preserve it.
