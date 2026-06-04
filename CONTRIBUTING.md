# Contributing

Thanks for helping improve Verbally.

## Development Setup

Verbally is an Android app, not a Python project. If Python tooling is added later,
use `uv`.

Run the standard checks before opening a pull request:

```zsh
openspec validate --all --strict
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

For release-affecting changes, also run:

```zsh
./gradlew bundleRelease
```

## Product Constraints

- Keep the app local-first.
- Do not add a backend, account system, cloud sync, analytics SDK, advertising SDK,
  or provider-key proxy unless an accepted OpenSpec change explicitly adds it.
- Do not add an IME keyboard unless an accepted OpenSpec change changes scope.
- Keep first-party product copy clear and conservative, especially around privacy,
  AccessibilityService API use, and AI provider behavior.
- Treat the Android AccessibilityService API as a policy-sensitive surface.

## OpenSpec

Create or update an OpenSpec change for non-trivial product behavior changes. Keep
implementation, specs, and tests aligned.

## Pull Requests

- Keep changes focused.
- Include tests for behavior changes.
- Update docs and store metadata when user-facing behavior, privacy posture, provider
  behavior, or permissions change.
- Do not commit `local.properties`, `.gradle/`, `.kotlin/`, `app/build/`, `.idea/`,
  `*.iml`, `.DS_Store`, keystores, secrets, or generated local machine state.
