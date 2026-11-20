# dictionary-and-snippets Specification

## Purpose
TBD - created by archiving change redesign-main-navigation. Update Purpose after archive.
## Requirements
### Requirement: Dictionary destination is searchable and addable
The system SHALL provide a Dictionary destination with a Traditional Chinese search field, searchable local dictionary entries, and accessible add action.

#### Scenario: Dictionary has no entries
- **WHEN** the user opens Dictionary before entries exist
- **THEN** the screen shows a search field with Traditional Chinese placeholder copy
- **THEN** the screen explains in Traditional Chinese that dictionary words will show there
- **THEN** the screen shows an add action with a Traditional Chinese accessibility label

#### Scenario: User adds a dictionary entry
- **WHEN** the user adds a dictionary entry with a term and optional note
- **THEN** the entry is stored locally on device
- **THEN** the Dictionary list shows the saved term and note

#### Scenario: User searches dictionary entries
- **WHEN** the user searches for text that matches a saved term or note
- **THEN** the Dictionary list shows matching entries
- **THEN** non-matching entries are hidden

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

### Requirement: Dictionary entries can be edited and deleted
The system SHALL allow users to edit and delete local dictionary entries from the Dictionary destination.

#### Scenario: User edits a dictionary entry
- **WHEN** the user edits the term or note of a saved dictionary entry
- **THEN** the Dictionary list shows the updated entry
- **THEN** cleanup uses the updated entry in later requests

#### Scenario: User deletes a dictionary entry
- **WHEN** the user deletes a saved dictionary entry
- **THEN** the entry no longer appears in the Dictionary list
- **THEN** cleanup no longer uses the deleted entry in later requests

### Requirement: Dictionary data remains local
The system SHALL store dictionary entries locally on device and SHALL NOT require accounts, sync, or remote storage.

#### Scenario: Dictionary is used offline
- **WHEN** the user adds, searches, edits, or deletes dictionary entries without network connectivity
- **THEN** the Dictionary operation succeeds using local storage

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

### Requirement: Dictionary and snippet keys do not conflict
The system SHALL prevent a dictionary term and snippet trigger from using the same normalized phrase.

#### Scenario: User saves snippet trigger that matches a dictionary term
- **WHEN** the user attempts to save a snippet whose trigger matches an existing dictionary term after trimming whitespace and ignoring case
- **THEN** the system rejects the save
- **AND** the system shows a localized validation message explaining that dictionary terms and snippet triggers must be unique

#### Scenario: User saves dictionary term that matches a snippet trigger
- **WHEN** the user attempts to save a dictionary entry whose term matches an existing snippet trigger after trimming whitespace and ignoring case
- **THEN** the system rejects the save
- **AND** the system shows a localized validation message explaining that dictionary terms and snippet triggers must be unique

#### Scenario: Different dictionary and snippet keys are saved
- **WHEN** the user saves a dictionary term and a snippet trigger with different normalized phrases
- **THEN** both entries are stored locally
- **AND** later cleanup and snippet expansion can use their respective entries

### Requirement: Dictionary and snippet edits are duplicate-safe
The system SHALL treat trimmed, case-insensitive dictionary terms and snippet triggers as stable identity keys for update and duplicate prevention.

#### Scenario: User edits dictionary term casing
- **WHEN** the user edits a dictionary entry by changing only term casing or surrounding whitespace
- **THEN** the system updates the existing dictionary entry
- **AND** the Dictionary list does not contain a duplicate entry for the same normalized term

#### Scenario: User renames dictionary term to existing term
- **WHEN** the user attempts to rename a dictionary entry to another existing dictionary term after trimming whitespace and ignoring case
- **THEN** the system rejects the save
- **AND** the Dictionary list remains duplicate-free

#### Scenario: User edits snippet trigger casing
- **WHEN** the user edits a snippet by changing only trigger casing or surrounding whitespace
- **THEN** the system updates the existing snippet entry
- **AND** the Snippets list does not contain a duplicate entry for the same normalized trigger

#### Scenario: User renames snippet trigger to existing trigger
- **WHEN** the user attempts to rename a snippet to another existing snippet trigger after trimming whitespace and ignoring case
- **THEN** the system rejects the save
- **AND** the Snippets list remains duplicate-free

#### Scenario: Existing duplicate local data is loaded
- **WHEN** local dictionary or snippet storage contains duplicate normalized keys
- **THEN** the repository returns at most one entry per normalized key
- **AND** the surviving entry preserves the most recently saved data when ordering metadata is available

