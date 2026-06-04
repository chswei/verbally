# Verbally

Verbally is a local-first floating dictation app for Android 14+. It shows a small
dictation bubble beside editable text fields, records only when the user taps it,
transcribes speech through a user-selected provider, optionally cleans up the text,
and inserts the result at the active cursor.

Traditional Chinese documentation is available in [README.zh-TW.md](README.zh-TW.md).

## Project Status

Verbally is preparing for its first public release on Google Play and F-Droid. The
current app version is `0.1.0` (`versionCode = 1`). Expect rapid iteration before
the first stable release.

## Features

- Floating dictation bubble for Android text fields.
- Tap-to-record flow; audio is sent only after the user confirms.
- Speech-to-text providers: OpenAI, Soniox, Groq, and Deepgram.
- Text cleanup providers: OpenAI and Gemini.
- Bring-your-own-key provider setup; API keys are stored locally with Android
  encrypted storage.
- Direct cursor insertion through Android Accessibility IME support, with clipboard
  fallback only when direct insertion cannot be verified.
- Local dictation history with latest-100, 24-hour auto-delete, or no-history modes.
- Local dictionary and snippets for preferred terms and deterministic expansions.
- Interface localization across supported languages, with Traditional Chinese as the
  primary product language.

## Privacy Model

Verbally has no backend, account system, analytics SDK, advertising SDK, crash
reporting SDK, or cloud sync. Audio is temporary and is deleted after success,
failure, or cancellation. Dictation history stays on the device according to the
user's retention setting.

Verbally does use third-party AI network services selected by the user. Temporary
audio, transcript text, cleanup text, and the matching user-provided API key are sent
to the selected providers only for transcription and cleanup. See [PRIVACY.md](PRIVACY.md).

## Accessibility Use

Verbally uses Android's `AccessibilityService` API to detect editable fields, show
the floating dictation bubble, and insert or verify dictated text at the cursor. It
is not declared as an accessibility tool because its primary purpose is general
dictation, not disability-specific assistive access.

Before opening Android Accessibility settings, the app shows an in-app disclosure and
requires affirmative consent. Sensitive fields such as passwords, numeric-only
fields, phone fields, and known financial apps are excluded from bubble display.

## Install From Source

The app is built with Kotlin, Jetpack Compose, Android Gradle Plugin 9.0.1, and
Gradle wrapper 9.1.0.

Local requirements:

- JDK compatible with the Android Gradle Plugin.
- Android SDK. On this maintainer machine, `local.properties` points to
  `/opt/homebrew/share/android-commandlinetools`.

Run unit tests:

```zsh
./gradlew testDebugUnitTest
```

Build a debug APK:

```zsh
./gradlew assembleDebug
```

Build a release app bundle for Play Console upload:

```zsh
./gradlew bundleRelease
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Required Android Permissions

- `RECORD_AUDIO`
- `SYSTEM_ALERT_WINDOW`
- Accessibility service: `Verbally Floating Dictation`

Sideloaded/debug APKs may show Accessibility as controlled by restricted settings.
Open Verbally's Android App info, use the top-right menu, choose "Allow restricted
settings", then return to Accessibility settings and enable the service.

## Store Metadata

The upstream Fastlane/F-Droid metadata lives under:

```text
fastlane/metadata/android/
```

Release and store-preparation docs live under:

```text
docs/release.md
docs/store/
```

F-Droid official repository submission should declare the `NonFreeNet` anti-feature
because the core dictation workflow depends on user-selected proprietary AI network
services.

## OpenSpec

Verbally uses OpenSpec for product behavior specs.

Validate specs:

```zsh
openspec validate --all --strict
```

Completed release-preparation change:

```text
openspec/changes/archive/2026-06-04-prepare-store-open-source-release/
```

## Contributing

Issues and pull requests are welcome after the repository is made public. Please read
[CONTRIBUTING.md](CONTRIBUTING.md), [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md), and
[SECURITY.md](SECURITY.md) before contributing.

## License

Verbally is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE)
and [NOTICE](NOTICE).
