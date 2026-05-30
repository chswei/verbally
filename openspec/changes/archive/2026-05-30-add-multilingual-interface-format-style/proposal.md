## Why

Verbally currently presents the main app UI almost entirely in Traditional Chinese, which limits use for people who prefer another interface language. At the same time, the existing Formal/Casual cleanup layer can feel like a second rewrite pass, causing words to be replaced, shortened, or polished beyond the user's spoken intent.

## What Changes

- Add a Settings-level interface language option with `Follow system` as the default and manual choices for Traditional Chinese, English, Spanish, French, German, Italian, Brazilian Portuguese, Japanese, Korean, and Simplified Chinese.
- Localize first-party app UI copy for the supported interface languages while keeping dictation output language independent from the UI language.
- Show the default `基本文字處理提示詞` in the selected interface language when the user has not customized it.
- Preserve user-customized basic text-processing prompts exactly across interface language changes.
- Change Formal/Casual cleanup behavior from rewrite-style guidance to format-only guidance: the style layer may adjust punctuation, spacing, capitalization, and language-required formatting, but must not rewrite, shorten, replace words, translate, or change the user's tone.
- Keep dictionary context and basic cleanup behavior available to both OpenAI and Gemini cleanup requests.

## Capabilities

### New Capabilities
- `app-interface-language`: Manual interface language selection, system-language fallback, and localized first-party UI copy.

### Modified Capabilities
- `local-history-and-settings`: Settings copy and prompt defaults are no longer Traditional-Chinese-only; the UI language setting appears in Settings.
- `ai-transcription-cleanup`: Default cleanup prompt presentation becomes locale-aware, and Formal/Casual style guidance becomes format-only instead of rewrite-oriented.
- `app-style-profiles`: App category style choices continue to select Formal/Casual, but those choices only affect formatting and must preserve wording.

## Impact

- Affected app code: Compose UI and settings code in `MainActivity.kt`, settings persistence in `settings/`, prompt construction in `providers/CleanupPromptFactory.kt`, and user-visible provider/permission/overlay strings where they are part of first-party UI.
- Affected resources: Android string resources for supported locales.
- Affected tests: cleanup prompt unit tests, settings/model tests, and Compose settings/UI tests.
- No backend, account system, cloud sync, transcription-language setting, or model provider change is introduced.
