# Canvas Studio — combined validation

**- made by SuprixZ**

This document covers **Canvas Studio Lite** and **Canvas Studio**. It separates completed checks from checks still needed and provides a validation process for future releases. A project description or broad dependency range is not proof that every game or loader version works.

## Release evidence

| Edition and release | Evidence | Scope |
| --- | --- | --- |
| Lite 1.0.3 | Compilation and automated painting, image, packet, recipe and input checks passed | Minecraft 26.3 / NeoForge 26.3.0.51-beta / JDK 25 |
| Canvas Studio 2.0.0 | Compilation, shared editor checks, item matching and exchange validation checks passed | Same baseline; includes full-edition checks |
| Lite 1.0.4 | Version-range audit and NeoForge binary API signature audit passed; gameplay bytes unchanged from 1.0.3 | 56 published NeoForge 26.3 builds through 26.3.0.57-beta |
| Canvas Studio 2.0.1 | Same compatibility audits passed; gameplay bytes unchanged from 2.0.0 | Same 56-build audit |

The compatibility audit was recorded on **8 October 2026**. The 56 published builds run from 26.3.0.0-beta through 26.3.0.57-beta, with gaps in the published sequence. See `verification/compatibility-audit.json` on either branch for the actual version list, artifact hashes and per-build results. See `verification/range-audit.json` for range tests.

The 1.0.4 and 2.0.1 JARs were compared with their predecessors: every ZIP entry except `META-INF/neoforge.mods.toml` has identical bytes. No fresh compilation or interactive gameplay matrix was performed for this metadata-only update. Original source snapshots and release notes remain available in earlier tags.

## Completed shared automated checks

- PNG/JPEG decoding, fit and center-crop resizing, white transparency flattening and map-palette quantization to a 16,384-byte image.
- Rejection of unsupported/corrupt images, files above 16 MiB and image dimensions above 8,192 pixels.
- Save-painting packet roundtrips, including painting title and hand; truncated and incorrect-length payload rejection.
- Actual editor input handlers in a headless client shell at scales 1, 2 and 3: painting/dragging, erasing/dragging, palette selection, fill/erase and middle-click rejection.
- Minecraft 26.3 recipe and advancement codec checks, including rejection of the former advancement format. Standalone recipe fixtures use vanilla stand-in item IDs; this does not test runtime registration.
- Resource JSON syntax, map-palette channel order, archive resources, mod metadata and SuprixZ credit.

The input regression check failed against Lite 1.0.2 and passed against 1.0.3 after the mouse-button correction. The headless input fixture uses Unsafe only in verification code; fixtures are not packaged in the mod JAR.

## Completed full-edition automated checks

- Draw-item packet roundtrips and invalid/truncated payload rejection.
- Synthetic shape matching for sword/apple silhouettes, translated/scaled/rotated drawings, closed black outlines, empty drawings, distinct item IDs and bounded scores.
- A catalogue of 1,198 supported vanilla inventory item/block candidates. Nine sampled item sprites each appeared in their own top three matches: diamond, diamond sword, apple, stick, bread, iron pickaxe, golden apple, emerald and bow. Sprite roundtrips do not measure freehand recognition accuracy.
- Registry validation accepts supported vanilla items and rejects air, nonexistent or malformed IDs, other namespaces and null.
- Drawing validation rejects empty canvases, invalid palette IDs and incorrect bitmap lengths.

The client selects a visual match. The server validates the vanilla item ID, held canvas and drawing data before the one-for-one exchange. It does not repeat visual recognition using the client's resource pack. Actual reward execution still needs an in-game test.

## NeoForge compatibility audit

Current compatibility releases use NeoForge range `[26.3-alpha,26.4-alpha)` and Minecraft range `[26.3]`. Range tests accept the 26.3 beta series and reject 26.4, including prereleases, as well as earlier series.

The binary audit resolves 12 referenced NeoForge classes and 18 method/field references per edition against each published universal JAR and its declared FML/event-bus dependencies. Inherited Minecraft interface methods are resolved against the vanilla Minecraft 26.3 baseline. All audited builds declare NeoForm 26.3-1.

This checks class and member signature availability. It does not validate every Minecraft patch difference, bootstrap behavior, native library, mod interaction or gameplay path. Future 26.3 builds are accepted but remain unverified until checked. Other Minecraft versions and NeoForge series need compatible code, dependencies and new evidence before support is claimed.

## In-game acceptance still required

No interactive Minecraft client or dedicated-server gameplay session was used for the recorded checks. Windows/macOS dialogs, resource-pack reloads, modpack interactions and save/reload persistence remain unverified.

For **both editions**:

1. Confirm the mod is listed and a new world finishes preparing; repeat with an existing world.
2. Craft Quill and Blank Canvas, then open a canvas while aiming into the air.
3. Test color selection, brush sizes, left-click/drag, right-click/drag, fill, undo and clear at different GUI scales.
4. Import PNG/JPEG and transparent PNG; test fit/crop, export, reopen and reimport.
5. Save a painting; confirm one canvas becomes one named map and repeated clicks do not duplicate it.
6. Check artwork in normal/glowing item frames, leave and reload the world, and verify persistence.
7. Repeat with an off-hand canvas and another dimension.
8. Join a dedicated server with a second client using the same edition/release; verify both see the artwork.

For **Canvas Studio full edition**, additionally:

1. Draw a sword and a block; inspect the suggested result cards and alternatives.
2. Choose a result while that item is absent from the inventory. Confirm one canvas becomes exactly one new selected item, in either hand.
3. Confirm repeated clicks cannot duplicate rewards, and invalid requests do not consume the canvas or grant an item.
4. Return to drawing without spending a canvas; verify Save Painting still works.
5. Repeat the exchange on a dedicated server and after a resource-pack reload.

## Process for future releases

1. Record the exact mod release, edition, Minecraft/NeoForge versions, Java version, operating system and relevant resource packs/mods.
2. Compile each edition against the intended supported API; run `./gradlew build` with its required JDK. Current sources also accept `-PneoForgeVersion=<26.3 build>`.
3. Repeat the dependency-range and binary API audits for every published loader build in the claimed range. Rebuild or adapt code when references change.
4. Run the in-game acceptance checks on the oldest and newest supported builds and on intermediate builds with relevant changes, including a dedicated server.
5. Inspect both release JARs, keep verification fixtures out, record checksums and update this evidence table with actual results and limitations.
6. On Modrinth, select the game versions and loader verified for each uploaded file. Do not mark unrelated game versions as supported just because this project overview is reusable.

Keep this document as a living record. Mark new checks as pending until they pass; a future release must supply its own results rather than inherit an unqualified compatibility claim.

## Build-environment note

Earlier compilation required a sandbox-only JDK-path fallback in NeoForm Runtime's process-command lookup. It changed build tooling only and is absent from the source project and mod JARs. No Minecraft EULA was accepted and no world was started in that environment.
