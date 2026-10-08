# Canvas Studio+ 2.1.0 validation

Date: **2026-10-08**. Baseline: full Canvas Studio 2.0.1, commit `b167119e9ab456e21e993a166a63e0a1e94eff40`.
Credit: **made by SuprixZ**. Target: Minecraft Java **26.3**, NeoForge **26.3.x**, including beta builds; JDK **25**.

## Build result

**PASS** — changed Java sources compiled, resources packaged, and all Gradle checks completed. Built against NeoForge **26.3.0.51-beta** with Temurin **25.0.4.1**. JAR: `canvas-studio-plus-26.3-2.1.0.jar`.

SHA-256: `d0d485fe8163076e9c82456760f72b5c5e9e40481ab2ad41e57efe519c12eefa`

This container prevents NeoForm Runtime from discovering its Java executable through `ProcessHandle`. The build reused an existing, ZIP-verified Minecraft 26.3 / NeoForge 26.3.0.51-beta merged development artifact, plus the existing vanilla Minecraft 26.3 validation JAR. The mod itself was compiled from the updated source; it is not a metadata-only repack. No build-tool patch or Minecraft library is included in the mod.

Executed Gradle command, using the existing development artifacts:

```sh
gradle build -x createMinecraftArtifacts -x prepareVanillaValidation
```

On a normal development machine, use JDK 25 and `./gradlew build`; it generates/downloads these dependencies. Source validation scripts are included. Build log: `verification/build-2.1.log`.

## Automated checks actually performed

| Area | Result and scope |
| --- | --- |
| Crafting | PASS: Minecraft 26.3 recipe codecs decode all six recipes. All shapeless recipes match multiple grid placements; upgraded quills require all nine slots and a centered normal Quill. Outputs have count one. |
| Advancements | PASS: all six unlock advancements decode with the current game codec; old singular `recipe` schema is rejected. Feather shortcut recipe and advancement are absent. |
| Item assets | PASS: packaged names, item/model/texture paths, six distinct textures, output IDs/counts, recipe ingredients, credit, version, icon, Java 25 class target and loader range. Textures also visually inspected. |
| Painting input | PASS: actual editor mouse handlers draw/drag, erase/drag, select palette colors, reject middle-click painting, and fill/erase at GUI scales 1, 2 and 3. |
| Image handling | PASS: PNG/JPEG decode, fit/crop, transparency flattening, palette quantization, and GIF/corrupt/oversized-image rejection. |
| Saving packets | PASS: Save Painting payload roundtrip; truncated and wrong-size payload rejection; actual Minecraft map palette ARGB. Server map-saving path reviewed for all three tiers; saved-map format unchanged. |
| Blank lock | PASS: production server authorization rejects Blank Canvas. Editor mode button is disabled and has the explanatory tooltip/footer; GUI configuration reviewed in source. |
| Molder | PASS: production authorization allows one valid exchange, consumes its capability, and rejects duplicates. Production reward delivery replaces only the selected held hand in a headless inventory adapter. |
| Infinity | PASS: 1,000 authorized requests per hand with token rotation and immediate/old replay rejection. Production delivery repeatedly retains the canvas and supplies one item/block per request in a headless inventory adapter. |
| Reward ownership | PASS: production reward creation makes distinct plain count-one ItemStacks; delivery succeeds with no pre-existing reward item. |
| Full inventory | PASS: production delivery chooses the drop adapter when insertion fails; a rejected drop reports failure and retains Infinity Canvas. This uses a simulated inventory/drop adapter, not a live world. |
| Invalid requests | PASS: invalid item, drawing size/palette/blank drawing, stack count, tier, held-stack identity, hand, token, missing/expired session and duplicate/replay checks. Rejected authorization preserves the capability. Cancellation/failed matching send no exchange packet; opening alone does not consume. |
| Network | PASS: both hand session requests, all four reply types and drawing reward payload codecs roundtrip. Handlers explicitly run on the main thread; rewards wait for acknowledgement and repeated pending clicks are ignored (source review). |
| Matcher | PASS: unchanged importer, matcher and catalogue hashes against the baseline. Synthetic shape/color/outline/rotation/scale checks; 1,198 actual vanilla inventory IDs and 2,394 references; nine representative items appear in top-three matches. Invalid/non-vanilla/air reward IDs rejected by production validation. |
| Old items/paintings | Existing `canvasstudio:canvas` ID and Minecraft locked-map saving format preserved in source. Old Blank items paint with Get Item locked under the shared tier check. Live save migration not run. |

Headless crafting uses vanilla stand-in IDs for mod items because standalone registry bootstrap cannot register mods. Minimal stand-in item components are bound for crafting. Inventory delivery uses the same production creation/delivery code with a simulated inventory adapter. These checks do **not** execute a live ServerPlayer or a loaded world.

## NeoForge compatibility

**PASS binary API audit:** all **56** published NeoForge 26.3 builds discovered in official Maven metadata, **26.3.0.0-beta through 26.3.0.57-beta** (numbers 15 and 32 are not published). Every checked build supplies the mod's **13 referenced NeoForge classes and 21 method/field signatures**; all use NeoForm `26.3-1`.

The machine-readable audit, including artifact hashes and the checked mod hash, is `verification/neoforge-2.1-audit.json`. Reproduce with:

```sh
python verification/ArtifactChecks.py
python verification/audit_neoforge.py build/libs/canvas-studio-plus-26.3-2.1.0.jar
```

Dependency metadata is `[26.3-alpha,26.4-alpha)` for NeoForge and `[26.3]` for Minecraft. Future 26.3 builds are accepted, but future API changes cannot be guaranteed. Binary compatibility is not a launch/gameplay test. Lite 1.0.4 is unchanged; this report validates the new full edition only. Historical Lite/full audit files remain for reference.

## In-game tests actually performed

**None.** No interactive Minecraft client, world, multiplayer session, dedicated server startup, native file-picker session or real world save/load was run for this release.

Before publishing a release described as gameplay-tested, verify in a real Minecraft 26.3 instance:

1. Craft all six items, inspect creative entries/tooltips, and confirm Feather + Leather + Paper yields nothing.
2. Open each canvas in both hands; draw/select colors, import/export, and save a map in an item frame.
3. Confirm Blank Get Item is locked, including an old Blank Canvas from an existing save.
4. With no selected reward item already owned, exchange Molder once; confirm exactly one reward and canvas consumption.
5. Reuse Infinity repeatedly; confirm one reward per click, canvas retained, and full-inventory drops.
6. Cancel/back out, try blank/invalid drawings, switch/drop the held canvas, and send duplicate requests; confirm no unintended reward or consumption.
7. Save/reload existing paintings and a new painting; test client/server matching version and multiplayer synchronization.
8. Repeat startup/gameplay on desired early and later NeoForge builds and with the intended modpack.

Saving Painting converts **any** canvas into a locked map, preserving existing behavior. Infinity's unlimited reuse applies to Get Item exchanges. Install one Canvas Studio edition at a time, with the same 2.1.0 release on client/server.
