## Why

Google has released Gemini 3.5 Transcribe as a dedicated speech-to-text model in public preview. Adding it gives Verbally users a Gemini BYOK transcription option with automatic language detection and smart dictation formatting while preserving the existing default.

## What Changes

- Add `Gemini: gemini-3.5-transcribe` to the transcription model picker as an opt-in choice.
- Route the existing encrypted Gemini API key to transcription when the Gemini model is selected.
- Encode recorded audio as inline Base64 data, submit it in one `v1beta/interactions` request, and parse the returned transcript text.
- Test Gemini transcription key selection, provider routing, request construction, response parsing, and picker behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-transcription-cleanup`: Add Gemini 3.5 Transcribe as a supported BYOK transcription provider and define its inline-audio interaction behavior.
- `local-history-and-settings`: Expose a sixth transcription option and reuse the locally encrypted Gemini API key for the selected Gemini transcription model.

## Impact

- Affects transcription provider metadata, model options, settings UI, provider routing, application wiring, API-key testing, and transcription HTTP clients.
- Adds a call to the Google Gemini Interactions API using the user's Gemini API key in the `x-goog-api-key` header.
- Does not change the default transcription model, cleanup behavior, local-only history, or temporary-audio deletion lifecycle.
