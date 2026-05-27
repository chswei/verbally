## 1. Dictionary Data And Prompt Tests

- [x] 1.1 Add failing unit tests for dictionary repository save, update, delete, search, trimming, and storage limits.
- [x] 1.2 Add failing unit tests for cleanup prompt dictionary context with default and custom prompts.
- [x] 1.3 Add failing coordinator/unit coverage proving cleanup requests receive dictionary entries.

## 2. Dictionary Data And Prompt Implementation

- [x] 2.1 Implement dictionary entry models, in-memory repository, and SharedPreferences repository.
- [x] 2.2 Wire the dictionary repository into the application container and dictation coordinator.
- [x] 2.3 Update cleanup prompt construction and provider request factories to include capped dictionary context.

## 3. Dictionary UI

- [x] 3.1 Add failing Compose instrumentation tests for adding, editing, deleting, and searching dictionary entries.
- [x] 3.2 Replace the Dictionary placeholder with a Traditional Chinese searchable list and add/edit dialog.
- [x] 3.3 Refresh Dictionary empty, no-results, and list states after repository changes.

## 4. Validation

- [x] 4.1 Run OpenSpec validation for the active change and all specs.
- [x] 4.2 Run unit tests.
- [x] 4.3 Run focused Compose instrumentation on an Android target.
- [x] 4.4 Build the debug APK and install it to the approved connected device if one is connected.
