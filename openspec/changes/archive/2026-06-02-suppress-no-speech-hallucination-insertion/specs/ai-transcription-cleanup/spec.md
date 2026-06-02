## MODIFIED Requirements

### Requirement: Audio is transcribed with OpenAI BYOK
The system SHALL transcribe confirmed recordings by sending temporary audio captured by the dictation recorder directly to the selected BYOK transcription provider. Supported transcription options SHALL include OpenAI `gpt-4o-mini-transcribe`, OpenAI `gpt-4o-transcribe`, Soniox `stt-async-v4`, and Groq `whisper-large-v3-turbo`. Provider clients SHOULD share transport resources so repeated provider calls can reuse DNS, TLS, and HTTP connection pooling when the platform and endpoint allow it.

#### Scenario: OpenAI transcription succeeds
- **WHEN** the user confirms a recording and a valid OpenAI key is configured
- **THEN** the system calls OpenAI transcription with the configured OpenAI transcription model
- **AND** the request includes token confidence signals when the selected OpenAI transcription model supports them
- **THEN** the system receives raw transcript text for cleanup

#### Scenario: Low-confidence brief transcript is suppressed
- **WHEN** a confirmed recording produces a brief raw transcript with low transcription confidence
- **THEN** the system treats the result as no dictated content
- **AND** the system does not clean, insert, or save that transcript

#### Scenario: Short or silent recording is suppressed before transcription
- **WHEN** a confirmed recording is too short to contain useful dictation or is clearly silent
- **THEN** the system treats the result as no dictated content before calling the transcription provider
- **AND** the system does not clean, insert, or save that recording

#### Scenario: Provider marks transcript as no-content hallucination
- **WHEN** a transcription provider returns an empty transcript or marks a transcript as no-content, silent, or hallucinated
- **THEN** the system treats the result as no dictated content
- **AND** the system does not clean, insert, or save that transcript

#### Scenario: Soniox transcription succeeds
- **WHEN** the user confirms a recording and Soniox is selected with a valid Soniox key
- **THEN** the system uploads the confirmed recording to Soniox async transcription
- **THEN** the system polls Soniox with a short initial interval suitable for short dictations
- **THEN** the system receives raw transcript text for cleanup
- **AND** successful remote artifact deletion is best-effort and SHALL NOT block text cleanup or insertion after transcript text is retrieved

#### Scenario: Soniox transcription fails before transcript retrieval
- **WHEN** Soniox async transcription fails or times out before transcript text is retrieved
- **THEN** the system attempts to delete Soniox remote artifacts before surfacing the failure

#### Scenario: Groq transcription succeeds
- **WHEN** the user confirms a recording and Groq `whisper-large-v3-turbo` is selected with a valid Groq key
- **THEN** the system sends the confirmed recording to Groq audio transcriptions
- **THEN** the system receives raw transcript text for cleanup

#### Scenario: Selected transcription key is missing
- **WHEN** the user confirms a recording without the API key required by the selected transcription provider
- **THEN** the system stops processing and shows a Traditional Chinese settings error naming the missing provider key
