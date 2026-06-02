## Context

Verbally already suppresses explicitly empty dictation outcomes, but speech-to-text providers can still return short hallucinated phrases when the user recorded silence, noise, or a sub-second clip. Reverse engineering and black-box testing of Wispr Flow on Android showed a layered shape: avoid sending obviously unusable audio, honor provider-side no-content/hallucination signals, and only paste after a trustworthy non-empty final transcript exists.

## Goals / Non-Goals

**Goals:**

- Suppress too-short and clearly silent recordings before provider transcription.
- Represent provider confidence and hallucination/no-content metadata in the transcription contract.
- Use OpenAI token logprobs as a confidence proxy when available.
- Use Soniox token confidence as a confidence proxy when available.
- Treat empty provider transcripts as no-content outcomes instead of provider failures.
- Preserve normal insertion for meaningful speech, including short but confident dictation.

**Non-Goals:**

- Add real-time streaming transcription.
- Add a separate AI judge or extra cleanup-provider call.
- Add visible warnings or prompts for no-content suppression.
- Block longer meaningful speech solely because it is brief.

## Decisions

1. Add a local WAV quality analyzer before transcription.
   - Rationale: The current recorder emits PCM16 WAV audio, so duration, peak amplitude, RMS, and active-frame ratio can be inspected locally without provider latency or cost.
   - Trade-off: Non-WAV or unreadable recordings are treated as unknown instead of blocked, preserving compatibility if a future recorder changes format.

2. Extend `RawTranscript` with confidence and hallucination metadata.
   - Rationale: Flow-like behavior depends on carrying provider no-content signals to the coordinator instead of losing them as plain text.
   - Trade-off: Existing providers that do not expose metadata keep nullable fields and rely on text/audio guards.

3. Request OpenAI transcription logprobs and derive coarse confidence.
   - Rationale: OpenAI does not return the same public hallucination enum observed in Flow, but token logprobs provide a useful confidence signal for brief suspicious transcripts.
   - Trade-off: Confidence thresholds are intentionally conservative and only suppress low-confidence brief/no-speech-like results.

4. Parse Soniox token confidence into the shared transcript contract.
   - Rationale: Soniox returns confidence per recognized token by default, so dropping that metadata would leave a provider-specific gap in the no-content guard.
   - Trade-off: The app uses a coarse average token-confidence bucket for now rather than preserving per-token detail.

5. Keep final suppression in `DictationContentGuard`.
   - Rationale: The coordinator is the shared safety point before cleanup, insertion, and history. Centralizing the guard prevents provider-specific gaps.

## Risks / Trade-offs

- Silence detection may vary by device microphone noise floor. Mitigation: use conservative thresholds and require multiple low-energy indicators.
- Provider confidence thresholds may need tuning. Mitigation: only low-confidence brief outputs are suppressed, while higher-confidence short dictation remains insertable.
- Providers may add richer metadata later. Mitigation: the contract already has explicit confidence and hallucination fields that can be mapped by future clients.
