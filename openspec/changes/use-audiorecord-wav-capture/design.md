## Context

Verbally currently uses `MediaRecorder` to record temporary `.m4a` files with AAC encoding at 44.1 kHz. That implementation is compact and simple, but it is optimized for general recorded audio rather than the simplest speech-to-text input path. Reverse engineering of Wispr Flow's installed Android build showed a lower-level `AudioRecord` path that captures 16 kHz mono PCM16, writes a WAV header, and feeds the same raw audio shape to transcription.

The app's orchestration already depends on an `AudioRecorder` interface, so the recorder implementation can change without redesigning confirmation, provider selection, cleanup, insertion, history, or temporary-file deletion.

## Goals / Non-Goals

**Goals:**

- Capture dictation as temporary 16 kHz mono PCM16 WAV audio.
- Keep the existing confirm-first batch transcription flow.
- Preserve current audio lifecycle behavior: stop, cancellation, delete, and amplitude reporting.
- Keep provider clients unchanged unless a provider rejects WAV in testing.
- Add focused JVM tests for WAV formatting and recorder configuration where Android APIs can be isolated.

**Non-Goals:**

- No real-time transcription or streaming provider path.
- No backend, provider proxy, account system, or cloud sync.
- No post-recording denoise or enhancement pass.
- No UI setting for choosing audio format in this change.

## Decisions

1. Use `AudioRecord` instead of `MediaRecorder`.
   - Rationale: `AudioRecord` exposes PCM buffers directly, avoiding local AAC encoding and m4a container finalization before upload.
   - Alternative considered: keep `MediaRecorder` and change bitrate/sample rate. That still leaves AAC/container work and does not match the Wispr-like capture shape.

2. Use 16 kHz mono PCM16 WAV as the temporary file format.
   - Rationale: 16 kHz mono PCM16 is speech-recognition friendly, avoids client-side compression artifacts, and is accepted by common STT upload APIs.
   - Trade-off: WAV files are larger than 128 kbps AAC, roughly 32 KB/s plus a small header.

3. Default to `MediaRecorder.AudioSource.MIC`.
   - Rationale: This mirrors the observed Wispr Flow recorder path and avoids relying on device-specific speech-recognition preprocessing.
   - Alternative considered: keep `VOICE_RECOGNITION`. That may be useful on some devices, but it keeps the current behavior's premise rather than testing the Wispr-like path the user wants to explore.

4. Keep `TemporaryAudioRecorder` as the integration point.
   - Rationale: Existing service construction and coordinator tests already use the `AudioRecorder` abstraction. A scoped replacement minimizes blast radius.
   - Implementation note: small internal helpers can handle WAV header writing and PCM amplitude calculation, making the low-level file format testable.

## Risks / Trade-offs

- WAV uploads are larger than AAC uploads -> Mitigation: keep this as a batch temp-file change first and validate real dictation latency on the connected phone before deciding whether to optimize further.
- `AudioRecord` has more failure modes than `MediaRecorder` -> Mitigation: handle initialization/read errors defensively, release `AudioRecord` in all stop/cancel paths, and preserve temp-file deletion.
- MIC vs `VOICE_RECOGNITION` may differ by device -> Mitigation: keep the audio source injectable for focused tests and future A/B experiments.
- WAV header corruption would break provider uploads -> Mitigation: test header fields, data length, byte rate, block align, and final file length.

## Migration Plan

1. Add testable WAV-writing helpers and recorder defaults.
2. Replace the `MediaRecorder` implementation with `AudioRecord` while preserving the `AudioRecorder` interface.
3. Update tests for `.wav` output and temporary-file deletion.
4. Run OpenSpec validation, unit tests, and debug APK build.
5. Install the debug APK on the connected approved phone for manual latency/accuracy comparison.
