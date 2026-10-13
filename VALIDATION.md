# Canvas Studio+ 3.1.2 validation — Minecraft 26.3

Baseline: Canvas Studio+ 3.1.0, main commit a3d87a841e0f260d63fc3c3c4394e6fdc4426f18. Credit: **made by SuprixZ**.

Release gates: Gradle build/check on default and earliest/latest tested NeoForge targets, all thirteen Java verification suites, packaged artifact checks, published NeoForge API audit and dependency-range checks. Release-attached results record the exact run, hash and audited count.

New automated checks enumerate all 64 width/height combinations in all four horizontal directions: unique occupied cells, lower-left anchor, top-right endpoint, physical bounds and wall thickness. They check persistent and wire codecs, invalid/truncated dimensions, CSP v3 roundtrip and v2 migration, copied artwork sizes and save/edit packet dimensions. Existing suites retain drawing, input, recipes, matching, reward handling, replay rejection, painting transfer and Kurlo Pillow coverage.

No interactive Minecraft client, rendered painting inspection, actual wall placement, multiplayer session or live world save/reload was performed. The headless geometry and codec checks do not prove visual rendering or in-game behavior. Manual checks remaining: 1×1, 2×3 and 8×8 placement on each wall direction; edges and missing supports; obstruction and overlap rejection; survival drops retaining size; client reconnection and server restart; resizing a pre-3.1.2 map; all previous painting and item-finder workflows.

NeoForge 26.3.x, including betas, is supported by the declared family range. API audits establish referenced binary-member availability across published builds, not complete behavioral compatibility or a guarantee for future versions.
