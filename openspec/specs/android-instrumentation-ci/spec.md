# android-instrumentation-ci Specification

## Purpose
Keep Android-facing behavior covered by emulator instrumentation on pull requests and main.
## Requirements
### Requirement: CI runs Android instrumentation
The project SHALL run the Android instrumentation suite on an explicitly selected emulator for pull requests and pushes to main, alongside existing unit, lint and build checks. CI SHALL retain instrumentation reports even on failure and SHALL bound execution time.

#### Scenario: Pull request changes Android behavior
- **WHEN** the pull request workflow runs
- **THEN** an API 36 emulator executes connectedDebugAndroidTest
- **AND** failures fail the job and test reports are uploaded
