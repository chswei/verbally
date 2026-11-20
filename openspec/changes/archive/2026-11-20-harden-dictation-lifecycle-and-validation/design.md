## Context
The service owns a main coroutine scope but does not cancel it on destruction. Provider calls block IO threads without cancellation hooks. Recorder shutdown and temporary file deletion must survive teardown. Retention is already access-driven.

## Goals / Non-Goals
Goals: deterministic service cleanup, cancellable HTTP including body reads, accurate retention copy, emulator CI.
Non-goals: transcript recovery, new background jobs, provider changes, database migration, personal-device instrumentation.

## Decisions
- Use a cancellable OkHttp callback bridge that consumes and closes the response inside the callback. Cancellation cancels the underlying call, including body reads. Keep shared connection pooling.
- Cancel the service scope and stop/delete any active recording on destruction. Processing owns its stopped audio file and deletes it in finally. Treat CancellationException as cancellation, never a user-facing failure.
- Preserve best-effort Soniox remote cleanup independently of cancelled processing, without delaying local cleanup.
- Keep retention storage behavior unchanged; update all localized labels, descriptions and public docs to explain next-use cleanup.
- Add a separate Ubuntu emulator CI job on API 36 with explicit target selection and uploaded test reports. Run local instrumentation on the existing API 36 emulator.

## Risks / Trade-offs
- Callback cancellation races → delayed-header/body transport tests and response closure in use blocks.
- Service teardown during recording → idempotent shutdown tests and emulator recorder checks.
- Remote cleanup remains best effort → retain existing cleanup behavior and avoid promising server-side deletion on network failure.
- Longer CI runtime → independent job with timeout and emulator acceleration.

## Migration Plan
No data migration. Deploy the new APK; revert commits to roll back behavior and CI.
