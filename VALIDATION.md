# Canvas Studio+ 2.1.2 validation

Date: 2026-10-09. Baseline: published Canvas Studio+ 2.1.1, commit `a1427487146614fb4ec33b5125789d503f9f50f6`.
**made by SuprixZ**. Minecraft Java 26.3, Java 25, NeoForge 26.3.x including betas; build target 26.3.0.51-beta.

## Automated verification

The release workflow runs a clean `./gradlew build --no-daemon` on GitHub Actions with JDK 25, followed by `python3 verification/ArtifactChecks.py`. Publication is blocked if either fails. The published validation report includes the run link, build result and JAR SHA-256; logs are included as release assets and in the source ZIP.

Local Java networking cannot reach the dependency repository in this session, so the release uses the GitHub build runner. Historical 2.1.0 logs/audits remain separately labelled and are not new 2.1.2 results.

| Check | Scope |
| --- | --- |
| Crafting | Real Minecraft 26.3 codecs decode all six recipes. Shapeless placement and missing-ingredient checks. Upgraded quills require all nine slots, the exact material in each slot, and the centered correct quill. Former iron-nugget quill and leather/paper upgraded canvases rejected. |
| Exact recipe data | Packaged Molten Quill: `IGI/GQG/IGI`, iron ingots/corners and gold ingots/edge centers. Infinity: `EDE/LQL/EDE`, echo shards/corners, diamond blocks/top-bottom, lapis blocks/left-right, Molten Quill/center. Packaged canvas upgrade ingredients, output IDs and count one are asserted independently. |
| Unlocks | All six advancement codecs, recipe rewards and current plural `recipes` schema checked. Infinity Quill unlock now uses Molten Quill; canvas upgrades unlock from their previous canvas tier. Feather shortcut absent. |
| Names and old items | Molten display names checked in packaged language data; original `molder_canvas` and `molder_quill` IDs retained. Source references use Molten. Existing registry IDs and saved-map format remain unchanged. Live save migration not run. |
| Textures | All six item/model/texture paths exist and have distinct texture hashes. Blank Canvas, normal Quill and Infinity Canvas match their original hashes; Molten Canvas, Molten Quill and Infinity Quill textures differ. Dedicated checks enforce 32×32 size, hard transparent/opaque pixels and fully opaque blank paper with no center hole. Final sprites visually inspected before publishing. |
| Painting | Actual headless editor input handlers: drawing/dragging, erasing, palette selection, fill at GUI scales 1, 2, 3. Image decode, fit/crop, quantization, transparency and invalid-image checks. |
| Item matching | Unchanged importer/matcher/catalogue hashes; shape/color/orientation checks and representative actual vanilla catalogue matches. |
| Exchange | Production authorization and reward delivery with test adapters: Blank locked, Molten single success, Infinity repeated reuse, both hands, newly created count-one rewards without ownership, full-inventory drop, duplicate/replay, wrong hand/held identity/tier/count/token, expiry and invalid drawings/IDs. |
| Metadata | Version, credit, Java 25 classes and Minecraft/NeoForge dependency ranges asserted in packaged JAR. |

Headless crafting uses distinct vanilla stand-in IDs for mod items and minimal item components. Inventory delivery uses the production logic with a simulated inventory/drop adapter. These do not execute a live ServerPlayer or loaded world.

## Compatibility

NeoForge `[26.3-alpha,26.4-alpha)` and Minecraft `[26.3]` are unchanged. The 2.1.0 binary audit covered 56 published NeoForge builds through 26.3.0.57-beta; it is historical evidence, not a fresh 2.1.2 audit. The 2.1.2 update changes only textures, version metadata, verification and documentation; all production Java gameplay source and recipe data are byte-identical to 2.1.1, with no new external API dependencies. Future 26.3 versions are accepted by metadata; future breaking API changes cannot be guaranteed compatible.

## In-game tests

**None performed for 2.1.2.** No interactive client, world, multiplayer, dedicated-server startup, native file picker or real save migration was run.

Before claiming gameplay verification:

1. Craft all six items, check exact quill slot placement and absence of old upgrade recipes.
2. Verify recipe-book unlocks, creative entries, Molten names and new Molten/Infinity textures.
3. Open each canvas in both hands; draw, select colors, import/export and save a map.
4. Confirm Blank lock, Molten one-reward consumption and Infinity repeated one-reward reuse without already owning the reward.
5. Check cancellation, failed/invalid drawings, inventory-full drops and duplicate requests.
6. Load a 2.1.0 save: confirm existing Molder items display Molten and old paintings remain usable.

Save Painting still converts any canvas into a locked map; Infinity's unlimited reuse applies to Get Item. Install only one Canvas Studio JAR, with matching versions on client and server.
