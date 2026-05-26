## ADDED Requirements

### Requirement: Home and history use polished Traditional Chinese presentation
The system SHALL present Home API setup and History as polished Traditional Chinese utility screens while preserving existing settings and history behavior.

#### Scenario: User opens Home
- **WHEN** the user opens the Home destination
- **THEN** the system shows a Traditional Chinese page header
- **THEN** the system shows transcription and text-processing setup as distinct operation panels
- **THEN** primary save actions remain reachable after long API key input

#### Scenario: User opens History
- **WHEN** the user selects History from bottom navigation
- **THEN** the system shows a Traditional Chinese page header
- **THEN** the system shows a Traditional Chinese search field
- **THEN** the system explains that only the latest 100 transcription results are saved
- **THEN** history entries remain copyable and deletable
