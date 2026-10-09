# Canvas Studio+ 2.1.3 — NeoForge 26.2.x validation

**made by SuprixZ**. Minecraft 26.2 / Java 25. Baseline: the exact-version 26.2 port, commit `9aa56ecc4b20edd3a80b4fee174dddd4b8b0ebbc`.

## Release gates

1. Clean build and all nine Java checks against the earliest published beta, `26.2.0.0-beta`, and current latest stable, `26.2.0.89`, in separate CI jobs.
2. Clean build and all checks against the development target `26.2.0.88`, plus packaged artifact checks.
3. Download official Maven metadata and audit the compiled mod's referenced NeoForge/FML/bus classes and method/field signatures against every published 26.2 build.
4. Validate Maven dependency-range semantics against synthetic boundary cases and every published NeoForge/loader pair found by the audit.

Publication is blocked by any failed gate. The published copy of this report includes the completed run URL, JAR hash and exact audited version count. Machine-readable audit, range-check output and build logs are attached and included in the source ZIP.

## Test scope

Nine Java suites cover recipe/advancement codecs and exact recipes, painting input (drawing/colors/erase/fill at three GUI scales), image decode/resize/quantization, packet codecs, real 26.2 vanilla item matching, server permissions, Molten consumption and repeated Infinity exchanges through test inventory adapters, texture dimensions/alpha/opaque paper, and version-range boundaries. Python artifact checks verify the packaged resources, metadata, credit and preserved source hashes.

Binary checks establish referenced API availability, not full behavioral equivalence. The NeoForm versions used by each audited build are recorded. Builds/checks against the earliest beta, 26.2.0.88 and latest stable verify compilation and headless behavior on those endpoints; they are not live Minecraft launches. Native TinyFD picker operation remains source-reviewed only.

## Compatibility boundaries

NeoForge range: `[26.2-alpha,26.3-alpha)`. FML: `[11,)`. Minecraft: `[26.2]`. Current and future NeoForge 26.2 builds, including betas, are accepted by metadata. Future API-breaking changes cannot be guaranteed compatible. Other Minecraft families remain unsupported.

Production gameplay code, recipe ingredients and textures are unchanged from the working 26.2 port. Existing item IDs and saved-map formats are preserved. Downgrading a 26.3 world is not tested.

## In-game tests

**None performed.** No interactive client/world/server, native picker, multiplayer or live save migration was tested. Recipe tests use vanilla stand-ins for mod items. Reward delivery tests use production logic with a simulated inventory/drop adapter.

In-game checklist: craft all six items; open both hands; draw/select colors/import/export/save; verify Blank lock, one Molten reward, Infinity reuse and inventory-full drops; cancel/invalid/duplicate requests; check existing paintings in a 26.2 save and the intended modpack.
