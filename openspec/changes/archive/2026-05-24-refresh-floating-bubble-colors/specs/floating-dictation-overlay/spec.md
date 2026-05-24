## MODIFIED Requirements

### Requirement: Bubble uses icon-based rounded-square styling
The system SHALL display the floating dictation overlay with a Verbally-aligned white-surface and brand-blue palette so the idle and active bubble states retain clear contrast in dark environments while preserving the existing compact rounded-square and three-part control shapes.

#### Scenario: Idle bubble appears
- **WHEN** the floating dictation bubble is idle
- **THEN** the bubble displays a white rounded-square surface
- **THEN** the waveform mark appears in the shared Verbally brand blue

#### Scenario: Recording controls appear
- **WHEN** the bubble is recording
- **THEN** the overlay displays white or near-white secondary surfaces for the cancel button and center capsule
- **THEN** the recording waveform uses the shared Verbally brand blue
- **THEN** the confirm button remains the primary action with a solid brand-blue surface and white checkmark

#### Scenario: Processing controls appear
- **WHEN** the bubble is processing
- **THEN** the overlay keeps the same white-surface and brand-blue control language as the recording state
- **THEN** the processing spinner and center indicator use brand-blue accents consistent with the ready and recording states
