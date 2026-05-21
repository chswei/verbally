# Verbally

Verbally 是一個 Android 14+ 的浮動語音聽寫工具原型，目標是做出類似 Wispr Flow Android / Spokenly 的「在任何文字框中說話輸入」體驗。

目前版本以內測 APK 為主：使用者自行提供 OpenAI / Gemini API Key，App 不包含後端、不建立帳號，也不會把歷史同步到雲端。

## 目前功能

- 在輔助使用服務偵測到文字輸入框時顯示浮動聽寫按鈕。
- 點擊浮動按鈕開始錄音，再點擊確認後才送出轉錄。
- 使用 OpenAI `audio/transcriptions` 做語音轉文字。
- 使用 OpenAI 或 Gemini 做自然文字整理。
- 透過剪貼簿與輔助使用 paste action 將文字貼到目前游標位置。
- 本機保存最近 100 筆轉錄歷史，可搜尋、複製、刪除。
- 設定頁支援 OpenAI Key、Gemini Key、模型名稱與整理供應商。
- 介面文案以繁體中文為主。

## 技術棧

- Kotlin
- Jetpack Compose
- Android Gradle Plugin 9.0.1
- Gradle wrapper 9.1.0
- `compileSdk 36` / `targetSdk 36` / `minSdk 34`
- OkHttp
- AndroidX Security Crypto
- OpenSpec

## 建置

本機需要 Android SDK，專案預期 SDK 位於：

```zsh
/opt/homebrew/share/android-commandlinetools
```

如果你的 SDK 在別的位置，請建立或更新本機的 `local.properties`：

```properties
sdk.dir=/path/to/android/sdk
```

執行測試：

```zsh
./gradlew testDebugUnitTest
```

產生 debug APK：

```zsh
./gradlew assembleDebug
```

APK 會輸出到：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 安裝與權限

安裝 APK：

```zsh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

第一次使用需要開啟：

- 麥克風權限
- 浮動視窗權限
- 輔助使用服務：`Verbally 浮動聽寫`

側載或 debug 安裝的 APK 在 Android 上可能會出現「由受限制的設定控管」。這是 Android 對輔助使用服務的安全限制。手動處理方式：

1. 開啟 Verbally 的 App 資訊。
2. 點右上角選單。
3. 選擇「允許受限制的設定」。
4. 回到輔助使用頁面。
5. 開啟 `Verbally 浮動聽寫`。

在 emulator 上除錯時，也可以用 adb 快速開權限：

```zsh
adb shell pm grant com.verbally.app android.permission.RECORD_AUDIO
adb shell appops set com.verbally.app SYSTEM_ALERT_WINDOW allow
adb shell settings put secure accessibility_enabled 1
adb shell settings put secure enabled_accessibility_services com.verbally.app/com.verbally.app.system.VerballyAccessibilityService
```

最後一行會覆蓋目前啟用的輔助使用服務，建議只在 emulator 使用。

## OpenSpec

本專案使用 OpenSpec 管理規格。正式 specs 位於：

- `openspec/specs/floating-dictation-overlay/spec.md`
- `openspec/specs/ai-transcription-cleanup/spec.md`
- `openspec/specs/local-history-and-settings/spec.md`

已封存的初始 change 位於：

```text
openspec/changes/archive/2026-05-21-add-android-floating-dictation/
```

驗證規格：

```zsh
openspec validate --all --strict
```

## 注意事項

- 目前沒有 IME 鍵盤。
- 目前沒有即時 partial transcription；使用者按確認後才轉錄與貼上。
- 目前沒有後端、帳號、雲端同步或計費系統。
- 真實輸入框相容性需要在 Pixel、Samsung，以及 Gmail、LINE、WhatsApp、Chrome、Google Keep/Docs 等 app 上逐一測試。
