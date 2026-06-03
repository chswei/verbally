## ADDED Requirements

### Requirement: Overlay visibility preserves touch safety
The system SHALL retain the floating overlay root during normal input-method-driven hide and show transitions while ensuring hidden overlay surfaces do not intercept user touch input.

#### Scenario: Input method closes
- **WHEN** the input method window closes while the floating overlay has an attached root
- **THEN** the overlay controls are visually hidden
- **AND** the hidden overlay does not intercept touch input

#### Scenario: Input method reopens
- **WHEN** the input method window becomes visible after a normal hide
- **THEN** the system shows the floating overlay from the retained root when overlay permission is still granted
- **AND** the visible overlay accepts touch input

#### Scenario: Overlay is disposed
- **WHEN** the accessibility service is destroyed or the overlay object is disposed
- **THEN** the system removes the attached overlay root from the window manager

## MODIFIED Requirements

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
