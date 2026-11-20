## ADDED Requirements

### Requirement: Service teardown cancels dictation work
The system SHALL cancel service-owned processing and HTTP calls, stop and release active recording, and delete temporary audio when the accessibility service is destroyed. Cancellation SHALL NOT produce failure feedback or insert text or save history after cancellation.

#### Scenario: Service is destroyed during recording
- **WHEN** the service is destroyed while recording
- **THEN** recording resources are released and the temporary audio is deleted

#### Scenario: Service is destroyed during provider processing
- **WHEN** the service is destroyed while a provider request or response body read is pending
- **THEN** the request is cancelled and temporary audio is deleted
- **AND** no insertion, history save or failure feedback is performed for the cancelled operation
