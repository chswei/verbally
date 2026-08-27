## MODIFIED Requirements

### Requirement: Provider keys are stored locally and encrypted
The system SHALL store OpenAI, Gemini, Soniox, Groq, and Deepgram API keys locally using Android encrypted storage and SHALL NOT send keys to any service other than the selected provider. The Home settings UI SHALL separate transcription API setup from text-processing setup as inline blocks.

#### Scenario: User opens home
- **WHEN** the user opens the Home destination
- **THEN** the system shows voice transcription and text processing settings blocks

#### Scenario: User edits transcription settings
- **WHEN** the user views the voice transcription block
- **THEN** the system shows a transcription model dropdown whose six options include provider names
- **THEN** the system shows only the generic API Key input for the provider implied by the selected transcription model

#### Scenario: User selects Gemini transcription
- **WHEN** the user selects `Gemini: gemini-3.5-transcribe`
- **THEN** the transcription API Key input reads and updates the locally encrypted Gemini API key
- **AND** the selected transcription provider sends that key only to the Gemini API

#### Scenario: User edits text processing settings
- **WHEN** the user views the text processing block
- **THEN** the system shows a text-processing model dropdown whose options include provider names
- **THEN** the system shows only the generic API Key input for the provider implied by the selected text-processing model
