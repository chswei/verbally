# main-navigation-shell Specification

## Purpose
Define the main branded navigation structure for Verbally.

## ADDED Requirements
### Requirement: Main shell uses a branded top header
The system SHALL show a top app header in the main app shell with a hamburger menu action and the Verbally app name.

#### Scenario: Main shell is visible
- **WHEN** the user completes required permissions and enters the main app
- **THEN** the shell shows the Verbally app name
- **THEN** the shell shows a hamburger menu action

### Requirement: Home is the API setup destination
The system SHALL use Home as the default bottom navigation destination and SHALL show API setup blocks there.

#### Scenario: Main app opens
- **WHEN** the main shell is first displayed
- **THEN** Home is selected
- **THEN** the screen shows voice transcription and text processing API setup blocks

### Requirement: Bottom navigation exposes product destinations
The system SHALL expose Home, Dictionary, Snippets, and History as bottom navigation destinations.

#### Scenario: User views bottom navigation
- **WHEN** the main shell is visible
- **THEN** the bottom navigation shows Home
- **THEN** the bottom navigation shows Dictionary
- **THEN** the bottom navigation shows Snippets
- **THEN** the bottom navigation shows History
- **THEN** Settings is not shown as a bottom navigation destination

### Requirement: Hamburger menu opens a left drawer
The system SHALL open a compact left-side drawer from the hamburger action and expose Settings in that drawer.

#### Scenario: User opens hamburger drawer
- **WHEN** the user taps the hamburger menu action
- **THEN** a left-side drawer appears
- **THEN** the drawer shows a menu heading
- **THEN** the menu shows Settings
- **THEN** the menu does not show other app destinations or Permission Setup
