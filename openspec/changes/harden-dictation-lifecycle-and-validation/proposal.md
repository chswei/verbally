## Why
Service teardown currently leaves recording and processing work alive. History retention copy promises timed deletion that is actually performed on access, and existing Android instrumentation tests are absent from CI.

## What Changes
- Cancel service-owned work and HTTP calls, release recording resources, and delete temporary audio on teardown.
- Explain in all supported languages and public documentation that entries older than 24 hours are removed on next history use, not by a background timer.
- Run Android instrumentation on an emulator in CI and retain diagnostic reports.

## Capabilities
### New Capabilities
- `android-instrumentation-ci`: Automated emulator coverage for Android-facing behavior.
### Modified Capabilities
- `ai-transcription-cleanup`: Service teardown cancels recording and processing safely.
- `local-history-and-settings`: Retention copy explains cleanup on next use.

## Impact
Accessibility service, recording orchestration, provider transport, localized resources, public documentation, Android tests and GitHub Actions. No new data collection or background retention job.
