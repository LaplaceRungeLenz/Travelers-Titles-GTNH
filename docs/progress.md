# Implementation ledger

Spec: 2026-09-22-gtnh-travelers-titles-design-roadmap.md. Plan: implementation-plan.md.

- User approved implementation on 2026-09-22.
- Ruling: use the fresh user-provided repository directory on an implementation branch; no existing code or main branch needs a separate worktree.
- Ruling: use already available official convention 2.0.29 / Gradle 9.4.0, pinned; no automatic updates or publishing.
- Pre-flight: controller consumes Location; renderer consumes TitleStyle; client resolver supplies Location; resource loader supplies RuleEngine. Core is independent of Minecraft.
