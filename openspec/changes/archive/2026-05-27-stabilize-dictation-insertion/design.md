## Context

Verbally previously treated insertion as clipboard-first: cleaned text was copied, an accessibility paste action was attempted, and the previous clipboard was restored only on success. That worked for some editable nodes, but WebView-backed editors such as Heptabase behaved inconsistently and often ended up with the text only on the clipboard.

Wispr Flow's APK shows a different primary path. Its accessibility service requests `flagInputMethodEditor`, gets the accessibility `InputMethod` current input connection, calls `commitText`, waits briefly, verifies the text through surrounding editor content, and falls back to copying only when direct insertion fails.

## Goals / Non-Goals

**Goals:**

- Make direct Accessibility IME insertion the first-class path for cleaned dictation text.
- Verify that direct insertion actually reached the editor before reporting success.
- Retry transient or silent commit failures before falling back.
- Preserve a manual recovery path by copying cleaned text to the clipboard after verified insertion fails.
- Keep the implementation local to the existing accessibility/insertion architecture.

**Non-Goals:**

- Do not add a custom Android keyboard or change the user's selected IME.
- Do not add a backend, sync layer, or cloud queue for failed insertions.
- Do not use accessibility `ACTION_PASTE` or `ACTION_SET_TEXT` as the normal automatic fallback.
- Do not change transcription, cleanup, snippets, dictionary, or local history semantics.

## Decisions

- **Use Accessibility `InputConnection.commitText()` as the primary insertion channel.** This matches Wispr Flow's successful cross-app behavior more closely than node paste actions and behaves like text entered through an IME.
- **Verify by reading surrounding text after a short delay.** Android editors, especially WebView-backed editors, may update asynchronously. A brief coroutine delay before `getSurroundingText(500, 500)` avoids treating a successful commit as a silent failure too early.
- **Retry direct insertion up to three times.** Wispr Flow uses multiple attempts around focus/input-connection instability. Verbally mirrors that pattern with a bounded retry loop and diagnostic logs.
- **Copy to clipboard only after direct insertion cannot be verified.** This preserves the user's recovery path while preventing successful insertion from touching clipboard state.
- **Remove automatic `ACTION_PASTE` / `ACTION_SET_TEXT` fallback.** The fallback was more likely to vary by app and could replace field contents in unsupported editors. The clipboard fallback is explicit and safer.
- **Expose a debug-only broadcast for device verification.** The debug APK can trigger the insertion path without making real recordings; release builds do not register the receiver.

## Risks / Trade-offs

- **Some editors do not expose surrounding text** -> Verbally may copy to clipboard even if `commitText` visually succeeded. The bounded verification prevents false success and keeps manual recovery available.
- **Text longer than the surrounding window may be hard to verify** -> The current 500-before/500-after window is suitable for short dictation snippets but may need tuning for very long insertions.
- **Clipboard fallback still affects clipboard state on failure** -> This intentionally matches Wispr Flow's recovery behavior and makes the fallback explicit in the user-facing message.
- **Real app compatibility is device-state dependent** -> Heptabase and similar app tests require an unlocked device focused in an editable field; unit tests cover insertion ordering and verification contracts.
