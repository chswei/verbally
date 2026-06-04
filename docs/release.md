# Release Process

Last updated: 2026-06-04

## Preflight

Run:

```zsh
openspec validate --all --strict
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew bundleRelease
git diff --check
```

For UI, Accessibility, overlay, or device-facing behavior changes, validate on an
emulator or explicitly approved Android device. Do not install or run instrumentation
on a personal USB phone unless that exact run is approved.

## Versioning

Update in `app/build.gradle.kts`:

- `versionCode`
- `versionName`

Use matching release tags:

```zsh
git tag v0.1.0
git push origin v0.1.0
```

## Google Play

Use Play App Signing. Keep the upload key and keystore outside the repository.

Build the release app bundle:

```zsh
./gradlew bundleRelease
```

Upload the signed `.aab` to an internal test track first. Complete Data safety,
AccessibilityService API declaration, content rating, pricing/countries, and store
listing before production rollout.

## F-Droid

The official F-Droid repository builds from source. Keep release tags stable and
avoid generated version names or time-based version codes. Declare `NonFreeNet`
because provider-backed transcription and cleanup depend on third-party network
services selected by the user.

## Do Not Commit

- `local.properties`
- `.gradle/`
- `.kotlin/`
- `app/build/`
- `.idea/`
- `*.iml`
- `.DS_Store`
- keystores
- API keys
- Play Console export files containing private account data
