## 1. Provider and Settings Integration

- [x] 1.1 Add Gemini 3.5 Transcribe to provider metadata, model options, encrypted-key selection, and the settings UI while preserving the existing default.
- [x] 1.2 Add Gemini transcription API-key test metadata and focused settings/provider tests.

## 2. Gemini Transcription Client

- [x] 2.1 Implement inline Base64 audio in one Interactions API request with transcript parsing.
- [x] 2.2 Add unit tests for successful transcription, no-content parsing, failure handling, authentication, and single-request inline body construction.

## 3. Routing and Application Wiring

- [x] 3.1 Route Gemini transcription through `TranscriptionClientRouter`, wire the production client, and test selected key/model dispatch.
- [x] 3.2 Add an Android UI assertion that the Gemini transcription model can be selected and exposes the Gemini key field.

## 4. Validation and Delivery

- [x] 4.1 Run strict OpenSpec validation and debug unit tests.
- [ ] 4.2 Run the focused settings instrumentation check on an approved Android target.
- [x] 4.3 Assemble the debug APK and install it to a connected USB device under the install-only policy.
