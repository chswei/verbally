## Context

Verbally sends completed `.m4a` recordings to `/v1/audio/transcriptions` through a shared `OpenAiTranscriptionClient`. Its request factory currently adds `response_format=json`, `include[]=logprobs`, and `chunking_strategy=auto` to every OpenAI transcription request. OpenAI documents `gpt-transcribe` as the recommended model for completed recordings, but token log probabilities remain limited to the GPT-4o transcription models.

The existing response parser already reads the common `text` field and treats log probabilities as optional, so response handling does not need a new model-specific path.

## Goals / Non-Goals

**Goals:**

- Expose `gpt-transcribe` as an OpenAI transcription option.
- Send a minimal documented file-transcription request for `gpt-transcribe`.
- Preserve confidence signals and existing request behavior for `gpt-4o-transcribe` and `gpt-4o-mini-transcribe`.
- Keep existing saved settings and default model unchanged.

**Non-Goals:**

- Make `gpt-transcribe` the default automatically.
- Add Realtime streaming transcription.
- Add UI for `prompt`, `keywords`, or `languages`.
- Persist the languages detected in the new model response.

## Decisions

1. **Use the existing OpenAI provider and endpoint.** The selected model continues through `OpenAiTranscriptionClient` and `/v1/audio/transcriptions`; a new provider abstraction would duplicate authentication, transport, and parsing without adding value.

2. **Make optional multipart fields model-aware.** For `gpt-transcribe`, the request contains only the documented required `model` and `file` fields and relies on the default JSON response. Existing GPT-4o transcription models retain `response_format=json`, `include[]=logprobs`, and `chunking_strategy=auto`. This avoids sending an unsupported confidence field to the new model while preserving current behavior.

3. **Keep response parsing shared.** `gpt-transcribe` returns `text` plus detected languages. The parser already accepts `text` and safely handles absent `logprobs`, so the extra response field can be ignored until the product needs language metadata.

4. **Keep the low-cost default.** Existing users and new installs continue to default to `gpt-4o-mini-transcribe`; the new model is opt-in from the picker.

## Risks / Trade-offs

- [OpenAI changes optional-parameter support] → Lock behavior to the currently documented request shape and cover multipart fields with unit tests.
- [No confidence log probabilities for `gpt-transcribe`] → Preserve the existing no-confidence path; short empty or silent recordings remain protected by the recorder and transcript heuristics.
- [Detected languages are discarded] → Accept this intentionally because the current history schema has no language field and the user only requested model selection.

## Migration Plan

No stored-setting migration is required. Add the option and model-specific request branch, validate, and install the debug APK. Rollback consists of removing the option and branch; existing stored selections fall back to the first OpenAI option through the current stale-model handling.

## Open Questions

None.
