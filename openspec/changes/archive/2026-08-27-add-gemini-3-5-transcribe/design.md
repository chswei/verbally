## Context

Verbally records a temporary 16 kHz mono PCM `.wav` file, selects a transcription provider through `TranscriptionClientRouter`, and then performs a separate cleanup step. Gemini is already available for cleanup and its API key is stored in encrypted settings, but Gemini is not a transcription provider. The Gemini Interactions API accepts small, transient audio as inline Base64 data in the request body.

The working tree also contains unarchived changes that add OpenAI `gpt-transcribe`; this change must preserve those additions and the existing OpenAI default.

## Goals / Non-Goals

**Goals:**

- Add Gemini 3.5 Transcribe as an opt-in prerecorded-audio transcription option.
- Reuse the existing encrypted Gemini API key and shared HTTP transport.
- Send each temporary recording directly in one Interactions API request without creating a remote Files API resource.
- Preserve raw-transcript then cleanup orchestration and existing provider behavior.
- Cover inline request construction, parsing, routing, key testing, and settings with focused tests.

**Non-Goals:**

- Replacing the default transcription model.
- Adding `gemini-3.5-transcribe-live` or changing the recorder to real-time PCM streaming.
- Enabling speaker diarization, word timestamps, or custom vocabulary in this change.
- Replacing Verbally's existing downstream cleanup stage with Gemini smart transcription.

## Decisions

### Use inline audio in one Interactions API request

The client will read the temporary `.wav`, Base64-encode it, and post an interaction containing `type: audio`, `data`, and its MIME type. Verbally's five-minute maximum recording is about 9.6 MB before Base64 encoding and about 12.8 MB afterward, so it remains below Gemini's 20 MB total inline-request limit. The Files API was rejected for this bounded input because its start, upload, and delete requests add sequential network latency. Direct `generateContent` and Live API streaming remain out of scope.

### Keep Gemini as a distinct transcription provider

`TranscriptionProvider.GEMINI` will route to a dedicated client while reading `AppSettings.geminiApiKey`. Treating it as an OpenAI-compatible model was rejected because its authentication, endpoint, JSON request body, and response schema are different.

### Preserve the two-stage dictation pipeline

The interaction will use Gemini's default verbatim transcription mode. Verbally will continue to store the raw transcript and run the selected cleanup provider afterward. Enabling smart mode here would combine transcription and cleanup, alter the meaning of raw history, and potentially apply formatting twice.

### Parse REST interaction steps directly

The client will collect text content from completed `model_output` steps rather than expecting an SDK-only `output_text` convenience field. Blank text maps to the existing no-content transcript contract. Malformed successful responses produce the existing localized parse failure.

### Avoid a remote file lifecycle

The client will not call the Files API, so it does not create a separately addressable remote file that needs a follow-up delete request. The existing local temporary-audio lifecycle remains unchanged and deletes the recording after success, failure, or cancellation.

### Test API keys through Gemini model metadata

The existing key tester will request `/v1beta/models/gemini-3.5-transcribe` with `x-goog-api-key`, matching the cleanup provider's low-cost model-access check and avoiding an audio upload during a key test.

## Risks / Trade-offs

- [Public preview API changes] → Isolate interaction construction and parsing in a dedicated client with request-level tests.
- [Base64 expands payload size and temporarily uses additional heap] → Keep the existing five-minute WAV cap; revisit the transport if recording size or format changes.
- [Unary model inference can still add latency] → Reuse the shared `OkHttpClient`; live streaming remains out of scope.
- [Existing Gemini key may lack model access] → Keep the in-app provider key test model-specific and surface the existing provider failure messages.
- [Concurrent unarchived OpenSpec changes overlap the model list] → Preserve the existing picker additions and express the complete six-option behavior in this delta.

## Migration Plan

No stored-setting migration is required. Existing users keep their selected provider and model; Gemini 3.5 Transcribe appears only as a new opt-in option. Rollback consists of removing the option, provider route, and dedicated client without altering encrypted key storage.

## Open Questions

None for this scope. Smart transcription, dictionary-based speech biasing, and Live API streaming can be considered as separate changes.
