## MODIFIED Requirements

### Requirement: Cleaned text is pasted at the active cursor
The system SHALL insert cleaned dictation text at the active cursor by first using Accessibility IME direct text insertion, verifying that the text reached the editor, and copying the text to the clipboard only when direct insertion cannot be verified.

#### Scenario: Direct insertion succeeds
- **WHEN** cleaned dictation text is ready
- **AND** the accessibility service has an active input method connection for the current editor
- **AND** `commitText` succeeds
- **AND** surrounding editor text confirms the inserted text
- **THEN** the system reports that the text was inserted
- **THEN** the system does not write the cleaned text to the clipboard

#### Scenario: Direct insertion silently fails before retry succeeds
- **WHEN** cleaned dictation text is ready
- **AND** a `commitText` attempt does not appear in surrounding editor text
- **AND** a later bounded retry is verified in surrounding editor text
- **THEN** the system reports that the text was inserted
- **THEN** the system does not write the cleaned text to the clipboard

#### Scenario: Direct insertion cannot be verified
- **WHEN** cleaned dictation text is ready
- **AND** no active input method connection is available, `commitText` throws, or all bounded attempts fail verification
- **THEN** the system copies the cleaned text to the clipboard for manual paste
- **THEN** the system displays a Traditional Chinese fallback message

#### Scenario: Accessibility service supports IME-style insertion
- **WHEN** the Verbally accessibility service is enabled
- **THEN** the service requests interactive windows, reported view IDs, key-event filtering, and input-method-editor support
- **THEN** the service remains marked as an accessibility tool
