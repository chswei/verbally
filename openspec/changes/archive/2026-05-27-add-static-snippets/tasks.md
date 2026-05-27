## 1. Snippet Data Model

- [x] 1.1 Add `SnippetEntry`, `SnippetRepository`, in-memory storage, and SharedPreferences-backed storage.
- [x] 1.2 Add unit tests for normalization, trigger uniqueness, search, deletion, and the 200-entry cap.

## 2. Static Expansion

- [x] 2.1 Add deterministic snippet expansion logic for whole-trigger and in-sentence replacement.
- [x] 2.2 Add coordinator tests proving expanded text is inserted and stored in history after cleanup.
- [x] 2.3 Wire `SnippetRepository` into `VerballyContainer`, `DictationCoordinator`, and the accessibility service coordinator construction.

## 3. Snippets UI

- [x] 3.1 Replace the Snippets placeholder with a real CRUD/search Compose surface.
- [x] 3.2 Add Compose instrumentation tests for empty state, add, edit, delete, and search behavior.

## 4. Validation

- [x] 4.1 Run `openspec validate --all --strict`.
- [x] 4.2 Run `./gradlew testDebugUnitTest`.
- [x] 4.3 Run `./gradlew assembleDebug`.
- [x] 4.4 Install the debug APK to the currently connected approved device if one is available.
