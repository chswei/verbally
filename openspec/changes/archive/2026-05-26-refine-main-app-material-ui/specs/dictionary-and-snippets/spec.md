## ADDED Requirements

### Requirement: Placeholder destinations explain their purpose
The system SHALL present Dictionary and Snippets placeholders with clear Material-style hierarchy, aligned controls, and obvious add actions.

#### Scenario: User opens Dictionary placeholder
- **WHEN** Dictionary has no entries
- **THEN** the screen shows a page title for `字典`
- **THEN** the screen explains that dictionary entries are for common terms and proper nouns
- **THEN** the search field, empty state, and add action are visually aligned

#### Scenario: User opens Snippets placeholder
- **WHEN** Snippets has no entries
- **THEN** the screen shows a page title for `片段`
- **THEN** the screen explains that snippets are for common phrases or templates
- **THEN** the search field, empty state, and add action are visually aligned
