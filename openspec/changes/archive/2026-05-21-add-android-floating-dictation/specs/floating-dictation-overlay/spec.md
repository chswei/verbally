## ADDED Requirements

### Requirement: Bubble appears for editable fields
The system SHALL show a floating dictation bubble only when an editable text field is focused and the required overlay and accessibility permissions are enabled.

#### Scenario: Editable field focused
- **WHEN** the accessibility service reports a focused editable text field
- **THEN** the system shows the idle floating dictation bubble

#### Scenario: No editable field focused
- **WHEN** focus leaves editable text fields
- **THEN** the system hides the floating dictation bubble

### Requirement: Bubble controls recording lifecycle
The system SHALL let the user start recording from the bubble, cancel recording, or confirm recording with a checkmark before transcription begins.

#### Scenario: Start recording
- **WHEN** the user taps the idle bubble
- **THEN** the system starts recording audio and shows recording controls

#### Scenario: Confirm recording
- **WHEN** the user taps the checkmark while recording
- **THEN** the system stops recording and starts dictation processing

#### Scenario: Cancel recording
- **WHEN** the user taps cancel while recording
- **THEN** the system stops recording, deletes temporary audio, and returns to idle

### Requirement: Cleaned text is pasted at the active cursor
The system SHALL insert cleaned dictation text by temporarily placing it on the clipboard and invoking paste on the currently focused editable field.

#### Scenario: Paste succeeds
- **WHEN** cleaned dictation text is ready and the focused field supports paste
- **THEN** the system pastes the text at the active cursor
- **THEN** the system best-effort restores the previous clipboard content

#### Scenario: Paste fails
- **WHEN** cleaned dictation text is ready but accessibility paste fails
- **THEN** the system leaves the cleaned text available to paste manually
- **THEN** the system displays a Traditional Chinese fallback message

### Requirement: Permission status is visible
The system SHALL guide the user through microphone, overlay, and accessibility permissions before the bubble workflow is considered ready.

#### Scenario: Missing required permission
- **WHEN** a required permission is not granted
- **THEN** the onboarding screen shows the missing permission and a button to open the relevant Android settings screen
