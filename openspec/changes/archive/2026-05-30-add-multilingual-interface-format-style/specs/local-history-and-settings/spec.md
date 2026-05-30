## MODIFIED Requirements

### Requirement: UI text is Traditional Chinese
The system SHALL present onboarding, settings, status, error, and history UI text in the selected interface language when a supported localization is available, SHALL default to Traditional Chinese resources, and SHALL keep Android permission instructions concise and aligned with actual platform behavior.

#### Scenario: Missing key error
- **WHEN** dictation cannot continue because a provider key is missing
- **THEN** the system shows the error in the selected interface language when localized copy is available

#### Scenario: Permission onboarding explains the current step
- **WHEN** a required permission is missing
- **THEN** the setup screen describes only the current missing permission in the selected interface language when localized copy is available
- **THEN** the action label matches whether the app is requesting a runtime permission or opening Android settings

### Requirement: Home and history use polished Traditional Chinese presentation
The system SHALL present Home API setup and History as polished localized utility screens while preserving existing settings and history behavior.

#### Scenario: User opens Home
- **WHEN** the user opens the Home destination
- **THEN** the system shows a localized page header
- **THEN** the system shows transcription and text-processing setup as distinct operation panels
- **THEN** primary save actions remain reachable after long API key input

#### Scenario: User opens History
- **WHEN** the user selects History from bottom navigation
- **THEN** the system shows a localized page header
- **THEN** the system shows a localized search field
- **THEN** the system explains that only the latest 100 transcription results are saved
- **THEN** history entries remain copyable and deletable
