# local-history-and-settings Specification

## Purpose
TBD - created by archiving change add-android-floating-dictation. Update Purpose after archive.
## Requirements
### Requirement: Provider keys are stored locally and encrypted
The system SHALL store OpenAI and Gemini API keys locally using Android encrypted storage and SHALL NOT send keys to any service other than the selected provider. The settings UI SHALL separate transcription API setup from second-pass cleanup provider setup.

#### Scenario: User saves provider key
- **WHEN** the user saves an OpenAI or Gemini API key in settings
- **THEN** the system stores the key in encrypted local preferences

#### Scenario: User opens transcription settings
- **WHEN** the user selects Transcribe settings
- **THEN** the system shows OpenAI transcription API key and transcription model fields
- **THEN** the system does not show cleanup-only Gemini fields on that page

#### Scenario: User opens cleanup settings
- **WHEN** the user selects second-pass cleanup settings
- **THEN** the system shows cleanup provider selection, cleanup model fields, and the cleanup provider key fields

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
The system SHALL present onboarding, settings, status, error, and history UI text in Traditional Chinese for v1, and SHALL keep Android permission instructions concise and aligned with actual platform behavior.

#### Scenario: Missing key error
- **WHEN** dictation cannot continue because a provider key is missing
- **THEN** the system shows the error in Traditional Chinese

#### Scenario: Permission onboarding explains the current step
- **WHEN** a required permission is missing
- **THEN** the setup screen describes only the current missing permission in Traditional Chinese
- **THEN** the action label matches whether the app is requesting a runtime permission or opening Android settings
