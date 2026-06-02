## 1. Recording Quality Gate

- [x] 1.1 Add a WAV quality analyzer for duration, peak amplitude, RMS amplitude, active-frame ratio, and SNR-like signal strength.
- [x] 1.2 Suppress recordings that are too short, empty, silent, or clearly inactive before calling the transcription provider.
- [x] 1.3 Preserve provider transcription for recordings whose quality is unknown rather than silently dropping unsupported future formats.

## 2. Provider No-Content Signals

- [x] 2.1 Extend raw transcript contracts with confidence and hallucination/no-content metadata.
- [x] 2.2 Request and parse OpenAI transcription logprobs, deriving coarse confidence for guard decisions.
- [x] 2.3 Parse Soniox token confidence, deriving coarse confidence for guard decisions.
- [x] 2.4 Treat blank OpenAI, Groq, and Soniox transcripts as no dictated content instead of insertable text or fatal provider errors.
- [x] 2.5 Keep provider metadata nullable for clients that do not expose confidence or hallucination signals.

## 3. Shared Insertion Safety

- [x] 3.1 Guard provider-marked silent, no-content, or hallucinated transcripts before cleanup, insertion, and history persistence.
- [x] 3.2 Suppress low-confidence brief transcripts while preserving confident short dictation.
- [x] 3.3 Keep no-content outcomes quiet and return the overlay to ready state.

## 4. Verification

- [x] 4.1 Add unit tests for short, silent, and meaningful WAV quality analysis.
- [x] 4.2 Add coordinator tests for pre-STT no-op, hallucination metadata, low confidence suppression, and high confidence preservation.
- [x] 4.3 Add provider client tests for OpenAI logprob confidence, Soniox token confidence, and Soniox blank-transcript no-content handling.
- [x] 4.4 Validate with OpenSpec, unit tests, debug build, lint, and real-device short-recording input-field QA.
