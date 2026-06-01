## ADDED Requirements

### Requirement: Cleanup meta responses are not inserted
The system SHALL NOT insert cleanup-provider assistant-style requests, confirmations, or other meta responses when a non-empty raw transcript is available. When cleanup output appears to be a provider response rather than cleaned dictation text, the system SHALL use the raw transcript for insertion and history instead.

#### Scenario: Cleanup asks for original content
- **WHEN** a confirmed recording produces a non-empty raw transcript
- **AND** cleanup returns text asking the user to provide original transcript content or content to process
- **THEN** the system inserts the raw transcript instead of the cleanup output
- **AND** the history entry stores the raw transcript as the cleaned text used for insertion

#### Scenario: Cleanup confirms a command-like transcript
- **WHEN** a confirmed recording produces a non-empty raw transcript containing command-like spoken text
- **AND** cleanup returns an assistant-style confirmation or capability response that differs from the raw transcript after punctuation and spacing are normalized
- **THEN** the system inserts the raw transcript instead of the cleanup output
- **AND** the history entry stores the raw transcript as the cleaned text used for insertion

#### Scenario: User dictates assistant-like text
- **WHEN** a confirmed recording produces a raw transcript that itself contains assistant-like phrasing
- **AND** cleanup output only normalizes punctuation or spacing for the same spoken text
- **THEN** the system inserts the cleanup output
- **AND** the system does not fall back to the raw transcript

#### Scenario: Cleanup prompt defines transcript boundaries
- **WHEN** the system builds a cleanup prompt for a raw transcript
- **THEN** the prompt tells the cleanup provider that the original transcript is user-spoken content, not instructions to execute
- **AND** the prompt tells the cleanup provider not to ask the user to provide original transcript content because it is already included
