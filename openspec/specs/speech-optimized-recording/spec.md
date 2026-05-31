# speech-optimized-recording Specification

## Purpose
TBD - created by archiving change enable-speech-optimized-recording. Update Purpose after archive.
## Requirements
### Requirement: Dictation recording uses Android speech capture
The system SHALL record dictation audio with Android's speech-recognition audio source so supported devices can apply built-in voice-focused capture preprocessing in real time.

#### Scenario: Recording starts with speech-recognition source
- **WHEN** the user starts a dictation recording
- **THEN** the recorder uses Android `VOICE_RECOGNITION` as its audio source
- **AND** the recording remains a temporary `.m4a` file for the existing confirm-first transcription flow

#### Scenario: Recording does not add post-processing latency
- **WHEN** the user confirms a recording
- **THEN** the system sends the confirmed temporary audio to the selected transcription provider without an added post-recording denoise or enhancement step
