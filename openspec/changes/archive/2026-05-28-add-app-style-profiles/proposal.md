## Why

Verbally currently has one global text-processing prompt, so users cannot make dictated text feel more conversational in chat apps while keeping work text polished. The new style profile page gives users a simple way to choose Formal or Casual output per app category without editing prompt text.

## What Changes

- Add app style profiles for three categories: `聊天`, `工作`, and `其他`.
- Add a fifth main navigation destination, `語氣`, where users select Formal or Casual for each category.
- Detect the foreground app package during dictation and map it to an app category, falling back to `其他`.
- Rename the existing cleanup prompt concept in UI and prompt composition to `基本文字處理提示詞`.
- Compose cleanup requests as basic text processing first, then apply the selected Formal or Casual style.
- Preserve dictionary context and safety rules across all styles.

## Capabilities

### New Capabilities
- `app-style-profiles`: App category classification, per-category Formal/Casual style settings, defaults, and the `語氣` UI.

### Modified Capabilities
- `ai-transcription-cleanup`: Cleanup must combine the basic text-processing prompt, dictionary context, detected app category, and selected style before producing final text.
- `main-navigation-shell`: Bottom navigation must expose `語氣` as the fifth product destination.

## Impact

- Adds local style-profile settings and app category classification logic.
- Updates `DictationCoordinator` and provider request prompt construction.
- Updates the Compose main shell and settings UI copy.
- Adds focused unit tests for style defaults, app category detection, prompt composition, and coordinator wiring.
