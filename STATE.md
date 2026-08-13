# Loop State — TaskHub

> Durable memory spine for the maintenance loops. The loop reads this at the start of every run and
> writes back what it learned. Humans skim the top section daily.

Last run: 2026-06-30 (L2 Daily Triage — first auto-fix run; human enabled L2 this session)

## High Priority (loop is acting or waiting on human)

- **L2 ENABLED (2026-06-30).** Human flipped Daily Triage L1→L2 this session. Auto-fix now allowed
  **only** within `docs/safety.md` "Allowed (L2)" column (EOL normalization, comment typos, import
  ordering, test-file lint). Everything else still escalates.
- **ESCALATE — one-time `git add --renormalize .` pending.** `.gitattributes` (eol=lf) was added this
  run to stop future CRLF churn, but the bulk renormalize was **not** run because the working tree has
  uncommitted WIP (see below). Run it on a clean tree: `git add --renormalize . && git commit`.
- **ESCALATE — uncommitted frontend WIP in working tree.** 8 files changed (118+/20-), real UI work,
  **not** CRLF: `Landing.jsx`, `Login.jsx`, `index.css` (+66), `tailwind.config.js` (+20),
  `Layout.jsx`, `ForgotPassword/Register/ResetPassword.jsx`, plus untracked `PageTransition.jsx`,
  `frontend/public/`, `vite.config.js`. Loop did **not** touch these. Human should review/commit so
  the loop can run maker/checker on a clean tree.
- **HELD (human review) — npm dep bumps.** `docs/safety.md` puts minor bumps + lockfile changes in the
  "Never" column, overriding the STATE watch-list note. Not auto-applied: axios 1.16.1→1.18.1,
  autoprefixer 10.4.27→10.5.2, postcss 8.5.14→8.5.15, react-router-dom 6.30.3→6.30.4.

## Watch List (monitor, do not act yet)

- **Dependency drift (frontend npm).** Safe patch/minor bumps available — candidates for the
  Dependency Sweeper at L2: `autoprefixer 10.4.27→10.5.2`, `axios 1.16.1→1.18.1`,
  `postcss 8.5.14→8.5.15`, `react-router-dom 6.30.3→6.30.4`. **Held behind human gate** (majors):
  react 18→19, eslint 8→10, tailwind 3→4, react-leaflet 4→5, @vitejs/plugin-react 4→6, jsdom 24→29.
- **CRLF line-ending churn.** `.gitattributes` (eol=lf) **added 2026-06-30** — stops churn for files
  touched going forward. The one-time bulk `git add --renormalize .` is still pending (blocked on the
  uncommitted WIP above); see High Priority.
- **Backend/mobile not buildable in the local sandbox** (no JDK17/Maven/Flutter). CI
  (`.github/workflows/ci.yml`, JDK 17 Temurin + Maven) is the source of truth — keep it green.

## Recent Noise (looked at, ignored this run)

- react-router v7 future-flag warnings in frontend tests — informational, not actionable.

## Known-Good Baseline (verified 2026-06-30)

- **Frontend**: `npm run build` ✓ (built in 3.32s) · `npm run lint` ✓ (0 warnings) · `vitest` ✓ (11/11)
  — re-verified after adding `.gitattributes` (no regression).
- **Backend**: password-reset feature present; CI runs `mvn test` on JDK 17.
- **Mobile**: Flutter — verify via `flutter analyze` locally / CI.

## Loop Readiness

- Baseline before scaffolding: **35/100 (L0)**. Post-scaffold: **L3, 100/100** (commit `e081c80`).
- **2026-06-30: graduated to L2** (human-enabled). First auto-fix shipped: `.gitattributes` EOL policy,
  gate green, maker/checker APPROVE. See `loop-run-log.md`.

---
Run log: see `loop-run-log.md` · Config: `LOOP.md` · Safety: `docs/safety.md`
