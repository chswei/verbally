## ADDED Requirements

### Requirement: Provider keys are stored locally and encrypted
The system SHALL store OpenAI and Gemini API keys locally using Android encrypted storage and SHALL NOT send keys to any service other than the selected provider.

#### Scenario: User saves provider key
- **WHEN** the user saves an OpenAI or Gemini API key in settings
- **THEN** the system stores the key in encrypted local preferences

### Requirement: Dictation history is local and capped
The system SHALL store dictation history locally and retain at most the latest 100 entries.

#### Scenario: New history entry exceeds cap
- **WHEN** a new dictation entry is saved and more than 100 entries exist
- **THEN** the system deletes the oldest entries until only 100 remain

### Requirement: History entries preserve useful recovery data
The system SHALL store raw transcript, cleaned text, timestamp, provider metadata, model metadata, and app label when available for each history entry.

#### Scenario: Dictation processing succeeds
- **WHEN** cleaned text is produced
- **THEN** the system saves a history entry with raw transcript, cleaned text, timestamp, provider/model metadata, and optional app label

### Requirement: History can be searched and reused
The system SHALL allow the user to search, copy, re-paste, delete one entry, and clear all history entries.

#### Scenario: User searches history
- **WHEN** the user enters text in history search
- **THEN** the system filters history by raw transcript and cleaned text

#### Scenario: User re-pastes a history entry
- **WHEN** the user chooses to re-paste a history entry while an editable field is focused
- **THEN** the system attempts the same paste insertion strategy used by new dictations

### Requirement: UI text is Traditional Chinese
The system SHALL present onboarding, settings, status, error, and history UI text in Traditional Chinese for v1.

#### Scenario: Missing key error
- **WHEN** dictation cannot continue because a provider key is missing
- **THEN** the system shows the error in Traditional Chinese
