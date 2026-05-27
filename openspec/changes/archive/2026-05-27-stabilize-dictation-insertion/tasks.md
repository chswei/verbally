## 1. Accessibility Service Capability

- [x] 1.1 Add input-method-editor and key-event filtering flags to Verbally's accessibility service config.
- [x] 1.2 Confirm the enabled service exposes Wispr Flow-aligned capabilities on the connected Android device.

## 2. Direct Text Insertion

- [x] 2.1 Replace clipboard-first paste insertion with a direct text target abstraction.
- [x] 2.2 Implement Accessibility IME `InputConnection.commitText()` insertion.
- [x] 2.3 Verify committed text with surrounding editor text after a short asynchronous delay.
- [x] 2.4 Retry unverified direct insertion attempts before falling back.
- [x] 2.5 Copy cleaned text to the clipboard only after direct insertion cannot be verified.
- [x] 2.6 Remove automatic `ACTION_PASTE` / `ACTION_SET_TEXT` fallback from the normal dictation insertion path.

## 3. Testing And Device Verification

- [x] 3.1 Add unit coverage proving direct insertion success does not touch the clipboard.
- [x] 3.2 Add unit coverage proving direct insertion failure copies the cleaned text to the clipboard.
- [x] 3.3 Add unit coverage for surrounding-text verification, retry success, and retry exhaustion.
- [x] 3.4 Build and install the debug APK to the connected Android device.
- [x] 3.5 Verify the no-active-field device path retries direct insertion and then copies to clipboard without reporting success.
