## MODIFIED Requirements

### Requirement: Overlay visibility preserves touch safety
The system SHALL remove the floating overlay root during normal input-method-driven hide states so Verbally does not keep an invisible application overlay attached when the bubble is not needed.

#### Scenario: Input method closes
- **WHEN** the input method window closes while the floating overlay has an attached root
- **THEN** the system removes the attached overlay root from the window manager
- **AND** no hidden overlay surface remains available to intercept touch input

#### Scenario: Input method reopens
- **WHEN** the input method window becomes visible after a normal hide
- **THEN** the system attaches a fresh floating overlay root when overlay permission is still granted
- **AND** the visible overlay accepts touch input
- **AND** the visible overlay uses the saved edge and height

#### Scenario: Overlay is disposed
- **WHEN** the accessibility service is destroyed or the overlay object is disposed
- **THEN** the system removes the attached overlay root from the window manager
