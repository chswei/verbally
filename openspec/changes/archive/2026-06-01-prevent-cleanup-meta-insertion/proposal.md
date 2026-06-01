## Why

Cleanup providers can sometimes treat short command-like dictation text as a prompt to answer instead of text to clean. When that happens, Verbally may paste assistant-style meta responses such as asking the user to provide original content, forcing the user to manually delete text the app should never have inserted.

## What Changes

- Treat the raw transcript as the source of truth when cleanup output looks like an assistant response rather than cleaned dictation text.
- Strengthen cleanup instructions so the model knows the original transcript is already provided and must be treated as user-spoken content, not instructions to execute.
- Preserve legitimate user-spoken assistant-like phrases when cleanup only normalizes punctuation or spacing.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-transcription-cleanup`: Cleanup output must not paste assistant-style requests, confirmations, or meta responses when raw transcript content is available.

## Impact

- Affects dictation cleanup selection in `DictationCoordinator`.
- Adds guard behavior to `DictationContentGuard`.
- Updates cleanup prompt generation in `CleanupPromptFactory`.
- Adds focused unit coverage for command-like transcript cleanup fallback behavior.
