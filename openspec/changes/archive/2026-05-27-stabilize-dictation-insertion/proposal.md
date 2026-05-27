## Why

Verbally's previous insertion path depended on clipboard paste behavior, which made cross-app dictation feel inconsistent: some apps accepted the text directly, while others only received copied clipboard text. Wispr Flow handles the same class of Android text fields by treating accessibility as an IME-capable insertion channel first, then falling back only after verified direct insertion fails.

## What Changes

- Use Accessibility IME `InputConnection.commitText()` as the primary insertion path for cleaned dictation text.
- Verify direct insertion by checking surrounding editor text after a short delay, and retry before declaring insertion failure.
- Align Verbally's accessibility service capabilities with Wispr Flow's relevant service flags, including input-method-editor and filter-key-events support.
- Remove automatic accessibility `ACTION_PASTE` / `ACTION_SET_TEXT` as the normal fallback path for dictated text.
- Copy cleaned text to the clipboard only after direct insertion cannot be verified, so the existing manual-paste recovery remains available.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `floating-dictation-overlay`: cleaned dictation insertion changes from clipboard-first paste to verified direct text insertion with clipboard recovery only after failure.

## Impact

- Affects `app/src/main/java/com/verbally/app/insertion/` insertion abstractions and Android accessibility implementation.
- Affects `VerballyAccessibilityService` setup and debug-only insertion testing hook.
- Affects `app/src/main/res/xml/accessibility_service.xml` service capabilities.
- Adds focused unit coverage for direct insertion, verification, retries, and clipboard fallback ordering.
