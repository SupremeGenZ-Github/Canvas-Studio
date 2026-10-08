# Canvas Studio+

<p align="center"><img src="docs/canvas-studio-icon.png" alt="Canvas Studio icon" width="192"></p>

**made by SuprixZ**

Canvas Studio+ **2.1.0** lets you draw, import PNG/JPEG images, export PNG artwork, save custom paintings, and match drawings to Minecraft items and blocks offline.

## Download 2.1.0

[Release page](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases/tag/v2.1.0) · [JAR](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases/download/v2.1.0/canvas-studio-plus-26.3-2.1.0.jar) · [Source ZIP](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases/download/v2.1.0/Canvas-Studio-Plus-2.1.0-source.zip)

## Requirements and installation

Minecraft Java **26.3**, **Java 25**, and **NeoForge 26.3.x**, including alpha/beta builds accepted by the dependency range `[26.3-alpha,26.4-alpha)`. The development target is **26.3.0.51-beta**. Other Minecraft versions, Forge, Fabric, and NeoForge 26.4+ are unsupported. Future 26.3 builds are accepted by metadata; compatibility with future API changes is not guaranteed. See [VALIDATION.md](VALIDATION.md) for actual checks.

Put the JAR in your instance's `mods` folder. Remove previous Canvas Studio JARs first. Install **one edition only**: Canvas Studio Lite, Canvas Studio, and Canvas Studio+ all use `canvasstudio`. Servers and clients require the same release; 2.1.0 uses network protocol 3 and cannot mix with 2.0.x.

Canvas Studio Lite 1.0.4 remains the painting-only edition on the `lite` branch. This update changes the full edition on `main`; older releases remain available in [GitHub Releases](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases).

## Crafting

| Result | Ingredients | Recipe type | Get Item |
| --- | --- | --- | --- |
| Quill | Feather + Ink Sac | Shapeless | — |
| Blank Canvas | Leather + Paper + Quill | Shapeless | Locked |
| Molder Quill | Quill in center; eight Iron Nuggets surrounding it | Shaped, all nine slots | — |
| Infinity Quill | Quill in center; eight Diamonds surrounding it | Shaped, all nine slots | — |
| Molder Canvas | Leather + Paper + Molder Quill | Shapeless | One successful exchange |
| Infinity Canvas | Leather + Paper + Infinity Quill | Shapeless | Unlimited successful exchanges |

The former Leather + Paper + Feather shortcut has been removed, including its advancement. The normal Quill recipe remains.

## Painting

Use any canvas in either hand to open the editor. Left-click/drag to draw; right-click/drag to erase. Colors, brushes, fill, undo, clear, PNG/JPEG import, fit/crop, and PNG export remain available on all three tiers. The editor shows your canvas type and Get Item availability.

**Save Painting** exchanges the held canvas for a named, locked Minecraft map, as in earlier releases. Place it in a normal/glowing item frame. This applies to all canvas tiers: Infinity's unlimited reuse applies to **Get Item**, not to saving maps. PNG exports appear under `canvasstudio/exports` in your game folder.

Existing paintings retain Minecraft's saved map data. Existing Blank Canvas items retain the ID `canvasstudio:canvas` and remain usable for painting; their Get Item mode is now locked. No world conversion is needed.

## Draw to item

With a Molder or Infinity Canvas, select **Mode: Get Item**, draw an item/block, then select **Find Item / Block**. Compare up to three suggestions and choose a result. The reward is **one newly created plain item/block**, even if you do not own it. Enchantments/custom data are not copied.

A successful Molder exchange replaces the held canvas with the reward. Infinity keeps the canvas and inserts the reward into your inventory; if full, the reward drops beside you. Each confirmed Infinity request grants one item, and you may choose again or return to drawing.

Returning to drawing, cancelling, failed matching, or rejected requests do not consume a canvas. Blank Canvas shows Get Item as locked and explains that Molder or Infinity is required. Exchange buttons wait for server confirmation. Each server-issued token works once and is bound to the held stack, hand and tier; it expires after 6,000 game ticks. Reopen the canvas to renew an expired session.

The existing finder catalogue and visual rules are unchanged: supported vanilla inventory textures, shape/outline, color and orientation. Matching is approximate, local, and requires no cloud service/API key. Scores describe visual similarity, not certainty. Modded items and unsupported special models are outside the catalogue. As in 2.0.1, server ID validation accepts registered non-air vanilla items; the server does not recompute client texture rankings.

## Build and verify

Use JDK 25:

```sh
./gradlew build
# Optional alternate development dependency:
./gradlew build -PneoForgeVersion=26.3.0.57-beta
```

JARs appear in `build/libs`. `check` runs image, palette, packet, editor-input, recipe/advancement codec, matching, and server authorization checks. See [VALIDATION.md](VALIDATION.md) for scope and in-game checks still needed, and [CHANGELOG.md](CHANGELOG.md) for changes.

## Help and license

[Issue tracker](https://github.com/SupremeGenZ-Github/Canvas-Studio/issues): include release, Minecraft/NeoForge versions, reproduction steps, and `logs/latest.log` with private information removed.

[MIT license](LICENSE).
