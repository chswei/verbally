## ADDED Requirements

### Requirement: Snippet entries can be edited and deleted
The system SHALL allow users to edit and delete local snippet entries from the Snippets destination.

#### Scenario: User edits a snippet
- **WHEN** the user edits the trigger or expansion of a saved snippet
- **THEN** the Snippets list shows the updated snippet
- **THEN** later dictation expansion uses the updated snippet

#### Scenario: User deletes a snippet
- **WHEN** the user deletes a saved snippet
- **THEN** the snippet no longer appears in the Snippets list
- **THEN** later dictation expansion no longer uses the deleted snippet

### Requirement: Snippet data remains local
The system SHALL store snippet entries locally on device and SHALL NOT require accounts, sync, or remote storage.

#### Scenario: Snippets are used offline
- **WHEN** the user adds, searches, edits, deletes, or expands snippets without network connectivity
- **THEN** the snippet operation succeeds using local storage

### Requirement: Dictation expands static snippets before insertion
The system SHALL replace saved snippet trigger phrases in cleaned dictation text with their exact saved expansion before inserting text into the target app.

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

## MODIFIED Requirements

### Requirement: Snippets destination is searchable and addable
The system SHALL provide a Snippets destination with a Traditional Chinese search field, searchable local snippet entries, and an accessible add action.

#### Scenario: Snippets has no entries
- **WHEN** the user opens Snippets before entries exist
- **THEN** the screen shows a search field with Traditional Chinese placeholder copy
- **THEN** the screen explains in Traditional Chinese that snippets are trigger phrases that expand into saved text
- **THEN** the screen shows an add action with a Traditional Chinese accessibility label

#### Scenario: User adds a snippet
- **WHEN** the user adds a snippet with a trigger phrase and expansion text
- **THEN** the snippet is stored locally on device
- **THEN** the Snippets list shows the saved trigger phrase and expansion

#### Scenario: User searches snippet entries
- **WHEN** the user searches for text that matches a saved trigger phrase or expansion
- **THEN** the Snippets list shows matching entries
- **THEN** non-matching entries are hidden

### Requirement: Placeholder destinations explain their purpose
The system SHALL present Dictionary and Snippets destinations with clear Material-style hierarchy, aligned controls, and obvious add actions. Dictionary SHALL explain and manage real local entries. Snippets SHALL explain and manage static text expansions.

#### Scenario: User opens Dictionary with entries
- **WHEN** Dictionary has saved entries
- **THEN** the screen shows a page title for `字典`
- **THEN** the screen explains that dictionary entries are for common terms and proper nouns
- **THEN** the search field, entries, and add action are visually aligned

#### Scenario: User opens Dictionary without entries
- **WHEN** Dictionary has no entries
- **THEN** the screen shows a page title for `字典`
- **THEN** the screen explains that dictionary entries are for common terms and proper nouns
- **THEN** the search field, empty state, and add action are visually aligned

#### Scenario: User opens Snippets with entries
- **WHEN** Snippets has saved entries
- **THEN** the screen shows a page title for `片段`
- **THEN** the screen explains that snippets are trigger phrases that expand into saved text
- **THEN** the search field, entries, and add action are visually aligned

#### Scenario: User opens Snippets without entries
- **WHEN** Snippets has no entries
- **THEN** the screen shows a page title for `片段`
- **THEN** the screen explains that snippets are trigger phrases that expand into saved text
- **THEN** the search field, empty state, and add action are visually aligned
