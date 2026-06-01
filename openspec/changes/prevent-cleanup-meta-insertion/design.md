## Context

Verbally inserts cleaned dictation text directly into the active text field. Cleanup providers receive the raw transcript and prompt context, but short command-like transcripts can be misinterpreted as instructions to answer rather than content to clean. Before this change, any non-empty cleanup output could be inserted, including assistant-style requests such as asking the user to provide original content.

## Goals / Non-Goals

**Goals:**
- Prevent cleanup-provider meta responses from being inserted into the user's active text field.
- Preserve raw transcript content when cleanup output is clearly not a cleaned version of the dictation.
- Keep legitimate user-spoken assistant-like text insertable when cleanup only normalizes punctuation or spacing.
- Strengthen prompt instructions without relying on enumerating specific spoken phrases.

**Non-Goals:**
- Replace cleanup providers with deterministic cleanup.
- Perform broad semantic equivalence scoring between raw transcript and cleanup output.
- Block users from intentionally dictating phrases that sound like assistant responses.
- Add visible UI prompts or warnings for this fallback path.

## Decisions

- Compare compacted raw and cleaned text before fallback.
  - Rationale: If compacted text matches after removing punctuation and spacing, cleanup likely only formatted the transcript and should be preserved.
  - Alternative considered: Always fallback for assistant-like cleaned text. Rejected because it would incorrectly discard legitimate dictated text such as "好的，我會使用繁體中文。"

- Fallback to raw transcript when cleanup output is assistant-style meta text and differs from raw.
  - Rationale: Raw transcript is still user-spoken content, while assistant-style confirmations or requests are not useful insertion text.
  - Alternative considered: Treat these outputs as no-content. Rejected because the user did speak useful content and expects something to be inserted.

- Keep the detector narrow and pattern-based.
  - Rationale: The app needs a reliable local safeguard without adding another model call or external dependency.
  - Alternative considered: Add an AI judge step. Rejected because it adds latency, cost, and another failure surface to a hot dictation path.

- Update cleanup prompts to clarify transcript boundaries.
  - Rationale: Prevention is better than fallback; providers should be told that the raw transcript is already present and is user-spoken content.
  - Alternative considered: Only add programmatic fallback. Rejected because prompt-level prevention reduces the number of fallback cases.

## Risks / Trade-offs

- Pattern-based detection may miss a new provider phrasing. Mitigation: Add focused tests and extend detector only around observed meta-response shapes.
- Pattern-based detection may over-match. Mitigation: Require raw and cleaned compact text to differ before fallback, preserving formatter-only cleanup.
- Fallback uses raw transcript without cleanup. Mitigation: This path is reserved for cleanup outputs that appear unsafe to insert; raw transcript is less polished but avoids inserting model chatter.
