## MODIFIED Requirements

### Requirement: Bubble controls recording lifecycle
The system SHALL provide noticeable vibration feedback for overlay taps and drag milestones so the floating bubble feels responsive on-device.

#### Scenario: Overlay tap occurs
- **WHEN** the user taps the ready bubble, cancel control, or confirm control without dragging
- **THEN** the system emits a short noticeable vibration

#### Scenario: Drag begins
- **WHEN** the user moves the floating bubble far enough to begin dragging
- **THEN** the system emits a short vibration once for that drag gesture

#### Scenario: Drag snaps to edge
- **WHEN** the user releases a dragged bubble and it snaps to its saved edge
- **THEN** the system emits a snap-completion vibration once
