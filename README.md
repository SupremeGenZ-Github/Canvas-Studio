# Canvas Studio

**- made by SuprixZ**

Canvas Studio is a Minecraft mod for drawing, importing images, and saving custom paintings. The full edition can also turn your drawing into the nearest matching Minecraft item or block.

> **Supports NeoForge 26.3.x on Minecraft Java Edition 26.3, including the 26.3 beta builds.**
> Java 25 is required. Fabric, Forge, NeoForge 26.4+, and other Minecraft versions are not supported.

## History

Canvas Studio Lite 1.0.4 provides the painting editor. Canvas Studio 2.0.1 builds on that editor and adds an offline item finder and draw-to-item mode. Both editions are maintained in this repository and published as separate releases.

## Downloads

Download the JAR for your preferred edition from [GitHub Releases](../../releases).

| Release | Edition | Painting and image import | Item/block finder |
| --- | --- | --- | --- |
| [1.0.4](../../releases/tag/v1.0.4) | **Canvas Studio Lite** | Yes | No |
| [2.0.1](../../releases/tag/v2.0.1) | **Canvas Studio** | Yes | Yes |

Install **one edition at a time**. Both share the `canvasstudio` mod ID so existing canvases and saved paintings remain compatible. Multiplayer clients and servers must use the same edition and version.

## NeoForge compatibility

The dependency range now accepts the entire **26.3.x** series, rather than only 26.3.0.51-beta. Binary API references were checked against every published 26.3 build available on 8 October 2026 (26.3.0.0-beta through 26.3.0.57-beta). See [COMPATIBILITY.md](COMPATIBILITY.md). Future 26.3 builds are accepted, but future API changes cannot be guaranteed before those builds exist. No interactive world/multiplayer tests were run across the version matrix.

Older Lite 1.0.3 and full 2.0.0 downloads remain available in Releases and keep their original 26.3.0.51-beta requirement.

## Features

### Both editions

- Craft a Quill and a Blank Canvas.
- Draw with selectable colors and brush sizes; erase, fill, undo, and clear.
- Import your own PNG or JPEG images; fit or crop them to the canvas.
- Export artwork as a PNG.
- Save named paintings as locked maps and display them in normal or glowing item frames.
- Keep the artwork when the world is saved and reopened through vanilla map data.

### Canvas Studio 2.0.1

- Switch between **Painting** and **Get Item** modes.
- Draw an item or block, then find the closest visual matches.
- Compare the top three candidates with names, inventory icons, and similarity scores.
- Receive one chosen item/block by exchanging one Blank Canvas.
- Match locally against Minecraft textures without an API key or cloud service.

The default Minecraft 26.3 catalogue contains 1,198 supported vanilla inventory items and blocks. Matching compares shape, outline, color, and orientation. It is approximate: rough drawings and similar-looking items may produce unexpected results. Similarity scores are not probabilities. Special models and unsupported texture layouts may be absent from the catalogue.

## Installation

1. Install Minecraft Java **26.3** with **NeoForge 26.3.x** and Java **25**.
2. Download either Canvas Studio Lite 1.0.4 or Canvas Studio 2.0.1 from Releases.
3. Put the JAR in that instance's `mods` folder. Remove earlier Canvas Studio JARs first.
4. Launch the NeoForge instance. For multiplayer, install the same release on the server and every client.

## How to use

**Quill:** Feather + Ink Sac.  
**Blank Canvas:** Leather + Paper + Quill.  
**Alternative Blank Canvas:** Leather + Paper + Feather.

Recipes are shapeless and consume their ingredients. Canvas and Quill are also available in the creative Tools & Utilities tab.

Hold a Blank Canvas and use it to open the editor. **Left-click/drag draws**, **right-click/drag erases**, and clicking a color swatch selects that color. Import/export and drawing tools are available in both editions. Save Painting creates a named map for an item frame.

In Canvas Studio 2.0.1, switch to **Mode: Get Item**, draw an item/block, and click **Find Item / Block**. Choose **Get closest** or another candidate. The held canvas becomes **one plain item/block**; no custom NBT or enchantments are copied. Back to drawing lets you revise without spending the canvas.

## Other versions and source

- `main`: Canvas Studio 2.0.1, with item finder.
- `lite`: Canvas Studio Lite 1.0.4, without item finder.
- `v1.0.4` and `v2.0.1`: source snapshots for the corresponding releases.

## Building

Use a JDK 25. The default development compile target remains 26.3.0.51-beta; it does not restrict the supported runtime range. To build against another 26.3 release, use `./gradlew build -PneoForgeVersion=26.3.0.57-beta`.

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
