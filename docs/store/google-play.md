# Google Play Submission Notes

Last updated: 2026-06-04

## Listing

- App name: Verbally
- Package name: `com.verbally.app`
- Category: Productivity
- Short description: Floating dictation for Android text fields, using your own AI keys.
- Privacy policy URL: `https://github.com/chswei/verbally/blob/main/PRIVACY.md`
- Contains ads: No
- Target audience: Adults / general productivity users, not children.

## Required Assets

- 512x512 app icon: `fastlane/metadata/android/en-US/images/icon.png`
- 1024x500 feature graphic: `fastlane/metadata/android/en-US/images/featureGraphic.png`
- Phone screenshots: `fastlane/metadata/android/en-US/images/phoneScreenshots/`
- Screenshot alt text should describe the actual screen and avoid promotional claims.

## App Content

Complete these Play Console sections before production rollout:

- Privacy policy
- Data safety
- Ads declaration
- App access
- Target audience and content
- Content rating
- AccessibilityService API declaration
- Pricing and countries

## AccessibilityService Listing Disclosure

Suggested listing copy:

> Verbally uses Android Accessibility to detect editable text fields, show the
> floating dictation button beside the active keyboard, and insert or verify dictated
> text at the cursor. It does not read passwords, numeric-only fields, phone fields,
> or known financial apps.

The in-app disclosure must remain separate from this listing copy.

## Release Artifact

Google Play new apps should use Android App Bundle:

```zsh
./gradlew bundleRelease
```

Upload the signed `.aab` through an internal test track first, then closed/open test,
then production.
