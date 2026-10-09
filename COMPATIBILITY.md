# Minecraft 26.2 port compatibility

Canvas Studio+ 2.1.2 — made by SuprixZ. This port builds specifically against Minecraft 26.2 / NeoForge 26.2.0.88 / Java 25, using FML 11. The metadata pins that target. The 26.3 edition remains separately available.

26.2 uses TinyFD for native image selection, its own Minecraft client validation artifact, Predicate resource selectors and the 26.2 server drop API. Advancements use the singular 26.2 `recipe` field. Recipe ingredients, textures, item IDs, map format, matching rules and server canvas permissions are retained. The actual available vanilla catalogue follows 26.2 resources. Existing 26.3 saves cannot be assumed safe to downgrade.

Historical 26.3 audit files are retained as history and do not validate this 26.2 port. Read VALIDATION.md and the attached build logs for the port's checks. No support for other Minecraft versions or future NeoForge versions is claimed.
