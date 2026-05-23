## MODIFIED Requirements

### Requirement: Bubble controls recording lifecycle
The system SHALL let the user start recording from the bubble, cancel recording, or confirm recording from a three-part control row before transcription begins. While the input method remains visible, the overlay SHALL return to a reusable ready bubble after each processing attempt.

#### Scenario: Start recording
- **WHEN** the user taps the ready bubble
- **THEN** the system starts recording audio
- **THEN** the overlay shows a three-part recording control row

#### Scenario: Confirm recording
- **WHEN** the user taps the trailing confirm control while recording
- **THEN** the system stops recording
- **THEN** the overlay enters processing without hiding

#### Scenario: Cancel recording
- **WHEN** the user taps the leading cancel control while recording
- **THEN** the system stops recording
- **THEN** the system deletes temporary audio
- **THEN** the overlay returns to the ready bubble

#### Scenario: Processing completes while input method remains visible
- **WHEN** dictation processing succeeds or fails while the input method window is still visible
- **THEN** the overlay returns to the ready bubble
- **THEN** the user can immediately start another recording

### Requirement: Bubble uses icon-based rounded-square styling
The system SHALL display the idle floating dictation bubble as a compact rounded square and SHALL display recording and processing as a rounded three-part control row with animated indicators instead of text labels.

#### Scenario: Idle bubble appears
- **WHEN** the floating dictation bubble is ready
- **THEN** the overlay displays a compact rounded-square bubble with the app waveform mark

#### Scenario: Recording controls appear
- **WHEN** the bubble is recording
- **THEN** the overlay displays a leading cancel button, a center rounded capsule, and a trailing confirm button
- **THEN** the center capsule animates a waveform to indicate live recording

#### Scenario: Processing controls appear
- **WHEN** the bubble is processing
- **THEN** the overlay keeps the same three-part layout
- **THEN** the center capsule shows a processing state indicator
- **THEN** the trailing control shows a rotating progress ring
