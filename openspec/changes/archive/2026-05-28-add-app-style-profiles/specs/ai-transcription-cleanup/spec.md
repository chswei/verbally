## MODIFIED Requirements

### Requirement: Transcript cleanup supports OpenAI and Gemini
The system SHALL clean raw transcript text with either OpenAI or Gemini according to the selected cleanup provider, and SHALL include local dictionary vocabulary, active app category, and selected Formal/Casual style as cleanup context.

#### Scenario: OpenAI cleanup selected
- **WHEN** cleanup provider is OpenAI
- **THEN** the system sends the raw transcript, local dictionary context, active app category, selected style, and basic text-processing prompt to the configured OpenAI text model
- **THEN** the system uses the returned cleaned text for insertion and history

#### Scenario: Gemini cleanup selected
- **WHEN** cleanup provider is Gemini
- **THEN** the system sends the raw transcript, local dictionary context, active app category, selected style, and basic text-processing prompt to Gemini `generateContent` with the user's Gemini API key in the `x-goog-api-key` header
- **THEN** the system uses the returned cleaned text for insertion and history

### Requirement: Cleanup preserves language and intent
The built-in default basic text-processing prompt SHALL preserve the user's original language and mixed-language style, remove filler words, prefer local dictionary terms when relevant, avoid translation, and then apply the selected Formal or Casual output style.

#### Scenario: Mixed Chinese and English transcript
- **WHEN** the raw transcript contains mixed Traditional Chinese and English
- **THEN** the cleaned output preserves both languages and does not translate the transcript into a single language

#### Scenario: Dictionary term is relevant to transcript
- **WHEN** local dictionary context contains a term or note relevant to the raw transcript
- **THEN** cleanup instructions tell the model to prefer the saved dictionary spelling or wording
- **THEN** cleanup instructions do not ask the model to invent content that is not supported by the transcript

#### Scenario: Formal style is selected
- **WHEN** the selected style is Formal
- **THEN** cleanup instructions ask only to add punctuation marks

#### Scenario: Casual style is selected
- **WHEN** the selected style is Casual
- **THEN** cleanup instructions ask only to replace punctuation marks with spaces

### Requirement: Cleanup prompt can be customized
The system SHALL provide a user-editable basic text-processing prompt setting, SHALL default it to the built-in natural cleanup prompt, and SHALL use the configured prompt plus local dictionary context and selected style when cleaning transcript text with OpenAI or Gemini.

#### Scenario: Default basic text-processing prompt is used
- **WHEN** no custom basic text-processing prompt has been saved
- **THEN** the cleanup settings show the built-in natural cleanup prompt
- **THEN** cleanup requests use the built-in natural cleanup prompt with dictionary context, selected style, and the raw transcript attached

#### Scenario: Custom basic text-processing prompt is used
- **WHEN** the user saves a custom basic text-processing prompt
- **THEN** subsequent OpenAI cleanup requests send the custom prompt with dictionary context, selected style, and the raw transcript attached
- **THEN** subsequent Gemini cleanup requests send the custom prompt with dictionary context, selected style, and the raw transcript attached

#### Scenario: User restores the default basic text-processing prompt
- **WHEN** the user chooses to restore the default basic text-processing prompt
- **THEN** the basic text-processing prompt setting returns to the built-in natural cleanup prompt
- **THEN** provider, API key, model, dictionary entries, and style profiles remain unchanged
