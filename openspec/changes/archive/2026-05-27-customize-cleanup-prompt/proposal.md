## Why

The default cleanup prompt works for general dictation, but users may want different formatting or writing conventions depending on the target text field. Allowing the prompt to be edited keeps the current default behavior while making cleanup style user-controlled.

## What Changes

- Add a user-editable cleanup prompt in settings near the text cleanup API key/model controls.
- Keep the existing natural cleanup prompt as the default value for new installs and as the restore target.
- Send the saved cleanup prompt to both OpenAI and Gemini cleanup requests, with the raw transcript inserted into the prompt at request time.
- Preserve existing cleanup provider selection and model settings.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ai-transcription-cleanup`: Cleanup prompt behavior changes from a fixed built-in prompt to a default prompt that can be customized by the user.

## Impact

- Settings model and encrypted settings persistence gain a cleanup prompt field.
- Main settings UI gains a multi-line cleanup prompt editor and restore-default action.
- OpenAI and Gemini cleanup request construction accept the configured prompt.
- Provider/request tests and settings UI tests cover default and customized prompt behavior.
