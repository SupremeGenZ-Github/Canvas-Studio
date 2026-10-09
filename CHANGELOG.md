# Changelog

## Canvas Studio+ 2.1.2

- Refreshes Molten Canvas and Quill with molten gold/amber and iron accents.
- Refreshes Infinity Quill with lapis blue, echo teal and diamond-cyan accents.
- Normal Quill, Blank Canvas and Infinity Canvas textures remain unchanged. Recipes, IDs, Java gameplay code and networking are identical to 2.1.1.
- Adds automated texture size/transparency checks.


## Canvas Studio+ 2.1.1

- Renames Molder Canvas/Quill to Molten throughout names, tooltips and editor messages. Legacy item IDs remain for existing worlds.
- Molten Quill: centered Quill, four Iron Ingots in corners and four Gold Ingots at edge centers.
- Molten Canvas: shapeless Blank Canvas + Molten Quill + Compass.
- Infinity Quill: centered Molten Quill, Echo Shards in corners, Lapis Blocks left/right and Diamond Blocks top/bottom.
- Infinity Canvas: shapeless Molten Canvas + Infinity Quill + Recovery Compass.
- Updates recipe-book unlocks, documentation and crafting checks. Existing textures, painting, matching and server exchange rules remain unchanged.


## Canvas Studio+ 2.1.0

- Based on the full Canvas Studio 2.0.1 source; preserves painting, import/export, saving and the existing offline item matcher.
- Blank Canvas keeps its existing item ID and painting features; Get Item is locked.
- Adds single-use Molder Canvas and reusable Infinity Canvas, plus Molder and Infinity Quills.
- Adds shapeless canvas recipes and nine-slot shaped quill recipes, names, textures, tooltips, recipe unlock advancements and creative entries.
- Removes the feather shortcut recipe and its advancement.
- Server-issued, one-use exchange tokens validate the held stack, hand, tier, count, item ID and drawing. Tokens rotate for Infinity and reject duplicate/replayed requests.
- Infinity grants one new plain item per successful request without consuming its canvas; full inventories drop the reward nearby. Molder is exchanged only on success.
- Editor displays the canvas tier, availability and server response; repeated clicks while awaiting confirmation are ignored.
- Existing Blank Canvas items and saved Minecraft map paintings remain compatible.
- Retains “made by SuprixZ”, Minecraft 26.3 and NeoForge 26.3.x metadata including prereleases.

# Canvas Studio 2.0.1

- Accept all NeoForge 26.3.x releases, including beta builds.
- Keep Minecraft exactly 26.3 and preserve all edition features and SuprixZ credit.
- Add binary API compatibility evidence for all currently published 26.3 builds.
- Allow the development compile target to be selected with -PneoForgeVersion.

# Canvas Studio 2.0.0

- Added Painting / Get Item mode in the canvas editor.
- Added asynchronous offline matching against vanilla item sprites and block textures from active resource packs.
- Rank up to three distinct item IDs using shape, contour, color and eight drawing orientations; show item names, real icons and visual similarity.
- Added palette-adjusted item references and cube references for blocks.
- Added server-side canvas-for-item exchange: one held canvas becomes one plain registered vanilla item. Reject missing/air/non-vanilla items, malformed palette data and blank drawings. Repeated packets cannot reuse the consumed canvas.
- Retained Canvas Studio 1.0.3's fixed left/right mouse input, swatches, import/export, drawing tools and painting saving.
- Retained Minecraft 26.3, exact NeoForge 26.3.0.51-beta, existing registry IDs and the “- made by SuprixZ” credit.
