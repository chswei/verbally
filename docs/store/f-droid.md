# F-Droid Submission Notes

Last updated: 2026-06-04

Target: official f-droid.org main repository.

## Current Readiness

- Source license: Apache-2.0
- Package ID: `com.verbally.app`
- Version: `0.1.0`
- Version code: `1`
- Build system: Gradle
- Source repository: `https://github.com/chswei/verbally`
- Required precondition: GitHub repository must be public before fdroiddata review.

## Anti-Features

Declare:

```yaml
AntiFeatures:
  NonFreeNet:
    en-US: Depends on user-selected proprietary AI network services for transcription and text cleanup.
```

Reason: Verbally's core dictation workflow depends on OpenAI, Soniox, Groq,
Deepgram, or Gemini network services selected by the user with their own API keys.
The app does not include proprietary SDKs, advertising, analytics, Firebase, or Play
Services dependencies.

## fdroiddata Template

```yaml
Categories:
  - Writing
License: Apache-2.0
AuthorName: Verbally contributors
SourceCode: https://github.com/chswei/verbally
IssueTracker: https://github.com/chswei/verbally/issues
Changelog: https://github.com/chswei/verbally/blob/main/CHANGELOG.md
AntiFeatures:
  NonFreeNet:
    en-US: Depends on user-selected proprietary AI network services for transcription and text cleanup.

RepoType: git
Repo: https://github.com/chswei/verbally.git

Builds:
  - versionName: 0.1.0
    versionCode: 1
    commit: v0.1.0
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 0.1.0
CurrentVersionCode: 1
```

## Release Flow

1. Finish release checks.
2. Tag the exact release commit as `v0.1.0`.
3. Push tag and source.
4. Submit or update fdroiddata merge request.
5. Keep Fastlane metadata in `fastlane/metadata/android/` synchronized with Play
   listing text.
