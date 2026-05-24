# floating-dictation-overlay Specification

## Purpose
TBD - created by archiving change add-android-floating-dictation. Update Purpose after archive.
## Requirements
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

### Requirement: Bubble snaps to a screen edge after dragging
The system SHALL allow the user to drag the floating dictation bubble and SHALL snap it to the nearest left or right screen edge when the drag ends.

#### Scenario: Drag releases closer to the left edge
- **WHEN** the user drags the bubble and releases it with the bubble center closer to the left half of the screen
- **THEN** the system positions the bubble at the left edge with an approximately 20dp margin
- **THEN** the system preserves the release height

#### Scenario: Drag releases closer to the right edge
- **WHEN** the user drags the bubble and releases it with the bubble center closer to the right half of the screen
- **THEN** the system positions the bubble at the right edge with an approximately 20dp margin
- **THEN** the system preserves the release height

#### Scenario: Snapped position is reused
- **WHEN** the user has previously dragged and released the bubble
- **THEN** future bubble displays use the saved edge and height until the user drags the bubble again

### Requirement: Bubble uses icon-based rounded-square styling
The system SHALL display the floating dictation overlay with compact, refined controls whose proportions, spacing, and icon weight align with the Whispr Flow-style reference rather than a heavy or chunky control row.

#### Scenario: Idle bubble appears
- **WHEN** the floating dictation bubble is idle
- **THEN** the bubble displays a compact rounded-square surface with restrained icon sizing and spacing

#### Scenario: Recording controls appear
- **WHEN** the bubble is recording
- **THEN** the overlay displays a three-part control row with compact circular side buttons and a slim rounded center capsule
- **THEN** the side icons and center marks use thinner visual weight than the earlier heavier style

#### Scenario: Processing controls appear
- **WHEN** the bubble is processing
- **THEN** the overlay keeps the same compact three-part silhouette
- **THEN** the motion and spacing remain visually restrained and consistent with the recording state

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

### Requirement: Recording waveform responds to live voice amplitude
The system SHALL animate the recording waveform from live microphone amplitude so louder speech visibly produces larger waveform bars while quieter speech produces smaller bars.

#### Scenario: Louder speech increases bar height
- **WHEN** the user speaks louder while recording
- **THEN** the waveform displays visibly larger bar amplitudes than during quieter speech

#### Scenario: Quiet input keeps waveform controlled
- **WHEN** the microphone input is quiet or near silence while recording
- **THEN** the waveform remains animated but with smaller restrained amplitudes

