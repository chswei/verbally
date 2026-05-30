## ADDED Requirements

### Requirement: Dictation processing feedback is visible only when attention is needed
The system SHALL keep successful direct dictation insertion silent while showing visible user feedback when dictation falls back to clipboard insertion or fails due to provider or processing errors.

#### Scenario: Dictation inserts text directly
- **WHEN** dictation processing finishes and direct insertion succeeds
- **THEN** the system does not show a visible success message
- **AND** the floating overlay returns to the ready state

#### Scenario: Dictation falls back to clipboard
- **WHEN** dictation processing finishes but direct insertion cannot be verified
- **THEN** the system copies the cleaned text to the clipboard
- **AND** shows a visible message telling the user to paste manually

#### Scenario: Dictation provider or processing fails
- **WHEN** dictation processing fails because a provider key is missing, provider request fails, or no usable text is returned
- **THEN** the system shows a visible localized error message
- **AND** the floating overlay returns to the ready state
