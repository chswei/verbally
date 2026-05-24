## Context

The current overlay already supports a repeatable ready -> recording -> processing loop, but its active controls still feel visually thick and synthetic compared with the Whispr Flow reference. The biggest behavioral gap is the waveform: it animates on a timer but does not react to actual microphone volume. Android `MediaRecorder` exposes `maxAmplitude`, which is enough to drive a lightweight live waveform without changing the audio capture architecture.

## Goals / Non-Goals

**Goals:**
- Make the active bubble controls feel noticeably closer to Whispr Flow in proportion, spacing, icon weight, and motion.
- Reflect live speaking amplitude in the recording waveform so louder speech visibly produces taller bars.
- Preserve the existing repeatable dictation loop, IME visibility behavior, and drag persistence.

**Non-Goals:**
- Pixel-perfect cloning of every Whispr Flow visual detail.
- Replacing `MediaRecorder` or changing the transcription pipeline.
- Adding new result states, labels, or extra prompts.

## Decisions

### Poll recorder amplitude from the accessibility service while recording

The service already owns the overlay lifecycle, so it will also own a short-lived coroutine that samples recorder amplitude during recording and pushes normalized levels into the overlay. This keeps live meter timing outside the recorder class and avoids tying the view layer to direct Android media APIs.

Alternative considered:
- Let the overlay poll the recorder directly. Rejected because it would couple a view object to media state and complicate cleanup.

### Isolate waveform math in a small pure Kotlin helper

Amplitude normalization and smoothing will live in a tiny helper so we can test the important behavior without instrumentation. The overlay view will consume a single smoothed level value and map that into bar heights.

Alternative considered:
- Encode all smoothing directly inside the custom `View`. Rejected because it would make the live-audio behavior hard to test.

### Push the visuals toward a thinner, more restrained profile

The buttons, capsule, icon strokes, and animated marks will all be made narrower and more evenly spaced. The goal is not just smaller dimensions, but a more restrained silhouette and motion profile.

## Risks / Trade-offs

- [Recorder amplitude can be noisy] -> Smooth and clamp the sampled values before they reach the waveform renderer.
- [Polling too fast wastes work] -> Sample at a modest interval suitable for UI responsiveness rather than raw audio metering.
- [Closer-to-reference visuals can still miss the exact feel] -> Tune proportions and motion together instead of only shrinking dimensions.
