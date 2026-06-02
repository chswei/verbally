## Why

Very short, silent, or no-speech recordings can still produce transcription-provider hallucinations that look like dictated text. Because Verbally inserts confirmed dictation directly into the active input field, these hallucinations must be suppressed before cleanup, insertion, and history persistence.

## What Changes

- Add Flow-like no-content gates before transcription for recordings that are too short or clearly silent.
- Carry transcription confidence and hallucination/no-content metadata through provider contracts when providers expose it.
- Treat provider empty, silent, hallucinated, or low-confidence brief transcripts as no dictated content.
- Keep unsafe no-content outcomes quiet: no cleanup call, no insertion, and no history entry.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-transcription-cleanup`: Transcription must suppress short/silent recordings and provider-marked no-content or hallucinated transcripts before cleanup and insertion.

## Impact

- Affects dictation orchestration in `DictationCoordinator`.
- Adds recording-quality analysis for WAV recordings.
- Extends transcription provider contracts, OpenAI logprob parsing, and Soniox token confidence parsing.
- Adjusts OpenAI, Groq, and Soniox no-content handling.
- Adds focused unit tests for recording quality, provider metadata, and coordinator no-op behavior.
