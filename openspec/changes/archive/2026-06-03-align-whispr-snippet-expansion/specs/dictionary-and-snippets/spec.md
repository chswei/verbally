## MODIFIED Requirements

### Requirement: Dictation expands static snippets before insertion
The system SHALL replace saved snippet trigger phrases in cleaned dictation text with their exact saved expansion before inserting text into the target app. Snippet expansion SHALL preserve the saved expansion casing, punctuation, line breaks, and formatting whitespace. Snippet matching SHALL treat triggers as spoken cues or phrases and SHALL NOT expand a trigger when it only appears as a substring inside a longer Latin-style word or number token.

#### Scenario: Trigger appears inside longer dictation
- **WHEN** the cleaned dictation text contains a saved snippet trigger phrase inside a longer sentence
- **THEN** the inserted text replaces the trigger phrase with the saved expansion
- **THEN** the expansion preserves the saved casing, punctuation, line breaks, and formatting

#### Scenario: Dictation contains only the trigger
- **WHEN** the cleaned dictation text contains only a saved snippet trigger phrase plus optional surrounding whitespace or trailing sentence punctuation
- **THEN** the inserted text is exactly the saved expansion

#### Scenario: No trigger appears
- **WHEN** the cleaned dictation text contains no saved snippet trigger phrase
- **THEN** the inserted text remains unchanged by snippets

#### Scenario: Trigger appears inside a longer Latin token
- **WHEN** the cleaned dictation text contains a saved snippet trigger only as part of a longer Latin-style word or number token
- **THEN** the inserted text remains unchanged by snippets
