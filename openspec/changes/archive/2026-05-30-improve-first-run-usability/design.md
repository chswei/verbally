## Context

Verbally already has step-by-step permission onboarding and a Home API setup screen, but provider setup is currently save-only. Provider errors and clipboard fallback are returned to the floating overlay as content descriptions, which is useful for accessibility but not enough visual feedback for sighted first-time users. Successful direct insertion should stay silent so normal dictation does not create extra interruption. History entries can also be deleted immediately from the history list.

## Goals / Non-Goals

**Goals:**
- Let users test the currently selected transcription and text-processing provider key from Home without adding a permission checklist there.
- Show visible processing feedback only when user attention is needed: clipboard fallback or provider/processing errors.
- Localize Traditional Chinese style option labels as `正式` and `口語` while keeping the underlying Formal/Casual output-style contract.
- Confirm single-entry deletes for history entries.
- Keep tests focused on provider key testing, feedback routing, and Compose UI contracts.

**Non-Goals:**
- Do not change snippet expansion behavior.
- Do not change dictionary behavior or dictionary explanatory copy.
- Do not add a backend, account system, provider-key proxy, or Home permission checklist.
- Do not archive this change without explicit user approval.

## Decisions

1. Add a small provider key tester abstraction inside the Android app.
   - Rationale: Home can test the user's selected provider without coupling Compose UI to raw OkHttp requests.
   - Alternative considered: test by running a full transcription/cleanup request. Rejected because transcription tests would require generated audio, can consume more resources, and can fail on silence even when the key is valid.

2. Test provider keys with lightweight provider endpoints.
   - Rationale: a GET-style authentication/model check is enough for a "can this key be used" button and avoids mutating user data.
   - Trade-off: it verifies authentication and endpoint access, not every runtime transcription or cleanup edge case.

3. Keep API test status local to the Home screen.
   - Rationale: the user asked for a test button, not a persistent checklist. The result should be visible near the controls it tests and should not become app-wide state.

4. Use an Android message sink for dictation processing feedback that needs attention.
   - Rationale: the accessibility service can keep updating the floating overlay for every completion while showing Toast feedback only for clipboard fallback or failures.
   - Alternative considered: adding visible text inside the floating overlay. Rejected for now because the overlay surface is intentionally compact and icon-based.

5. Confirm destructive history-entry deletes with the existing dialog pattern.
   - Rationale: avoids accidental data loss while preserving current list storage and repository behavior.

## Risks / Trade-offs

- Provider endpoints can reject keys for reasons beyond invalid credentials, such as model access or network failure. The UI will show the returned status/detail rather than claiming the exact cause.
- Toast feedback is transient. Limiting it to clipboard fallback and failures avoids interrupting successful direct dictation while still surfacing cases where the user needs to act.
- Adding confirmation dialogs costs one extra tap for deletion, but only on destructive actions.
