# Canvas Studio+ 3.1.0 validation — Minecraft 26.3

Baseline: main commit `b508f28a3bd8d4bf685db92f5cad943681bc1bd7` (2.1.2); its original build and publishing checks passed before development. Credit: **made by SuprixZ**.

## Release gates

JDK 25 Gradle build/check on the selected development target and published compatibility endpoints; packaged artifact checks; class/member API audit of every published NeoForge 26.3 build; Maven version-range checks against every published loader pair. Publication is gated by successful checks. The release-attached copy records the final run, artifact hash and exact audited count.

## Automated scope

Existing checks cover real Minecraft recipe/advancement codecs, palette conversion and image rejection, packet roundtrips, vanilla asset matching, server sessions and replay rejection, inventory/drop reward delivery adapters and texture integrity.

Added checks cover persistent counter codec and boundaries, finite/unlimited policy, independent counters, failed delivery, Molten independence and the production exchange transaction with replayed packets. Artwork tests cover stroke undo/redo, rectangles/ellipses, mirroring, layer visibility/duplication/order/merge, project source-image/layer/settings roundtrips, version-1 migration, invalid versions/truncation/size rejection, gallery save/load/rename/duplicate/delete, sharing and symlink-target preservation. Headless editor tests cover actual mouse constants at three GUI scales, shortcut focus guards, undo/redo shortcuts and zoom/pan pointer mapping. Catalogue tests resolve a synthetic modded model parent and texture alias and skip an unsupported special model. Painting-transfer packets are roundtrip-tested.

The API audit checks referenced NeoForge/FML/bus classes and binary members. It does not prove complete behavioral compatibility. Reference availability and headless checks are distinct from launching every loader version.

## Not performed

No interactive Minecraft client, multiplayer session, live world-save migration, native image-picker operation, rendered UI inspection or actual server restart was tested in this environment. Persistent usage uses Minecraft's registered saved and network-synchronized data component; its codec and item operations are tested, while actual world save/reload remains a manual check. Saved-map editing packet transfer is tested; the live hold/map/session workflow still requires in-game verification.

Before relying on a production world, verify: all controls at intended GUI scales; drawing and typing; library/recovery and sharing; resource-pack reload while matching; modded rewards with server configuration; both hands; inventory-full drops; limits across item transfer/relog/world restart; editing an older held locked painting and confirming other map copies remain unchanged; normal/glowing frame and wall previews. Back up worlds before upgrading Minecraft itself.

## Feature boundaries

Model-based matching supports simple texture references, not full GPU model capture. Previews are 2D mock-ups. Maps reopen as one flattened layer; `.csp` files preserve layers. Project gallery is capped at 200 valid entries and asset indexing at 5,000 sources. Future NeoForge 26.3 builds are accepted by metadata but cannot be guaranteed free of API-breaking changes.

## Frameless paintings and Kurlo Pillow

Additional automated checks cover placement eligibility, build/space/locked-map restrictions, the absence of an item-frame drop, completed versus interrupted/early sleep, and the installed game's actual enchanted golden apple status-effect catalogue. Recipe checks include all nine Pillow crafting slots. Wall entities reuse Minecraft hanging-map synchronization and serialization; native map rendering is registered for the custom type.

No live placement, support-removal drop, world save/reload, sleep cycle or rendered wall entity was tested here. These remain explicit in-game checks alongside the existing manual checklist.
