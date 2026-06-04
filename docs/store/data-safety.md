# Google Play Data Safety Draft

Data safety declarations must stay consistent with the privacy policy and the app's
actual provider behavior.

Last updated: 2026-06-04

This is a maintainer draft for Play Console. The account owner is responsible for
submitting accurate declarations.

## Collection And Sharing

Verbally does not collect data to a Verbally-controlled backend. It does transmit
data to third-party providers selected by the user for app functionality:

- Audio recordings: sent to the selected transcription provider.
- User-generated text: transcript and cleanup text sent to selected AI providers.
- API keys: sent only to the matching provider as authentication.

The app does not sell data, does not use data for advertising, and does not include
analytics or crash reporting SDKs.

## Data Types To Review In Play Console

- Audio files: voice or sound recordings, used for app functionality.
- App activity / other user-generated content: dictated text and cleanup text, used
  for app functionality.
- Device or other identifiers: not intentionally collected by Verbally, but provider
  HTTP infrastructure may receive network metadata as part of HTTPS requests.

## Security Practices

- Data is encrypted in transit with HTTPS.
- API keys are stored locally using Android encrypted storage.
- Local history can be capped, auto-deleted after 24 hours, or disabled.
- Temporary audio is deleted after success, failure, or cancellation.

## Account And Deletion

Verbally has no account system. Users can clear local history in the app and remove
API keys from settings. Uninstalling the app removes app-local data according to
Android app storage behavior.
