## 1. Tooling and Project Scaffold

- [x] 1.1 Install Android command-line tooling and record Homebrew/zsh changes in dotfiles.
- [x] 1.2 Create the Android Gradle project, wrapper, manifest, resources, and baseline Compose app.
- [x] 1.3 Add dependencies and build configuration for Compose, OkHttp, coroutines, encrypted preferences, and tests.

## 2. Test-First Core Contracts

- [x] 2.1 Add unit tests for OpenAI transcription multipart request construction.
- [x] 2.2 Add unit tests for Gemini cleanup request construction using `x-goog-api-key`.
- [x] 2.3 Add unit tests for cleanup prompt language preservation rules.
- [x] 2.4 Add unit tests for paste insertion fallback and clipboard restoration behavior.
- [x] 2.5 Add unit tests for latest-100 history retention.

## 3. Dictation Services

- [x] 3.1 Implement provider models, settings storage, and encrypted API key repository.
- [x] 3.2 Implement OpenAI transcription client and OpenAI/Gemini cleanup clients.
- [x] 3.3 Implement audio recording to temporary files with cleanup on success, failure, and cancellation.
- [x] 3.4 Implement clipboard paste insertion service with manual-paste fallback state.
- [x] 3.5 Implement local history repository with search, copy/re-paste metadata, delete, and clear operations.

## 4. Android System Integration

- [x] 4.1 Implement AccessibilityService editable-field detection and focused field tracking.
- [x] 4.2 Implement floating overlay bubble with idle, recording, processing, success, error, cancel, and checkmark states.
- [x] 4.3 Wire the overlay to recording, transcription, cleanup, insertion, and history persistence.
- [x] 4.4 Add microphone, overlay, accessibility, and foreground-service permission declarations and settings intents.

## 5. Compose UI

- [x] 5.1 Implement Traditional Chinese onboarding and permission status UI.
- [x] 5.2 Implement settings UI for OpenAI key, Gemini key, cleanup provider, model strings, and history clearing.
- [x] 5.3 Implement searchable history UI with copy, re-paste, delete, and clear-all controls.

## 6. Verification

- [x] 6.1 Run `openspec validate --all --strict`.
- [x] 6.2 Run `./gradlew testDebugUnitTest`.
- [x] 6.3 Run `./gradlew assembleDebug`.
- [x] 6.4 Document any manual Android testing that remains pending.
