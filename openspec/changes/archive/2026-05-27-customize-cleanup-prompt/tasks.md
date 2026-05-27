## 1. Settings Data

- [x] 1.1 Add cleanup prompt to `AppSettings` with the built-in prompt as the default.
- [x] 1.2 Persist the cleanup prompt in `EncryptedSettingsRepository` and default blank/missing stored values safely.

## 2. Provider Requests

- [x] 2.1 Update cleanup prompt construction so OpenAI and Gemini both use the configured prompt plus raw transcript.
- [x] 2.2 Update cleanup request tests to cover custom prompt behavior.

## 3. Settings UI

- [x] 3.1 Add a multi-line cleanup prompt editor near the cleanup provider/API key/model settings.
- [x] 3.2 Add a restore-default cleanup prompt action that preserves provider, API key, and model settings.
- [x] 3.3 Update settings UI tests for provider-specific visibility and prompt editing/default restore behavior.

## 4. Validation

- [x] 4.1 Run OpenSpec validation.
- [x] 4.2 Run unit tests, targeted instrumentation checks, and build the debug APK.
- [x] 4.3 Install the latest debug APK to the currently connected Android device.
