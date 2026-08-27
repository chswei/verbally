## MODIFIED Requirements

### Requirement: Overlay visibility preserves touch safety
The system SHALL remove the floating overlay root during normal input-method-driven hide states so Verbally does not keep an invisible application overlay attached when the bubble is not needed. While the overlay is visible, the system SHALL restrict its touchable region to the currently visible bubble or control row so transparent padding and unused stable-frame space do not intercept touch input intended for the underlying app.

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

#### Scenario: Idle bubble is visible inside a stable frame
- **WHEN** the ready floating bubble is visible over another app
- **THEN** taps and drags inside the visible bubble are delivered to Verbally
- **AND** touches within the transparent remainder of the overlay window pass through to the underlying app
- **AND** Android does not reject the pass-through touch as untrusted or show an overlay touch warning

#### Scenario: Active controls are visible inside a stable frame
- **WHEN** recording or processing controls are visible
- **THEN** the visible control row remains available for its supported tap and drag interactions
- **AND** touches within transparent outer padding or unused frame space pass through to the underlying app
- **AND** Android does not reject the pass-through touch as untrusted or show an overlay touch warning

#### Scenario: Visible overlay layout changes
- **WHEN** the overlay changes state, edge alignment, or configuration while remaining visible
- **THEN** the touchable region is updated to match the newly laid-out visible bubble or control row
