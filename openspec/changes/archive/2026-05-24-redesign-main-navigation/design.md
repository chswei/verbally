## Overview

Verbally will keep its current Compose single-activity architecture and reshape the main shell around a Whispr Flow-like structure. The app opens on Home, where API setup is the primary content. Dictionary, Snippets, and History become peer bottom destinations. Secondary app settings move behind the hamburger menu so the bottom navigation stays product-focused.

## UI Structure

- The root scaffold has a top app header with a hamburger button, a compact Verbally wordmark, and an optional permission banner below it.
- Bottom navigation contains Home, Dictionary, Snippets, and History.
- Home renders API setup as two inline blocks: voice transcription and text processing. The blocks show model dropdowns followed by the relevant API key field directly instead of opening nested pages. The text-processing model dropdown includes the provider in each option, such as `OpenAI: gpt-5.4-mini`, so a separate provider selector is not shown.
- Dictionary and Snippets render searchable empty states with a floating add-style action. This establishes the destination structure without committing to persistence semantics in this change.
- History keeps search, per-entry copy/delete actions, and a clear-all action guarded by a confirmation dialog.
- The hamburger menu exposes Settings only. Settings returns the user to the Home API settings surface.
- All primary pages use consistent horizontal margins, title hierarchy, card padding, form-field height, and primary-action height so the UI reads as one coherent system.

## State And Data Flow

- `AppDestination` drives the selected bottom destination.
- Home owns API settings state directly. Updating fields mutates the loaded `AppSettings`, and each inline block can save through the existing settings repository.
- Dictionary and Snippets are presentational placeholders in this change. Their add buttons are non-persistent affordances until a later OpenSpec change defines models and storage.
- Existing history repository interactions remain unchanged.

## Testing

- Add Compose UI tests for the new bottom navigation labels and default Home destination.
- Add tests for hamburger menu actions.
- Add tests that Dictionary and Snippets show search fields, empty states, and add actions.
- Keep existing API settings and history tests green.
- Verify permission setup, API settings, Dictionary, Snippets, and History still compile and pass instrumentation checks after visual polish.
