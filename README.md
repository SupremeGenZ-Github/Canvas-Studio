# Canvas Studio

**- made by SuprixZ**

Canvas Studio is a Minecraft mod for drawing, importing images, and saving custom paintings. The full edition can also turn your drawing into the nearest matching Minecraft item or block.

> **Currently available only for NeoForge 26.3.0.51 (26.3.0.51-beta), on Minecraft Java Edition 26.3.**
> Java 25 is required. Fabric, Forge, and other Minecraft or NeoForge versions are not currently supported.

## History

Canvas Studio Lite 1.0.3 provides the painting editor. Canvas Studio 2.0.0 builds on that editor and adds an offline item finder and draw-to-item mode. Both editions are maintained in this repository and published as separate releases.

## Downloads

Download the JAR for your preferred edition from [GitHub Releases](../../releases).

| Release | Edition | Painting and image import | Item/block finder |
| --- | --- | --- | --- |
| [1.0.3](../../releases/tag/v1.0.3) | **Canvas Studio Lite** | Yes | No |
| [2.0.0](../../releases/tag/v2.0.0) | **Canvas Studio** | Yes | Yes |

Install **one edition at a time**. Both share the `canvasstudio` mod ID so existing canvases and saved paintings remain compatible. Multiplayer clients and servers must use the same edition and version.

## Features

### Both editions

- Craft a Quill and a Blank Canvas.
- Draw with selectable colors and brush sizes; erase, fill, undo, and clear.
- Import your own PNG or JPEG images; fit or crop them to the canvas.
- Export artwork as a PNG.
- Save named paintings as locked maps and display them in normal or glowing item frames.
- Keep the artwork when the world is saved and reopened through vanilla map data.

### Canvas Studio 2.0.0

- Switch between **Painting** and **Get Item** modes.
- Draw an item or block, then find the closest visual matches.
- Compare the top three candidates with names, inventory icons, and similarity scores.
- Receive one chosen item/block by exchanging one Blank Canvas.
- Match locally against Minecraft textures without an API key or cloud service.

The default Minecraft 26.3 catalogue contains 1,198 supported vanilla inventory items and blocks. Matching compares shape, outline, color, and orientation. It is approximate: rough drawings and similar-looking items may produce unexpected results. Similarity scores are not probabilities. Special models and unsupported texture layouts may be absent from the catalogue.

## Installation

1. Install Minecraft Java **26.3** with **NeoForge 26.3.0.51-beta** and Java **25**.
2. Download either Canvas Studio Lite 1.0.3 or Canvas Studio 2.0.0 from Releases.
3. Put the JAR in that instance's `mods` folder. Remove earlier Canvas Studio JARs first.
4. Launch the NeoForge instance. For multiplayer, install the same release on the server and every client.

## How to use

**Quill:** Feather + Ink Sac.  
**Blank Canvas:** Leather + Paper + Quill.  
**Alternative Blank Canvas:** Leather + Paper + Feather.

Recipes are shapeless and consume their ingredients. Canvas and Quill are also available in the creative Tools & Utilities tab.

Hold a Blank Canvas and use it to open the editor. **Left-click/drag draws**, **right-click/drag erases**, and clicking a color swatch selects that color. Import/export and drawing tools are available in both editions. Save Painting creates a named map for an item frame.

In Canvas Studio 2.0.0, switch to **Mode: Get Item**, draw an item/block, and click **Find Item / Block**. Choose **Get closest** or another candidate. The held canvas becomes **one plain item/block**; no custom NBT or enchantments are copied. Back to drawing lets you revise without spending the canvas.

## Other versions and source

- `main`: Canvas Studio 2.0.0, with item finder.
- `lite`: Canvas Studio Lite 1.0.3, without item finder.
- `v1.0.3` and `v2.0.0`: source snapshots for the corresponding releases.

## Building

Use a JDK 25:

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

JARs are written to `build/libs`. The Gradle wrapper downloads build dependencies; standalone verification downloads a checksum-verified Minecraft 26.3 JAR. Build and verification outputs are not committed.

## Testing and issues

Automated checks cover editor mouse input, image import, save packets, recipe/advancement parsing, and, in the full edition, matching and exchange validation. See [VALIDATION.md](VALIDATION.md) for the exact coverage and in-game checks still needed. Automated checks do not guarantee interactive or multiplayer compatibility.

Report problems in [Issues](../../issues) with the edition/version, NeoForge version, `logs/latest.log`, reproduction steps, and a screenshot when useful.

## License

MIT. See [LICENSE](LICENSE).
