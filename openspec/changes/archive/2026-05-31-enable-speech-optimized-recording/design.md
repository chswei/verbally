## Context

Verbally records a temporary `.m4a` through `MediaRecorder` and starts transcription only after the user confirms the recording. The current recorder uses `MediaRecorder.AudioSource.MIC`, which captures from the microphone without explicitly selecting a voice-recognition capture profile.

Android exposes built-in capture profiles through `MediaRecorder.AudioSource`. `VOICE_RECOGNITION` is the closest match for Verbally because the app captures speech for transcription, not a two-way call. Device manufacturers can attach voice-focused preprocessing to that capture path, so this gives supported devices a chance to reduce background noise or optimize speech clarity while recording is happening in real time.

## Goals / Non-Goals

**Goals:**

- Use Android's speech-recognition capture source for dictation recordings.
- Keep transcription latency unchanged apart from normal device capture behavior.
- Keep the existing temporary-file recorder architecture and provider request flow.
- Make the selected audio source visible to unit tests so regressions are caught.

**Non-Goals:**

- Do not add post-recording denoise/enhancement.
- Do not add cloud audio preprocessing.
- Do not replace `MediaRecorder` with an `AudioRecord` PCM pipeline.
- Do not add user-facing settings for audio source selection in this change.

## Decisions

- Use `MediaRecorder.AudioSource.VOICE_RECOGNITION` as the default recorder source.
  - Rationale: Verbally's use case is speech recognition, and this source is designed for that capture profile.
  - Alternative considered: `VOICE_COMMUNICATION`. Rejected for the default because it is tuned for VoIP and may apply call-oriented echo cancellation or gain behavior that can be more aggressive than dictation needs.
  - Alternative considered: explicit `NoiseSuppressor` through `AudioRecord`. Deferred because it would require a new low-level recording and encoding path, increasing implementation risk and potentially affecting recording reliability.

- Keep output encoding unchanged.
  - Rationale: Providers already receive `.m4a` audio successfully. Changing codec, sample rate, or bitrate is not required to request Android's speech capture path.

- Expose the recorder audio source as a small default constant.
  - Rationale: JVM unit tests cannot safely exercise real `MediaRecorder` capture, but they can verify that the production default selects the intended Android audio source.

## Risks / Trade-offs

- Device-specific behavior: Android/OEM preprocessing for `VOICE_RECOGNITION` can vary across phones. Mitigation: keep the change limited to the documented capture source and validate on the available Android device after building.
- Possible over-processing: Some devices may process speech differently than raw mic input. Mitigation: choose `VOICE_RECOGNITION` rather than the more call-oriented `VOICE_COMMUNICATION`, and keep provider/model flow unchanged.
- No guaranteed denoise: If a device does not apply noise suppression for this source, behavior may be similar to `MIC`. Mitigation: the change still selects the correct Android path without adding latency; deeper noise suppression can be evaluated separately if real-device results are insufficient.
