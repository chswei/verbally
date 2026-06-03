# floating-dictation-overlay Specification

## Purpose
TBD - created by archiving change add-android-floating-dictation. Update Purpose after archive.
## Requirements
### Requirement: Bubble appears for editable fields
The system SHALL show the floating dictation bubble only while the input method window is visible and the required overlay and accessibility permissions are enabled, and SHALL hide it without leaving an invisible touch blocker when the input method window is not visible.

#### Scenario: Input method opens
- **WHEN** the accessibility service observes that the input method window is visible
- **THEN** the system shows the idle floating dictation bubble
- **AND** the bubble can receive taps and drags

#### Scenario: Input method opens without focused editable metadata
- **WHEN** the accessibility service observes that the input method window is visible
- **AND** the currently focused editable field is unavailable, delayed, or stale
- **THEN** the system shows the idle floating dictation bubble

#### Scenario: Editable field clicked before input method opens
- **WHEN** the accessibility service reports a click event whose source is a focused editable text field
- **AND** the input method window is not visible
- **THEN** the system keeps the floating dictation bubble hidden
- **AND** the hidden overlay does not intercept touch input

#### Scenario: Passive editable focus without input method
- **WHEN** an app reports editable focus
- **AND** the input method window is not visible
- **THEN** the system keeps the floating dictation bubble hidden
- **AND** the hidden overlay does not intercept touch input

#### Scenario: Non-input events while input method remains visible
- **WHEN** System UI, launcher, gesture, or unrelated app events occur
- **AND** the input method window remains visible
- **THEN** the system keeps the floating dictation bubble visible

#### Scenario: Input method closes while editable focus is retained
- **WHEN** the input method window closes while an editable text field remains focused
- **THEN** the system hides the floating dictation bubble
- **AND** the hidden overlay does not intercept touch input

#### Scenario: No input method window
- **WHEN** the input method window is not visible
- **THEN** the system hides the floating dictation bubble if it is visible
- **AND** the hidden overlay does not intercept touch input

### Requirement: Bubble controls recording lifecycle
The system SHALL provide noticeable vibration feedback for overlay taps and drag milestones so the floating bubble feels responsive on-device.

#### Scenario: Overlay tap occurs
- **WHEN** the user taps the ready bubble, cancel control, or confirm control without dragging
- **THEN** the system emits a short noticeable vibration

#### Scenario: Drag begins
- **WHEN** the user moves the floating bubble far enough to begin dragging
- **THEN** the system emits a short vibration once for that drag gesture

#### Scenario: Drag snaps to edge
- **WHEN** the user releases a dragged bubble and it snaps to its saved edge
- **THEN** the system emits a snap-completion vibration once

### Requirement: Bubble snaps to a screen edge after dragging
The system SHALL allow the user to drag the floating dictation bubble and SHALL snap it to the nearest left or right screen edge when the drag ends.

#### Scenario: Drag releases closer to the left edge
- **WHEN** the user drags the bubble and releases it with the bubble center closer to the left half of the screen
- **THEN** the system positions the bubble at the left edge with an approximately 20dp margin
- **THEN** the system preserves the release height

#### Scenario: Drag releases closer to the right edge
- **WHEN** the user drags the bubble and releases it with the bubble center closer to the right half of the screen
- **THEN** the system positions the bubble at the right edge with an approximately 20dp margin
- **THEN** the system preserves the release height

#### Scenario: Snapped position is reused
- **WHEN** the user has previously dragged and released the bubble
- **THEN** future bubble displays use the saved edge and height until the user drags the bubble again

### Requirement: Bubble uses icon-based rounded-square styling
The system SHALL preserve translucent Verbally-branded overlay surfaces in the floating dictation bubble while using refined rounded-square idle styling and a right-edge-expanding active control row.

#### Scenario: Idle bubble appears
- **WHEN** the floating dictation bubble is idle
- **THEN** the bubble displays a floating rounded-square surface with larger corners, soft elevation, and a Verbally brand-colored waveform icon
- **AND** the idle bubble does not use Wispr Flow purple styling or Wispr Flow brand assets

#### Scenario: Ready and secondary surfaces appear
- **WHEN** the idle bubble, cancel button, center capsule, or processing button backgrounds are shown
- **THEN** those surfaces use translucent Verbally overlay styling instead of fully opaque fills

