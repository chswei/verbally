# local-history-and-settings Delta

## MODIFIED Requirements
### Requirement: Provider keys are stored locally and encrypted
The system SHALL store OpenAI and Gemini API keys locally using Android encrypted storage and SHALL NOT send keys to any service other than the selected provider. The Home settings UI SHALL separate transcription API setup from text-processing setup as inline blocks.

#### Scenario: User opens home
- **WHEN** the user opens the Home destination
- **THEN** the system shows voice transcription and text processing settings blocks

#### Scenario: User edits transcription settings
- **WHEN** the user views the voice transcription block
- **THEN** the system shows a transcription model dropdown followed by a generic API Key input inline

#### Scenario: User edits text processing settings
- **WHEN** the user views the text processing block
- **THEN** the system shows a text-processing model dropdown whose options include provider names
- **THEN** the system shows only the generic API Key input for the provider implied by the selected text-processing model

### Requirement: History can be searched and reused
The system SHALL allow the user to access history from bottom navigation, search, copy, re-paste, delete one entry, and clear all history entries after confirmation.

#### Scenario: User opens history
- **WHEN** the user selects History from bottom navigation
- **THEN** the system shows history search and available history actions
- **THEN** the system explains that only the latest 100 transcription results are saved

#### Scenario: User clears all history
- **WHEN** the user chooses to clear all history
- **THEN** the system asks for confirmation before deleting entries
- **AND** the system clears history only after the user confirms deletion

#### Scenario: User searches history
- **WHEN** the user enters text in history search
- **THEN** the system filters history by raw transcript and cleaned text

#### Scenario: User re-pastes a history entry
- **WHEN** the user chooses to re-paste a history entry while an editable field is focused
- **THEN** the system attempts the same paste insertion strategy used by new dictations
