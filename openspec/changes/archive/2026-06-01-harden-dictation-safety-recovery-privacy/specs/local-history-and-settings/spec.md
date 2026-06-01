## ADDED Requirements

### Requirement: Local history retention is user configurable
The system SHALL let the user choose how local dictation history is retained on device.

#### Scenario: Latest entries retention is selected
- **WHEN** the user selects the latest-entries retention mode
- **THEN** the system saves future successful dictations locally
- **AND** the system retains at most the latest 100 entries

#### Scenario: Daily auto-delete retention is selected
- **WHEN** the user selects the auto-delete retention mode
- **THEN** the system saves future successful dictations locally
- **AND** the system removes history entries older than 24 hours whenever history is loaded or a new entry is saved
- **AND** the system still retains at most the latest 100 entries

#### Scenario: No-history retention is selected
- **WHEN** the user selects the no-history retention mode and confirms the destructive change
- **THEN** the system clears existing local dictation history
- **AND** the system does not save future successful dictations to local history
- **AND** the History screen explains that history storage is disabled

#### Scenario: User cancels destructive retention change
- **WHEN** the user selects a retention mode that would delete existing local history
- **AND** the user cancels the confirmation dialog
- **THEN** the existing retention mode remains active
- **AND** existing local history remains unchanged

#### Scenario: Dictionary and snippets are unaffected by history retention
- **WHEN** the user changes local history retention mode
- **THEN** dictionary entries, snippet entries, API keys, theme, language, and style settings remain unchanged
