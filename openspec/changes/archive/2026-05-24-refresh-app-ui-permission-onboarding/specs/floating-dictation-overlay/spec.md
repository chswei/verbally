## MODIFIED Requirements

### Requirement: Permission status is visible
The system SHALL guide the user through microphone, overlay, and accessibility permissions one step at a time before the bubble workflow is considered ready, and SHALL refresh permission state when the app returns from Android settings.

#### Scenario: Microphone is the next missing permission
- **WHEN** microphone permission is not granted
- **THEN** the onboarding screen shows only the recording permission step
- **THEN** the primary action opens the Android microphone permission flow or App Info recovery path as appropriate

#### Scenario: Overlay is the next missing permission
- **WHEN** microphone permission is granted
- **AND** overlay permission is not granted
- **THEN** the onboarding screen shows only the floating-window permission step
- **THEN** the primary action opens Android display-over-other-apps settings for Verbally when supported by the platform

#### Scenario: Accessibility is the next missing permission
- **WHEN** microphone and overlay permissions are granted
- **AND** the Verbally accessibility service is not enabled
- **THEN** the onboarding screen shows only the accessibility permission step
- **THEN** the instructions tell the user to enable Verbally floating dictation and keep the shortcut disabled

#### Scenario: App returns from Android settings
- **WHEN** the user returns to Verbally after changing overlay or accessibility settings
- **THEN** the system refreshes permission state
- **THEN** the onboarding screen advances to the next missing permission or completes setup

#### Scenario: All required permissions are granted
- **WHEN** microphone, overlay, and accessibility permissions are granted
- **THEN** Verbally leaves the permission setup flow and shows the main app shell
