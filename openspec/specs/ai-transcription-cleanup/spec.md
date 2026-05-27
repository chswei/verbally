# ai-transcription-cleanup Specification

## Purpose
TBD - created by archiving change add-android-floating-dictation. Update Purpose after archive.
## Requirements
### Requirement: Audio is transcribed with OpenAI BYOK
The system SHALL transcribe confirmed recordings by sending temporary audio directly to OpenAI `audio/transcriptions` using the user's stored OpenAI API key.

#### Scenario: OpenAI transcription succeeds
- **WHEN** the user confirms a recording and a valid OpenAI key is configured
- **THEN** the system calls OpenAI transcription with the configured transcription model
- **THEN** the system receives raw transcript text for cleanup

#### Scenario: OpenAI key is missing
- **WHEN** the user confirms a recording without an OpenAI API key configured
- **THEN** the system stops processing and shows a Traditional Chinese settings error

### Requirement: Transcript cleanup supports OpenAI and Gemini
The system SHALL clean raw transcript text with either OpenAI or Gemini according to the selected cleanup provider.

#### Scenario: OpenAI cleanup selected
- **WHEN** cleanup provider is OpenAI
- **THEN** the system sends the raw transcript to the configured OpenAI text model
- **THEN** the system uses the returned cleaned text for insertion and history

#### Scenario: Gemini cleanup selected
- **WHEN** cleanup provider is Gemini
- **THEN** the system sends the raw transcript to Gemini `generateContent` with the user's Gemini API key in the `x-goog-api-key` header
- **THEN** the system uses the returned cleaned text for insertion and history

### Requirement: Cleanup preserves language and intent
The built-in default cleanup prompt SHALL preserve the user's original language and mixed-language style, improve punctuation and readability, remove filler words, and avoid translation.

#### Scenario: Mixed Chinese and English transcript
- **WHEN** the raw transcript contains mixed Traditional Chinese and English
- **THEN** the cleaned output preserves both languages and does not translate the transcript into a single language

### Requirement: Temporary audio is not retained
The system SHALL delete temporary audio files after transcription succeeds, fails, or is cancelled.

#### Scenario: Processing finishes
- **WHEN** dictation processing reaches success or failure
- **THEN** the system deletes the temporary recording file

### Requirement: Cleanup prompt can be customized
The system SHALL provide a user-editable cleanup prompt setting, SHALL default it to the built-in natural cleanup prompt, and SHALL use the configured prompt when cleaning transcript text with OpenAI or Gemini.

#### Scenario: Default cleanup prompt is used
- **WHEN** no custom cleanup prompt has been saved
- **THEN** the cleanup settings show the built-in natural cleanup prompt
- **THEN** cleanup requests use the built-in natural cleanup prompt with the raw transcript attached

#### Scenario: Custom cleanup prompt is used
- **WHEN** the user saves a custom cleanup prompt
- **THEN** subsequent OpenAI cleanup requests send the custom prompt with the raw transcript attached
- **THEN** subsequent Gemini cleanup requests send the custom prompt with the raw transcript attached

#### Scenario: User restores the default cleanup prompt
- **WHEN** the user chooses to restore the default cleanup prompt
- **THEN** the cleanup prompt setting returns to the built-in natural cleanup prompt
- **THEN** provider, API key, and model settings remain unchanged

