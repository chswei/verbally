# main-navigation-shell Specification

## Purpose
TBD - created by archiving change redesign-main-navigation. Update Purpose after archive.
## Requirements
### Requirement: Main shell uses a branded top header
The system SHALL show a polished top app header in the main app shell with an accessible hamburger menu action and the Verbally app name.

#### Scenario: Main shell is visible
- **WHEN** the user completes required permissions and enters the main app
- **THEN** the shell shows the Verbally app name
- **THEN** the shell shows a menu action with a Traditional Chinese accessibility label
- **THEN** the shell does not expose the menu action as a visible prototype text marker

### Requirement: Home is the API setup destination
The system SHALL use `首頁` as the default bottom navigation destination and SHALL show API setup blocks there.

#### Scenario: Main app opens
- **WHEN** the main shell is first displayed
- **THEN** `首頁` is selected
- **THEN** the screen shows voice transcription and text processing API setup blocks

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

### Requirement: Hamburger menu opens a left drawer
The system SHALL open a compact left-side drawer from the hamburger action and expose `設定` in that drawer.

#### Scenario: User opens hamburger drawer
- **WHEN** the user taps the hamburger menu action
- **THEN** a left-side drawer appears
- **THEN** the drawer shows a Traditional Chinese menu heading
- **THEN** the menu shows `設定`
- **THEN** the menu does not show other app destinations or Permission Setup

### Requirement: Main shell follows Material visual hierarchy
The system SHALL present the main app shell with Material-style navigation, readable Traditional Chinese typography, and aligned spacing.

#### Scenario: User views the main shell
- **WHEN** the main shell is visible
- **THEN** the top header, page content, and bottom navigation use consistent horizontal alignment
- **THEN** navigation labels remain short Traditional Chinese labels
- **THEN** selected bottom navigation state is visually distinct from unselected destinations
- **THEN** the fifth bottom navigation item fits without overlapping labels or icons

