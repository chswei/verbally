# AccessibilityService API Declaration Draft

Last updated: 2026-06-04

Verbally uses the AccessibilityService API but is not declared as an accessibility
tool. `android:isAccessibilityTool` must remain `false`.

## Why Verbally Uses Accessibility

Verbally needs AccessibilityService API access for app functionality:

- Detect when an editable text field and Android keyboard are active.
- Show the floating dictation bubble only near eligible text input.
- Insert dictated text at the current cursor by using Accessibility IME support.
- Verify direct insertion before falling back to clipboard.

## Data Accessed Through Accessibility

Verbally accesses only the metadata and editor connection needed for text-field
detection and insertion. It does not use Accessibility to read passwords,
numeric-only fields, phone fields, or known financial apps; the bubble is hidden in
those contexts.

## Disclosure And Consent

The app shows an in-app Accessibility disclosure before opening Android Accessibility
settings. The disclosure is separate from the privacy policy and listing. The user
must tap an affirmative consent button before continuing.

## Review Video Checklist

Record a short video showing:

1. Open Verbally.
2. Reach the permission setup screen.
3. Show the full Accessibility disclosure.
4. Tap the affirmative consent button.
5. Open Android Accessibility settings and enable Verbally Floating Dictation.
6. Open a normal text field in another app.
7. Use the floating bubble to dictate text and insert it at the cursor.
8. Show that the bubble does not appear in a password field if practical.
