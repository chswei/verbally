## MODIFIED Requirements

### Requirement: Audio is transcribed with OpenAI BYOK
The system SHALL transcribe confirmed recordings by sending temporary audio captured with Android's speech-recognition audio source directly to the selected BYOK transcription provider. Supported transcription options SHALL include OpenAI `gpt-4o-mini-transcribe`, OpenAI `gpt-4o-transcribe`, Soniox Realtime `stt-rt-v4`, Groq `whisper-large-v3-turbo`, and Deepgram Nova-3 multilingual.

#### Scenario: OpenAI transcription succeeds
- **WHEN** the user confirms a recording and a valid OpenAI key is configured
- **THEN** the system calls OpenAI transcription with the configured OpenAI transcription model
- **THEN** the system receives raw transcript text for cleanup

#### Scenario: Soniox transcription succeeds
- **WHEN** the user confirms a recording and Soniox Realtime is selected with a valid Soniox key
- **THEN** the system sends the confirmed recording to Soniox Realtime `stt-rt-v4`
- **THEN** the system receives raw transcript text for cleanup

#### Scenario: Groq transcription succeeds
- **WHEN** the user confirms a recording and Groq `whisper-large-v3-turbo` is selected with a valid Groq key
- **THEN** the system sends the confirmed recording to Groq audio transcriptions
- **THEN** the system receives raw transcript text for cleanup

#### Scenario: Deepgram transcription succeeds
- **WHEN** the user confirms a recording and Deepgram Nova-3 multilingual is selected with a valid Deepgram key
- **THEN** the system sends the confirmed recording to Deepgram Nova-3 with multilingual transcription enabled
- **THEN** the system receives raw transcript text for cleanup

#### Scenario: Selected transcription key is missing
- **WHEN** the user confirms a recording without the API key required by the selected transcription provider
- **THEN** the system stops processing and shows a Traditional Chinese settings error naming the missing provider key
