## MODIFIED Requirements

### Requirement: History screen is scannable and understandable
The system SHALL present History with clear Material-style hierarchy for search, destructive clear action, and empty state. The system SHALL only expose the clear-history action from a top-right overflow menu when history entries are available to clear.

#### Scenario: User opens empty History
- **WHEN** the user opens History before entries exist
- **THEN** the screen shows the retention rule near the page title
- **THEN** the search field is aligned with the page content
- **THEN** the system does not show a clear-history action or history overflow menu
- **THEN** the empty state explains what will appear after dictation

#### Scenario: User opens History with entries
- **WHEN** the user opens History after entries exist
- **THEN** the screen shows the retention rule near the page title
- **THEN** the search field is aligned with the page content
- **THEN** a top-right overflow menu aligns with the History title and exposes the clear-history action
- **THEN** selecting the clear-history action asks for confirmation before deleting history
- **THEN** history entries remain copyable and deletable
