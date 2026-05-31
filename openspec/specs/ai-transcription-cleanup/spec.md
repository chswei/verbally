# ai-transcription-cleanup Specification

## Purpose
TBD - created by archiving change add-android-floating-dictation. Update Purpose after archive.
## Requirements
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

### Requirement: Transcript cleanup supports OpenAI and Gemini
The system SHALL clean raw transcript text with either OpenAI or Gemini according to the selected cleanup provider, and SHALL include local dictionary vocabulary, active app category, and selected Formal/Casual style as cleanup context. Supported cleanup models SHALL include OpenAI `gpt-5.4-nano`, OpenAI `gpt-5.4-mini`, OpenAI `gpt-5.5`, Gemini `gemini-3.1-flash-lite`, and Gemini `gemini-3.1-pro-preview`.

#### Scenario: OpenAI cleanup selected
- **WHEN** cleanup provider is OpenAI
- **THEN** the system sends the raw transcript, local dictionary context, active app category, selected style, and basic text-processing prompt to the configured OpenAI text model
- **THEN** the system uses the returned cleaned text for insertion and history

#### Scenario: Gemini cleanup selected
- **WHEN** cleanup provider is Gemini
- **THEN** the system sends the raw transcript, local dictionary context, active app category, selected style, and basic text-processing prompt to Gemini `generateContent` with the user's Gemini API key in the `x-goog-api-key` header
- **THEN** the system uses the returned cleaned text for insertion and history

### Requirement: Cleanup preserves language and intent
The built-in default basic text-processing prompt SHALL preserve the user's original language and mixed-language style, remove filler words, prefer local dictionary terms when relevant, avoid translation, and then apply the selected Formal or Casual output format without rewriting, shortening, replacing words, translating, or changing the user's tone.

#### Scenario: Mixed Chinese and English transcript
- **WHEN** the raw transcript contains mixed Traditional Chinese and English
- **THEN** the cleaned output preserves both languages and does not translate the transcript into a single language

#### Scenario: Dictionary term is relevant to transcript
- **WHEN** local dictionary context contains a term or note relevant to the raw transcript
- **THEN** cleanup instructions tell the model to prefer the saved dictionary spelling or wording
- **THEN** cleanup instructions do not ask the model to invent content that is not supported by the transcript

#### Scenario: Formal style is selected
- **WHEN** the selected style is Formal
- **THEN** cleanup instructions ask only to add or normalize punctuation, capitalization, spacing, and language-required writing conventions
- **THEN** cleanup instructions forbid rewriting, shortening, synonym replacement, translation, and tone changes

#### Scenario: Casual style is selected
- **WHEN** the selected style is Casual
- **THEN** cleanup instructions ask only to use a lighter punctuation or spacing format appropriate to the transcript language
- **THEN** cleanup instructions forbid rewriting, shortening, synonym replacement, translation, and tone changes

### Requirement: Temporary audio is not retained
The system SHALL delete temporary audio files after transcription succeeds, fails, or is cancelled.

#### Scenario: Processing finishes
- **WHEN** dictation processing reaches success or failure
- **THEN** the system deletes the temporary recording file

### Requirement: Cleanup prompt can be customized
The system SHALL provide a user-editable basic text-processing prompt setting, SHALL default it to the built-in natural cleanup prompt in the selected interface language, and SHALL use the configured prompt plus local dictionary context and selected format-only style when cleaning transcript text with OpenAI or Gemini.

#### Scenario: Default basic text-processing prompt is used
- **WHEN** no custom basic text-processing prompt has been saved
- **THEN** the cleanup settings show the built-in natural cleanup prompt in the selected interface language
- **THEN** cleanup requests use the built-in natural cleanup prompt with dictionary context, selected format-only style, and the raw transcript attached

#### Scenario: Custom basic text-processing prompt is used
- **WHEN** the user saves a custom basic text-processing prompt
- **THEN** subsequent OpenAI cleanup requests send the custom prompt with dictionary context, selected format-only style, and the raw transcript attached
- **THEN** subsequent Gemini cleanup requests send the custom prompt with dictionary context, selected format-only style, and the raw transcript attached
- **THEN** interface language changes do not translate, replace, or otherwise alter the custom prompt

#### Scenario: User restores the default basic text-processing prompt
- **WHEN** the user chooses to restore the default basic text-processing prompt
- **THEN** the basic text-processing prompt setting returns to the built-in natural cleanup prompt in the selected interface language
- **THEN** provider, API key, model, dictionary entries, and style profiles remain unchanged
