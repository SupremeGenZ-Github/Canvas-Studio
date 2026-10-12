# Canvas Studio project format

`.csp` is bounded, uncompressed binary data. Integers use Java DataInput/DataOutput big-endian encoding; strings use Java modified UTF-8 (`writeUTF`), then are validated to at most 64 Java characters without control characters or formatting §. Maximum file size is 512 KiB. No embedded paths, scripts, capabilities or server item data are accepted.

Version 2 layout: magic int `0x43535031`; format-version int `2`; width int `128`; height int `128`; title UTF; active-layer int; brush int (1–16); snap int (1–32); grid boolean; mirror-X boolean; mirror-Y boolean; tool-name UTF; selected map-color unsigned byte (4–247); layer count int (1–16). For each bottom-to-top layer: name UTF, visible boolean, exactly 16,384 pixel bytes. Layer pixels are 0 (transparent) or 4–247 (Minecraft map palette). Finally: original-image-present boolean and, when present, exactly 16,384 ARGB ints containing the fitted source image. Trailing data is rejected.

Version 1 migration: the same magic, version `1`, width, height and title, followed by one fixed 16,384-byte pixel plane. It becomes a single Background layer with default settings. This is a supported schema migration fixture, not a claim that older public 2.x mod releases produced `.csp` files. Unknown newer versions are rejected without overwriting the file.

History is in-memory only. Zoom/pan and preview wall arrangements are transient UI state. The original import is stored as a fitted 128 × 128 image; full-resolution source files are not embedded. Palette values are Minecraft map-palette indexes; each Minecraft edition must preserve that mapping for appearance to remain identical.

Managed filenames are UUIDs, independent of project titles. Writes use a temporary file and atomic replacement where supported. Project and export directory symlinks and project-file symlinks are rejected. Imports are bounded and validated before creating a new managed project. The fixed recovery UUID is `00000000-0000-0000-0000-000000000001`.
