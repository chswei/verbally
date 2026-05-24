## MODIFIED Requirements

### Requirement: Bubble uses icon-based rounded-square styling
The system SHALL preserve translucent overlay surfaces in the floating dictation bubble so the refreshed white-and-blue palette still feels like a lightweight floating layer above the underlying app.

#### Scenario: Ready and secondary controls appear
- **WHEN** the idle bubble, cancel button, center capsule, or processing button backgrounds are shown
- **THEN** those surfaces use translucent white styling instead of fully opaque white fills

#### Scenario: Primary confirm control appears
- **WHEN** the confirm control is shown during recording
- **THEN** it uses a translucent Verbally brand-blue fill with a white checkmark
