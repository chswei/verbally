## Context

Verbally cleans transcribed text through the configured OpenAI or Gemini cleanup provider. The current cleanup prompt is global, so users must edit prompt text if they want different output for chat and work apps. The app already keeps local settings, dictionary entries, snippets, and dictation history, and the accessibility service can observe the active editable app package during dictation.

The product direction is to keep one shared basic text-processing prompt, then apply a lightweight Formal or Casual style according to the detected app category. The first version uses only three categories: `聊天`, `工作`, and `其他`.

## Goals / Non-Goals

**Goals:**
- Add local app style profiles for `聊天`, `工作`, and `其他`.
- Add a `語氣` main navigation destination for changing each category's Formal/Casual style.
- Classify known app packages into the three categories and use `其他` when unknown.
- Keep the existing cleanup prompt capability, but present it as `基本文字處理提示詞`.
- Compose cleanup instructions as basic processing first, then selected style.
- Keep dictionary context higher priority than style.

**Non-Goals:**
- No per-app override UI in the first version.
- No new cleanup styles beyond Formal and Casual.
- No cloud sync, backend classification service, or account-level profile storage.
- No provider changes beyond prompt construction.

## Decisions

### Store style profiles locally with existing settings patterns

Use a small local repository backed by SharedPreferences, matching dictionary, snippets, history, and settings storage already used in the app. A dedicated repository keeps profile defaults and app classification separate from encrypted provider settings. Defaults are `聊天 = Casual`, `工作 = Formal`, and `其他 = Formal`.

Alternative considered: store the values in `AppSettings`. This would be simpler at first, but `AppSettings` is provider/API-key focused and already uses encrypted preferences. Style profiles are non-secret product settings, so a focused repository is clearer.

### Use category-level settings only

The first version exposes only three category rows. The classifier maps known package prefixes and package names into categories, then falls back to `其他`.

Alternative considered: per-app overrides. This is more flexible, but it adds management UI, unknown-app discovery, and conflict handling before the basic model is proven useful.

### Treat Formal/Casual as a style layer, not a replacement prompt

Prompt composition keeps this order: safety rules, dictionary context, basic text-processing prompt, selected style, raw transcript. The provider receives one request, but the instructions are structured as if the model first performs common cleanup and then applies style. This avoids extra API cost and latency while preserving a clean product model.

Alternative considered: two provider calls, one for basic cleanup and one for style. That would be easier to reason about but doubles latency and cost for little first-version benefit.

### Rename the existing cleanup prompt in UI

The current user-editable cleanup prompt remains available and keeps existing saved values. UI copy changes to `基本文字處理提示詞` to make clear that it applies before Formal/Casual style. The implementation continues using the existing settings field to avoid data migration.

Alternative considered: hide the prompt entirely. That would simplify the UI, but it would remove an existing advanced capability and make saved custom prompts harder to discover.

## Risks / Trade-offs

- Style and custom prompt can still conflict if the basic prompt asks for a specific tone. Mitigation: UI copy describes the field as basic processing, and prompt composition tells the model that Formal/Casual controls final style.
- Foreground package detection may be unavailable in some accessibility states. Mitigation: fall back to `其他`.
- Package classification will be incomplete. Mitigation: classify high-confidence common apps and keep unknown apps predictable through `其他`.
- A fifth bottom navigation item creates tighter mobile spacing. Mitigation: use the short label `語氣` and a simple icon matching existing destinations.
