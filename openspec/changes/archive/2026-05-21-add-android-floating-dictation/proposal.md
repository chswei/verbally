## Why

Android users need a fast system-wide dictation tool that works in ordinary app text fields without switching keyboards or copying text through a separate notes app. A floating bubble workflow can provide the Spokenly/Wispr Flow style experience while keeping the first release focused on internal testing and BYOK privacy.

## What Changes

- Add a native Android 14+ app named Verbally using Kotlin and Jetpack Compose.
- Show a floating dictation bubble when an editable text field is focused.
- Record speech from the bubble, transcribe it with the user's OpenAI API key, clean the text with OpenAI or Gemini, and paste it at the active cursor.
- Store only local settings, encrypted API keys, and the latest 100 dictation history entries.
- Provide Traditional Chinese onboarding, permission guidance, settings, and history screens.
- Do not add an IME keyboard, backend service, account system, cloud sync, or realtime partial transcription in v1.

## Capabilities

### New Capabilities

- `floating-dictation-overlay`: Detect editable fields, present the floating bubble, manage recording states, and paste text into the active cursor.
- `ai-transcription-cleanup`: Transcribe audio with OpenAI and clean transcripts with either OpenAI or Gemini using BYOK credentials.
- `local-history-and-settings`: Store encrypted provider keys, user settings, and local dictation history capped at 100 entries.

### Modified Capabilities

None.

## Impact

- Creates a new Android application project in the repository.
- Adds Android command-line build requirements: Android SDK, Gradle wrapper, Android Gradle Plugin, Compose, OkHttp, coroutines, and AndroidX Security Crypto.
- Uses Android accessibility, overlay, microphone, clipboard, and local storage APIs.
- Adds OpenSpec artifacts, Android unit tests, and a debug APK build path for internal testing.
