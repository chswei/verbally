# local-history-and-settings Specification

## Purpose
TBD - created by archiving change add-android-floating-dictation. Update Purpose after archive.
## Requirements
### Requirement: Provider keys are stored locally and encrypted
The system SHALL store OpenAI, Gemini, Soniox, Groq, and Deepgram API keys locally using Android encrypted storage and SHALL NOT send keys to any service other than the selected provider. The Home settings UI SHALL separate transcription API setup from text-processing setup as inline blocks.

#### Scenario: User opens home
- **WHEN** the user opens the Home destination
- **THEN** the system shows voice transcription and text processing settings blocks

#### Scenario: User edits transcription settings
- **WHEN** the user views the voice transcription block
- **THEN** the system shows a transcription model dropdown whose five options include provider names
- **THEN** the system shows only the generic API Key input for the provider implied by the selected transcription model

#### Scenario: User edits text processing settings
- **WHEN** the user views the text processing block
- **THEN** the system shows a text-processing model dropdown whose options include provider names
- **THEN** the system shows only the generic API Key input for the provider implied by the selected text-processing model

### Requirement: Dictation history is local and capped
The system SHALL store dictation history locally and retain at most the latest 100 entries.

#### Scenario: New history entry exceeds cap
- **WHEN** a new dictation entry is saved and more than 100 entries exist
- **THEN** the system deletes the oldest entries until only 100 remain

### Requirement: History entries preserve useful recovery data
The system SHALL store raw transcript, cleaned text, timestamp, transcription provider/model metadata, cleanup provider/model metadata, and app label when available for each history entry.

#### Scenario: Dictation processing succeeds
- **WHEN** cleaned text is produced
- **THEN** the system saves a history entry with raw transcript, cleaned text, timestamp, transcription provider/model metadata, cleanup provider/model metadata, and optional app label

### Requirement: History can be searched and reused
The system SHALL allow the user to access history from bottom navigation, search, copy, re-paste, delete one entry, and clear all history entries after confirmation.

#### Scenario: User opens history
- **WHEN** the user selects History from bottom navigation
- **THEN** the system shows history search and available history actions
- **THEN** the system explains that only the latest 100 transcription results are saved

#### Scenario: User clears all history
- **WHEN** the user chooses to clear all history
- **THEN** the system asks for confirmation before deleting entries
- **AND** the system clears history only after the user confirms deletion

#### Scenario: User searches history
- **WHEN** the user enters text in history search
- **THEN** the system filters history by raw transcript and cleaned text

#### Scenario: User re-pastes a history entry
- **WHEN** the user chooses to re-paste a history entry while an editable field is focused
- **THEN** the system attempts the same paste insertion strategy used by new dictations

### Requirement: UI text is Traditional Chinese
The system SHALL present onboarding, settings, status, error, and history UI text in the selected interface language when a supported localization is available, SHALL default to Traditional Chinese resources, and SHALL keep Android permission instructions concise and aligned with actual platform behavior.

#### Scenario: Missing key error
- **WHEN** dictation cannot continue because a provider key is missing
- **THEN** the system shows the error in the selected interface language when localized copy is available

#### Scenario: Permission onboarding explains the current step
- **WHEN** a required permission is missing
- **THEN** the setup screen describes only the current missing permission in the selected interface language when localized copy is available
- **THEN** the action label matches whether the app is requesting a runtime permission or opening Android settings

### Requirement: Home and history use polished Traditional Chinese presentation
The system SHALL present Home API setup and History as polished localized utility screens while preserving existing settings and history behavior.

#### Scenario: User opens Home
- **WHEN** the user opens the Home destination
- **THEN** the system shows a localized page header
- **THEN** the system shows transcription and text-processing setup as distinct operation panels
- **THEN** primary save actions remain reachable after long API key input

#### Scenario: User opens History
- **WHEN** the user selects History from bottom navigation
- **THEN** the system shows a localized page header
- **THEN** the system shows a localized search field
- **THEN** the system explains that only the latest 100 transcription results are saved
- **THEN** history entries remain copyable and deletable

### Requirement: Home setup explains the setup order
The system SHALL make the Home API setup screen clearly explain the order and purpose of setup while preserving local-only key storage.

