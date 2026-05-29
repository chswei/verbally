## MODIFIED Requirements

### Requirement: Appearance mode controls app theme
The system SHALL apply the selected appearance mode to the main Compose Material theme, refreshed Material color roles, and readable Android system bar icon contrast across app destinations.

#### Scenario: Mode is follow-system
- **WHEN** the selected appearance mode is follow-system
- **THEN** the app uses the Android system dark-mode state to choose the active theme

#### Scenario: Mode is light
- **WHEN** the selected appearance mode is light
- **THEN** the app uses the light Material color scheme
- **THEN** Android system bar icons remain readable against the light app background

#### Scenario: Mode is dark
- **WHEN** the selected appearance mode is dark
- **THEN** the app uses the dark Material color scheme
- **THEN** Android system bar icons remain readable against the dark app background

#### Scenario: User views refreshed color roles
- **WHEN** the app theme is applied
- **THEN** the brand blue remains the primary identity color
- **THEN** supporting teal or mint roles are available for support and permission states
- **THEN** supporting lavender roles are available for AI, style, or preference accents
