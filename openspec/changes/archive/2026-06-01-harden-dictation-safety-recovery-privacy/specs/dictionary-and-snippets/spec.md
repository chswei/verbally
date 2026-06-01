## ADDED Requirements

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
