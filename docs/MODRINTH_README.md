# Canvas Studio+ · v3.1.2

**Draw. Import. Edit. Hang.** Turn your artwork into Minecraft paintings, or draw an item/block and find its closest supported visual match.

**made by SuprixZ**

> This README applies to **both editions**: Minecraft **26.2 + NeoForge 26.2.x** and Minecraft **26.3 + NeoForge 26.3.x**, including beta builds. The features are shared; each Minecraft version uses its own JAR.

[Versions](https://modrinth.com/mod/canvas-studio-suprixz/versions) · [Changelog](https://github.com/SupremeGenZ-Github/Canvas-Studio/blob/main/CHANGELOG.md) · [Report an issue](https://github.com/SupremeGenZ-Github/Canvas-Studio/issues)

## What you can create

| Feature | What it does |
|---|---|
| Pixel-art studio | Draw with brushes, shapes, fill, mirrors, grid, snapping and undo/redo |
| Image import/export | Convert local PNG/JPEG images to Minecraft map colors and export finished PNGs |
| Layered projects | Keep up to 16 editable layers in local `.csp` project files |
| Saved-painting editing | Reopen an existing locked map and save an edited copy |
| Large wall paintings | Place frameless artwork from **1×1 to 8×8 blocks**, including rectangles |
| Offline item finder | Compare a drawing with supported item/block artwork and choose a match |
| Canvas progression | Blank for painting, Molten for one reward, Infinity for repeated rewards |
| Kurlo Pillow | Complete a night's sleep while holding it to receive enchanted golden apple status effects |

Creative tools and matching run locally; no cloud account or image-upload service is required.

## Installation

Download the file for your Minecraft version from this project's **Versions** tab. Canvas Studio+ has separate builds with the same features:

| Minecraft Java | Required loader family |
|---|---|
| **26.3** | **NeoForge 26.3.x**, including betas |
| **26.2** | **NeoForge 26.2.x**, including betas |

Check the Minecraft version listed on the file before installing. Source archives and validation reports are available on the [GitHub releases page](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases).

1. Use Minecraft 26.2 or 26.3 with the corresponding NeoForge family and **Java 25**.
2. Remove the older Canvas Studio JAR from the instance's `mods` folder.
3. Put the matching v3.1.2 JAR in that folder.
4. For multiplayer, install the **same edition and mod version on both client and server**.
5. Launch the game and find the **Canvas Studio+** creative tab, or craft the items below.

Canvas Studio Lite and Canvas Studio+ share the `canvasstudio` mod ID; install one edition of the mod at a time. A 26.2 JAR cannot be used in Minecraft 26.3, or vice versa.

The declared NeoForge ranges accept builds within the corresponding family, including betas and future family builds. Future API/behavior changes can still require a mod update; compatibility with unreleased builds cannot be guaranteed.

## Start painting in five steps

1. Craft a **Quill**, then a **Blank Canvas**.
2. Right-click the canvas to open the editor.
3. Draw, or choose **Studio menu → Import PNG / JPEG**.
4. Choose **Studio menu → Painting size**, then save the painting.
5. Hold the saved map and right-click a vertical wall at the intended **lower-left corner**.

Saving a painting produces a locked Minecraft map and **replaces the held canvas**, including an Infinity Canvas. Save an editable project first if you want to keep layers for later work.

## Three canvases, three reward rules

| Canvas | Drawing, importing and painting | Get Item mode |
|---|---|---|
| **Blank Canvas** | Available | Locked; requires Molten or Infinity |
| **Molten Canvas** | Available | One successful exchange replaces the canvas with one selected item/block |
| **Infinity Canvas** | Available | One selected item/block per successful request; reusable and unlimited by default |

The reward is a **newly created plain item**. You do not need to own it already. Infinity rewards go into the inventory, or are dropped when it is full. Cancellation, failed delivery and rejected requests do not consume successful uses; the server also guards against duplicate/replayed reward requests.

Infinity's unlimited reuse applies to **Get Item mode**. It does not make saving a painting free of canvas replacement.

## Crafting recipes

The creative tab contains all seven items.

| Item | Ingredients | Arrangement |
|---|---|---|
| **Quill** | Feather + Ink Sac | Shapeless |
| **Blank Canvas** | Leather + Paper + Quill | Shapeless |
| **Molten Quill** | 1 Quill + 4 Iron Ingots + 4 Gold Ingots | Quill in the center; iron in all four corners; gold on all four center edges |
| **Molten Canvas** | Blank Canvas + Molten Quill + Compass | Shapeless |
| **Infinity Quill** | 1 Molten Quill + 4 Echo Shards + 2 Lapis Blocks + 2 Diamond Blocks | Molten Quill in the center; echo shards in the corners; lapis blocks left/right; diamond blocks top/bottom |
| **Infinity Canvas** | Molten Canvas + Infinity Quill + Recovery Compass | Shapeless |
| **Kurlo Pillow** | 1 Enchanted Golden Apple + 8 White Wool | Apple in the center, wool in all eight surrounding slots |

All shaped recipes use the full 3×3 crafting grid. The Leather + Paper + Feather shortcut is not available. Legacy registry IDs `molder_canvas` and `molder_quill` are retained for saved items; their displayed names are **Molten Canvas** and **Molten Quill**.

## Drawing tools and controls

Choose the active-tool button for brush, eraser, flood fill, line, outlined/filled rectangle, outlined/filled ellipse, eyedropper, dotted, dashed and spray tools.

| Control | Action |
|---|---|
| Left-click/drag | Draw with the selected tool |
| Right-click/drag on canvas | Erase; upper-layer erasing reveals lower layers |
| B / E / I | Brush / Eraser / Eyedropper |
| G | Toggle grid |
| Ctrl+Z / Ctrl+Y | Undo / Redo |
| Shift while drawing a line or shape | Constrain a line, square or circle |
| Middle-drag while zoomed | Pan |
| Escape | Cancel an active stroke or return/close the current screen |

Drawing shortcuts are ignored while typing in text fields. Options provide horizontal/vertical mirrors, snapping, 1×/2×/4× zoom and Default/Grayscale/Retro 8 palette presets. History groups complete strokes/shapes and keeps up to 32 operations in memory; a new edit clears redo.

For imports, choose a local **PNG or JPEG** up to 16 MiB, no more than 8,192 pixels per side and 16 megapixels total. Fit and center-crop modes convert it to the 128×128 map canvas; transparency is composited over white. Minecraft's map palette can change the original colors.

## Wall paintings up to 8×8 blocks

Open **Studio menu → Painting size**. Set width and height from **1–8**. Keep square enabled for square paintings, or disable it for rectangles such as 2×3 or 8×4. Save to apply the chosen size.

Place by right-clicking a vertical wall **without sneaking**. The clicked tile is the **lower-left corner as seen from the front**; the painting grows upward and to your right.

The whole area needs loaded chunks, a supported wall, free collision space and build permission. Overlapping hanging decorations are checked. Failed placement does not consume the map.

- No item frame or visible border is required.
- Right-click a placed painting to rotate the artwork.
- Break it to recover its saved map under normal survival/entity-drop rules.
- Dimensions are stored with map items, placed entities and editable projects, and are restored when editing.
- Older paintings and projects default to **1×1**.
- Vanilla item frames still show a normal one-block map.

The source artwork stays **128×128 pixels**. Larger displays scale that artwork; rectangles stretch it. Selecting 8×8 does not increase the pixel resolution.

## Edit a painting you already saved

Pick up the painting, hold **one locked painting map**, then **Shift-right-click in the air**. Change its pixels or wall size, then choose **Save edited painting**.

Saving creates a new locked map in the held hand. Other copies of the original retain their previous artwork and dimensions. Editing also supports older Canvas Studio paintings and other held locked maps.

A map retains finished pixels, so it reopens as **one flattened layer**. Keep a `.csp` project for editable layers. Map-edit sessions expire after 20 minutes; if needed, save a project, reopen the held map and try again. Saved-map editing does not grant Get Item access.

## Projects, layers and sharing

Use **Studio menu → Save editable project** to keep your working document, or open **Projects** for the gallery.

Projects preserve up to 16 named layers, their visibility/order, the active layer, pixels, brush/tool/color/mirror/grid/snap settings, wall dimensions and the fitted original image when available. Add, duplicate, reorder, rename, hide, delete or merge layers; visible layers form the final painting.

| Location inside the Minecraft instance | Contents |
|---|---|
| `canvasstudio/projects` | Managed `.csp` projects, including the reserved recovery project |
| `canvasstudio/exports` | Exported PNGs and shareable project copies |

The gallery supports thumbnails, paging, opening, renaming, duplication, deletion and sharing, with up to 200 valid projects displayed. Select a project and use the title field for renaming; paste a shared `.csp` path into the field to import it.

A reserved recovery project is saved about every 30 seconds while the editor is active and on closing. This is **one local recovery slot shared across worlds**, separate from named projects. Undo history is not stored in project files.

v3.1.2 writes **CSP format 3** and reads older formats 1 and 2 as 1×1 paintings. Full-resolution source images are not embedded; the fitted 128×128 image is retained when available. [Project-format details](https://github.com/SupremeGenZ-Github/Canvas-Studio/blob/main/docs/PROJECT_FORMAT.md).

## Offline item matching and previews

On a Molten or Infinity Canvas, choose **Studio menu → Draw to Get Item**. Draw an item/block, review its closest supported visual matches, select one and request it. Matching is approximate: it compares artwork with the supported catalogue rather than guaranteeing recognition of every drawing.

Search item names/IDs, filter Items/Blocks/Vanilla/Modded, choose 3/6/12/24 results and save favorites. Recent results and favorites are local. **Browse catalogue** works without a drawing and does not grant rewards.

Supported modded PNG textures and simple model references can be indexed. Special/composite models are skipped, animations use their first square frame, and block references approximate cube appearance. Indexing is limited to 5,000 source references and refreshes after resource reloads. Modded rewards require server opt-in.

The image preview compares the fitted source and final map colors. A **six-slot 2D wall planner** lets you arrange the current artwork or saved projects and change preview frames/display size. It is a mock-up; actual placement uses the saved map and a wall.

## Kurlo Pillow

Craft it with an Enchanted Golden Apple surrounded by eight White Wool. Hold the **same pillow in the same main-hand or off-hand slot** when entering bed and throughout the night's sleep.

On normal morning wake-up, the server grants the installed game's enchanted golden apple status effects: **Regeneration, Absorption, Resistance and Fire Resistance**. The pillow is reusable and does not restore food points.

Leaving bed early, interrupted sleep, swapping away the pillow or relogging does not grant the effects.

## Server settings

Edit the generated `canvasstudio-server.toml`, then restart the world/server. Depending on the loader, it may be in `config` with a world-specific `syncedconfig` override, or in the world's `serverconfig` folder.

```toml
maxInfinityCanvasUses = 0
allowModdedRewards = false
blockedRewardItems = []
```

| Setting | Meaning |
|---|---|
| `maxInfinityCanvasUses` | 0 = unlimited; a positive value limits successful rewards per individual Infinity Canvas |
| `allowModdedRewards` | Enables supported modded item rewards when true |
| `blockedRewardItems` | Item IDs rejected as rewards for both Molten and Infinity |

Infinity counts stay on the item through normal transfers/saves and accumulate even while unlimited. Lowering a cap below an existing count blocks further rewards; returning to zero restores unlimited eligibility. Failed requests do not spend uses. Molten remains independent of Infinity quotas. Creative/admin duplication and external item-data editing are outside normal-play quota guarantees.

The server handles reward authorization, canvas permissions, drawing/item validation and wall placement. **Protocol 5** requires matching client/server releases.

## Upgrading and troubleshooting

| Question | What to check |
|---|---|
| The mod is not listed | Correct Minecraft/NeoForge edition, Java 25, JAR in the active instance's `mods` folder, and no duplicate Canvas Studio/Lite JAR |
| Get Item is locked | Use a Molten or Infinity Canvas; saved-map editing and Blank Canvas do not unlock rewards |
| A large painting will not place | Click the lower-left tile of a vertical wall without sneaking; check the entire selected area for support, clearance and permission |
| An old painting is still 1×1 | Pick it up, Shift-right-click in air, choose a new Painting size and save the edited painting |
| Layers disappeared when reopening a map | Maps hold flattened pixels; open the saved `.csp` project for layers |
| Imported colors look different | Images are converted to Minecraft's map palette |
| A finder result is unavailable | The server may block that ID, disable modded rewards or limit that Infinity Canvas |
| Client/server connection rejects the mod | Use the same v3.1.2 edition on both sides; v3.1.0 uses a different network protocol |

For a bug report, include Minecraft, NeoForge and mod versions; the relevant log; reproduction steps; and whether it happens in singleplayer or multiplayer. [Open an issue](https://github.com/SupremeGenZ-Github/Canvas-Studio/issues).

## Source, building and validation

Source branches: [`main` — Minecraft 26.3](https://github.com/SupremeGenZ-Github/Canvas-Studio/tree/main) and [`minecraft-26.2` — Minecraft 26.2](https://github.com/SupremeGenZ-Github/Canvas-Studio/tree/minecraft-26.2).

From the source for your chosen edition, use **JDK 25**:

```sh
./gradlew build
```

Windows: `gradlew.bat build`. The JAR is written to `build/libs`. To compile against a different dependency **within that source edition's NeoForge family**:

```sh
./gradlew build -PneoForgeVersion=<matching-NeoForge-version>
```

Changing this property does not convert 26.3 source into the separate 26.2 edition.

Both v3.1.2 editions passed **13 Java verification suites**, packaged-artifact checks and default/endpoint builds. API/dependency-range auditing covered **75 published NeoForge 26.3 builds** and **91 published NeoForge 26.2 builds** at release time, including betas. New size checks cover all 64 dimensions in all four horizontal directions.

**No interactive in-game playtest, rendered world inspection, multiplayer session or live world save/reload test was performed.** Headless checks and API audits are not proof of every loader's gameplay behavior. The release files include source ZIPs, detailed validation, logs, audit data and SHA-256 checksums.

[26.3 validation and logs](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases/tag/v3.1.2) · [26.2 validation and logs](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases/tag/v3.1.2-mc26.2) · [Full changelog](https://github.com/SupremeGenZ-Github/Canvas-Studio/blob/main/CHANGELOG.md)

**Canvas Studio+ — made by SuprixZ**
