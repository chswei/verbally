# Codex Instructions for `verbally`

## Environment and workflow

- This project is an Android app, not a Python project. If Python tooling is ever added, use `uv` for virtual environments, dependency management, and script execution.
- On macOS, list the project directory before doing work so you can see the current files.
- Do not commit local machine state or generated artifacts. Keep `local.properties`, `.gradle/`, `.kotlin/`, `app/build/`, `.DS_Store`, `.idea/`, and `*.iml` out of git.
- The Android SDK is expected at `/opt/homebrew/share/android-commandlinetools` on this machine. `local.properties` may point there locally, but it is intentionally ignored.

## Build, test, and validation commands

- Run unit tests:
  ```zsh
  ./gradlew testDebugUnitTest
  ```
- Build the debug APK:
  ```zsh
  ./gradlew assembleDebug
  ```
- Validate OpenSpec artifacts:
  ```zsh
  openspec validate --all --strict
  ```
- The debug APK is generated at:
  ```text
  app/build/outputs/apk/debug/app-debug.apk
  ```

## Project architecture

- `app/src/main/java/com/verbally/app/MainActivity.kt`: Compose UI for permissions, settings, and history.
- `DictationCoordinator.kt`: orchestrates recording, OpenAI transcription, AI cleanup, paste insertion, and history persistence.
- `system/VerballyAccessibilityService.kt`: detects editable fields and owns the floating dictation overlay lifecycle.
- `overlay/FloatingDictationOverlay.kt`: floating bubble UI states and click handling.
- `audio/TemporaryAudioRecorder.kt`: records temporary `.m4a` audio and deletes it after use.
- `providers/`: OpenAI transcription, OpenAI cleanup, Gemini cleanup, and cleanup prompt construction.
- `insertion/`: clipboard + accessibility paste insertion strategy.
- `history/`: latest-100 dictation history models and repositories.
- `settings/`: BYOK provider settings and encrypted API key storage.
- `permissions/`: testable permission guidance, including restricted accessibility settings copy.

## Product constraints

- Verbally is a floating-bubble dictation app for Android 14+.
- Do not add an IME keyboard unless a future OpenSpec change explicitly changes scope.
- Do not add a backend, account system, cloud sync, or provider-key proxy unless explicitly requested.
- First-party UI copy should remain Traditional Chinese unless a localization change is requested.
- Audio is temporary and should be deleted on success, failure, or cancellation.
- History is local-only and capped at the latest 100 entries.
- The app uses OpenAI for transcription. Cleanup can use OpenAI or Gemini.
- Gemini REST calls use the `x-goog-api-key` header; `GEMINI_API_KEY` is only an environment variable name in examples.

## Android permission notes

- Required permissions and settings:
  - `RECORD_AUDIO`
  - overlay permission via `SYSTEM_ALERT_WINDOW`
  - accessibility service: `Verbally 浮動聽寫`
- Debug/sideloaded APKs may show the accessibility service as controlled by restricted settings. The user must open Verbally's App info, use the top-right menu, choose `允許受限制的設定`, then return to Accessibility settings.
- Do not claim accessibility or overlay behavior is fully verified without testing on a real Android device. Emulator/ADB checks are useful but not enough for cross-app text-field compatibility.

## OpenSpec workflow

- Formal specs live in `openspec/specs/`.
- Archived changes live in `openspec/changes/archive/`.
- The initial implemented change is archived at:
  ```text
  openspec/changes/archive/2026-05-21-add-android-floating-dictation/
  ```
- For new feature work, create an OpenSpec change first, validate it, implement against the tasks, then archive after completion.

## Testing expectations

- Use test-first changes for non-trivial provider, insertion, history, permission, and orchestration behavior.
- For UI, Compose, permission-flow, overlay, accessibility, or device-facing behavior changes, run instrumentation checks on an Android target before reporting completion. Prefer the available emulator for automated instrumentation.
- Treat the user's USB debugging phone as a personal device, not a disposable test target. Do not run `connectedDebugAndroidTest`, `adb install`, `adb uninstall`, or other commands that install, replace, clear, or remove apps on the USB device unless the user explicitly asks for that exact device run and acknowledges the app/data impact for that run.
- If the user explicitly approves a USB device run, warn that instrumentation may install/replace the debug app and test APK, and may remove or reset app data depending on Gradle/Android behavior.
- When more than one Android device is connected, inspect `adb devices -l` and set `ANDROID_SERIAL=<device-or-emulator-serial>` so Gradle targets the intended device explicitly.
- Run approved instrumentation tests with:
  ```zsh
  env ANDROID_SERIAL=<serial> ./gradlew connectedDebugAndroidTest
  ```
- Keep tests focused on stable contracts:
  - provider request construction
  - cleanup prompt invariants
  - clipboard paste fallback behavior
  - history retention and search
  - permission guidance
- Before reporting completion, run:
  ```zsh
  openspec validate --all --strict
  ./gradlew testDebugUnitTest
  ./gradlew assembleDebug
  ```

## Dotfiles maintenance

When installing or configuring command-line tools for this user, keep `/Users/chswei/.dotfiles` organized instead of editing `~/.zshrc` directly.

- Homebrew formulae/casks/VS Code extensions/uv tools: update `/Users/chswei/.dotfiles/Brewfile`.
- Environment variables: `/Users/chswei/.dotfiles/zsh/modules/env.zsh`.
- PATH entries: `/Users/chswei/.dotfiles/zsh/modules/path.zsh`.
- Aliases: `/Users/chswei/.dotfiles/zsh/modules/aliases.zsh`.
- Shell functions: `/Users/chswei/.dotfiles/zsh/modules/functions.zsh`.
- Tool initialization snippets: `/Users/chswei/.dotfiles/zsh/modules/tools.zsh`.

After Homebrew-managed tool changes:

1. Run `brew bundle dump --file /Users/chswei/.dotfiles/Brewfile --describe --force`.
2. Put shell snippets in the correct zsh module.
3. Run `zsh -n` on edited zsh files.
4. Use `dotsync "Describe the dotfiles change"` to commit and push.
