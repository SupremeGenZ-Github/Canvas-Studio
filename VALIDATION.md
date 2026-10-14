# Canvas Studio+ 3.2.1 validation — Minecraft 26.2

Baseline: Living Gallery v3.2.0, main commit 3b29ab9421ed97585f8e49a2c99671003fcadccd. Credit: **made by SuprixZ**.

## Release gates
- JDK 25 Gradle build/check, all 16 Java verification suites and packaged JAR checks.
- Default and tested endpoint NeoForge dependency builds.
- Referenced class/member API audit and dependency-range checks across published family builds.
- A fresh headless dedicated server must reach readiness, accept an authenticated local stop command, save its world and exit cleanly.
- Release assets include source, logs, exact audit data, the executed result and SHA-256 checksums.

## Automated scope
The original 13 suites remain. GalleryChecks exercises actual production ownership/lock policy, UUID codec errors, bound session token/hand/map/revision/expiry checks, bounded sequential frame assembly and duplicate/out-of-order/expired handling, packet limits/codecs, native map-ID animation clocks, all size/layout combinations, independently editable frame layers, undo deletion, CSP1–4 migration, raw source/gallery filesystem persistence, owner/admin deletion, artist quotas and symlink target preservation. It decodes/converts all 51 actual Minecraft painting textures.

WallGeometryChecks calls the production border/ghost code with captured vertices across all 64 sizes and four wall directions. It checks extents, normal depth, alpha and the bounded 32×32 preview raster.

The headless server smoke proves mod/registry loading and fresh-world startup plus clean save/stop. The storage tests reload studio data from disk. These are distinct from placing and editing an actual entity in a running world.

## Not performed
No interactive Minecraft client rendering, live player interactions, multiplayer gameplay, real painted-entity save/reload, input picker operation or player sleep was tested. Remaining manual checks include ghost transparency/orientation/culling at different GUI scales, all frame styles and layout modes, owner/non-owner/admin interactions, wall resize collisions, two viewers seeing animation, pickup/replacement retaining all frames, importing resource-pack and placed vanilla paintings, gallery workflows and full world backup/restore.

An API audit establishes referenced binary-member availability, not complete behavioral compatibility or a guarantee for future NeoForge releases. The release-attached report records actual execution results.

## Material-frame verification
FrameMaterialChecks calls the same payment transaction used by StudioService with real vanilla ItemStacks and a headless container. It covers every material, planks tag membership, all 144 old/new style transitions, hotbar/main/off-hand slots, exact one-item spending, missing/wrong resources, simulated persistence failure, free same-frame edits/removal, legacy colors/IDs and wire/persistent/CSP/gallery roundtrips. The requirement also applies in Creative mode and to gallery/project imports when saved as a new painting. Spending happens after the successful save callback on the main server thread.

Interactive frame appearance, live player inventory updates, Creative-mode interactions and actual gallery-to-wall exchanges remain manual checks; the headless payment adapter does not emulate a connected player.
