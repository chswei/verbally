## MODIFIED Requirements

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

### Requirement: Placeholder destinations explain their purpose
The system SHALL present Dictionary and Snippets destinations with clear Material-style hierarchy, aligned controls, and obvious add actions. Dictionary SHALL explain and manage real local entries, while Snippets SHALL remain an honest placeholder.

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

#### Scenario: User opens Snippets placeholder
- **WHEN** Snippets has no entries
- **THEN** the screen shows a page title for `片段`
- **THEN** the screen explains that snippets are for common phrases or templates
- **THEN** the search field, empty state, and add action are visually aligned

## ADDED Requirements

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