#### Scenario: Recording controls appear
- **WHEN** the bubble is recording
- **THEN** the overlay displays a right-edge-expanding three-part control row with a cancel button, slim rounded waveform capsule, and confirm button
- **AND** the recording row preserves the anchored trailing edge while expanding from the idle bubble size

#### Scenario: Primary confirm control appears
- **WHEN** the confirm control is shown during recording
- **THEN** it uses a translucent Verbally brand-blue fill with a white checkmark

#### Scenario: Processing controls appear
- **WHEN** the bubble is processing
- **THEN** the overlay keeps a compact three-part silhouette aligned with the recording state
- **AND** the motion and spacing remain visually restrained

### Requirement: Cleaned text is pasted at the active cursor
The system SHALL insert cleaned dictation text at the active cursor by first using Accessibility IME direct text insertion, verifying that the text reached the editor, and copying the text to the clipboard only when direct insertion cannot be verified.

#### Scenario: Direct insertion succeeds
- **WHEN** cleaned dictation text is ready
- **AND** the accessibility service has an active input method connection for the current editor
- **AND** `commitText` succeeds
- **AND** surrounding editor text confirms the inserted text
- **THEN** the system reports that the text was inserted
- **THEN** the system does not write the cleaned text to the clipboard

#### Scenario: Direct insertion silently fails before retry succeeds
- **WHEN** cleaned dictation text is ready
- **AND** a `commitText` attempt does not appear in surrounding editor text
- **AND** a later bounded retry is verified in surrounding editor text
- **THEN** the system reports that the text was inserted
- **THEN** the system does not write the cleaned text to the clipboard

#### Scenario: Direct insertion cannot be verified
- **WHEN** cleaned dictation text is ready
- **AND** no active input method connection is available, `commitText` throws, or all bounded attempts fail verification
- **THEN** the system copies the cleaned text to the clipboard for manual paste
- **THEN** the system displays a Traditional Chinese fallback message

#### Scenario: Accessibility service supports IME-style insertion
- **WHEN** the Verbally accessibility service is enabled
- **THEN** the service requests interactive windows, reported view IDs, key-event filtering, and input-method-editor support
- **THEN** the service is not declared as an accessibility tool

### Requirement: Permission status is visible
The system SHALL guide the user through microphone, overlay, and accessibility permissions one step at a time before the bubble workflow is considered ready, and SHALL refresh permission state when the app returns from Android settings.

#### Scenario: Microphone is the next missing permission
- **WHEN** microphone permission is not granted
- **THEN** the onboarding screen shows only the recording permission step
- **THEN** the primary action opens the Android microphone permission flow or App Info recovery path as appropriate

#### Scenario: Overlay is the next missing permission
- **WHEN** microphone permission is granted
- **AND** overlay permission is not granted
- **THEN** the onboarding screen shows only the floating-window permission step
- **THEN** the primary action opens Android display-over-other-apps settings for Verbally when supported by the platform

#### Scenario: Accessibility is the next missing permission
- **WHEN** microphone and overlay permissions are granted
- **AND** the Verbally accessibility service is not enabled
- **THEN** the onboarding screen shows only the accessibility permission step
- **THEN** the instructions tell the user to enable Verbally floating dictation and keep the shortcut disabled

#### Scenario: App returns from Android settings
- **WHEN** the user returns to Verbally after changing overlay or accessibility settings
- **THEN** the system refreshes permission state
- **THEN** the onboarding screen advances to the next missing permission or completes setup

#### Scenario: All required permissions are granted
- **WHEN** microphone, overlay, and accessibility permissions are granted
- **THEN** Verbally leaves the permission setup flow and shows the main app shell

### Requirement: Recording waveform responds to live voice amplitude
The system SHALL animate a refined recording waveform from live microphone amplitude so louder speech visibly produces larger restrained waveform marks while quieter speech produces smaller animated marks.

#### Scenario: Louder speech increases bar height
- **WHEN** the user speaks louder while recording
- **THEN** the waveform displays visibly larger bar amplitudes than during quieter speech
- **AND** the waveform remains inside the center capsule bounds

#### Scenario: Quiet input keeps waveform controlled
- **WHEN** the microphone input is quiet or near silence while recording
- **THEN** the waveform remains animated with small dots or short bars rather than large jumps

