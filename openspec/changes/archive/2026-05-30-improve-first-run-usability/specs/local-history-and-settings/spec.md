## ADDED Requirements

### Requirement: Home provider setup supports API key testing
The system SHALL let users test the currently selected transcription provider key and text-processing provider key from the Home API setup screen without adding permission readiness status to Home.

#### Scenario: User tests transcription API key
- **WHEN** the user taps the transcription API key test action
- **THEN** the system tests the currently selected transcription provider and API key
- **THEN** the result is shown near the transcription setup controls

#### Scenario: User tests text-processing API key
- **WHEN** the user taps the text-processing API key test action
- **THEN** the system tests the currently selected text-processing provider and API key
- **THEN** the result is shown near the text-processing setup controls

#### Scenario: User opens Home after completing permissions
- **WHEN** the user opens the Home destination
- **THEN** the system does not show permission completion status or a permission checklist in the Home content

### Requirement: History entry deletion requires confirmation
The system SHALL ask for confirmation before deleting one history entry.

#### Scenario: User deletes a history entry
- **WHEN** the user chooses to delete one history entry
- **THEN** the system asks for confirmation before deleting it
- **AND** deletes it only after the user confirms deletion
