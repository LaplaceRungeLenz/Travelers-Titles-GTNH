# GTNH titles implementation plan

Goal: implement the approved design in `2026-09-22-gtnh-travelers-titles-design-roadmap.md`.
Architecture: pure Java trigger/rule core, Forge client adapter, optional reflective GC adapter, resource-manager based presentation.
Tech stack: Forge 1.7.10, Java 8 bytecode, GTNH convention 2.0.29, Gradle 9.4.0, JUnit 4.
Execution: inline using executing-plans; the user explicitly approved implementation, testing and private synchronization.

## Constraints
- Client only; no server requirement or world-generation changes.
- No hardcoded dimension IDs in built-in identification; retain complete integer biome IDs.
- Missing optional mods and resources fall back safely.
- Use layered rules, original resource pack priority, Chinese/English and reload.
- Do not alter the everyday Prism instance or publish any release.

## Review focus
- Same biome on different planets must not suppress the planet title.
- A temporary null world must not leak the old world's pending title.
- A bad high-priority resource must not remove valid fallback settings.
- Resource packs must not silently override explicit local settings.
- Optional mod reflection must never execute server-only data access.

## Tasks and completion checks
- [x] M1: wrapper/build configuration, test dependencies and client-only entrypoint. `gradlew test build` must produce a reobfuscated Java 8 jar.
- [x] M2: `core/Location`, `core/TitleController`, `core/Animation`. Test initially missing implementations, then verify debounce, latest candidate, same-name dimensions, reset, zero duration.
- [x] M3: `core/RuleEngine`, `core/TitleStyle`, `client/TitleResources`, `client/ClientConfig`, `client/ConfigGuiFactory`, `client/HudRenderer`. Test layering, invalid fields, exact/glob matches and resource fallback. Verify rendering in-game.
- [x] M4: `client/LocationResolver`, `client/SpaceCompat`, `client/TitleCommand`. Match runtime provider/body rather than configured numeric IDs; enumerate runtime biome/body directories and test absent GC.
- [x] M5: `scripts/` isolated test setup, runtime probes and screenshots. Run automated suite plus target instance. Record pass/fail/not tested separately.
- [ ] M6: README, resource-pack guide, example pack, license and test report. Review changes, commit, create new private GitHub repo, push, verify private visibility, remote SHA, zero releases.

Each task uses a failing behavioral test before implementation where executable logic is involved. Core tests exercise real objects with hand-derived expected results. Runtime-specific behavior is verified with a development probe and real client observations. Keep implementation choices and evidence in `docs/progress.md`.

Validation coverage and deferred environment checks are explicitly listed in test-report.md; checked milestones indicate delivered implementation, not exhaustive runtime coverage.
