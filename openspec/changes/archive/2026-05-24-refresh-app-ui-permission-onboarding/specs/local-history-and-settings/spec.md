## MODIFIED Requirements

### Requirement: UI text is Traditional Chinese
The system SHALL present onboarding, settings, status, error, and history UI text in Traditional Chinese for v1, and SHALL keep Android permission instructions concise and aligned with actual platform behavior.

#### Scenario: Permission onboarding explains the current step
- **WHEN** a required permission is missing
- **THEN** the setup screen describes only the current missing permission in Traditional Chinese
- **THEN** the action label matches whether the app is requesting a runtime permission or opening Android settings

### Requirement: Provider keys are stored locally and encrypted
The system SHALL store OpenAI and Gemini API keys locally using Android encrypted storage and SHALL NOT send keys to any service other than the selected provider. The settings UI SHALL separate transcription API setup from second-pass cleanup provider setup.

#### Scenario: User opens transcription settings
- **WHEN** the user selects Transcribe settings
- **THEN** the system shows OpenAI transcription API key and transcription model fields
- **THEN** the system does not show cleanup-only Gemini fields on that page

#### Scenario: User opens cleanup settings
- **WHEN** the user selects second-pass cleanup settings
- **THEN** the system shows cleanup provider selection, cleanup model fields, and the cleanup provider key fields
