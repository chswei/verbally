## Why

Verbally currently records confirmed dictation audio from the raw microphone source, so it does not explicitly ask Android for a speech-recognition capture path. Using Android's built-in speech-optimized capture source can improve voice clarity on supported devices without adding post-recording processing that would delay transcription.

## What Changes

- Record dictation audio with Android's speech-recognition audio source by default.
- Keep the existing confirm-first recording lifecycle, temporary `.m4a` output, provider transcription requests, cleanup, insertion, and history behavior.
- Do not add cloud denoising, post-recording audio enhancement, new settings UI, or a custom audio processing pipeline.

## Capabilities

### New Capabilities

- `speech-optimized-recording`: Captures dictation audio through Android's speech-recognition capture path so supported devices can apply built-in voice-focused preprocessing in real time.

### Modified Capabilities

- `ai-transcription-cleanup`: Confirmed recordings remain temporary audio sent directly to the selected BYOK transcription provider, but the temporary audio is captured with Android's speech-recognition source before transcription.

## Impact

- Affects `app/src/main/java/com/verbally/app/audio/TemporaryAudioRecorder.kt`.
- Adds focused unit coverage for the recorder's default audio source selection.
- No new dependencies, permissions, provider APIs, persistence, or user-visible settings.