#### Scenario: Waveform uses refined visual weight
- **WHEN** the recording waveform is visible
- **THEN** it uses fewer, wider Wispr-like marks with comfortable side inset inside the capsule
- **AND** it uses Verbally brand coloring

### Requirement: Dictation processing feedback is visible only when attention is needed
The system SHALL keep successful direct dictation insertion silent while showing visible user feedback when dictation falls back to clipboard insertion or fails due to provider or processing errors.

#### Scenario: Dictation inserts text directly
- **WHEN** dictation processing finishes and direct insertion succeeds
- **THEN** the system does not show a visible success message
- **AND** the floating overlay returns to the ready state

#### Scenario: Dictation falls back to clipboard
- **WHEN** dictation processing finishes but direct insertion cannot be verified
- **THEN** the system copies the cleaned text to the clipboard
- **AND** shows a visible message telling the user to paste manually

#### Scenario: Dictation provider or processing fails
- **WHEN** dictation processing fails because a provider key is missing, provider request fails, or no usable text is returned
- **THEN** the system shows a visible localized error message
- **AND** the floating overlay returns to the ready state

### Requirement: Bubble is hidden in sensitive contexts
The system SHALL hide the floating dictation bubble in sensitive input contexts, including password fields, numeric-only fields, phone-number fields, and known banking or financial apps.

#### Scenario: Password field is focused
- **WHEN** the accessibility service observes a focused editable password field while the input method is visible
- **THEN** the system hides the floating dictation bubble
- **AND** the system does not start recording from the hidden bubble

#### Scenario: Numeric field is focused
- **WHEN** the accessibility service observes a focused editable numeric-only field while the input method is visible
- **THEN** the system hides the floating dictation bubble

#### Scenario: Phone field is focused
- **WHEN** the accessibility service observes a focused editable phone-number field while the input method is visible
- **THEN** the system hides the floating dictation bubble

#### Scenario: Financial app is active
- **WHEN** the accessibility service observes an active editable field inside a known banking or financial app
- **THEN** the system hides the floating dictation bubble even if the input method is visible

#### Scenario: Standard text field is focused
- **WHEN** the accessibility service observes a standard editable text field in a non-sensitive app while the input method is visible
- **THEN** the system may show the floating dictation bubble according to the existing input-method visibility rules

### Requirement: Bubble exposes repair actions for missing runtime readiness
The system SHALL show a repair-oriented bubble state when dictation cannot run because microphone, overlay, or accessibility readiness is missing after setup had previously been available.

#### Scenario: Microphone permission is revoked
- **WHEN** the floating overlay is active or eligible to be active
- **AND** microphone permission is no longer granted
- **THEN** the system shows a repair bubble instead of a ready recording bubble
- **AND** tapping the repair bubble opens the app permission recovery flow for microphone permission

#### Scenario: Overlay permission is revoked
- **WHEN** Verbally detects that overlay permission is no longer granted
- **THEN** the system routes the user to overlay permission recovery from the main app
- **AND** the overlay does not attempt to add a normal recording bubble until permission is restored

#### Scenario: Accessibility service needs recovery
- **WHEN** the main app detects that the Verbally accessibility service is disabled
- **THEN** the system exposes an accessibility recovery action
- **AND** the action opens Android accessibility settings for the user to re-enable the service

#### Scenario: Runtime readiness is restored
- **WHEN** all required runtime permissions and settings are restored
- **THEN** the system returns to the normal ready bubble behavior for eligible editable fields

### Requirement: Overlay visibility preserves touch safety
The system SHALL remove the floating overlay root during normal input-method-driven hide states so Verbally does not keep an invisible application overlay attached when the bubble is not needed.

#### Scenario: Input method closes
- **WHEN** the input method window closes while the floating overlay has an attached root
- **THEN** the system removes the attached overlay root from the window manager
- **AND** no hidden overlay surface remains available to intercept touch input

#### Scenario: Input method reopens
- **WHEN** the input method window becomes visible after a normal hide
- **THEN** the system attaches a fresh floating overlay root when overlay permission is still granted
- **AND** the visible overlay accepts touch input
- **AND** the visible overlay uses the saved edge and height

#### Scenario: Overlay is disposed
- **WHEN** the accessibility service is destroyed or the overlay object is disposed
- **THEN** the system removes the attached overlay root from the window manager
