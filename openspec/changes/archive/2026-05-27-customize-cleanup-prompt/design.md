## Context

Verbally currently builds one fixed natural cleanup prompt in `CleanupPromptFactory` and sends that prompt to both OpenAI Responses and Gemini `generateContent`. Settings already persist API keys, provider selection, and model names through encrypted shared preferences, and the settings UI already contains the cleanup provider controls.

## Goals / Non-Goals

**Goals:**
- Keep the existing prompt as the default for new installs and unset settings.
- Let users edit the cleanup prompt near the text cleanup settings.
- Use the saved prompt for both OpenAI and Gemini cleanup requests.
- Provide a restore-default action without changing provider/model/API key settings.

**Non-Goals:**
- Add multiple prompt presets or per-app prompt profiles.
- Change transcription behavior or add a transcription prompt.
- Add cloud sync, backend storage, or provider-specific prompt templates.

## Decisions

- Store the editable prompt in `AppSettings` as plain user preference data. This follows the existing settings model and keeps prompt persistence local-only.
- Keep `CleanupPromptFactory` as the single source for the default prompt and for combining a prompt template with the raw transcript. This avoids provider drift between OpenAI and Gemini.
- Use a simple placeholder-free editor where the saved text is treated as the instructions and the app appends the raw transcript at request time. This keeps the UI understandable and prevents users from accidentally omitting the transcript.
- Add a restore-default button in the cleanup settings area. This is safer than requiring users to manually reconstruct the built-in prompt after experimentation.

## Risks / Trade-offs

- Custom prompts can request behavior that differs from the default language-preserving cleanup. Mitigation: keep the default prompt intact and make restore default available.
- Long multi-line prompts can make the settings page taller. Mitigation: reuse the already scrollable settings screen and constrain the editor with a reasonable minimum height.
- Existing users have no stored prompt. Mitigation: load the default prompt when the stored value is blank.
