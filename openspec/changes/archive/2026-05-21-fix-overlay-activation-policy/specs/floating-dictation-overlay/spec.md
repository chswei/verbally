## MODIFIED Requirements

### Requirement: Bubble appears for editable fields
The system SHALL show a floating dictation bubble only when text entry is user-initiated for an editable field and the required overlay and accessibility permissions are enabled.

#### Scenario: Editable field clicked
- **WHEN** the accessibility service reports a click event whose source is a focused editable text field
- **THEN** the system shows the idle floating dictation bubble

#### Scenario: Input method opens for focused editable field
- **WHEN** the active default input method emits an accessibility event while an editable text field is focused
- **THEN** the system shows the idle floating dictation bubble

#### Scenario: Passive editable focus without text-entry activation
- **WHEN** an app reports editable focus without an editable field click or active input-method event
- **THEN** the system keeps the floating dictation bubble hidden

#### Scenario: Non-input events while editable field remains focused
- **WHEN** System UI, launcher, gesture, or unrelated app events occur while an editable field remains focused
- **THEN** those events do not show the bubble if it is hidden
- **THEN** those events do not hide the bubble if it is already shown and the editable field remains focused

#### Scenario: No editable field focused
- **WHEN** focus leaves editable text fields
- **THEN** the system hides the floating dictation bubble
