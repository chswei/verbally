## Why

Verbally currently exposes too few model choices for a power-user dictation app: transcription is limited to OpenAI models, and text processing is limited to two OpenAI cleanup models plus one Gemini model. The next version should give users a concise, high-quality set of transcription and text-processing options without turning setup into a provider catalog.

## What Changes

- Expand transcription setup to five curated options:
  - OpenAI `gpt-4o-mini-transcribe`
  - OpenAI `gpt-4o-transcribe`
  - Soniox Realtime (`stt-rt-v4`)
  - Groq `whisper-large-v3-turbo`
  - Deepgram Nova-3 multilingual
- Store separate transcription provider keys for Soniox, Groq, and Deepgram while continuing to reuse the OpenAI key for OpenAI transcription and OpenAI text processing.
- Route confirmed recordings to the selected transcription provider and save the selected provider/model metadata in history.
- Expand text-processing setup to five curated options:
  - OpenAI `gpt-5.4-nano`
  - OpenAI `gpt-5.4-mini`
  - OpenAI `gpt-5.5`
  - Gemini `gemini-3.1-flash-lite`
  - Gemini `gemini-3.1-pro-preview`
- Preserve the current post-recording workflow: users still confirm a recording before transcription begins; no partial transcript UI is added.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `ai-transcription-cleanup`: Transcription routing expands from OpenAI-only to curated OpenAI, Soniox, Groq, and Deepgram options; cleanup expands to the selected five OpenAI/Gemini text models.
- `local-history-and-settings`: Settings must store provider-specific transcription API keys locally and show the API key field for the provider implied by the selected model.

## Impact

- Updates settings models, encrypted settings persistence, and Home setup UI.
- Updates dictation orchestration to route transcription through provider-specific clients before existing cleanup, snippets, insertion, and history behavior.
- Adds provider request construction and focused unit tests for model option normalization, provider routing, request construction, and settings persistence.
- No backend, account system, cloud proxy, or real-time partial transcription UI is introduced.
