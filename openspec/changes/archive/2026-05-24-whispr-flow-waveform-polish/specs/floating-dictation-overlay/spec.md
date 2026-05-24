## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: Recording waveform responds to live voice amplitude
The system SHALL animate the recording waveform from live microphone amplitude so louder speech visibly produces larger waveform bars while quieter speech produces smaller bars.

#### Scenario: Louder speech increases bar height
- **WHEN** the user speaks louder while recording
- **THEN** the waveform displays visibly larger bar amplitudes than during quieter speech

#### Scenario: Quiet input keeps waveform controlled
- **WHEN** the microphone input is quiet or near silence while recording
- **THEN** the waveform remains animated but with smaller restrained amplitudes
