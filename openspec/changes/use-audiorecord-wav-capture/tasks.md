## 1. WAV Capture Contracts

- [x] 1.1 Add tests for recorder defaults: `MIC`, 16 kHz, mono input, PCM16, and `.wav` temp files.
- [x] 1.2 Add tests for WAV header writing and final data-size patching.
- [x] 1.3 Add tests for PCM amplitude calculation used by `currentAmplitude()`.

## 2. Recorder Implementation

- [x] 2.1 Replace the `MediaRecorder` temporary recorder path with `AudioRecord` PCM capture.
- [x] 2.2 Write confirmed recordings as temporary WAV files with correct PCM byte counts.
- [x] 2.3 Preserve stop, cancel, release, delete, max-duration, and temp-file cleanup behavior.

## 3. Verification

- [x] 3.1 Run `openspec validate --all --strict`.
- [x] 3.2 Run focused recorder unit tests.
- [x] 3.3 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`.
- [x] 3.4 Install the debug APK on the connected approved device.
