## 1. Overlay lifecycle contracts

- [x] 1.1 Add or update unit tests that prove the overlay lifecycle returns to a reusable ready state after processing completes.
- [x] 1.2 Add or update unit tests that prove cancel and confirm actions keep the recording state machine reusable across multiple dictation runs.

## 2. Overlay UI implementation

- [x] 2.1 Replace the single-icon overlay content with a ready bubble plus a three-part active control row for recording and processing.
- [x] 2.2 Implement animated recording waveform and rotating processing indicator behavior inside the overlay view layer.

## 3. Service integration

- [x] 3.1 Update accessibility service state handling so processing completion always restores the ready bubble while IME visibility remains unchanged.
- [x] 3.2 Preserve drag positioning, accessibility descriptions, and existing IME-driven show/hide behavior with the new overlay states.

## 4. Verification

- [x] 4.1 Validate the OpenSpec change, relevant unit tests, and debug build for the new overlay workflow.
