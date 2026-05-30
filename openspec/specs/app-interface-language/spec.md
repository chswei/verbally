# app-interface-language Specification

## Purpose
TBD - created by archiving change add-multilingual-interface-format-style. Update Purpose after archive.
## Requirements
### Requirement: Interface language can follow system or be manually selected
The system SHALL provide an interface language setting with `Follow system` as the default and SHALL support manual choices for Traditional Chinese, English, Spanish, French, German, Italian, Brazilian Portuguese, Japanese, Korean, and Simplified Chinese.

#### Scenario: Default language follows system
- **WHEN** no interface language has been saved
- **THEN** the system uses the Android system language for supported localized UI resources
- **THEN** the settings page shows `Follow system` as the selected interface language

#### Scenario: Unsupported system language falls back to English
- **WHEN** no interface language has been saved
- **AND** the Android system language is not supported by Verbally
- **THEN** first-party UI copy falls back to English
- **THEN** built-in default prompt and style-rule language defaults resolve to English

#### Scenario: User selects a manual interface language
- **WHEN** the user selects a supported manual interface language in Settings
- **THEN** the selected language is saved locally
- **THEN** first-party UI copy uses the selected language instead of the system language

#### Scenario: User returns to system language
- **WHEN** the user selects `Follow system` after using a manual language
- **THEN** the saved interface language returns to the system-following mode
- **THEN** the app clears its manual locale override

### Requirement: Interface language does not control dictation output language
The system SHALL keep interface language independent from transcription and cleanup output language.

#### Scenario: Dictation language differs from interface language
- **WHEN** the interface language is English
- **AND** the raw transcript is Japanese
- **THEN** cleanup instructions preserve Japanese output
- **THEN** cleanup instructions do not translate the transcript into English

#### Scenario: Mixed-language dictation
- **WHEN** a transcript contains mixed languages
- **THEN** cleanup preserves the mixed-language content
- **THEN** cleanup does not normalize the output to the interface language

### Requirement: Default prompt display follows interface language until customized
The system SHALL display the built-in default basic text-processing prompt in the selected interface language while preserving the same behavior across languages.

#### Scenario: Default prompt follows selected interface language
- **WHEN** the user has not customized the basic text-processing prompt
- **AND** the interface language changes
- **THEN** the cleanup settings show the built-in default prompt in the selected interface language

#### Scenario: Custom prompt is preserved across interface language changes
- **WHEN** the user has saved a custom basic text-processing prompt
- **AND** the interface language changes
- **THEN** the cleanup settings keep the user's custom prompt text unchanged

#### Scenario: Restoring default prompt uses current interface language
- **WHEN** the user restores the default basic text-processing prompt
- **THEN** the prompt is restored in the currently selected interface language
