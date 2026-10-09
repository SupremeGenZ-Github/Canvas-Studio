# Canvas Studio+ 2.1.2 — Minecraft 26.2 validation

Target: Minecraft 26.2, NeoForge 26.2.0.88, Java 25, FML 11. Credit: **made by SuprixZ**. Baseline: 26.3 release 2.1.2, commit `b508f28a3bd8d4bf685db92f5cad943681bc1bd7`.

This is a separately compiled port, with 26.2-specific build dependencies and Minecraft validation assets. Release publication is gated on a clean Gradle build and packaged artifact checks. The release copy of this report includes the actual successful run URL and JAR checksum.

## Automated verification

Uses 26.2 Recipe.CODEC and singular `recipe` advancement conditions, checked against actual 26.2 vanilla data. Eight Java suites cover real 26.2 recipe/advancement codecs, exact quill layouts and shapeless progression, painting input including colors/drag/fill at three GUI scales, image processing, packet validation, real vanilla item texture matching, server authorization, single-use Molten/infinite Infinity rewards through test inventory adapters, and texture size/transparency. Python artifact checks inspect version, exact target metadata, recipes, credits, unchanged matcher/image-processing source and texture hashes. Historical 26.3 logs/audits do not count as port results.

The port removes all SDL references and uses TinyFD 3.4.1 supplied by Minecraft 26.2. Native picker cancellation and exceptions retain the canvas, while selection/decoding run off the game thread. Native picker operation is source-reviewed; an interactive dialog has not been tested here.

Headless crafting uses vanilla stand-in IDs for mod items. Inventory delivery uses production logic through simulated inventory/drop adapters. No actual ServerPlayer, GPU window or loaded world is tested by these suites.

## In-game tests

**None performed.** A live 26.2 client/world/server, native picker, multiplayer, full-inventory drop and old-world migration must still be checked in-game.

Suggested checklist: install only this port; craft all six items; select colors/draw with both hands; import/export PNG/JPEG and save maps; verify Blank lock, Molten one successful reward, Infinity repeated rewards without owning the item; check cancellation/invalid requests and inventory-full drop.

No claim is made for other Minecraft/NeoForge versions. Existing IDs/map formats are preserved, but downgrading a 26.3 world to 26.2 is not validated.
