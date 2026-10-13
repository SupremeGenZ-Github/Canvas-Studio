# Canvas Studio+ · Changelog

**made by SuprixZ**

Detailed release notes for the Minecraft 26.2 and 26.3 editions. Features are shared; download the JAR for your Minecraft version. Each release links to its source, validation and checksums.

- [v3.1.2 — The Bigger Paintings Update](#v312--the-bigger-paintings-update)
- [v3.1.0 — The Creative Studio Update](#v310--the-creative-studio-update)

---

## v3.1.2 — The Bigger Paintings Update
**Released 13 October 2026 · Minecraft 26.2 / 26.3 · NeoForge**

Take your artwork beyond a single block. v3.1.2 adds wall paintings from **1×1 up to 8×8 blocks**, with both square and rectangular layouts, while retaining the complete v3.1.0 studio, finder, saved-painting editor and Kurlo Pillow.

### 🖼️ Adjustable wall painting sizes

- Adds **Painting size** to the editor's **Studio menu**.
- Choose width and height independently from **1 to 8 blocks**: all 64 combinations are supported.
- **Keep square** links both dimensions; turn it off for rectangles such as 2×3, 4×2 or 8×4.
- **Reset to 1×1** returns to the original display size.
- The editor footer shows the selected wall dimensions.
- **Save Painting** or **Save edited painting** applies those dimensions to the resulting map.

Artwork remains **128×128 pixels** and scales to fill the selected wall area. Rectangular display sizes stretch the square artwork; this update does not add a higher-resolution pixel canvas.

### 🧱 Placement across multiple blocks

Click the wall at the painting's **lower-left corner as viewed from the front**. It expands **upward and to your right**.

The server checks the complete area for loaded chunks, wall support, placement/build permission, collision clearance and overlapping hanging decorations. Failed placement does not consume the held map.

Rendering and the physical hit box use the selected width and height. Scaling applies to the wall-plane axes while retaining normal depth and Minecraft's native map orientation/rotation. The renderer neutralizes vanilla's offset cancellation so taller paintings stay centered on their enlarged bounds.

Frameless placement, artwork rotation and normal survival pickup/drop behavior remain available.

### 💾 Sizes travel with your artwork

- Wall dimensions are stored on the saved map in the registered `canvasstudio:painting_size` data component.
- Placed paintings synchronize their dimensions to clients and store them with entity save data.
- Picking up a painting retains its chosen size on the map.
- Opening a saved painting restores its size in the editor.
- Saving an edited painting can resize it as well as change its pixels; other copies of the original map remain unchanged.
- **CSP project format 3** stores wall width and height alongside the existing layers and editor settings.
- Existing format-1/format-2 projects and paintings without size data default to **1×1**.
- Invalid or truncated project dimensions are rejected; the maximum remains 8 blocks per side.

Vanilla item frames continue to show a normal one-block map. Large displays use Canvas Studio+'s direct wall-placement feature.

### 🛠️ Server validation and upgrade behavior

Save and painting-edit transfer packets carry the display dimensions. The server validates the 1–8 bounds before accepting a save, and placement is server-controlled.

**Network protocol changes from 4 to 5.** Update client and server to the same v3.1.2 edition. Remove the older Canvas Studio JAR before installing its replacement.

Existing maps, canvas/quill IDs, crafting recipes, creative-tab items, reward rules, Infinity counters and the Kurlo Pillow are retained. No new crafting ingredient is required to select a painting size.

### 🧪 Verification and release coverage

Both Minecraft editions passed **13 Java verification suites**, packaged-artifact checks and builds on their default and tested endpoint dependencies:

| Minecraft edition | Default build target | Tested endpoints | Published NeoForge API/range audit |
|---|---|---|---|
| 26.3 | 26.3.0.51-beta | 26.3.0.0-beta and 26.3.0.76 | 75 builds |
| 26.2 | 26.2.0.88 | 26.2.0.0-beta and 26.2.0.90 | 91 builds |

New checks enumerate all **64 sizes in all four horizontal wall directions**, covering occupied tiles, placement anchors, physical bounds, wall thickness and production rendering plane transforms. They also verify persistent/network codecs, invalid/truncated sizes, project-format migration, copied artwork dimensions and save/edit packet roundtrips.

These results are automated and headless. **No interactive Minecraft rendering, actual wall placement, multiplayer session or live world save/reload test was performed.** API audits verify referenced binary-member availability; they do not guarantee future loader behavior.

**Release files:** [Minecraft 26.3](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases/tag/v3.1.2) · [Minecraft 26.2](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases/tag/v3.1.2-mc26.2)

---

## v3.1.0 — The Creative Studio Update
**Released 12 October 2026 · Minecraft 26.2 / 26.3 · NeoForge**

v3.1.0 expands Canvas Studio+ into a fuller creative workspace: draw with more tools, keep editable layered projects, revisit saved paintings, browse an improved item catalogue and hang your artwork directly on walls. It also introduces the reusable **Kurlo Pillow**.

### 🎨 Drawing tools and editor controls

- **Redo joins Undo.** Revisit an undone stroke with Redo or Ctrl+Y. A complete stroke or shape counts as one history action, with up to 32 operations retained in memory. Starting a new edit clears the redo history.
- **More ways to draw:** brush, eraser, flood fill, straight line, outlined and filled rectangles, outlined and filled ellipses, eyedropper, dotted strokes, dashed strokes and spray.
- **Constrained shapes:** hold Shift for a constrained line, square or circle.
- **Mirror options:** draw symmetrically across the horizontal axis, vertical axis or both.
- **Grid and snapping:** align pixel art and shapes with selectable snapping and a visible grid at sufficiently enlarged scales.
- **Zoom and pan:** switch between 1×, 2× and 4× zoom; middle-drag to pan a zoomed canvas.
- **Palette presets:** Default, Grayscale and Retro 8, while final paintings continue to use Minecraft's map colors.
- **Keyboard shortcuts:** B for brush, E for eraser, I for eyedropper, G for grid, Ctrl+Z for Undo and Ctrl+Y for Redo. Drawing shortcuts are ignored while typing in text fields.
- **Dedicated Studio menu:** access import/export, projects, layers, previews, the finder and catalogue from the editor.

The existing 128×128 painting format, local PNG/JPEG import, fit/center-crop conversion and PNG export remain available.

### 🗂️ Editable projects, layers and recovery

- **Up to 16 named layers**, with visibility, selection, duplication, ordering, deletion and merge-down controls.
- Erasing an upper layer reveals the layers below it; visible layers are combined when saving a painting.
- **Local `.csp` project files** preserve editable layers and drawing settings, plus the fitted original image when one is available.
- **Project gallery** with thumbnails, paging, opening, renaming, duplicating, deleting and sharing. Browsing displays up to 200 valid projects.
- **Project sharing:** export a `.csp` copy or import a shared file through its path. Project files contain bounded artwork data, not executable scripts.
- **Recovery saving:** a reserved recovery project is saved approximately every 30 seconds while the editor is active and on closing. It is one local recovery slot shared across worlds.
- **Bounded project parsing and managed paths:** invalid, truncated and oversized files are rejected; managed project writes check for unsafe symlink targets.

**Save editable project** retains your working document. **Save Painting** produces the finished locked Minecraft map and replaces the held canvas. Saving a painting also replaces an Infinity Canvas; unlimited reuse applies to its item-reward mode.

### 🖼️ Edit paintings you already saved

Hold **one locked painting map** and **Shift-right-click in the air** to reopen its pixels in the editor. Older Canvas Studio paintings and other held locked maps can use this workflow.

Saving the edited painting creates a **new map in the same hand**. Other copies of the original map retain their original artwork. A saved map reopens as a single flattened layer; keep a `.csp` project if you want to preserve your layer structure.

Editing is authorized by the server and tied to the held map and hand. Editing sessions expire after 20 minutes, and map editing does not unlock Get Item rewards.

### 🧱 Frameless wall paintings

- **Hang a painting without an item frame:** right-click a vertical wall with a saved locked map, without sneaking.
- A custom hanging entity displays the map with **no visible frame or border**.
- **Right-click to rotate** the artwork.
- **Break to retrieve** the map under normal survival and entity-drop rules; creative behavior follows normal creative rules.
- Placed paintings use Minecraft's map synchronization and entity persistence.
- Placement checks wall support, clearance and build permission. Unsupported placement is rejected.

v3.1.0 wall paintings are **one block in size**. Adjustable sizes arrive in v3.1.2.

### 🔎 Finder, catalogue and previews

- **Expanded offline catalogue interface** with name/ID search and Items, Blocks, Vanilla and Modded filters.
- Choose **3, 6, 12 or 24 matching results**.
- **Favorites and recent results** are stored locally.
- **Browse without drawing:** catalogue browsing is available independently, but does not grant items.
- Index supported modded PNG textures and simple model texture references. Unsupported special/composite models are skipped, animated textures use their first square frame, and block references approximate cube appearance.
- Catalogue rebuilding responds to resource reloads; indexing is bounded to 5,000 source references.
- **Side-by-side preview** compares a fitted imported image with the final Minecraft map colors.
- **Six-slot 2D wall planner** lets you arrange artwork or saved projects and change mock-up frame styles and display size.

Matching remains approximate and offline. The wall planner is a preview, not world placement, a 3D room renderer or a world-lighting simulation.

### ♾️ Infinity Canvas limits and server administration

- Introduces an optional **lifetime reward limit per individual Infinity Canvas**.
- `maxInfinityCanvasUses = 0` keeps Infinity rewards unlimited by default.
- Successful rewards update a persistent item counter, including while the limit is unlimited.
- A positive limit blocks rewards when that particular canvas reaches its allowance. Lowering the limit below its existing count blocks further rewards; changing it back to zero restores unlimited eligibility.
- Counts stay with the canvas through normal transfers and saves.
- Failed delivery, rejected requests, cancellation and duplicate/replayed packets do not consume successful uses.
- **Molten Canvas remains independent:** one successful reward per canvas, with no Infinity quota applied.
- Servers can enable supported modded rewards with `allowModdedRewards` and block specific IDs with `blockedRewardItems`.
- The mod selects the synchronized server-config type supported by the installed loader.

Rewards are newly created plain items, **one selected item/block per successful exchange**, even when the player does not already own it. Blank Canvas remains painting-only.

### 🛏️ Kurlo Pillow

- Adds a reusable item in the dedicated Canvas Studio+ creative tab.
- **Recipe:** one Enchanted Golden Apple in the center, surrounded by eight White Wool.
- Hold the same pillow in the same main-hand or off-hand slot from entering bed through a completed night's sleep.
- On normal morning wake-up, the server applies the installed game's enchanted golden apple status effects: **Regeneration, Absorption, Resistance and Fire Resistance**.
- Early/interrupted sleep, swapping away the pillow or relogging does not grant these effects.
- The pillow is not consumed, and it does not restore food points.

### Compatibility, upgrades and verification

Existing canvas/quill registry IDs and crafting progression remain, including the legacy `molder_canvas` and `molder_quill` IDs displayed as **Molten**. Existing painting maps remain usable. The dedicated creative tab contains all seven items. Credit remains **made by SuprixZ**.

Separate JARs are supplied for Minecraft 26.2 with NeoForge 26.2.x and Minecraft 26.3 with NeoForge 26.3.x, including beta builds. Java 25 is required. **Network protocol 4** requires matching client/server mod releases.

Release checks passed: **12 Java verification suites**, packaged-artifact checks, default/endpoint builds, and API/dependency-range audits across **75 published NeoForge 26.3 builds** and **91 published NeoForge 26.2 builds** at release time. These are automated checks; **interactive Minecraft, multiplayer, live placement/sleep and world save/reload tests were not performed**.

**Release files:** [Minecraft 26.3](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases/tag/v3.1.0) · [Minecraft 26.2](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases/tag/v3.1.0-mc26.2)
