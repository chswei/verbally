## Why

Verbally already has the core floating dictation workflow, local history, dictionary, and snippets, but it still needs more product-grade safety and recovery behavior before it feels dependable in everyday Android text fields. This change hardens the existing local-first experience without changing the recording/transcription architecture.

## What Changes

- Hide the floating dictation bubble in sensitive contexts such as password fields, numeric/phone fields, and known banking or financial apps.
- Add a repair-oriented bubble state for permission/service problems so users can recover microphone, overlay, or accessibility readiness from the overlay instead of seeing a silent failure.
- Add local history retention controls for saving the latest 100 entries, automatically deleting older entries, or disabling future history storage.
- Prevent dictionary terms and snippet triggers from conflicting with each other.
- Normalize dictionary/snippet saves so editing case, whitespace, or trigger/term text updates the existing entry instead of leaving duplicates behind.
- Keep all new behavior local-only and avoid account, sync, backend, realtime, or streaming transcription scope.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `floating-dictation-overlay`: add sensitive-context hiding and repair-state behavior.
- `local-history-and-settings`: add local history retention controls.
- `dictionary-and-snippets`: enforce snippet/dictionary conflicts and duplicate-safe edits.

## Impact

- Affected code: accessibility service visibility routing, overlay state/UI, permission readiness checks, settings repository/UI, history repository, dictionary/snippet repositories and screens.
- Affected specs: `floating-dictation-overlay`, `local-history-and-settings`, `dictionary-and-snippets`.
- Affected tests: unit tests for policies/repositories/history behavior; Compose tests for settings and dictionary/snippet validation; focused overlay/accessibility tests where existing seams support it.
- No new provider APIs, network dependencies, account system, or transcription endpoint changes.
