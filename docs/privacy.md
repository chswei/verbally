---
title: Privacy Policy
permalink: /privacy/
---

# Privacy Policy

Public URL: https://chswei.github.io/verbally/privacy/

Last updated: 2026-06-04

Verbally is a local-first Android dictation app. It has no backend, account
system, advertising SDK, analytics SDK, crash reporting SDK, or cloud sync.

## Data Verbally Handles

- Temporary microphone audio, recorded only after the user taps the floating bubble.
- Transcript text returned by the selected speech-to-text provider.
- Cleaned text returned by the selected text-processing provider.
- User-provided provider API keys.
- Local dictation history, if enabled.
- Local dictionary entries, snippets, style settings, interface language, and theme.
- Accessibility metadata needed to detect editable text fields and insert or verify
  dictated text at the cursor.

## Third-Party AI Providers

Verbally sends data only to the providers selected by the user:

- OpenAI for transcription and/or text cleanup.
- Soniox, Groq, or Deepgram for transcription.
- Gemini for text cleanup.

When a provider is used, Verbally sends the matching user-provided API key and the
minimum content needed for the selected operation. Audio is sent to the selected
transcription provider. Transcript text can be sent to the selected cleanup provider.
Provider handling is governed by the provider's own terms and privacy policy.

Verbally does not sell user data and does not use provider data for advertising or
analytics.

## AccessibilityService API

Verbally uses Android's AccessibilityService API to:

- detect when an editable text field and keyboard are active;
- show or hide the floating dictation bubble;
- insert dictated text at the active cursor;
- verify whether insertion succeeded before using clipboard fallback.

Verbally does not use Accessibility to read passwords, numeric-only fields, phone
number fields, or known financial apps. The floating bubble is hidden in those
contexts. Verbally does not autonomously initiate actions or make decisions on the
user's behalf.

## Storage And Retention

API keys are stored on device using Android encrypted storage. Dictation history,
dictionary entries, snippets, and app settings are stored locally. The user can keep
the latest 100 dictations, auto-delete dictations older than 24 hours, or disable
history storage.

Temporary audio is deleted after dictation succeeds, fails, or is canceled.

## Security

Network requests use HTTPS through the selected provider endpoints. Verbally does not
commit API keys, signing keys, local Android SDK paths, build outputs, or personal
device state to this repository.

## Contact

Use GitHub issues for general privacy questions after the repository is public. Do
not post API keys, transcripts, audio, or other private data in public issues.
