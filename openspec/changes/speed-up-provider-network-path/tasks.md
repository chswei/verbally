## 1. Provider Network Reuse

- [x] 1.1 Add failing tests showing default OpenAI transcription and cleanup clients reuse the same connection.
- [x] 1.2 Share a default provider `OkHttpClient` across provider clients and key testing.

## 2. Soniox Success-Path Latency

- [x] 2.1 Add failing tests for shorter default Soniox polling and non-blocking successful remote cleanup.
- [x] 2.2 Implement shorter initial Soniox polling and background best-effort remote cleanup after successful transcript retrieval.

## 3. Verification

- [x] 3.1 Run `openspec validate --all --strict`.
- [x] 3.2 Run focused unit tests for provider network changes.
- [x] 3.3 Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug`.
