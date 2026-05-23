## MODIFIED Requirements

### Requirement: Bubble appears for editable fields
The system SHALL show the floating dictation bubble only while the input method window is visible and the required overlay and accessibility permissions are enabled.

#### Scenario: Input method opens
- **WHEN** the accessibility service observes that the input method window is visible
- **THEN** the system shows the idle floating dictation bubble

#### Scenario: Input method opens without focused editable metadata
- **WHEN** the accessibility service observes that the input method window is visible
- **AND** the currently focused editable field is unavailable, delayed, or stale
- **THEN** the system shows the idle floating dictation bubble

#### Scenario: Editable field clicked before input method opens
- **WHEN** the accessibility service reports a click event whose source is a focused editable text field
- **AND** the input method window is not visible
- **THEN** the system keeps the floating dictation bubble hidden

#### Scenario: Passive editable focus without input method
- **WHEN** an app reports editable focus
- **AND** the input method window is not visible
- **THEN** the system keeps the floating dictation bubble hidden

#### Scenario: Non-input events while input method remains visible
- **WHEN** System UI, launcher, gesture, or unrelated app events occur
- **AND** the input method window remains visible
- **THEN** the system keeps the floating dictation bubble visible

#### Scenario: Input method closes while editable focus is retained
- **WHEN** the input method window closes while an editable text field remains focused
- **THEN** the system hides the floating dictation bubble

#### Scenario: No input method window
- **WHEN** the input method window is not visible
- **THEN** the system hides the floating dictation bubble if it is visible
