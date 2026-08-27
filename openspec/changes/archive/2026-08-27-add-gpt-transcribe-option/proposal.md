## Why

OpenAI now recommends `gpt-transcribe` for high-accuracy transcription of completed recordings, but Verbally does not expose it in the transcription model picker. The new model also rejects the legacy `include[]=logprobs` parameter that Verbally currently sends with every OpenAI transcription request, so selection and request construction must be updated together.

## What Changes

- Add `OpenAI: gpt-transcribe` to the curated transcription model options.
- Route the selected model through the existing OpenAI BYOK transcription client.
- Build `gpt-transcribe` requests without legacy-only confidence and chunking parameters while preserving those parameters for the existing GPT-4o transcription models.
- Cover option selection and model-specific multipart request construction with unit tests.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-transcription-cleanup`: Add OpenAI `gpt-transcribe` as a supported transcription option and require model-compatible OpenAI request parameters.

## Impact

- Settings model registry and picker behavior.
- OpenAI multipart transcription request construction.
- Unit tests and OpenSpec coverage for supported transcription models.
- No new dependency, permission, backend, or stored credential is introduced.
