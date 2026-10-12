# Canvas Studio+ 3.0.0

**made by SuprixZ**

A local pixel-art studio and offline drawing-to-item finder for Minecraft Java **26.3**, **NeoForge 26.3.x** (including published prereleases), and **Java 25**. Install the same version on client and server. Replace older Canvas Studio JARs; Lite and Plus share the `canvasstudio` ID and cannot be installed together.

The separate Minecraft 26.2 edition uses a different JAR. Do not install a 26.3 JAR in 26.2. Metadata accepts future builds within the corresponding NeoForge family, but future API compatibility cannot be guaranteed.

## Draw and edit

Right-click a canvas to open the editor. The Canvas Studio+ creative tab contains all six items. The existing 128 × 128 map palette and textures are preserved.

- Left-drag paints; right-drag erases. On upper layers, erasing reveals lower layers.
- Click the active-tool button to choose brush, eraser, fill, line, outlined/filled rectangle, outlined/filled ellipse, eyedropper, dotted, dashed or spray tools.
- Hold **Shift** while drawing a line to constrain its direction; Shift with rectangles/ellipses produces squares/circles.
- **B/E/I** select brush/eraser/eyedropper; **G** toggles grid; **Ctrl+Z/Y** undo/redo. Tool shortcuts are ignored while typing in text fields. Escape cancels an active stroke, or returns/closes the current screen.
- Options include horizontal/vertical mirrors, grid, snapping, 1×/2×/4× zoom and Default/Grayscale/Retro 8 palettes. Middle-drag pans a zoomed canvas. The pixel grid becomes visible at sufficiently enlarged scales.
- Undo groups a complete stroke or shape into one action. History retains up to 32 operations in memory; new edits clear redo.
- Studio menu contains image import, fit/crop, PNG export, project saving, layers, preview, catalogue and active-layer clearing.

## Edit an existing saved painting

Remove the painting from its frame, hold **one locked painting map**, then **Shift + right-click in the air**. The server sends its pixels to the editor. This also supports older Canvas Studio paintings and other held locked maps.

Saving replaces the held map with a **new locked map** containing the edited artwork. Other copies of the original retain their artwork. Put the edited map back into its frame. Map editing has no Get Item access and grants no extra canvas or reward.

A map stores finished pixels, so it reopens as one layer. Keep a `.csp` project when you need to retain editable layers. Editing sessions expire after 20 minutes; if a session expires, save a project and reopen the held painting before saving the edited map.

## Projects, layers and sharing

**Save editable project** stores artwork under `canvasstudio/projects`. **Save Painting** is separate: it creates a locked Minecraft map and consumes/replaces the held canvas as before, including an Infinity Canvas.

Projects retain up to 16 named layers, visibility, ordering, selected layer, map pixels, brush/tool/color/mirror/grid/snap settings and the fitted original image when available. Layers can be added, duplicated, moved, renamed, hidden, deleted and merged down. The final image composites visible layers over map-palette white.

The Projects gallery provides thumbnails, paging, open, save current, rename, duplicate, delete and sharing. Select an entry, enter a new title in the field, then Rename. To import a shared project, paste its full `.csp` file path into the field and click Import. Export writes a shareable copy under `canvasstudio/exports`; PNG export uses the same directory. Gallery browsing displays at most 200 valid projects.

The reserved recovery project is saved every 30 seconds while the editor is active and on closing. It is separate from named projects and can be reopened from the gallery. It is one local recovery slot shared across worlds. Project history is not persisted; layer data is. See [project format](docs/PROJECT_FORMAT.md).

## Finder and previews

Draw an item/block and choose **Draw to Get Item**. Visual matching is offline and approximate. Choose 3, 6, 12 or 24 results, search names/IDs, filter Items/Blocks/Vanilla/Modded, and mark favorites. Recent results and favorites are stored locally. Catalogue browsing works without a drawing but does not grant rewards.

Supported modded PNG assets and simple model texture references can be indexed; unsupported special/composite models are skipped. Animated textures use their first square frame. Cube references approximate block appearance; exact custom model transforms and GPU-rendered models are not supported. At most 5,000 source references are indexed. Resource reloads trigger rebuilding of an open catalogue. Servers must explicitly enable modded rewards.

Preview compares the stored fitted source image with final map colors. If no original source exists, both panels show the map artwork. The 2D wall planner has six slots: select the current drawing or a saved project, left-click to place and right-click to clear. Frame style and display size can be changed. This is a mock-up; it does not place frames or simulate actual world lighting.

## Canvas rewards and administration

| Canvas | Painting | Get Item |
|---|---|---|
| Blank | Yes | Locked |
| Molten | Yes | One successful reward replaces the canvas |
| Infinity | Yes | Reusable; unlimited by default |

Rewards are newly created plain items, one per successful exchange, regardless of inventory ownership. Infinity inserts the reward into inventory or drops it when full. Failed delivery, cancellation, invalid requests and duplicate packets do not consume uses. Molten does not use Infinity quotas.

Server configuration is named `canvasstudio-server.toml`. Newer loaders store it in `config`, with a world-specific `syncedconfig` override. Older loaders use the world's `serverconfig` directory. Edit the file generated by your installed loader, then restart the world/server. The mod selects the supported synchronized server configuration type at runtime.

```toml
maxInfinityCanvasUses = 0
allowModdedRewards = false
blockedRewardItems = []
```

`0` means unlimited. A positive maximum limits successful rewards per individual Infinity Canvas. Counts remain on the item through transfers and saves, and accumulate even while unlimited. Older canvases begin at zero. Lowering a limit below an existing count blocks further rewards; returning to zero restores unlimited eligibility. Normal painting, projects and crafting are unaffected. Administrative/creative duplication and external item-data editing are outside normal-play quota guarantees.

Blocked IDs reject individual rewards, including Molten requests for those IDs. There is no global Molten disable switch, Infinity cooldown applied to Molten, or player-wide quota. Configure item restrictions sensibly if Molten rewards must remain available. Protocol **4** requires matching client/server releases. Clients never supply trusted usage counts, item components or project paths to the server.

## Recipes and compatibility

All registry IDs, existing recipes and item textures are retained. Legacy `molder_canvas` / `molder_quill` IDs still display as Molten.

- Quill: Feather + Ink Sac, shapeless.
- Blank Canvas: Leather + Paper + Quill, shapeless.
- Molten Quill: Quill center, Iron Ingots in four corners, Gold Ingots on four edges.
- Molten Canvas: Blank Canvas + Molten Quill + Compass, shapeless.
- Infinity Quill: Molten Quill center; Echo Shards in corners, Lapis Blocks left/right, Diamond Blocks top/bottom.
- Infinity Canvas: Molten Canvas + Infinity Quill + Recovery Compass, shapeless.

Build with JDK 25: `./gradlew build`. Alternate supported dependency: `./gradlew build -PneoForgeVersion=<version>`. See [validation](VALIDATION.md) for executed checks and limitations. No telemetry, cloud account or network service is required for creative tools or matching.
