## Context

The previous refresh centralized the overlay palette into named resources, which makes this follow-up straightforward: the visual mistake lives in the resource choices, not in the overlay layout or state machine. Fixing the resource contract is enough to restore the floating feel.

## Decisions

### Use dedicated translucent resources for overlay surfaces

Rather than overloading `overlay_surface_primary` and `verbally_brand_blue`, the overlay will point at translucent variants whose names make the intended alpha obvious. This keeps the palette readable and preserves a fully opaque brand blue for any future non-overlay use.

### Keep the existing role mapping and only swap the affected resources

The Kotlin defaults object already gives us a clean seam. We will keep the same semantic roles and update only the resource assignments needed for translucency.
