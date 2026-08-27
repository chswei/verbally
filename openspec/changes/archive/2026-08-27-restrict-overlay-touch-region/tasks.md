## 1. Touchable Region Tests

- [x] 1.1 Add failing unit tests for compact idle, active-row, clipped, and empty touch-region bounds inside the stable overlay frame.
- [x] 1.2 Add focused Android instrumentation coverage for translating a visible descendant surface into root-local touch bounds.

## 2. Overlay Input Region

- [x] 2.1 Implement the pure touch-region geometry helper required by the focused unit tests.
- [x] 2.2 Register the visible bubble or active row as the interaction surface for each overlay state.
- [x] 2.3 Apply the interaction surface bounds through `AttachedSurfaceControl.setTouchableRegion` after attachment/layout and refresh them after rendered layout changes.

## 3. Validation and Device Delivery

- [x] 3.1 Run strict OpenSpec validation and the debug unit-test suite.
- [x] 3.2 Run focused instrumentation on an available emulator and build the debug APK.
- [x] 3.3 Install the latest debug APK on the connected USB phone without running tests or clearing app data, and record the remaining manual cross-app tap check.

Manual verification remaining: with the bubble visible over another app, tap controls horizontally aligned beside the bubble and confirm the underlying app receives those taps.

## 4. Android Untrusted-Touch Follow-Up

- [x] 4.1 Add failing unit tests for deriving a valid pass-through window alpha from Android's maximum obscuring opacity.
- [x] 4.2 Apply the system maximum obscuring opacity to the application-overlay `LayoutParams.alpha` while preserving child animation alpha.
- [x] 4.3 Re-run strict validation, unit tests, focused emulator instrumentation, and the debug build.
- [x] 4.4 Install-only the corrected APK on the connected USB phone and record the repeated manual cross-app tap check.

Repeated manual verification remaining: tap beside the visible bubble and confirm the underlying app receives the tap without Android showing the Verbally touch-compatibility warning.
