# Canvas Studio 2.0.0 verification

Target: Minecraft Java 26.3 / NeoForge 26.3.0.51-beta / JDK 25.

## Automated coverage

- Full compilation against the exact NeoForge beta.
- Existing PNG/JPEG import, resize/crop, transparency, unsupported/corrupt/oversize rejection and map quantization checks.
- Save-painting and draw-item packet encode/decode roundtrips; truncated and wrong-length payload rejection.
- Actual editor input handlers: left paint/drag, right erase/drag, palette, fill/erase and middle-click rejection at scales 1, 2 and 3.
- Minecraft 26.3 advancement/recipe codecs, including rejection of the old advancement format.
- Synthetic matching: distinct sword/apple silhouettes, translated/scaled/rotated drawings, closed black outline, blank drawing rejection, unique item IDs and bounded similarity.
- Actual Minecraft 26.3 asset catalogue: 1,198 distinct inventory items/block items. All nine sampled map-palette item sprites appeared in their own top-three matches: diamond, diamond sword, apple, stick, bread, iron pickaxe, golden apple, emerald and bow. This is a sprite roundtrip check, not a measurement of freehand recognition accuracy.
- Minecraft registry item validation accepts diamond sword and stone; rejects air, nonexistent IDs, other namespaces, malformed IDs and null.
- Draw-item validation rejects empty canvases, invalid palette IDs and incorrect bitmap length. Its blank color matches the editor's actual map palette.
- Archive metadata and resources checked before delivery. Verification fixtures are not packaged in the mod JAR.

## Limits and in-game acceptance

No interactive Minecraft client or multiplayer session was used. Native import dialog, actual rendered result cards, server reward execution and resource-pack reload interaction still require in-game acceptance.

The input fixture allocates a headless test client shell without starting Minecraft; Unsafe is confined to verification. Game-codec and asset fixtures place the checksum-verified unmodified game JAR first on their classpath because patched NeoForge bootstrap requires a running FML loader.

Matching is local visual comparison with a supported vanilla texture catalogue, not a semantic AI model. The client selects a match. The server validates the vanilla item ID, held canvas and drawing data and performs the one-for-one exchange; it does not recompute visual recognition against client resource packs. Receiving items this way is the enabled gameplay feature.

In-game checks: draw a colored sword and a cube; inspect the nearest result and alternatives; confirm one canvas becomes exactly one chosen item in both main and off hand; confirm another click cannot duplicate it; return to drawing without spending the canvas; confirm normal Save Painting still produces item-frame artwork; repeat on a server with the same full version.

## Build environment

NeoForm Runtime's local process-command lookup needed the same sandbox-only JDK-path fallback used for earlier builds. This modifies build tooling only and is absent from the source project and mod JAR. No world was started or Minecraft EULA accepted.
