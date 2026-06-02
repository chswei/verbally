## MODIFIED Requirements

### Requirement: Dictation recording uses Android speech capture
The system SHALL record dictation audio as temporary 16 kHz mono PCM16 WAV audio so confirmed recordings can be sent to speech-to-text providers without AAC compression or m4a container finalization.

#### Scenario: Recording starts with WAV speech capture
- **WHEN** the user starts a dictation recording
- **THEN** the recorder uses Android `AudioRecord` with `MIC` as its audio source
- **AND** the recorder captures 16 kHz mono PCM16 audio
- **AND** the recording remains a temporary `.wav` file for the existing confirm-first transcription flow

#### Scenario: WAV header matches captured PCM data
- **WHEN** a dictation recording is stopped
- **THEN** the temporary WAV file declares mono PCM16 audio at 16 kHz
- **AND** the WAV data size matches the captured PCM byte count

#### Scenario: Recording does not add post-processing latency
- **WHEN** the user confirms a recording
- **THEN** the system sends the confirmed temporary audio to the selected transcription provider without an added post-recording denoise, enhancement, AAC encode, or resampling step
