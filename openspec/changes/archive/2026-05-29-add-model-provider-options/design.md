## Context

Verbally records temporary `.m4a` files and starts transcription only after the user confirms the recording. The current implementation sends all recordings to OpenAI `audio/transcriptions`, then sends the raw transcript through the selected OpenAI or Gemini cleanup model with dictionary and style context.

The new model list mixes file-based and real-time-branded providers. OpenAI and Groq support direct file transcription through OpenAI-compatible audio transcription endpoints. Deepgram Nova-3 supports both pre-recorded and streaming audio, so this change can use pre-recorded `/listen` with `language=multi` while preserving the existing confirm-first UX. Soniox Realtime uses WebSocket streaming, but it can stream an already recorded file in chunks after confirmation. True live partial transcription remains a separate product change.

## Goals / Non-Goals

**Goals:**

- Provide exactly five curated transcription choices and five curated text-processing choices.
- Keep provider keys local-only and encrypted through the existing settings repository.
- Preserve the current record-confirm-cleanup-insert-history pipeline.
- Make provider routing explicit and testable.

**Non-Goals:**

- Add live partial transcript UI or change the overlay recording lifecycle.
- Add provider accounts, backend key proxying, cloud sync, or usage billing.
- Add every provider shown in competing products.
- Add medical, diarization, word timestamp, or custom vocabulary controls in this change.

## Decisions

1. **Represent transcription provider separately from model.**
   - Rationale: OpenAI, Soniox, Groq, and Deepgram require different API keys and request shapes. A model string alone cannot safely choose the correct endpoint.
   - Alternative considered: keep one `transcriptionModel` string and infer provider from label prefixes. Rejected because persistence and routing would become fragile.

2. **Keep the current confirm-first workflow.**
   - Rationale: It avoids a larger MediaRecorder/PCM streaming rewrite and keeps the feature focused on model choice. Soniox can still receive the recorded file over WebSocket after confirmation, and Deepgram can use pre-recorded Nova-3 with `language=multi`.
   - Alternative considered: stream microphone audio live to all real-time providers. Deferred because it requires a new recorder path, partial transcript state, and new overlay UI behavior.

3. **Store provider keys as separate settings fields.**
   - Rationale: The app is BYOK and must show only the key needed for the selected provider. Separate fields avoid overwriting one provider key with another.
   - Alternative considered: one generic transcription API key field. Rejected because switching providers would be error-prone and could silently send the wrong credential.

4. **Keep cleanup provider support to OpenAI and Gemini, but expand model IDs.**
   - Rationale: The existing cleanup clients already support arbitrary OpenAI/Gemini model names. Adding `gpt-5.5` and `gemini-3.1-pro-preview` is a low-risk option expansion.
   - Alternative considered: add Claude or Mistral cleanup in the same change. Rejected to keep scope to the user's selected five text-processing options.

## Risks / Trade-offs

- Provider APIs may differ in latency and accepted audio formats -> request factories and parsers stay provider-specific, with focused tests for each contract.
- Soniox WebSocket transcription after recording will not provide visible partial results -> UI copy and specs keep the existing confirm-first behavior explicit.
- Additional API key fields can make Home setup denser -> only the key field for the selected transcription or cleanup provider is shown.
- Stored settings from older installs may contain now-unknown models -> normalization falls back to the recommended defaults.

## Migration Plan

Existing installs keep their OpenAI and Gemini keys. Missing new provider keys default to blank. Unknown transcription or cleanup model values are normalized to supported defaults when settings are loaded into the UI. Rollback is safe because old settings keys remain intact and new provider key fields are ignored by older builds.
