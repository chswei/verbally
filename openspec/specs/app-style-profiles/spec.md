# app-style-profiles Specification

## Purpose
TBD - created by archiving change add-app-style-profiles. Update Purpose after archive.
## Requirements
### Requirement: App style profiles define category defaults
The system SHALL maintain local style settings for `聊天`, `工作`, and `其他`, each set to either Formal or Casual, where Formal and Casual only select output formatting behavior and do not authorize rewriting the cleaned text.

#### Scenario: Default style profiles are loaded
- **WHEN** no style profiles have been saved
- **THEN** `聊天` uses Casual
- **THEN** `工作` uses Formal
- **THEN** `其他` uses Formal

#### Scenario: Style profile changes are saved locally
- **WHEN** the user changes a category style
- **THEN** subsequent dictations use the saved style for that category
- **THEN** the setting remains local to the device

#### Scenario: Style profile applies format-only behavior
- **WHEN** a category style is used during cleanup
- **THEN** Formal or Casual may affect punctuation, spacing, capitalization, and language-required writing conventions
- **THEN** Formal or Casual must not ask the model to rewrite, shorten, translate, replace words, or change the user's tone

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

### Requirement: Style rules are customizable per language and output style
The system SHALL allow users to customize the Formal and Casual rule text used by the style layer, scoped to a specific interface language and output style, without replacing the basic text-processing prompt.

#### Scenario: Default style rules are used when no custom rule exists
- **WHEN** a dictation cleanup uses Formal or Casual
- **AND** no custom rule exists for the selected style language and output style
- **THEN** the cleanup prompt uses the built-in default rule for that output style

#### Scenario: Custom style rule overrides only one language and style
- **WHEN** the user saves a custom Casual rule for Traditional Chinese
- **THEN** Traditional Chinese Casual cleanup uses the custom rule
- **AND** Traditional Chinese Formal cleanup still uses the built-in Formal rule
- **AND** other languages still use their built-in Casual rules

#### Scenario: Custom style rule preserves format-only guardrails
- **WHEN** a custom Formal or Casual rule is applied during cleanup
- **THEN** the prompt still includes guardrails that forbid rewriting, shortening, synonym replacement, translation, tone changes, and adding facts
- **AND** the custom rule is applied after the basic text-processing instructions

#### Scenario: User restores a custom style rule
- **WHEN** the user restores a custom Formal or Casual rule to default
- **THEN** the custom rule for that language and output style is removed
- **AND** future cleanup uses the built-in default rule for that language and output style

