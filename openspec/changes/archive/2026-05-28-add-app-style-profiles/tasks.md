## 1. Style Model And Storage

- [x] 1.1 Add tests for default app style profiles, saved profile updates, and known app category classification.
- [x] 1.2 Implement app style profile model, classifier, in-memory repository, and SharedPreferences repository.
- [x] 1.3 Wire the style profile repository into `VerballyContainer`.

## 2. Prompt Composition And Dictation Flow

- [x] 2.1 Add tests that cleanup prompts include the basic text-processing prompt before Formal/Casual style instructions.
- [x] 2.2 Update cleanup prompt construction for basic prompt, dictionary context, app category, and selected style.
- [x] 2.3 Add coordinator tests proving dictation passes the active app category style to OpenAI and Gemini cleanup.
- [x] 2.4 Update `DictationCoordinator` and text cleanup client interfaces to pass app style context.

## 3. Main App UI

- [x] 3.1 Add a `語氣` bottom navigation destination and icon.
- [x] 3.2 Add tests for the style page showing `聊天`, `工作`, and `其他` with Formal/Casual controls.
- [x] 3.3 Implement the `語氣` page and persist changes through the style profile repository.
- [x] 3.4 Rename settings UI copy from cleanup prompt to `基本文字處理提示詞`.

## 4. Validation

- [x] 4.1 Run `openspec validate --all --strict`.
- [x] 4.2 Run `./gradlew testDebugUnitTest`.
- [x] 4.3 Run `./gradlew assembleDebug`.
- [x] 4.4 Install the latest debug APK to the approved connected device if available.
