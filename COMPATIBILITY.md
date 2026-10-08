# NeoForge 26.3 compatibility

Canvas Studio Lite 1.0.4 supports the NeoForge **26.3.x** series on Minecraft Java **26.3**, Java **25**.

Dependency range: `[26.3-alpha,26.4-alpha)`. The alpha boundary includes the 26.3 beta series and excludes the next 26.4 series, including its prereleases. Minecraft stays `[26.3]`.

The compatibility audit checks every NeoForge 26.3 artifact published on 8 October 2026, from 26.3.0.0-beta through 26.3.0.57-beta. It parses the mod class files and resolves the exact referenced NeoForge/FML/event-bus classes, methods and fields, including inherited members, against each published artifact and its declared loader/bus dependencies. Inherited Minecraft interface methods are resolved against the vanilla Minecraft 26.3 baseline; all builds declare NeoForm 26.3-1. NeoForge Minecraft patch differences and native runtime behavior are outside this audit. Artifact hashes and results are in `verification/compatibility-audit.json`.

This is binary API evidence, not a test of startup, native dialogs, freehand matching or multiplayer gameplay on every version. The update preserves existing class bytes and gameplay resources; only the version and NeoForge dependency metadata change. Previously completed behavioral checks are documented in VALIDATION.md. Future 26.3 versions are accepted but cannot be tested or guaranteed before they exist.

Install one edition only, and use matching edition/version on clients and servers. The full edition still creates one new selected vanilla item/block in exchange for a Blank Canvas, without requiring that item to be present in the inventory. Lite remains painting-only.
