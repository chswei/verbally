## 1. OpenSpec and Planning

- [x] 1.1 Create and validate OpenSpec proposal, design, spec deltas, and implementation tasks.
- [x] 1.2 Map current settings, prompt, and localization seams before production edits.

## 2. Format-Only Prompt Behavior

- [x] 2.1 Add failing unit tests that Formal/Casual style guidance is format-only and forbids rewriting, shortening, synonym replacement, translation, and tone changes.
- [x] 2.2 Update cleanup prompt construction so Formal/Casual only adjusts formatting while preserving the basic text-processing result.
- [x] 2.3 Verify OpenAI and Gemini cleanup request tests still include dictionary, style, and transcript context correctly.

## 3. Interface Language Settings

- [x] 3.1 Add an `AppLanguage` setting with `Follow system` default, supported locale tags, stable persistence, and unit tests.
- [x] 3.2 Apply the selected app locale on startup and after Settings changes without affecting dictation output language.
- [x] 3.3 Add Settings UI for interface language selection and focused Compose instrumentation coverage.
- [x] 3.4 Present appearance and interface language as compact Settings rows that open YouTube-style single-choice dialogs.

## 4. Localized UI and Default Prompt Presentation

- [x] 4.1 Move first-party Settings/navigation/Home/History/Style/Dictionary/Snippets/permission copy toward localized resources for the first supported locale set.
- [x] 4.2 Make default basic text-processing prompt display in the selected interface language when unchanged.
- [x] 4.3 Preserve custom basic text-processing prompts exactly across interface language changes and restore defaults in the current interface language.

## 5. Validation and Delivery

- [x] 5.1 Run `openspec validate --all --strict`.
- [x] 5.2 Run `./gradlew testDebugUnitTest`.
- [x] 5.3 Run focused Compose instrumentation on an emulator when available.
- [x] 5.4 Run `./gradlew assembleDebug`.
- [x] 5.5 Install the latest debug APK to the currently connected approved device if one is connected, install-only with no data clearing.
- [x] 5.6 Review the final diff for accidental rewrites, generated artifacts, or unlocalized obvious copy.

## 6. Custom Formal/Casual Style Rules

- [x] 6.1 Add failing unit tests for per-language Formal/Casual rule persistence and prompt override behavior.
- [x] 6.2 Add a local style-rule repository scoped by interface language and output style.
- [x] 6.3 Update cleanup prompt construction so custom rules override only the selected language/style while preserving format-only guardrails.
- [x] 6.4 Add Style-page UI entries and an editor flow for Formal and Casual rules with restore-default behavior.
- [x] 6.5 Add focused Compose instrumentation for the custom style-rule editor.
- [x] 6.6 Run OpenSpec, unit, instrumentation, build, install/screenshot, and diff validation.
