## Context

Verbally is an Android 14+ Compose app with user-visible copy currently concentrated in Kotlin strings, plus a small number of resource strings for the app name and accessibility service metadata. Settings are stored in `AppSettings` through `EncryptedSettingsRepository`. Cleanup prompts are built in `CleanupPromptFactory`, where the basic text-processing prompt, dictionary context, active app category, and selected Formal/Casual style are currently composed into one provider prompt.

The user wants the app interface to support common Western and Asian languages, but dictation output must remain independent from the interface language. The user also clarified an important quality constraint: after transcription and basic text processing, Formal/Casual must not become a second rewriting or polishing pass. For Traditional Chinese, the intended difference is only punctuation versus spacing; for other languages, style should remain similarly conservative and format-only.

## Goals / Non-Goals

**Goals:**
- Add a Settings language control with `Follow system` and the first supported manual language set: `zh-TW`, `en`, `es`, `fr`, `de`, `it`, `pt-BR`, `ja`, `ko`, and `zh-CN`.
- Make first-party UI copy follow the selected interface language while preserving local-only settings and BYOK behavior.
- Make default basic text-processing prompt display in the selected interface language when it is still the built-in default.
- Preserve custom basic text-processing prompts exactly across language changes.
- Make Formal/Casual style guidance format-only and test it as a prompt contract.

**Non-Goals:**
- No transcription-language picker; transcription remains provider/model-driven multilingual detection.
- No translation of dictated output into the interface language.
- No backend, account system, cloud sync, or model provider change.
- No advanced per-language style editor in Settings.
- No OpenSpec archive unless the user explicitly asks after implementation.

## Decisions

1. **Persist interface language as an app setting and apply it through Android app locales.**
   - Store an `AppLanguage` value in `AppSettings`, defaulting to `SYSTEM`.
   - On startup and when saved from Settings, apply the language tag through Android's `LocaleManager` application locales. `SYSTEM` clears the app locale override.
   - Rationale: Verbally targets Android 14+, so the platform app-locale API is available and avoids adding AppCompat only for locale management.
   - Alternative considered: keep a custom in-memory string map and ignore Android locale APIs. Rejected because services, resources, and system surfaces should follow the same locale source.

2. **Move user-facing copy toward Android string resources and locale resource files.**
   - Use `values/strings.xml` as the Traditional Chinese base and add locale-qualified `strings.xml` files for supported languages.
   - Use resource formatting for short dynamic labels where practical.
   - Rationale: This follows Android conventions and lets platform locale changes refresh Compose `stringResource` usage.
   - Trade-off: The first implementation touches many UI strings. Tests should focus on high-signal settings and prompt behavior instead of asserting every translation.

3. **Represent default prompt display separately from custom prompt persistence.**
   - Add locale-aware default prompt text for UI display and prompt construction.
   - Track whether the saved prompt is custom versus default by comparing against known built-in defaults or by an explicit custom flag if needed during implementation.
   - When the prompt is still default, interface-language changes show the default in the new language. When custom, language changes leave the user's text untouched.
   - Rationale: The user sees a prompt they can read, but custom prompts remain the user's exact wording.

4. **Make Formal/Casual a format layer, not a rewrite layer.**
   - The style block must say the model may only adjust formatting: punctuation, spacing, capitalization, and language-required writing conventions.
   - The style block must forbid rewriting, shortening, synonym replacement, translation, invented facts, and tone/register changes.
   - For Traditional Chinese, Formal means punctuation; Casual means spacing/light separation instead of formal punctuation.
   - For other supported languages, the guidance remains conservative: preserve words and register, only normalize readable formatting.
   - Rationale: The user explicitly prefers the basic text-processing result and wants style to avoid self-important polishing.

5. **Keep the app-style page simple.**
   - The user still chooses Formal/Casual per `聊天`, `工作`, and `其他`.
   - No per-language style editor appears in Settings.
   - Rationale: The product surface remains simple while the internal prompt contract becomes stricter.

## Risks / Trade-offs

- Large string migration can miss copy in less common flows → use `rg` for Traditional Chinese literals and add focused Compose coverage around Settings language controls.
- Runtime locale switching can require activity recreation to refresh visible strings → apply the locale on save and recreate the activity if needed.
- Localized prompt wording could drift in meaning across languages → keep tests focused on the canonical prompt contract and use conservative translations.
- Comparing saved custom prompts against built-in defaults can be brittle as defaults evolve → prefer an explicit persisted custom/default marker if implementation shows ambiguity.
- Prompt-only constraints cannot guarantee model behavior → keep wording strict, add regression tests for generated request text, and leave stronger deterministic post-processing for a future change if needed.

## Migration Plan

Existing installs load `AppLanguage.SYSTEM`. Existing API keys, history, dictionary entries, snippets, theme mode, and style profiles remain unchanged. Existing custom cleanup prompts remain custom text. Existing saved prompt values equal to any known built-in default can be treated as default and displayed in the selected interface language; other saved values remain untouched.
