## MODIFIED Requirements

### Requirement: Bottom navigation exposes product destinations
The system SHALL expose `首頁`, `字典`, `片段`, `歷史`, and `語氣` as bottom navigation destinations.

#### Scenario: User views bottom navigation
- **WHEN** the main shell is visible
- **THEN** the bottom navigation shows `首頁`
- **THEN** the bottom navigation shows `字典`
- **THEN** the bottom navigation shows `片段`
- **THEN** the bottom navigation shows `歷史`
- **THEN** the bottom navigation shows `語氣`
- **THEN** the bottom navigation does not show English destination labels
- **THEN** `設定` is not shown as a bottom navigation destination

### Requirement: Main shell follows Material visual hierarchy
The system SHALL present the main app shell with Material-style navigation, readable Traditional Chinese typography, and aligned spacing.

#### Scenario: User views the main shell
- **WHEN** the main shell is visible
- **THEN** the top header, page content, and bottom navigation use consistent horizontal alignment
- **THEN** navigation labels remain short Traditional Chinese labels
- **THEN** selected bottom navigation state is visually distinct from unselected destinations
- **THEN** the fifth bottom navigation item fits without overlapping labels or icons
