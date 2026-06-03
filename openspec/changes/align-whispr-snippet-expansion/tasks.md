## 1. Tests

- [x] 1.1 Add failing repository tests that saved snippet expansions preserve surrounding whitespace and line breaks.
- [x] 1.2 Add failing expander tests that exact saved expansion formatting survives whole-trigger and in-sentence expansion.
- [x] 1.3 Add failing expander tests that triggers do not expand inside longer Latin-style tokens.

## 2. Implementation

- [x] 2.1 Update snippet normalization to trim triggers while preserving non-blank expansion text exactly.
- [x] 2.2 Update snippet expansion to use exact expansion text and token-boundary-aware matching.
- [x] 2.3 Run focused snippet tests, OpenSpec validation, unit tests, debug build, and install the debug APK to the approved connected phone.
