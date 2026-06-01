## ADDED Requirements

### Requirement: Bubble is hidden in sensitive contexts
The system SHALL hide the floating dictation bubble in sensitive input contexts, including password fields, numeric-only fields, phone-number fields, and known banking or financial apps.

#### Scenario: Password field is focused
- **WHEN** the accessibility service observes a focused editable password field while the input method is visible
- **THEN** the system hides the floating dictation bubble
- **AND** the system does not start recording from the hidden bubble

#### Scenario: Numeric field is focused
- **WHEN** the accessibility service observes a focused editable numeric-only field while the input method is visible
- **THEN** the system hides the floating dictation bubble

#### Scenario: Phone field is focused
- **WHEN** the accessibility service observes a focused editable phone-number field while the input method is visible
- **THEN** the system hides the floating dictation bubble

#### Scenario: Financial app is active
- **WHEN** the accessibility service observes an active editable field inside a known banking or financial app
- **THEN** the system hides the floating dictation bubble even if the input method is visible

#### Scenario: Standard text field is focused
- **WHEN** the accessibility service observes a standard editable text field in a non-sensitive app while the input method is visible
- **THEN** the system may show the floating dictation bubble according to the existing input-method visibility rules

### Requirement: Bubble exposes repair actions for missing runtime readiness
The system SHALL show a repair-oriented bubble state when dictation cannot run because microphone, overlay, or accessibility readiness is missing after setup had previously been available.

#### Scenario: Microphone permission is revoked
- **WHEN** the floating overlay is active or eligible to be active
- **AND** microphone permission is no longer granted
- **THEN** the system shows a repair bubble instead of a ready recording bubble
- **AND** tapping the repair bubble opens the app permission recovery flow for microphone permission

#### Scenario: Overlay permission is revoked
- **WHEN** Verbally detects that overlay permission is no longer granted
- **THEN** the system routes the user to overlay permission recovery from the main app
- **AND** the overlay does not attempt to add a normal recording bubble until permission is restored

#### Scenario: Accessibility service needs recovery
- **WHEN** the main app detects that the Verbally accessibility service is disabled
- **THEN** the system exposes an accessibility recovery action
- **AND** the action opens Android accessibility settings for the user to re-enable the service

#### Scenario: Runtime readiness is restored
- **WHEN** all required runtime permissions and settings are restored
- **THEN** the system returns to the normal ready bubble behavior for eligible editable fields
