## 1. Recorder Behavior

- [x] 1.1 Add a focused unit test proving Verbally's default recorder audio source is Android `VOICE_RECOGNITION`.
- [x] 1.2 Update `TemporaryAudioRecorder` to use the speech-recognition audio source while preserving output format, encoder, bitrate, sample rate, max duration, temporary file cleanup, and amplitude polling.

## 2. Validation

- [x] 2.1 Run the focused recorder unit test and confirm it fails before implementation, then passes after implementation.
- [x] 2.2 Run `openspec validate --all --strict`.
- [x] 2.3 Run `./gradlew testDebugUnitTest`.
- [x] 2.4 Run `./gradlew assembleDebug`.
