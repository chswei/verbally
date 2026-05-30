## MODIFIED Requirements

### Requirement: Cleanup preserves language and intent
The built-in default basic text-processing prompt SHALL preserve the user's original language and mixed-language style, remove filler words, prefer local dictionary terms when relevant, avoid translation, and then apply the selected Formal or Casual output format without rewriting, shortening, replacing words, translating, or changing the user's tone.

#### Scenario: Mixed Chinese and English transcript
- **WHEN** the raw transcript contains mixed Traditional Chinese and English
- **THEN** the cleaned output preserves both languages and does not translate the transcript into a single language

#### Scenario: Dictionary term is relevant to transcript
- **WHEN** local dictionary context contains a term or note relevant to the raw transcript
- **THEN** cleanup instructions tell the model to prefer the saved dictionary spelling or wording
- **THEN** cleanup instructions do not ask the model to invent content that is not supported by the transcript

#### Scenario: Formal style is selected
- **WHEN** the selected style is Formal
- **THEN** cleanup instructions ask only to add or normalize punctuation, capitalization, spacing, and language-required writing conventions
- **THEN** cleanup instructions forbid rewriting, shortening, synonym replacement, translation, and tone changes

#### Scenario: Casual style is selected
- **WHEN** the selected style is Casual
- **THEN** cleanup instructions ask only to use a lighter punctuation or spacing format appropriate to the transcript language
- **THEN** cleanup instructions forbid rewriting, shortening, synonym replacement, translation, and tone changes

### Requirement: Cleanup prompt can be customized
The system SHALL provide a user-editable basic text-processing prompt setting, SHALL default it to the built-in natural cleanup prompt in the selected interface language, and SHALL use the configured prompt plus local dictionary context and selected format-only style when cleaning transcript text with OpenAI or Gemini.

#### Scenario: Default basic text-processing prompt is used
- **WHEN** no custom basic text-processing prompt has been saved
- **THEN** the cleanup settings show the built-in natural cleanup prompt in the selected interface language
- **THEN** cleanup requests use the built-in natural cleanup prompt with dictionary context, selected format-only style, and the raw transcript attached

#### Scenario: Custom basic text-processing prompt is used
- **WHEN** the user saves a custom basic text-processing prompt
- **THEN** subsequent OpenAI cleanup requests send the custom prompt with dictionary context, selected format-only style, and the raw transcript attached
- **THEN** subsequent Gemini cleanup requests send the custom prompt with dictionary context, selected format-only style, and the raw transcript attached
- **THEN** interface language changes do not translate, replace, or otherwise alter the custom prompt

#### Scenario: User restores the default basic text-processing prompt
- **WHEN** the user chooses to restore the default basic text-processing prompt
- **THEN** the basic text-processing prompt setting returns to the built-in natural cleanup prompt in the selected interface language
- **THEN** provider, API key, model, dictionary entries, and style profiles remain unchanged
