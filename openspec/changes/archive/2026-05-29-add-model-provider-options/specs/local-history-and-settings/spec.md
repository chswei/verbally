## MODIFIED Requirements

### Requirement: Provider keys are stored locally and encrypted
The system SHALL store OpenAI, Gemini, Soniox, Groq, and Deepgram API keys locally using Android encrypted storage and SHALL NOT send keys to any service other than the selected provider. The Home settings UI SHALL separate transcription API setup from text-processing setup as inline blocks.

#### Scenario: User opens home
- **WHEN** the user opens the Home destination
- **THEN** the system shows voice transcription and text processing settings blocks

#### Scenario: User edits transcription settings
- **WHEN** the user views the voice transcription block
- **THEN** the system shows a transcription model dropdown whose five options include provider names
- **THEN** the system shows only the generic API Key input for the provider implied by the selected transcription model

#### Scenario: User edits text processing settings
- **WHEN** the user views the text processing block
- **THEN** the system shows a text-processing model dropdown whose five options include provider names
- **THEN** the system shows only the generic API Key input for the provider implied by the selected text-processing model

### Requirement: History entries preserve useful recovery data
The system SHALL store raw transcript, cleaned text, timestamp, transcription provider/model metadata, cleanup provider/model metadata, and app label when available for each history entry.

#### Scenario: Dictation processing succeeds
- **WHEN** cleaned text is produced
- **THEN** the system saves a history entry with raw transcript, cleaned text, timestamp, transcription provider/model metadata, cleanup provider/model metadata, and optional app label
