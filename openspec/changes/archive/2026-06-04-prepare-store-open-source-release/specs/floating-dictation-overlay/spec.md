## ADDED Requirements

### Requirement: Accessibility disclosure gates accessibility setup
The system SHALL show an in-app Accessibility disclosure and require affirmative
consent before opening Android Accessibility settings when Verbally is not declared
as an accessibility tool.

#### Scenario: Accessibility setup is reached before disclosure consent
- **WHEN** microphone and overlay permissions are granted
- **AND** the Verbally accessibility service is not enabled
- **AND** the user has not accepted the Accessibility disclosure
- **THEN** the permission setup screen shows a separate Accessibility disclosure
- **AND** the primary Android Accessibility settings action is not shown

#### Scenario: User accepts Accessibility disclosure
- **WHEN** the Accessibility disclosure is visible
- **AND** the user taps the affirmative consent action
- **THEN** the system stores the consent locally
- **AND** the permission setup screen shows the normal Accessibility settings action

#### Scenario: User continues after disclosure consent
- **WHEN** the user has accepted the Accessibility disclosure
- **AND** Accessibility remains disabled
- **THEN** the permission setup action opens Android Accessibility settings

#### Scenario: Service metadata remains non-tool
- **WHEN** Verbally declares its AccessibilityService metadata
- **THEN** `android:isAccessibilityTool` is `false`
