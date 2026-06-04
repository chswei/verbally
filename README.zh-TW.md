# Verbally

Verbally 是 Android 14+ 的浮動語音聽寫 app。它會在可輸入文字框旁顯示小型聽寫按鈕，使用者點擊後才錄音，確認後才送到使用者選擇的 AI 服務做轉錄與文字整理，最後把結果插入目前游標位置。

## 專案狀態

Verbally 正在準備第一次公開上架 Google Play 和 F-Droid。目前版本是 `0.1.0`（`versionCode = 1`），仍屬第一個公開版前的快速迭代階段。

## 主要功能

- 在 Android 文字輸入框旁顯示浮動聽寫按鈕。
- 點擊開始錄音，再次確認後才送出轉錄。
- 語音轉文字支援 OpenAI、Soniox、Groq、Deepgram。
- 文字整理支援 OpenAI、Gemini。
- 使用者自行提供 API Key；Key 使用 Android 加密儲存在本機。
- 優先透過 Android Accessibility IME 直接插入游標位置；無法確認成功時才退回剪貼簿。
- 本機聽寫歷史支援最近 100 筆、24 小時自動刪除、或完全不保存。
- 本機字典與片段，可保存常用詞、專有名詞與固定文字展開。
- 介面支援多語系，產品文案以繁體中文為主。

## 隱私模式

Verbally 沒有後端、帳號系統、分析 SDK、廣告 SDK、崩潰回報 SDK 或雲端同步。音訊是暫存的，會在成功、失敗或取消後刪除。聽寫歷史只會依照使用者的保留設定留在這台裝置上。

Verbally 會使用使用者自行選擇的第三方 AI 網路服務。暫存音訊、轉錄文字、整理文字，以及對應的使用者 API Key，只會為了轉錄與整理送到選定服務。完整內容請看 [PRIVACY.md](PRIVACY.md)。

## 輔助使用用途

Verbally 使用 Android `AccessibilityService` API 偵測可輸入文字框、顯示浮動聽寫按鈕，並把聽寫文字插入或確認到目前游標位置。Verbally 不宣告為 accessibility tool，因為它的主要用途是一般聽寫，不是針對特定障礙情境設計的輔助工具。

在開啟 Android 輔助使用設定前，app 會顯示獨立的使用揭露並要求使用者明確同意。密碼欄位、純數字欄位、電話欄位和已知金融 app 會排除浮動按鈕顯示。

## 從原始碼建置

需要 JDK 和 Android SDK。本維護者機器的 SDK 預設位於：

```zsh
/opt/homebrew/share/android-commandlinetools
```

如果 SDK 在別的位置，請建立本機 `local.properties`：

```properties
sdk.dir=/path/to/android/sdk
```

執行單元測試：

```zsh
./gradlew testDebugUnitTest
```

產生 debug APK：

```zsh
./gradlew assembleDebug
```

產生 Play Console 用的 release app bundle：

```zsh
./gradlew bundleRelease
```

## 必要 Android 權限

- `RECORD_AUDIO`
- `SYSTEM_ALERT_WINDOW`
- 輔助使用服務：`Verbally Floating Dictation`

側載或 debug 安裝的 APK 可能會在 Android 顯示「由受限制的設定控管」。請先開啟 Verbally 的 App 資訊，點右上角選單，選擇「允許受限制的設定」，再回到輔助使用設定開啟服務。

## 上架 metadata

Fastlane / F-Droid 上游 metadata 放在：

```text
fastlane/metadata/android/
```

發佈與上架文件放在：

```text
docs/release.md
docs/store/
```

F-Droid 官方主倉送件時，應標註 `NonFreeNet` anti-feature，因為核心聽寫流程依賴使用者選擇的第三方 AI 網路服務。

## 授權

Verbally 使用 Apache License, Version 2.0。詳見 [LICENSE](LICENSE) 和 [NOTICE](NOTICE)。
