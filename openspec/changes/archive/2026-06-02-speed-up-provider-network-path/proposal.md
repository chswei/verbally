## Why

Verbally's confirm-first dictation flow now supports multiple network providers. The current provider path leaves avoidable latency in two places: each provider client owns its own default `OkHttpClient`, and successful Soniox async transcription waits for remote artifact deletion before text cleanup can begin.

## What Changes

- Share a default provider `OkHttpClient` so transcription, cleanup, Soniox, Groq, Gemini, and provider-key checks can reuse DNS, TLS, and connection pooling where possible.
- Make Soniox polling start with a shorter default interval for short dictations while still preserving the existing bounded timeout.
- Let successful Soniox transcripts return as soon as transcript text is retrieved, then delete Soniox remote artifacts best-effort in the background.
- Keep failure-path Soniox cleanup synchronous so uploaded files are still deleted before an error is surfaced when processing fails or times out.

## Capabilities

### Modified Capabilities

- `ai-transcription-cleanup`: Provider network clients share transport resources, and Soniox async transcription no longer blocks downstream cleanup on successful remote artifact deletion.

## Impact

- Updates provider client defaults and Soniox async transcription control flow.
- Adds focused unit tests around default connection reuse and Soniox success-path latency.
- Does not add real-time transcription, a backend, account sync, or a new recorder flow.
