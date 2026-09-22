# Implementation ledger

Spec: 2026-09-22-gtnh-travelers-titles-design-roadmap.md. Plan: implementation-plan.md.

- User approved implementation on 2026-09-22.
- Ruling: use the fresh user-provided repository directory on an implementation branch; no existing code or main branch needs a separate worktree.
- Ruling: use already available official convention 2.0.29 / Gradle 9.4.0, pinned; no automatic updates or publishing.
- Pre-flight: controller consumes Location; renderer consumes TitleStyle; client resolver supplies Location; resource loader supplies RuleEngine. Core is independent of Minecraft.

- M1-M4 first build passed (test + Spotless + Checkstyle + reobfuscation). 16 core tests passed; 3 optional-API tests added.
- Runtime QA uses a separate opt-in source directory excluded from normal JARs, and requires an isolated-instance marker. The original instance has not been modified.
- Review fixes: actual loaded-chunk guard, GL blending after background, safe texture decoder for first load and TextureManager reload, BOP/HEE aliases, plus and Unicode names.
- Final normal clean build passed 25 tests, Spotless and Checkstyle. Artifact contains 24 Java 8 classes, no QA code or bundled dependencies.
- Production class/resource entries are byte-for-byte identical between the final normal JAR and the final in-game QA JAR.
- Runtime resource tests passed missing/invalid textures, malformed JSON, local overrides, full resource reload, sound registration, and valid-to-corrupt-to-restored PNG reload.
- Complete runtime evidence and honest coverage limits are recorded in test-report.md and qa/*.json.
