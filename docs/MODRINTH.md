# Canvas Studio

**- made by SuprixZ**

Draw your own artwork, import images, and turn them into custom paintings in Minecraft. Choose **Canvas Studio Lite** for painting, or **Canvas Studio** for painting plus an offline item/block finder that turns a drawing into a matching inventory item.

This README covers both editions. Use the compatibility information for the specific file you download when installing a current or future release.

## Choose your edition

| Feature | Canvas Studio Lite | Canvas Studio |
| --- | --- | --- |
| Canvas and quill crafting | Yes | Yes |
| Drawing, colors, brushes, eraser and fill | Yes | Yes |
| Undo, clear and PNG/JPEG import | Yes | Yes |
| PNG export and saved paintings | Yes | Yes |
| Item/block matching and three suggested results | No | Yes |
| Exchange a canvas for a selected item/block | No | Yes |

Both editions use the `canvasstudio` mod ID. **Install one edition only.** Clients and servers must use the same edition and release.

## Compatibility

The current compatibility releases are **Lite 1.0.4** and **Canvas Studio 2.0.1**. They require **Minecraft Java 26.3**, **NeoForge 26.3.x**, and **Java 25**. The NeoForge range includes beta builds. Fabric, Forge, other Minecraft versions and NeoForge 26.4+ are outside this release's support.

Future NeoForge builds within the 26.3 series are accepted by these files' dependency metadata. Compatibility with unreleased API changes cannot be guaranteed. Future Minecraft versions or NeoForge series require a separately validated release; editing a description does not add runtime support.

For future mod releases, check the selected file's game version, loader and release notes. On Modrinth, choose a file marked for your Minecraft version and NeoForge. Each release's metadata takes precedence over this general project overview. Older Lite 1.0.3 and full 2.0.0 files retain their exact NeoForge 26.3.0.51-beta requirement.

See [combined validation](https://github.com/SupremeGenZ-Github/Canvas-Studio/blob/main/VALIDATION.md) for tested versions and remaining checks.

## Download and install

Get your edition from [GitHub Releases](https://github.com/SupremeGenZ-Github/Canvas-Studio/releases), or the corresponding project's Versions tab on Modrinth when available.

1. Create a Minecraft instance with the game version, NeoForge version and Java version listed for your chosen release.
2. Download the edition's **JAR**. Source ZIPs are for development.
3. Remove older Canvas Studio JARs and place the chosen JAR in the instance's `mods` folder.
4. Launch the instance. For multiplayer, install the same edition and release on the server and every client.

## Crafting and painting

Recipes are shapeless:

| Result | Ingredients |
| --- | --- |
| Quill | Feather + Ink Sac |
| Blank Canvas | Leather + Paper + Quill |
| Blank Canvas, alternative recipe | Leather + Paper + Feather |

Hold a Blank Canvas and use it to open the editor. Left-click and drag to paint; right-click and drag to erase. Select a color and brush size, or use fill, undo and clear.

Import a PNG or JPEG and choose fit or crop. Transparent areas are flattened onto white. Export your artwork as a PNG, or use **Save Painting** to turn the canvas into a named map. Display the map in a normal or glowing item frame. Saved artwork uses Minecraft's map data.

## Draw an item or block

In the full edition, switch to **Mode: Get Item**, draw an item or block, then click **Find Item / Block**. Compare up to three suggested results and choose **Get closest** or another candidate. Returning to drawing lets you revise without spending the canvas.

Choosing a result exchanges one held Blank Canvas for **one newly created plain item/block**. You do not need that item in your inventory. Custom data and enchantments are not copied.

Matching runs locally without an API key or cloud service. It compares shape, outline, color and orientation against supported vanilla inventory textures. It is approximate: rough drawings, similar shapes and unsupported special models can produce unexpected results. Scores measure visual similarity, not probability. Modded items are outside the current finder catalogue.

## Validation and support

Both editions share automated editor and image checks; the full edition adds item matching and exchange validation. Current compatibility releases passed API signature checks across 56 published NeoForge 26.3 builds. Interactive gameplay across that matrix has not been tested.

Read the [combined validation report](https://github.com/SupremeGenZ-Github/Canvas-Studio/blob/main/VALIDATION.md) for completed checks, limitations and future-release validation.

Report bugs in [GitHub Issues](https://github.com/SupremeGenZ-Github/Canvas-Studio/issues), including your edition/release, Minecraft and NeoForge versions, and reproduction steps.

**- made by SuprixZ** · [MIT license](https://github.com/SupremeGenZ-Github/Canvas-Studio/blob/main/LICENSE)
