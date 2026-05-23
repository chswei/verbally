## ADDED Requirements

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
The system SHALL display the floating dictation bubble as a compact rounded square with state symbols instead of text labels.

#### Scenario: Idle bubble appears
- **WHEN** the floating dictation bubble is idle
- **THEN** the bubble displays the app reference-style waveform mark inside a rounded-square surface

#### Scenario: Recording lifecycle states appear
- **WHEN** the bubble is recording, processing, successful, or in error state
- **THEN** the bubble displays a white symbolic icon for the current state instead of a text label

#### Scenario: Launcher and bubble share the mark
- **WHEN** the app launcher icon and idle floating bubble are shown
- **THEN** both use the same modern reference-style waveform mark
