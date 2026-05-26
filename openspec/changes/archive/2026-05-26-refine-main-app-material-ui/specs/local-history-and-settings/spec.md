## ADDED Requirements

### Requirement: Home setup explains the setup order
The system SHALL make the Home API setup screen clearly explain the order and purpose of setup while preserving local-only key storage.

#### Scenario: User opens Home setup
- **WHEN** the user opens the Home destination
- **THEN** the screen explains that the user should set transcription first, then text processing
- **THEN** transcription and text-processing panels use distinct visual roles
- **THEN** each panel shows concise usage copy before its controls
- **THEN** save actions remain visible and aligned with their panel controls

### Requirement: History screen is scannable and understandable
The system SHALL present History with clear Material-style hierarchy for search, destructive clear action, and empty state.

#### Scenario: User opens empty History
- **WHEN** the user opens History before entries exist
- **THEN** the screen shows the retention rule near the page title
- **THEN** the search field and clear action are aligned with the page content
- **THEN** the empty state explains what will appear after dictation
