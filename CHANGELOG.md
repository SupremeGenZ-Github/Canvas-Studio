# Canvas Studio Lite 1.0.3

- Fixed drawing, erasing and palette selection using Minecraft 26.3 mouse button constants. Left-click previously erased; right-click and palette clicks were ignored.
- Added checks exercising the actual editor click/drag/fill handlers with Minecraft mouse constants at all three canvas scales.

# Canvas Studio 1.0.2

- Fixed all three recipe advancements for Minecraft 26.3: recipe_unlocked uses a recipes list. The previous singular field prevented registry loading and left the client at Preparing world.
- Preserved NeoForge 26.3.0.51-beta support and the SuprixZ credit.

# Canvas Studio 1.0.1

- Rebuilt for Minecraft 26.3 and exactly NeoForge 26.3.0.51-beta.
- Replaced TinyFD image selection with the SDL dialog library included by this NeoForge loader. Dialog selection runs asynchronously and image decoding remains off the game thread.
- Retained crafting, painting, PNG/JPEG import/export and saved map artwork. Existing saved paintings use the same vanilla map data.
- Added Prism launcher load-order troubleshooting based on the earlier launch log.
- Added “- made by SuprixZ” to the editor heading and mod description; listed mod author is SuprixZ.
