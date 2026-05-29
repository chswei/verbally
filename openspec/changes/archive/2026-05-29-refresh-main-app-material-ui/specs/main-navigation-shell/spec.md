## MODIFIED Requirements

### Requirement: Hamburger menu opens a left drawer
The system SHALL open a compact left-side drawer from the hamburger action and expose low-frequency management and support actions in that drawer. The drawer SHALL keep bottom-navigation destinations out of the drawer and SHALL distinguish app preferences from permission troubleshooting.

#### Scenario: User opens hamburger drawer
- **WHEN** the user taps the hamburger menu action
- **THEN** a left-side drawer appears
- **THEN** the drawer shows a Traditional Chinese menu heading
- **THEN** the menu shows `設定`
- **THEN** the menu shows `權限與疑難排解`
- **THEN** the `設定` description references app appearance or preferences rather than API setup
- **THEN** the menu does not show other app destinations or Permission Setup

#### Scenario: User opens drawer Settings
- **WHEN** the user taps `設定` in the drawer
- **THEN** the app opens the Settings screen for app appearance and preferences
- **THEN** the Home API setup destination remains unchanged

#### Scenario: User opens permissions support
- **WHEN** the user taps `權限與疑難排解` in the drawer
- **THEN** the app opens the permission setup and troubleshooting flow

### Requirement: Main shell follows Material visual hierarchy
The system SHALL present the main app shell with Material-style navigation, readable Traditional Chinese typography, aligned spacing, and consistent color roles across light and dark appearance modes.

#### Scenario: User views the main shell
- **WHEN** the main shell is visible
- **THEN** the top header, page content, and bottom navigation use consistent horizontal alignment
- **THEN** navigation labels remain short Traditional Chinese labels
- **THEN** selected bottom navigation state is visually distinct from unselected destinations
- **THEN** the fifth bottom navigation item fits without overlapping labels or icons
- **THEN** major surfaces, buttons, fields, and support accents use consistent Material color roles
