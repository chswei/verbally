# app-style-profiles Specification

## Purpose
TBD - created by archiving change add-app-style-profiles. Update Purpose after archive.
## Requirements
### Requirement: App style profiles define category defaults
The system SHALL maintain local style settings for `聊天`, `工作`, and `其他`, each set to either Formal or Casual.

#### Scenario: Default style profiles are loaded
- **WHEN** no style profiles have been saved
- **THEN** `聊天` uses Casual
- **THEN** `工作` uses Formal
- **THEN** `其他` uses Formal

#### Scenario: Style profile changes are saved locally
- **WHEN** the user changes a category style
- **THEN** subsequent dictations use the saved style for that category
- **THEN** the setting remains local to the device

### Requirement: Foreground apps are classified into style categories
The system SHALL classify the active editable app into `聊天`, `工作`, or `其他` before cleanup.

#### Scenario: Known chat app is active
- **WHEN** the active editable app is a known chat app
- **THEN** the system classifies it as `聊天`

#### Scenario: Known work app is active
- **WHEN** the active editable app is a known work or productivity app
- **THEN** the system classifies it as `工作`

#### Scenario: Unknown app is active
- **WHEN** the active editable app is unknown or unavailable
- **THEN** the system classifies it as `其他`

### Requirement: Style profiles are editable from the main app
The system SHALL expose `語氣` as a main app page for choosing Formal or Casual output per category.

#### Scenario: User opens the style page
- **WHEN** the user selects `語氣`
- **THEN** the page shows `聊天`, `工作`, and `其他`
- **THEN** each category shows a Formal/Casual control
- **THEN** the page uses Traditional Chinese copy

#### Scenario: User changes a category style
- **WHEN** the user changes a category between Formal and Casual
- **THEN** the selected value is saved
- **THEN** the page reflects the updated value

