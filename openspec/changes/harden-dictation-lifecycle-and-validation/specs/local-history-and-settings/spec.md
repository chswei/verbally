## ADDED Requirements

### Requirement: Retention copy explains cleanup on next use
The system SHALL explain in settings, confirmation, history, and public documentation that entries older than 24 hours are removed on next history use (loading history or saving a new entry), not by a background timer. This SHALL preserve the existing latest-100 cap and retention settings.

#### Scenario: User selects 24-hour retention
- **WHEN** the user reads the retention option and its confirmation
- **THEN** the copy describes removal of entries older than 24 hours on next use
- **AND** switching modes explains that already expired entries are removed immediately

#### Scenario: User reads history retention information
- **WHEN** the user views the history retention subtitle or privacy documentation
- **THEN** it does not promise deletion while the app remains unused
