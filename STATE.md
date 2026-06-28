# Loop State — TaskHub

> Durable memory spine for the maintenance loops. The loop reads this at the start of every run and
> writes back what it learned. Humans skim the top section daily.

Last run: 2026-06-28 (L1 Daily Triage — report-only, via loop-engineering skill)

## High Priority (loop is acting or waiting on human)

- _none_

## Watch List (monitor, do not act yet)

- **Dependency drift (frontend npm).** Safe patch/minor bumps available — candidates for the
  Dependency Sweeper at L2: `autoprefixer 10.4.27→10.5.2`, `axios 1.16.1→1.18.1`,
  `postcss 8.5.14→8.5.15`, `react-router-dom 6.30.3→6.30.4`. **Held behind human gate** (majors):
  react 18→19, eslint 8→10, tailwind 3→4, react-leaflet 4→5, @vitejs/plugin-react 4→6, jsdom 24→29.
- **CRLF line-ending churn.** Working tree periodically shows ~25k-line diffs that are pure CRLF↔LF.
  A one-time `.gitattributes` (eol=lf) normalization would end it. Decision pending with human.
- **Backend/mobile not buildable in the local sandbox** (no JDK17/Maven/Flutter). CI
  (`.github/workflows/ci.yml`, JDK 17 Temurin + Maven) is the source of truth — keep it green.

## Recent Noise (looked at, ignored this run)

- react-router v7 future-flag warnings in frontend tests — informational, not actionable.

## Known-Good Baseline (verified 2026-06-28)

- **Frontend**: `npm run build` ✓ (178 modules) · `npm run lint` ✓ (0 warnings) · `vitest` ✓ (11/11).
- **Backend**: password-reset feature present; CI runs `mvn test` on JDK 17.
- **Mobile**: Flutter — verify via `flutter analyze` locally / CI.

## Loop Readiness

- Baseline before scaffolding: **35/100 (L0)**. See `loop-run-log.md` for the post-scaffold score.

---
Run log: see `loop-run-log.md` · Config: `LOOP.md` · Safety: `docs/safety.md`
