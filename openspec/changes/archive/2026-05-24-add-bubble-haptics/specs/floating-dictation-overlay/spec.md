## MODIFIED Requirements

### Requirement: Bubble controls recording lifecycle
The system SHALL provide light tactile feedback when the user taps the ready bubble, the cancel control, or the confirm control in the floating dictation overlay.

#### Scenario: Ready bubble tapped
- **WHEN** the user taps the ready bubble without dragging
- **THEN** the system emits a light haptic tap
- **THEN** the system starts recording audio

#### Scenario: Cancel control tapped
- **WHEN** the user taps cancel during recording without dragging
- **THEN** the system emits a light haptic tap
- **THEN** the system cancels the recording flow

#### Scenario: Confirm control tapped
- **WHEN** the user taps confirm during recording without dragging
- **THEN** the system emits a light haptic tap
- **THEN** the system confirms the recording flow