#### Scenario: User opens Home setup
- **WHEN** the user opens the Home destination
- **THEN** the screen explains that the user should set transcription first, then text processing
- **THEN** transcription and text-processing panels use distinct visual roles
- **THEN** each panel shows concise usage copy before its controls
- **THEN** save actions remain visible and aligned with their panel controls

### Requirement: History screen is scannable and understandable
The system SHALL present History with clear Material-style hierarchy for search, destructive clear action, and empty state. The system SHALL only expose the clear-history action from a top-right overflow menu when history entries are available to clear.

#### Scenario: User opens empty History
- **WHEN** the user opens History before entries exist
- **THEN** the screen shows the retention rule near the page title
- **THEN** the search field is aligned with the page content
- **THEN** the system does not show a clear-history action or history overflow menu
- **THEN** the empty state explains what will appear after dictation

#### Scenario: User opens History with entries
- **WHEN** the user opens History after entries exist
- **THEN** the screen shows the retention rule near the page title
- **THEN** the search field is aligned with the page content
- **THEN** a top-right overflow menu aligns with the History title and exposes the clear-history action
- **THEN** selecting the clear-history action asks for confirmation before deleting history
- **THEN** history entries remain copyable and deletable

### Requirement: Home provider setup supports API key testing
The system SHALL let users test the currently selected transcription provider key and text-processing provider key from the Home API setup screen without adding permission readiness status to Home.

#### Scenario: User tests transcription API key
- **WHEN** the user taps the transcription API key test action
- **THEN** the system tests the currently selected transcription provider and API key
- **THEN** the result is shown near the transcription setup controls

#### Scenario: User tests text-processing API key
- **WHEN** the user taps the text-processing API key test action
- **THEN** the system tests the currently selected text-processing provider and API key
- **THEN** the result is shown near the text-processing setup controls

#### Scenario: User opens Home after completing permissions
- **WHEN** the user opens the Home destination
- **THEN** the system does not show permission completion status or a permission checklist in the Home content

### Requirement: History entry deletion requires confirmation
The system SHALL ask for confirmation before deleting one history entry.

#### Scenario: User deletes a history entry
- **WHEN** the user chooses to delete one history entry
- **THEN** the system asks for confirmation before deleting it
- **AND** deletes it only after the user confirms deletion

### Requirement: Local history retention is user configurable
The system SHALL let the user choose how local dictation history is retained on device.

#### Scenario: Latest entries retention is selected
- **WHEN** the user selects the latest-entries retention mode
- **THEN** the system saves future successful dictations locally
- **AND** the system retains at most the latest 100 entries

#### Scenario: Daily auto-delete retention is selected
- **WHEN** the user selects the auto-delete retention mode
- **THEN** the system saves future successful dictations locally
- **AND** the system removes history entries older than 24 hours whenever history is loaded or a new entry is saved
- **AND** the system still retains at most the latest 100 entries

#### Scenario: No-history retention is selected
- **WHEN** the user selects the no-history retention mode and confirms the destructive change
- **THEN** the system clears existing local dictation history
- **AND** the system does not save future successful dictations to local history
- **AND** the History screen explains that history storage is disabled

#### Scenario: User cancels destructive retention change
- **WHEN** the user selects a retention mode that would delete existing local history
- **AND** the user cancels the confirmation dialog
- **THEN** the existing retention mode remains active
- **AND** existing local history remains unchanged

#### Scenario: Dictionary and snippets are unaffected by history retention
- **WHEN** the user changes local history retention mode
- **THEN** dictionary entries, snippet entries, API keys, theme, language, and style settings remain unchanged

### Requirement: Public privacy and store metadata describe data handling
The project SHALL maintain public privacy, release, Google Play, and F-Droid
documentation that reflects Verbally's local-first BYOK behavior.

#### Scenario: Privacy policy is published
- **WHEN** a store reviewer or user opens the repository privacy policy
- **THEN** it explains local storage, temporary audio, AI provider transmission,
  AccessibilityService API use, and local history retention

#### Scenario: F-Droid metadata is reviewed
- **WHEN** F-Droid metadata is prepared
- **THEN** it identifies Apache-2.0 licensing
- **AND** it declares the `NonFreeNet` anti-feature for user-selected proprietary AI
  network services

#### Scenario: Google Play data safety is prepared
- **WHEN** Play Console Data safety is filled
- **THEN** the maintainer can use the repository draft to align declarations with the
  privacy policy and app behavior
