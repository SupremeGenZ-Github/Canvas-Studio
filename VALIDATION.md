# Canvas Studio Lite 1.0.4 compatibility update

This update changes mod version and dependency metadata only. The compiled class files and other gameplay resources are byte-identical to 1.0.3; the earlier checks below remain relevant but were not rerun on every NeoForge version. See COMPATIBILITY.md for the new version-range and binary linkage audit. No new interactive Minecraft world or multiplayer tests were performed.

# Release validation

Target: Minecraft Java Edition 26.3, NeoForge 26.3.0.51-beta, JDK 25.

## 1.0.3 input regression check

The actual CanvasScreen handlers were exercised in a headless client shell with Minecraft 26.3's InputConstants. The test failed against the previous 1.0.2 editor because left-click erased instead of drawing. It passes against 1.0.3 at scales 1, 2 and 3: left-click drawing and continuous dragging, right-click erasing and dragging, palette selection, middle-click rejection, and flood fill/erase. The test shell uses Unsafe only in verification code; it is not part of the mod JAR. This is not an interactive desktop test.

## 1.0.2 advancement regression check

Minecraft 26.3's actual Advancement.CODEC accepts all three corrected advancement JSON files and rejects copies with the old singular recipe condition. Each recipe also parses through Recipe.DIRECT_CODEC with vanilla stand-in item IDs, since standalone bootstrap freezes the item registry. This verifies data format, not runtime registration or interactive world loading.

## Passed for 1.0.3

- Compiled the complete mod against the exact requested NeoForge beta and its patched Minecraft 26.3 API.
- Gradle `build` including `verifyCanvas`, `verifyRecipeData` and `verifyCanvasInput`.
- PNG and JPEG decoding; fit and centre-crop resizing; transparency flattened onto white.
- Rejection of GIF, corrupt files, files above 16 MiB, and image dimensions above 8192 pixels.
- Quantization to a fixed 16384-byte image.
- Save-packet encode/decode roundtrip including hand and painting title.
- Truncated and incorrect-length payload rejection.
- Actual Minecraft 26.3 map palette alpha and ARGB channel order.
- Recipe/model/language JSON syntax, metadata and archive contents.
- Finished JAR requires exactly NeoForge 26.3.0.51-beta; mod author and editor/description credit SuprixZ.
- Compiled SDL 3.4.3 file dialog integration; old TinyFD references removed. Actual desktop dialog interaction remains unverified.

## Partial / not verified

- This upgraded release was not launched in a Minecraft world or dedicated server.
- No interactive Minecraft client was available. Mouse editing, the new SDL file picker appearance, item-frame display, multiplayer synchronization, and save/reload persistence need an in-game check before treating this as a production release.
- Windows/macOS native dialogs and modpack compatibility have not been exercised.

## Build environment notes

The isolated build environment hides the process command from Java's ProcessHandle API. NeoForm Runtime's build-time process lookup was locally given a JDK-path fallback to complete dependency generation. This workaround touched build tooling only; it is not present in the delivered mod or source project. Minecraft's EULA was not accepted and no world was started.

## In-game acceptance checklist

1. Craft Quill from Feather + Ink Sac; craft Canvas from Leather + Paper + Quill.
2. Open Canvas while aiming into the air; test brush, eraser, fill, undo and clear.
3. Import PNG and JPEG from a local path; test fit/crop and a transparent PNG.
4. Export; close/reopen; reimport the export into a second canvas.
5. Save; confirm one canvas becomes one named map and no duplicate appears.
6. Place it in a normal and glowing item frame; confirm colors match the preview.
7. Exit and reload the world; verify art remains.
8. Join a dedicated server with a second modded client; verify both see the artwork.
9. Repeat with the canvas in the off-hand, in another dimension and with GUI Scale adjusted.
