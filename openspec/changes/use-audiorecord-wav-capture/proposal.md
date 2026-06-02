## Why

Verbally currently records confirmed dictation as AAC inside an `.m4a` container, which is good for compact general audio but may add avoidable encode, finalize, decode, and resampling work before speech-to-text can begin. Wispr Flow's Android capture path appears to favor 16 kHz mono PCM WAV, which is a simpler speech-recognition input format and fits Verbally's existing confirm-first flow without adding real-time streaming.

## What Changes

- Replace the temporary dictation recorder implementation with an `AudioRecord`-based recorder that writes 16 kHz mono PCM16 audio to a temporary `.wav` file.
- Preserve the existing `AudioRecorder` contract used by `DictationCoordinator`, including start, stop, amplitude, delete, and cancellation behavior.
- Keep the flow batch-oriented: recording still completes before transcription begins, and no real-time transcription or streaming provider path is added.
- Continue deleting temporary audio after success, failure, or cancellation.
- Add focused tests for WAV formatting, recorder defaults, and file cleanup behavior.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `speech-optimized-recording`: Dictation recording changes from Android `VOICE_RECOGNITION` `.m4a` capture to temporary 16 kHz mono PCM16 WAV capture.
- `ai-transcription-cleanup`: Transcription provider requirements should describe confirmed recorder audio generically instead of tying provider uploads to Android's speech-recognition audio source.

## Impact

- Affects `app/src/main/java/com/verbally/app/audio/TemporaryAudioRecorder.kt` and related recorder tests.
- May add small internal helpers for WAV header writing and PCM byte accounting.
- Audio files become larger than AAC `.m4a` files, but remain temporary and local-only.
- Provider request flow remains unchanged because OpenAI, Soniox, and Groq transcription clients already accept file uploads from the recorder path.
