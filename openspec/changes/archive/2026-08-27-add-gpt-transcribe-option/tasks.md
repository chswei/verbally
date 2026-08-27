## 1. Test Coverage

- [x] 1.1 Update model-option tests to require the new `OpenAI: gpt-transcribe` choice while preserving the existing default.
- [x] 1.2 Add request-factory tests proving `gpt-transcribe` omits unsupported legacy parameters and GPT-4o transcription models retain confidence parameters.

## 2. Implementation

- [x] 2.1 Add `gpt-transcribe` to the curated OpenAI transcription options.
- [x] 2.2 Make OpenAI multipart transcription parameters model-aware without changing shared response parsing.

## 3. Validation and Delivery

- [x] 3.1 Run strict OpenSpec validation, debug unit tests, and debug APK assembly.
- [x] 3.2 Install the latest debug APK on the currently connected Android device without running device tests or clearing app data.
