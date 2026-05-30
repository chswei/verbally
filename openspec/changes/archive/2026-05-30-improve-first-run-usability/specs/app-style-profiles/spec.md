## MODIFIED Requirements

### Requirement: Style profiles are editable from the main app
The system SHALL expose `語氣` as a localized main app page for choosing Formal or Casual output format per category. In the Traditional Chinese interface, the visible option labels SHALL be `正式` and `口語` while preserving Formal and Casual as the underlying output-style semantics.

#### Scenario: User opens the style page
- **WHEN** the user selects `語氣`
- **THEN** the page shows `聊天`, `工作`, and `其他`
- **THEN** each category shows a `正式`/`口語` control in the Traditional Chinese UI
- **THEN** the Traditional Chinese UI uses localized option labels instead of raw English `Formal` and `Casual`
- **THEN** the page uses the selected interface language when localized copy is available

#### Scenario: User changes a category style
- **WHEN** the user changes a category between Formal and Casual
- **THEN** the selected value is saved
- **THEN** the page reflects the updated value
