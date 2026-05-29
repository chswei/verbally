# app-appearance-mode Specification

## Purpose
TBD - created by archiving change add-theme-mode-settings. Update Purpose after archive.
## Requirements
### Requirement: User can choose app appearance mode
The system SHALL expose an appearance mode setting in the hamburger drawer's Settings screen with follow-system, light, and dark options shown in Traditional Chinese.

#### Scenario: User opens drawer Settings
- **WHEN** the user opens the hamburger drawer
- **AND** the user selects `設定`
- **THEN** the system shows an appearance section
- **THEN** the system shows the current appearance mode
- **THEN** the available modes include `跟隨系統`, `淺色`, and `深色`
- **THEN** each mode is presented as a single-choice radio option

#### Scenario: User opens Home
- **WHEN** the user opens the Home destination
- **THEN** the system does not show appearance mode controls

#### Scenario: User changes appearance mode
- **WHEN** the user selects a different appearance mode
- **THEN** the Settings UI reflects the selected mode immediately
- **AND** the system saves the selected mode without requiring a separate save button

### Requirement: Appearance mode persists locally
The system SHALL store the selected appearance mode locally with app settings and SHALL fall back to follow-system when no valid stored mode exists.

#### Scenario: Saved mode is loaded
- **WHEN** the app loads settings after a user saved `深色`
- **THEN** the loaded settings use dark appearance mode

#### Scenario: Stored mode is missing or invalid
- **WHEN** the app loads settings without a valid saved appearance mode
- **THEN** the loaded settings use follow-system appearance mode

### Requirement: Appearance mode controls app theme
The system SHALL apply the selected appearance mode to the main Compose Material theme across app destinations.

#### Scenario: Mode is follow-system
- **WHEN** the selected appearance mode is follow-system
- **THEN** the app uses the Android system dark-mode state to choose the active theme

#### Scenario: Mode is light
- **WHEN** the selected appearance mode is light
- **THEN** the app uses the light Material color scheme

#### Scenario: Mode is dark
- **WHEN** the selected appearance mode is dark
- **THEN** the app uses the dark Material color scheme

