## Why

Verbally is preparing for public distribution on the Play Store and F-Droid. The
repo needs open-source health files, store metadata, privacy and Accessibility
policy artifacts, and release documentation that matches the app's actual
local-first, bring-your-own-key behavior.

## What Changes

- Add Apache-2.0 licensing, GitHub community files, release docs, privacy policy,
  and English/Traditional Chinese public README files.
- Add Fastlane metadata and store assets for Google Play and F-Droid reuse.
- Add Google Play Data safety and AccessibilityService API declaration drafts.
- Add F-Droid official-main-repo submission notes, including `NonFreeNet`
  anti-feature disclosure for third-party AI provider dependency.
- Add an in-app Accessibility disclosure and affirmative consent gate before opening
  Android Accessibility settings.

## Impact

- Affected code: permission setup UI, permission guidance policy, manifest metadata.
- Affected docs: README, privacy, release, store, GitHub community files, Fastlane
  metadata.
- Affected tests: unit tests for permission policy, manifest metadata, release files;
  Compose tests for disclosure presentation.
- No backend, account system, provider-key proxy, analytics, advertising, or cloud
  sync is added.
