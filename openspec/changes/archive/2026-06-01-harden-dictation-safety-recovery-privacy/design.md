## Context

Verbally currently shows the floating bubble when the input method is visible, records temporary audio, sends the completed recording to the selected transcription provider, cleans the transcript, and inserts or falls back to clipboard. History, dictionary, snippets, style profiles, and API keys are already local-first. The new work hardens the surrounding Android product behavior without changing transcription architecture.

The affected seams already exist:

- `VerballyAccessibilityService` and `OverlayVisibilityPolicy` decide when the bubble appears.
- `FloatingDictationOverlay` owns ready, recording, processing, and feedback UI.
- `PermissionScreens` and settings repositories know microphone, overlay, and accessibility readiness.
- `DictationHistoryRepository` persists the latest local history.
- `DictionaryRepository`, `SnippetRepository`, and `SnippetExpander` provide local text helpers.

## Goals / Non-Goals

**Goals:**

- Keep the bubble out of sensitive input contexts and known financial apps.
- Surface permission/service recovery through a repair bubble state instead of silent failure.
- Let users choose local history retention behavior.
- Prevent dictionary/snippet conflicts and duplicate entries caused by edits or casing/whitespace changes.
- Preserve all local-first behavior and existing provider choices.

**Non-Goals:**

- No OpenAI Realtime, streaming transcription, gRPC, or audio pipeline rewrite.
- No account system, cloud sync, enterprise administration, HIPAA workflow, or backend proxy.
- No broad UI redesign beyond the controls and states required for this hardening.
- No automatic deletion of dictionary or snippet entries when history retention changes.

## Decisions

1. Add focused policies instead of expanding the accessibility service inline.

   `SensitiveInputPolicy` will classify the active app package and focused node metadata. It will return a reason when the bubble must be hidden, keeping `VerballyAccessibilityService` focused on routing events. This is preferred over inlining checks because password/numeric/app-package rules will need tests and future maintenance.

2. Represent repair as an overlay state driven by readiness, not as a separate onboarding surface.

   `FloatingDictationOverlay` will gain a repair state with a short localized message and tap action. `VerballyAccessibilityService` can show it when a recoverable permission or service dependency is missing. This keeps recovery close to the place where the user notices the problem and avoids duplicating the full permission onboarding flow.

3. Store history retention as a local setting and enforce it inside the repository.

   A `HistoryRetentionMode` setting will control whether history is saved normally, auto-deletes entries older than 24 hours, or does not save new entries. Enforcement belongs in the repository so every save/list path observes the same policy. Existing history will remain intact when the setting is changed unless the selected mode explicitly requires deletion.

4. Normalize identity keys for dictionary terms and snippet triggers.

   Dictionary terms and snippet triggers will use trimmed, case-insensitive normalized keys for conflict checks and update semantics. This prevents Android-style duplicate leftovers when an entry is edited only by case/whitespace or renamed to an existing key.

5. Keep conflict rules local and deterministic.

   The dictionary/snippet conflict check will happen before persistence and return a validation error for UI display. It will not call AI, infer synonyms, or rewrite user entries.

## Risks / Trade-offs

- Sensitive field metadata differs by app and keyboard → Start with stable Android signals (`isPassword`, input type class/variation where available, package blocklist) and tests for policy decisions; keep unknown fields allowed unless clearly sensitive.
- Banking package coverage can drift → Use a small built-in package list for high-confidence apps first, with tests and a single policy file for updates.
- Repair state could appear too aggressively → Only show repair when a required dependency is actually missing; otherwise preserve existing bubble behavior.
- Retention settings may surprise users if they delete history → Require confirmation for destructive retention changes and make disabled history visible in the History screen.
- Repository conflict checks can complicate current simple CRUD APIs → Prefer small result types over throwing exceptions so Compose screens can show localized validation messages.
