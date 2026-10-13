# Canvas Studio project format

`.csp` is bounded, uncompressed binary data. Integers use Java DataInput/DataOutput big-endian encoding; strings use Java modified UTF-8 (`writeUTF`), then are validated to at most 64 Java characters without control characters or formatting §. Version-4 files are bounded to 8 MiB; older layouts remain readable. No embedded paths, scripts, capabilities or server item data are accepted.

Version 2 layout: magic int `0x43535031`; format-version int `2`; width int `128`; height int `128`; title UTF; active-layer int; brush int (1–16); snap int (1–32); grid boolean; mirror-X boolean; mirror-Y boolean; tool-name UTF; selected map-color unsigned byte (4–247); layer count int (1–16). For each bottom-to-top layer: name UTF, visible boolean, exactly 16,384 pixel bytes. Layer pixels are 0 (transparent) or 4–247 (Minecraft map palette). Finally: original-image-present boolean and, when present, exactly 16,384 ARGB ints containing the fitted source image. Trailing data is rejected.

Version 1 migration: the same magic, version `1`, width, height and title, followed by one fixed 16,384-byte pixel plane. It becomes a single Background layer with default settings. This is a supported schema migration fixture, not a claim that older public 2.x mod releases produced `.csp` files. Unknown newer versions are rejected without overwriting the file.

History is in-memory only. Zoom/pan and preview wall arrangements are transient UI state. The original import is stored as a fitted 128 × 128 image; full-resolution source files are not embedded. Palette values are Minecraft map-palette indexes; each Minecraft edition must preserve that mapping for appearance to remain identical.

Managed filenames are UUIDs, independent of project titles. Writes use a temporary file and atomic replacement where supported. Project and export directory symlinks and project-file symlinks are rejected. Imports are bounded and validated before creating a new managed project. The fixed recovery UUID is `00000000-0000-0000-0000-000000000001`.

## Version 3 (3.1.2)

Version 3 retains the complete version-2 layout and appends two unsigned bytes: wall width, then wall height. Both must be 1–8. Artwork pixels remain 128×128 regardless of wall display size. Version-1 and version-2 files load as 1×1. Invalid or truncated dimensions are rejected.

## Version 4 (3.2.0)

After the complete version-3 payload, version 4 appends frame-style byte (0–4), layout byte (0–2), frame-delay byte (10–200 ticks), locked boolean, timeline-count byte (1–16) and selected-frame byte (0–count-1).

Each timeline frame stores layer-count byte (1–16), active-layer byte, then each layer's name UTF, visibility boolean and 16,384 palette bytes. This is followed by original-image-present boolean and, when present, exactly 16,384 fitted-source ARGB ints. The selected frame supplies the editor's active layers. Layer names retain the 64-character validation.

Timeline frames preserve their own layers and fitted reference images. The earlier top-level layer fields retain the pre-animation layout; old project versions become one static frame. Version-3 width/height are retained, while versions 1/2 default to 1×1. Network/gallery transfers send flattened palette frames instead of CSP files.

Server-side raw source and gallery files use the separate bounded CSA1 format: magic int 0x43534131, title UTF, width byte, height byte, frame-style byte, layout byte, delay byte, locked boolean, frame-count byte and one 16,384-byte palette plane per frame. The file limit is 16×16,384 + 1,024 bytes. Gallery identity/ownership metadata lives in a world-local managed index; saved-map ownership and native animation map IDs use registered item components.
