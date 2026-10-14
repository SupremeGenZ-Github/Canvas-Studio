# Canvas Studio

**Draw. Import. Create. Display.**

**made by SuprixZ**

Canvas Studio brings custom painting tools into Minecraft Java Edition. Paint pixel art, import pictures, save artwork as Minecraft maps and, in the full editions, draw items or blocks to find their closest supported visual matches. Later Canvas Studio+ releases add layered projects, large wall paintings, animation and shared galleries.

This is the shared guide for **Canvas Studio Lite, Canvas Studio and Canvas Studio+**, across their released Minecraft builds. **Features depend on the installed mod release and edition.** Use the timeline below to find when a feature became available.

[Releases and downloads](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases) · [Source repository](https://github.com/SupremeGenZ-Github/Canvas-Studio) · [Report an issue](https://github.com/SupremeGenZ-Github/Canvas-Studio/issues)

## Contents

- [Editions and installation](#editions-and-installation)
- [Version timeline](#version-timeline)
- [Painting and drawing](#painting-and-drawing)
- [Canvases and item rewards](#canvases-and-item-rewards)
- [Crafting progression](#crafting-progression)
- [Projects, layers and recovery](#projects-layers-and-recovery)
- [Wall paintings and saved-art editing](#wall-paintings-and-saved-art-editing)
- [Animation and Minecraft painting imports](#animation-and-minecraft-painting-imports)
- [Frames and material costs](#frames-and-material-costs)
- [Server gallery and settings](#server-gallery-and-settings)
- [Kurlo Pillow](#kurlo-pillow)
- [Upgrading, troubleshooting and validation](#upgrading-troubleshooting-and-validation)

## Editions and installation

| Edition | Purpose |
|---|---|
| **Canvas Studio Lite — 1.x** | Painting, drawing, local image import/export and saved map artwork; no item finder or rewards |
| **Canvas Studio — 2.0.x** | Lite painting tools plus the offline item/block finder and canvas exchange |
| **Canvas Studio+ — 2.1.x onward** | Canvas progression; later versions introduce the expanded studio and world-gallery features |

### Choose the correct Minecraft build

One README can describe every edition; **one JAR does not support every Minecraft version**. Check the selected release's Minecraft version, loader, Java requirement and supported loader range before installing. Matching mod version numbers do not make different Minecraft builds interchangeable.

The release history documented here includes these targets:

| Release/build | Minecraft Java | Loader requirement |
|---|---|---|
| Lite 1.0.3 / full 2.0.0 | 26.3 | NeoForge 26.3.0.51-beta |
| Lite 1.0.4 / full 2.0.1 | 26.3 | NeoForge 26.3.x, including betas |
| Plus 2.1.0–2.1.2, original edition | 26.3 | NeoForge 26.3.x, including betas |
| Plus 2.1.2, separate 26.2 port | 26.2 | NeoForge 26.2.0.88 |
| Plus 2.1.3, 26.2 edition | 26.2 | NeoForge 26.2.x, including betas |
| Plus 3.0.0 | 26.3 | NeoForge 26.3.x, including betas |
| Plus 3.1.0, 3.1.2 and 3.2.0–3.2.2 | 26.2 or 26.3, using separate JARs | Corresponding NeoForge 26.2.x or 26.3.x family |

These documented builds require **Java 25**. For any additional or later Minecraft build, follow its own release requirements. A loader range accepting future family builds is not a guarantee against future API changes. No Fabric, Forge, Bedrock or other Minecraft-version support is implied by this guide.

1. Download the release asset matching your Minecraft and loader.
2. Remove the previous Canvas Studio JAR from the active instance's `mods` folder.
3. Put the chosen JAR in that folder and launch with the required Java version.
4. In multiplayer, use the **same mod release, edition and Minecraft build on the client and server**.
5. Craft a canvas or find the mod's items in Creative inventory. The dedicated Canvas Studio+ tab is available in the releases noted below.

Install **one Canvas Studio edition at a time**. Lite, full and Plus use the same `canvasstudio` mod ID.

## Version timeline

This timeline follows the published release history through **v3.2.2**. Minecraft ports are identified separately; a port does not imply that every earlier release has that port. Version gaps are not undocumented feature updates.

### v1.0.3 — Canvas Studio Lite: the painting foundation

- Canvas and quill crafting; open a canvas and draw directly in Minecraft.
- Selectable colors and brush sizes, erasing, flood fill and undo.
- Local PNG/JPEG image import and PNG export.
- Save named artwork as a locked Minecraft map for display in an item frame.
- Corrected Minecraft 26.3 recipe-book advancement data and editor mouse-button handling.
- Painting-only edition: no item/block matching or rewards.

### v1.0.4 — Lite loader compatibility

- Expanded Lite from the exact NeoForge 26.3.0.51-beta target to the NeoForge 26.3.x family, including betas.
- Preserved Lite 1.0.3 painting behavior, gameplay resources and class bytes; this was a metadata compatibility update.

### v2.0.0 — Canvas Studio: draw to find items

- Retained the Lite painting, import/export and saved-map features.
- Added **Get Item**, **Find Item / Block** and offline approximate shape/color matching.
- Compared drawings against 1,198 supported vanilla inventory candidates and suggested up to three matches.
- Exchanged one Blank Canvas for one newly created plain selected item/block; owning the reward beforehand was unnecessary.
- Required no API key, cloud recognition service or internet matching request.
- Published as Canvas Studio, rebranding the earlier full-edition build.

### v2.0.1 — Full-edition loader compatibility

- Expanded the full edition to NeoForge 26.3.x, including betas.
- Retained 2.0.0 class bytes, gameplay resources, finder and exchange behavior.

### v2.1.0 — Canvas Studio+: three canvas tiers

- Introduced **Blank**, **Molder** and **Infinity** canvases, plus Molder and Infinity quills and distinct item assets.
- Blank became painting-only; Molder allowed one successful reward; Infinity allowed repeated successful rewards.
- Each reward created one plain selected item/block. Molder was replaced by its reward; Infinity stayed held and inserted the reward into inventory or dropped it if full.
- Added shaped quill upgrades and shapeless canvas recipes; removed the Leather + Paper + Feather shortcut.
- Added server-issued single-use authorization and checks for hand, stack, canvas tier, drawing and item ID, with duplicate/replay rejection.
- Cancelled, failed and rejected exchanges retained the canvas. Existing paintings and image tools remained available.

### v2.1.1 — Molten progression

- Renamed **Molder** to **Molten**, preserving internal item IDs so existing items remained usable.
- Molten Quill now required a central Quill, four corner Iron Ingots and four edge-center Gold Ingots.
- Molten Canvas became Blank Canvas + Molten Quill + Compass.
- Infinity Quill now required a central Molten Quill, four corner Echo Shards, left/right Lapis Blocks and top/bottom Diamond Blocks.
- Infinity Canvas became Molten Canvas + Infinity Quill + Recovery Compass.
- Kept existing textures and reward rules.

### v2.1.2 — Molten and Infinity artwork; separate Minecraft port

- Updated Molten Canvas/Quill textures with gold, amber and iron accents.
- Updated Infinity Quill with lapis blue, echo teal and diamond cyan accents; retained the existing Infinity Canvas texture.
- Left Blank Canvas/normal Quill textures, recipes and gameplay unchanged.
- Added a separate Minecraft **26.2 / NeoForge 26.2.0.88** build, adapting the file picker, recipe/advancement data, resource selection and server-drop API.

### v2.1.3 — NeoForge 26.2 family and creative tab

- Expanded the 26.2 port beyond the exact 26.2.0.88 dependency to NeoForge 26.2.x, including betas.
- Added the dedicated **Canvas Studio+ creative inventory tab**, with an Infinity Canvas icon and all six canvas/quill items.
- Preserved the 2.1.2 recipes, textures, painting and rewards.

### v3.0.0 — Creative Studio foundation

- Added redo and complete-stroke/shape undo grouping, with up to 32 in-memory operations.
- Added lines, outlined/filled rectangles and ellipses, eyedropper, dotted/dashed strokes and spray; Shift-constrained shapes.
- Added horizontal/vertical mirrors, grid, snapping, 1×/2×/4× zoom and middle-drag panning.
- Added Default, Grayscale and Retro 8 palette presets and drawing shortcuts that respect text-field focus.
- Introduced the Studio menu, up to 16 named editable layers, layer ordering/visibility/duplicate/delete/merge controls and layered erasing.
- Added local `.csp` projects, a thumbnail project gallery, renaming, duplication, deletion, sharing/import and periodic recovery saving.
- Added held saved-map editing: Shift-right-click a locked map, edit and save a new map without changing other copies.
- Expanded the offline catalogue with name/ID search, Items/Blocks/Vanilla/Modded filters, 3/6/12/24 result choices, favorites, recent results and informational browsing.
- Added supported modded PNG/simple-model indexing, bounded background matching and resource-reload refreshes; modded rewards require server opt-in.
- Added fitted-source/map-color previews and a six-slot **2D** wall arrangement planner.
- Added persistent per-Infinity-Canvas successful-use counters, optional lifetime caps, blocked reward IDs and supported server-config compatibility handling. Unlimited remained the default; Molten remained independent.
- Included the dedicated six-item creative tab in the 26.3 edition.

### v3.1.0 — Frameless paintings and Kurlo Pillow

- Retained the complete Creative Studio feature set and supplied separate 26.2 and 26.3 editions.
- Added **1×1 frameless wall paintings**: place a saved map on a vertical wall without an item frame, rotate it and recover it under normal drop rules.
- Added a persistent custom hanging entity using Minecraft map rendering/synchronization, with support, space and building-permission checks.
- Added **Kurlo Pillow**, its recipe and creative-tab entry: hold it throughout completed sleep to receive the installed game's enchanted golden apple status effects.

### v3.1.2 — Bigger Paintings

- Added **Painting size** with independently selectable width/height from 1–8 blocks, all 64 square/rectangular combinations, and a linked square option.
- Stored size in saved maps, placed entities and editable projects; retained it through pickup, reopening and placement.
- Checked the entire wall area for loaded support, clearance and permissions; older paintings/projects defaulted to 1×1.
- Introduced CSP format 3 wall dimensions. Artwork remained 128×128 pixels, scaled across the chosen wall size.

### v3.2.0 — Living Gallery

- Added a translucent placement preview: green/red local support and collision feedback, with server-authoritative placement.
- Added direct **Shift-right-click wall editing**, including resizing, without breaking the painting; server sessions bind to the target and revision.
- Added **Stretch, Fit and Crop** display modes and optional Wood, Gold, Iron and Diamond borders. These four borders had no material cost in this release.
- Added server-assigned owners and public/owner-locked access. Owners/admins control locks; protected player interactions follow those permissions.
- Added looping animation with up to 16 frames, independent editable layers per frame, blank/copied frames, deletion, playback preview and selectable frame delays.
- Added a world-local server art gallery: browse, publish, import and owner/admin-delete; persistent storage, artist/world quotas and a server disable setting.
- Added the Minecraft painting browser, name/asset search and import from the active painting registry/resource packs.
- Added copying a placed vanilla painting while holding a canvas, without removing the original. Available modded paintings can import when their textures are supported.
- Added CSP format 4 for animation/display/access data, old-project migration and bounded sequential artwork transfers with stale/replay rejection.

### v3.2.1 — Material Frames

- Expanded to **eleven** borders: Wood, Stone, Copper, Iron, Gold, Amethyst, Lapis, Diamond, Emerald, Quartz and Netherite.
- Required one corresponding inventory resource to apply/change a frame, spent only after successful saving, including Creative mode.
- Added material cost and ready/missing feedback in the editor.
- Kept same-frame edits, resizing and animation changes free; free removal with no refund; reapplication costs again.
- Preserved old frame IDs/colors. Project/gallery design data does not prove payment; saving a new framed painting requires material.
- Extended frame IDs while retaining the CSP4/CSA1 layouts. Added dedicated payment/failure/persistence regression checks.

### v3.2.2 — Reopen & Edit

- Added the explicit reopening choices **Edit keeping locked**, **Unlock & edit** and **Cancel** for locked paintings.
- Allowed owners/admins to immediately unlock and reopen held or placed paintings; wall paintings remain placed during editing.
- Preserved artwork, animation map IDs, size, name, layout, frame and ownership on unlock, without using another canvas/material.
- Added **Lock painting on save / Unlock painting on save** controls and an editing hint on saved paintings.
- Prevented public editors from taking ownership or relocking another player's painting.
- Made the actual saved item's access state authoritative when copies share source artwork.
- Persisted access changes before replacement, advanced revisions and issued fresh edit sessions; stale/replayed requests and failed persistence leave the original protected.
- Retained all earlier features, recipes, textures and project/gallery layouts; expanded to 17 automated verification suites.

## Painting and drawing

Right-click a held canvas to open its editor. Choose a color and brush, draw with left-click, erase with right-click, or import a PNG/JPEG. Save an editable project before converting your canvas into a finished painting if your version supports projects.

**Save Painting replaces the held canvas with a named locked Minecraft map**, including an Infinity Canvas. Infinity reuse applies to item rewards, not painting conversion. In older releases, display the map in an item frame; direct wall placement starts in 3.1.0.

### Expanded controls — Plus 3.0.0 onward

| Control | Action |
|---|---|
| Left-click/drag | Use the selected drawing tool |
| Right-click/drag on artwork | Erase; upper-layer erasing reveals lower layers |
| B / E / I | Brush / Eraser / Eyedropper |
| G | Toggle grid |
| Ctrl+Z / Ctrl+Y | Undo / Redo |
| Shift during a line/shape | Constrain the line, square or circle |
| Middle-drag while zoomed | Pan |
| Escape | Cancel the active stroke or return/close the current screen |

Shortcuts are ignored while typing. New artwork clears redo; history is not saved in projects. Tools and menu labels in older editions are simpler.

Current image importing accepts PNG/JPEG up to 16 MiB, 8,192 pixels per side and 16 megapixels total. Fit/center-crop conversion produces a **128×128 map-palette image**; transparency is composited over white. Colors can differ from the original. PNG export saves the finished image. Animated resource textures are not automatically converted to an editable animation timeline.

## Canvases and item rewards

**Plus 2.1.0 onward:**

| Canvas | Painting | Get Item |
|---|---|---|
| Blank | Available | Locked |
| Molten, called Molder in 2.1.0 | Available | One successful exchange replaces the canvas with one selected reward |
| Infinity | Available | One reward per successful request; canvas retained; unlimited by default |

Full 2.0.x instead exchanges a **Blank Canvas**; Lite has no rewards.

Matching is an approximate offline visual comparison, not guaranteed recognition or a probability of correctness. You do not need to own the reward. Infinity rewards use inventory space or a fallback drop. Cancelled/rejected/failed delivery does not spend successful uses. The server authorizes each request and rejects duplicates.

From 3.0.0, catalogue browsing, favorites, history and filters help explore supported textures, but **browsing alone does not grant rewards**. Saved-map editing cannot unlock Get Item. Special/composite models may be skipped; animated item textures use their first square frame; block references approximate cubes. Indexing is bounded to 5,000 source references. Server opt-in is required for modded rewards.

## Crafting progression

### Recipes from Plus 2.1.1 onward

| Item | Recipe |
|---|---|
| Quill | Feather + Ink Sac, shapeless |
| Blank Canvas | Leather + Paper + Quill, shapeless |
| Molten Quill | Quill centered; four Iron Ingots in corners; four Gold Ingots on center edges |
| Molten Canvas | Blank Canvas + Molten Quill + Compass, shapeless |
| Infinity Quill | Molten Quill centered; four Echo Shards in corners; Lapis Blocks left/right; Diamond Blocks top/bottom |
| Infinity Canvas | Molten Canvas + Infinity Quill + Recovery Compass, shapeless |
| Kurlo Pillow, from 3.1.0 | Enchanted Golden Apple centered, surrounded by eight White Wool |

Shaped upgrades and the Pillow use a full 3×3 crafting table. Legacy IDs `molder_canvas` and `molder_quill` remain; their displayed names changed to Molten.

### Historical Plus 2.1.0 recipes

Molder Quill used a normal Quill surrounded by eight Iron Nuggets. Infinity Quill used a normal Quill surrounded by eight Diamonds. Molder/Infinity canvases used Leather + Paper + the corresponding upgraded Quill, shapeless. The normal Quill and Blank Canvas recipes were as above. These upgrade recipes were replaced in 2.1.1; the feather shortcut was removed in 2.1.0. For Lite/full-era recipes, use the selected older release's recipe book/source.

## Projects, layers and recovery

**Plus 3.0.0 onward:** create up to 16 named layers, select, rename, reorder, hide, duplicate, delete or merge them. Visible layers flatten into the saved map; an erased upper pixel reveals artwork below.

Local `.csp` projects preserve editable layers and drawing settings. The gallery provides thumbnails, paging, open/rename/duplicate/delete/share and imports of shared project paths; it displays up to 200 valid projects. Fitted original images can be retained, but full-resolution source images are not embedded.

| Instance-relative location | Contents |
|---|---|
| `canvasstudio/projects` | Managed `.csp` projects and recovery slot |
| `canvasstudio/exports` | PNG exports and shareable project copies |

Recovery saves about every 30 seconds and on closing. It is one local slot shared across worlds, separate from named projects. Project reads are bounded; managed saves validate paths and use temporary-file replacement.

From 3.2.0, projects support up to **16 animation frames with 16 layers per frame** and display/access/timing settings, with an 8 MiB cap. Undo history, zoom/pan and temporary wall-planner arrangements are not persisted. Saved maps and server-gallery entries preserve flattened artwork, not editable layers.

## Wall paintings and saved-art editing

**From 3.1.0:** right-click a vertical wall with a saved locked map, without sneaking, to place it without an item frame. Right-click to rotate; break to recover under normal survival/entity-drop rules. Creative behavior and environmental breakage follow applicable game rules.

**From 3.1.2:** Studio menu → Painting size selects width and height from 1–8. Click the area's **lower-left corner as seen from the front**; the painting grows upward and right. The entire area needs loaded chunks, supported wall, clearance and building permission. Failed placement retains the map. Older artwork defaults to 1×1. Vanilla item frames still display a normal one-block map.

Larger paintings scale the 128×128 artwork; **8×8 is wall size, not higher image resolution**. From 3.2.0, Stretch fills the rectangle, Fit preserves proportions and Crop fills by trimming. Placement ghosts provide local feedback; the server makes the final decision.

**Held editing from 3.0.0:** hold one locked painting map and Shift-right-click in the air. Saving creates a new map in that hand, leaving other copies unchanged.

**Wall editing from 3.2.0:** Shift-right-click a placed Canvas Studio painting and save directly back to that entity. Resizing must pass wall checks. Sessions expire after 20 minutes; save a project and reopen if necessary. Reopened maps have one flattened layer per available animation frame; retain CSP files for editable layers.

**Explicit unlock from 3.2.2:** owners/admins choose Edit keeping locked or Unlock & edit. Unlock takes effect immediately and permits public editing. Only owners/admins can change the access lock; public editors cannot claim or relock someone else's art. The owner can lock it again through Display / access when saving.

Minecraft's **locked map pixels** and Canvas Studio's **owner access lock** are different settings: opening public access does not turn the saved artwork into a live terrain map.

## Animation and Minecraft painting imports

**Plus 3.2.0 onward:**

- Studio menu → Animation: add blank/copied frames, select/delete frames, preview looping playback and choose 0.5/1/2/4/8 seconds per frame. Maximum 16 frames; fastest playback is 2 FPS. Each project frame retains independent layers and a fitted reference image when available.
- Studio menu → Minecraft paintings: search names/asset IDs and import supported available painting textures into the active layer.
- Hold a canvas and Shift-right-click a placed vanilla painting to copy its registered texture; the original stays in place.
- The catalogue reads the installed game, active registry and resource packs, including available supported modded painting textures. Minecraft artwork is not bundled. Missing/unsupported textures are omitted.

All 51 vanilla painting textures in the tested 26.2/26.3 game assets were covered by automated import checks. Catalogue counts can vary with Minecraft versions and installed content. Imports use map colors and dimensions capped at 8×8.

## Frames and material costs

**3.2.0:** optional Wood, Gold, Iron and Diamond borders were free cosmetic choices.

**3.2.1 onward:** choose None or one of eleven frames in Studio menu → Display / access. Applying/changing a frame costs **one material per painting**, regardless of wall size or animation length.

| Frame | One required resource |
|---|---|
| Wood | Any wooden-planks-tag item |
| Stone | Stone, not cobblestone |
| Copper | Copper Ingot |
| Iron | Iron Ingot |
| Gold | Gold Ingot |
| Amethyst | Amethyst Shard |
| Lapis | Lapis Lazuli |
| Diamond | Diamond |
| Emerald | Emerald |
| Quartz | Nether Quartz |
| Netherite | Netherite Ingot |

The editor shows availability; the server validates inventory, hotbar/off-hand materials and spends only after a successful save. Creative mode follows the same rule. Missing resources or failed saving retain the original painting/materials.

Keeping the same frame while editing/resizing/unlocking is free. Removing it is free with **no refund**; reapplying or changing it costs again. Placing/picking up an existing framed painting adds no cost. Project/gallery imports require material when saved as new framed paintings; publishing and previews cost nothing. Older frame IDs/colors remain valid.

## Server gallery and settings

**Server gallery from 3.2.0:** browse, publish current artwork, import an entry, and owner/admin-delete. Limits are 64 entries per artist and 1,000 per world. Publishing shares flattened palette frames/display settings, creates no reward and consumes no canvas. Saving imported art still needs a canvas or an authorized edit session.

**Server settings from 3.0.0**, with gallery added in 3.2.0:

```toml
maxInfinityCanvasUses = 0
allowModdedRewards = false
blockedRewardItems = []
enableServerGallery = true
```

Use only settings supported by your release. Edit the generated `canvasstudio-server.toml` and restart. Loader-dependent locations can include `config`, a world `syncedconfig` override, or the world's `serverconfig` folder.

| Setting | Meaning |
|---|---|
| `maxInfinityCanvasUses` | 0 is unlimited; a positive number caps successful rewards per individual Infinity Canvas |
| `allowModdedRewards` | Opts into supported modded reward items |
| `blockedRewardItems` | Item IDs disallowed for both Molten and Infinity |
| `enableServerGallery` | Enables/disables the world's public gallery |

Infinity counts persist through normal saves/transfers and accumulate even in unlimited mode. Changing caps never resets them: lowering below the existing count blocks further rewards; zero restores unlimited eligibility. Molten is independent. Creative duplication and external item-data editing are outside normal-play quota guarantees.

Back up the entire world, including map data and `canvasstudio/paintings` and `canvasstudio/gallery`, plus local projects separately. Transfers are bounded and server-validated; project files never authorize item rewards or contain trusted canvas-use state.

## Kurlo Pillow

**Plus 3.1.0 onward:** hold the same Pillow in the same main-hand or off-hand slot from entering bed through completed nighttime sleep. Normal morning wake-up grants the installed game's enchanted golden apple status effects: **Regeneration, Absorption, Resistance and Fire Resistance**.

The Pillow is reusable and does not restore hunger. Early/interrupted sleep, swapping it away or relogging does not grant effects. Effect values come from the installed game rather than a universal hardcoded table.

## Upgrading, troubleshooting and validation

Back up worlds/projects before upgrading. Replace the old JAR, retain the correct Minecraft edition, and update multiplayer clients/server together. Consult the target release notes for migration requirements.

- Plus 3.1.2 writes CSP3 dimensions; older projects open at 1×1.
- Plus 3.2.0 writes CSP4 animation/display data; earlier releases cannot read it.
- Plus 3.2.1 extends frame IDs; 3.2.0 cannot read the new materials. Avoid downgrading affected worlds/projects.
- Plus 3.2.2 retains project/gallery layouts and changes the network protocol. Protocols vary by release; matching mod versions are required.
- A separate Minecraft port is not an assurance that world saves can safely move between Minecraft versions.

| Problem | Check |
|---|---|
| Mod fails to load | Release-specific Minecraft, loader and Java requirements; active `mods` folder; duplicate editions |
| Get Item is unavailable | Lite has none; Plus needs Molten/Infinity; older full 2.0.x uses Blank |
| Painting will not place | Frameless support begins in 3.1.0; larger sizes in 3.1.2; check support, space, permission and lower-left placement |
| Painting will not reopen/unlock | Held editing begins in 3.0.0, wall editing in 3.2.0, explicit unlock in 3.2.2; check ownership and session validity |
| Layers are missing on reopening | Open the CSP project; maps/gallery entries contain flattened artwork |
| Imported colors differ | Minecraft map-palette conversion changes colors |
| Frame save is rejected | From 3.2.1, have the exact resource; Stone is not cobblestone |
| Reward is rejected | Server item blocklist, disabled modded rewards, canvas allowance or drawing/authorization checks |
| Multiplayer connection fails | Match client/server mod edition, release and Minecraft build |

For bug reports include mod/Minecraft/loader/Java versions, logs, reproduction steps, and singleplayer/multiplayer context.

### Source and testing

Use the **source tag or source ZIP attached to the selected release** for its exact code. Development/default branches can differ from published releases. Build with that source's required JDK using `./gradlew build`, or `gradlew.bat build` on Windows; output is in `build/libs`. Changing a NeoForge dependency property does not port code to another Minecraft family.

Testing expanded with the mod: editor/image/codec/matching/reward checks in early editions; project and usage-limit checks in Creative Studio; wall size, geometry, ownership, gallery, animation, material payment and unlock checks in later releases. v3.2.2 passed **17 automated verification suites**, packaged checks, NeoForge endpoint builds and published-family API/range audits, plus a fresh dedicated-server startup/save/stop test for each edition.

Read each release's validation report for its actual targets and tests. API audits establish referenced-member availability, not complete gameplay compatibility. **Interactive client rendering, live multiplayer transactions, wall-edit gameplay and actual player sleep were not performed in the documented release checks.** Releases marked as previews retain that status.

Full GPU model capture, a 3D room renderer and higher-resolution multi-map murals are not implemented features. The current planner is 2D and each painting frame is 128×128 pixels.

**Canvas Studio — made by SuprixZ**
