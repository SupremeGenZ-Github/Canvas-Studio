# Canvas Studio 2.0.0

- Added Painting / Get Item mode in the canvas editor.
- Added asynchronous offline matching against vanilla item sprites and block textures from active resource packs.
- Rank up to three distinct item IDs using shape, contour, color and eight drawing orientations; show item names, real icons and visual similarity.
- Added palette-adjusted item references and cube references for blocks.
- Added server-side canvas-for-item exchange: one held canvas becomes one plain registered vanilla item. Reject missing/air/non-vanilla items, malformed palette data and blank drawings. Repeated packets cannot reuse the consumed canvas.
- Retained Canvas Studio 1.0.3's fixed left/right mouse input, swatches, import/export, drawing tools and painting saving.
- Retained Minecraft 26.3, exact NeoForge 26.3.0.51-beta, existing registry IDs and the “- made by SuprixZ” credit.
