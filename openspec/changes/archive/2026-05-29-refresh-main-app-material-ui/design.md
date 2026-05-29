## Context

Verbally already uses a Compose Material 3 shell with a top bar, five bottom navigation destinations, a hamburger drawer, local settings, and light/dark app appearance modes. Emulator review showed that the structure is sound, but some visual details weaken the experience: drawer copy makes `設定` sound like another API settings area, history shows a large destructive action even when empty, and dark mode can leave system bar icons hard to read. The user approved a restrained refresh that keeps the deep blue brand while adding a small supporting palette.

## Goals / Non-Goals

**Goals:**

- Keep deep blue as the brand anchor and add controlled teal/mint and lavender accents through Material color roles.
- Keep Home as the API setup destination and keep drawer Settings focused on appearance/app preferences.
- Add a drawer support entry for permission setup and troubleshooting.
- Make dark and light mode readable in app content and system bars.
- Reduce empty-history visual noise and align destructive actions with their actual availability.

**Non-Goals:**

- No new provider, backend, account, sync, or data model behavior.
- No change to bottom navigation destinations or order.
- No move of API setup out of Home.
- No archive of this change unless the user explicitly asks.

## Decisions

1. **Retain the brand blue, rebalance the palette.**
   Deep blue remains `primary` for identity and highest-emphasis actions. Teal/mint supports success/help/permission states, and lavender supports AI/style-related accents. This avoids turning the app into a one-hue interface while staying quiet enough for a utility app.

2. **Use drawer as low-frequency management, not primary navigation.**
   Bottom navigation remains the product navigation. The drawer shows a concise app identity area plus `設定` and `權限與疑難排解`. `設定` copy avoids API wording because Home owns API setup.

3. **Make permissions reachable without adding a bottom tab.**
   The existing `onOpenPermissions` path is reused from the drawer support item. This keeps permissions available for support without adding new app state or a new screen.

4. **Treat empty history differently from populated history.**
   The clear-history action is hidden when no entries exist. When entries exist, the action remains available but uses destructive outline emphasis instead of a visually dominant full-width red CTA.

5. **Synchronize system bar icon appearance with the selected app theme.**
   The theme composable reports whether dark content is active so the activity can set status/navigation bar icon contrast. This directly fixes the emulator-observed dark-mode readability issue.

## Risks / Trade-offs

- **Risk: Drawer looks underfilled with only two entries.** → Mitigation: make it intentionally compact and structured with short supporting copy, leaving room for future support/about entries without fake features.
- **Risk: Added accent colors feel decorative.** → Mitigation: only use accents for semantic roles and selected/support surfaces, not broad decorative backgrounds.
- **Risk: System bar APIs vary across Android versions.** → Mitigation: use AndroidX core window inset controller APIs already available through `core-ktx`, and verify on the Android 16 emulator target.
