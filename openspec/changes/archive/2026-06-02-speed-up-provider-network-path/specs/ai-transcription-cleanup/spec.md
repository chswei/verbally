## MODIFIED Requirements

### Requirement: Audio is transcribed with OpenAI BYOK
The system SHALL transcribe confirmed recordings by sending temporary audio captured with Android's speech-recognition audio source directly to the selected BYOK transcription provider. Supported transcription options SHALL include OpenAI `gpt-4o-mini-transcribe`, OpenAI `gpt-4o-transcribe`, Soniox `stt-async-v4`, and Groq `whisper-large-v3-turbo`. Provider clients SHOULD share transport resources so repeated provider calls can reuse DNS, TLS, and HTTP connection pooling when the platform and endpoint allow it.

#### Scenario: OpenAI transcription succeeds
- **WHEN** the user confirms a recording and a valid OpenAI key is configured
- **THEN** the system calls OpenAI transcription with the configured OpenAI transcription model
- **THEN** the system receives raw transcript text for cleanup

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

### Requirement: Transcript cleanup supports OpenAI and Gemini
The system SHALL clean raw transcript text with either OpenAI or Gemini according to the selected cleanup provider, and SHALL include local dictionary vocabulary, active app category, and selected Formal/Casual style as cleanup context. Supported cleanup models SHALL include OpenAI `gpt-5.4-nano`, OpenAI `gpt-5.4-mini`, OpenAI `gpt-5.5`, Gemini `gemini-3.1-flash-lite`, and Gemini `gemini-3.1-pro-preview`. Cleanup clients SHOULD share transport resources with other provider clients when possible.

#### Scenario: OpenAI cleanup selected
- **WHEN** cleanup provider is OpenAI
- **THEN** the system sends the raw transcript, local dictionary context, active app category, selected style, and basic text-processing prompt to the configured OpenAI text model
- **THEN** the system uses the returned cleaned text for insertion and history

#### Scenario: Gemini cleanup selected
- **WHEN** cleanup provider is Gemini
- **THEN** the system sends the raw transcript, local dictionary context, active app category, selected style, and basic text-processing prompt to Gemini `generateContent` with the user's Gemini API key in the `x-goog-api-key` header
- **THEN** the system uses the returned cleaned text for insertion and history
