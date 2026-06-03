## 1. Contracts and Tests

- [x] 1.1 Add focused tests for overlay visibility state and touchability flag behavior.
- [x] 1.2 Update visual default tests for Wispr-sized active controls, rounded idle bubble dimensions, and brand-color preservation.
- [x] 1.3 Update transition policy tests so ready, recording, processing, and repair state changes refresh the retained root instead of rebuilding windows.
- [x] 1.4 Add or update waveform rendering tests around Wispr-like mark count, wider bar weight, capsule inset, and restrained amplitude math where feasible.

## 2. Overlay Root and Animation Behavior

- [x] 2.1 Keep the overlay root attached for normal hide/show and make hidden overlays non-touchable.
- [x] 2.2 Replace remove/re-add state transitions with retained-root content updates and animated `updateViewLayout` resizing.
- [x] 2.3 Preserve right-edge anchoring while the active control row expands and collapses.

## 3. Visual Refinement

- [x] 3.1 Restyle the idle bubble as a Verbally-branded translucent rounded-square floating button.
- [x] 3.2 Size the recording row to the observed Wispr Flow proportions while keeping Verbally colors.
- [x] 3.3 Refine the recording waveform to use fewer, wider, smoother Wispr-like marks.
- [x] 3.4 Keep processing and repair states compatible with the retained-root layout.

## 4. Validation

- [x] 4.1 Run targeted unit tests for overlay behavior and waveform/defaults.
- [x] 4.2 Run `openspec validate --all --strict`.
- [x] 4.3 Run `./gradlew testDebugUnitTest`.
- [x] 4.4 Run `./gradlew assembleDebug`.
- [x] 4.5 Run `git diff --check`.
- [x] 4.6 Install the latest debug APK to the connected approved device and visually verify the overlay behavior.

## 5. Wispr Motion Black-Box Analysis

- [x] 5.1 Capture clean Wispr Flow expand and collapse references without Verbally overlay interference.
- [x] 5.2 Extract window count, window geometry, child surface geometry, opacity, and timing from the references.
- [x] 5.3 Write a short local analysis report summarizing Wispr Flow's observable motion model.
- [x] 5.4 Apply only the measured motion model to Verbally, keeping the dictation pipeline intact.
- [x] 5.5 Rebuild, install-only to the approved connected device, and visually verify expand/collapse against the Wispr reference.

## 6. Jitter Follow-Up

- [x] 6.1 Restore tap haptics for bubble actions.
- [x] 6.2 Capture Verbally frame evidence showing per-frame overlay-window relayouts during expand/collapse.
- [x] 6.3 Replace per-frame overlay-window resizing with a single right-edge geometry update to avoid visual position jitter.
- [x] 6.4 Rebuild, install-only to the connected device, and verify the jitter fix.

## 7. Right-Edge Reveal Follow-Up

- [x] 7.1 Add a focused policy test for deferring right-anchored content swaps until after the overlay window reaches its target geometry.
- [x] 7.2 Defer right-edge active/ready content insertion until after the single geometry update so controls do not render from the screen edge.
- [x] 7.3 Rebuild, install-only to the connected device, and visually verify right-edge expand/collapse.

## 8. Right-Edge Morph Follow-Up

- [x] 8.1 Keep the previous right-edge content visible during the deferred geometry update so the old bubble/control row carries the transition instead of showing an empty frame.
- [x] 8.2 Add a right-edge active reveal that keeps the trailing confirm control at the original bubble position while cancel and waveform unfold from that point.
- [x] 8.3 Replace right-edge ready/recording/processing window resizing with a stable active-width motion frame so WindowManager does not animate the overlay surface from the screen edge.
- [x] 8.4 Rebuild, install-only to the connected device, and visually verify the right-edge transition against the Whisper Flow reference.

## 9. Symmetric Edge Morph Follow-Up

- [x] 9.1 Use the same stable active-width motion frame for left-edge ready/recording/processing states.
- [x] 9.2 Align left-edge idle content to the frame start and reveal waveform/confirm from the original left bubble anchor.
- [x] 9.3 Fix drag bounds so the visible bubble, not the wider invisible motion frame, can reach both screen edges.
- [ ] 9.4 Rebuild, install-only to the connected device, and verify left/right drag plus left-edge expand/collapse.
