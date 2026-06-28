# Loop Configuration — TaskHub

> How the autonomous maintenance loops run against this repo. Built with the
> [loop-engineering](https://github.com/cobusgreyling/loop-engineering) skill (Cobus Greyling /
> Addy Osmani). Start L1 (report-only); graduate to L2 only after `reference` checklist passes.

## Stack under maintenance

Spring Boot backend (Java 17, Maven) · React + Vite frontend · Flutter mobile · PostgreSQL 16 · Redis 7 · Gemini AI.

## Active Loops

| Pattern | Cadence | Status | Trigger |
|---------|---------|--------|---------|
| Daily Triage | 1d | **L1 report-only** | `/loop 1d Run $loop-triage` → update `STATE.md` |
| CI Sweeper | on red CI | L1 report-only | Summarize `.github/workflows/ci.yml` failures into STATE |
| Dependency Sweeper | weekly | L1 report-only | Scan `frontend/package.json` (npm) + `backend/pom.xml` (Maven); patch bumps only |

Week one is report-only: the loop writes findings to `STATE.md`; it does **not** edit code.

## Human Gates (required)

- **No auto-fix** until the L2 checklist passes and a human flips the flag in `STATE.md`.
- Any change touching a denylist path (`docs/safety.md`) → escalate, never auto-edit:
  `.env*`, Gemini key / `application.yml`, auth/JWT (SecurityConfig, JwtUtil/JwtFilter),
  DB migrations (`backend/src/main/resources/db/migration/V*.sql`), deploy config.
- Design decisions, multi-file refactors, schema changes → human review.

## Worktrees

- Spawn implementer sub-agents (L2+) with `isolation: worktree`. One worktree per fix attempt;
  discard after a verifier REJECT. Never let two loops share a tree.

## Connectors (MCP)

- Optional for L1 report-only.
- L2+: GitHub MCP scoped to **read CI/issues + comment/label only** (no merge). Loops never get the
  production DB or the Gemini key.

## Budget

- See `loop-budget.md` for token caps + kill switch.
- L1: **0 sub-agent spawns/run.** Review `STATE.md` daily.
- Maker/checker: implementer proposes; `.claude/agents/loop-verifier.md` must APPROVE before anything ships.

## Verification (source of truth)

- Frontend: `npm run build && npm run lint && npm run test` (runs anywhere with Node).
- Backend: `mvn -q test` (Java 17) — CI only; local sandbox can't.
- Mobile: `flutter analyze` / `flutter test` — local or CI.
- Not "done" until the relevant gate is green.

## Links

- Safety / denylist: `docs/safety.md` · Budget: `loop-budget.md` · Run history: `loop-run-log.md`
- Triage skill: `.claude/skills/loop-triage/SKILL.md` · Verifier: `.claude/agents/loop-verifier.md`
- CI audit: `.github/workflows/loop-audit.yml`
