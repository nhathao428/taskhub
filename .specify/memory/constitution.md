<!--
SYNC IMPACT REPORT
==================
Version change: 1.0.0 → 1.1.0
Bump rationale: MINOR — Principle I clarified to accept URL-pattern
authorization in SecurityFilterChain as an equivalent alternative to
method-level @PreAuthorize. Quality & Operational Standards updated
accordingly. No principle removed or redefined incompatibly, so MINOR
(not MAJOR).

Previous version (1.0.0): initial ratification with four principles
(Code Quality, Testing Standards, UX Consistency, Performance
Requirements) on 2026-05-27.

Modified principles:
  I. Code Quality — broadened the authorization clause: URL-pattern
     rules in SecurityFilterChain are now explicitly allowed alongside
     @PreAuthorize, on the condition that the SecurityFilterChain rule
     set is comprehensive and reviewed.

Added sections: None.
Removed sections: None.

Templates / dependent artifacts reviewed:
  ✅ .specify/templates/plan-template.md — Constitution Check gate
        references this file generically; no edits required.
  ✅ .specify/templates/spec-template.md — no constitution-specific
        content; no edits required.
  ✅ .specify/templates/tasks-template.md — no constitution-specific
        content; no edits required.
  ✅ .specify/templates/checklist-template.md — no constitution-specific
        content; no edits required.
  ✅ CLAUDE.md — existing conventions remain compatible; no edits
        required.

Follow-up TODOs: None.
-->

# Task Management System Constitution

## Core Principles

### I. Code Quality

All code MUST be readable, consistent, and reviewable before it is merged.

- Every change MUST pass static checks before review: backend `mvn verify`
  (compile + checkstyle/spotbugs if configured), frontend `npm run lint`
  and `npm run build`, mobile `flutter analyze`. CI MUST block merges that
  fail any of these.
- Naming and structure MUST follow language idioms: Java packages and
  classes per the existing `backend/src/main/java/...` layout, React
  components in PascalCase, Dart files in snake_case, database tables and
  columns in `snake_case`.
- New API endpoints MUST return the project's `ApiResponse<T>` wrapper and
  surface failures through the existing `@RestControllerAdvice` — no
  ad-hoc error shapes.
- Secrets, connection strings, and API keys (including the Gemini key)
  MUST be read from environment variables or `application.yml`; hardcoding
  them is forbidden.
- Pull requests MUST be reviewed by at least one engineer other than the
  author. Reviewers MUST reject changes that introduce dead code,
  unjustified dependencies, or duplicated logic that already exists in
  `ApiResponse`, the Axios JWT interceptor, or shared Flutter services.
- Authorization on REST endpoints MAY be expressed either through
  method-level `@PreAuthorize` annotations OR through URL-pattern rules
  (`requestMatchers(...).hasAnyRole(...)`) in `SecurityFilterChain`. The
  chosen style MUST be applied consistently project-wide, the rule set
  MUST cover every non-public endpoint, and the default catch-all MUST
  be `.anyRequest().authenticated()`. Ad-hoc role checks scattered
  inside handler methods remain forbidden.

**Rationale**: This is a three-tier system (Spring Boot + React + Flutter)
maintained by a small team; consistency and machine-enforced gates are the
cheapest way to keep the surface area reviewable as it grows.

### II. Testing Standards (NON-NEGOTIABLE)

Tests MUST exist for every behavior the system promises, and the suite
MUST be green before merge.

- Backend: every service class and REST controller MUST have unit tests
  using JUnit 5 + Mockito. PowerMock is forbidden. Authorization-bearing
  endpoints (`@PreAuthorize`) MUST have at least one test that exercises
  the permitted role and one that asserts a 401/403 for an unauthorized
  caller.
- Backend integration: any change that touches JPA entities, repositories,
  or SQL MUST include a `@SpringBootTest` (or `@DataJpaTest`) that runs
  against PostgreSQL via Testcontainers or an embedded equivalent — never
  against an in-memory dialect that diverges from PostgreSQL 16 behavior.
- Frontend: components with conditional rendering, form validation, or
  data fetching MUST have tests (Vitest / React Testing Library). The
  Axios JWT interceptor and any new auth-gated route MUST have a test
  covering the unauthenticated path.
- Mobile: Dart files with branching logic MUST ship with widget or unit
  tests (`flutter test`). Null-safety violations (`?`, `!`, `late`) MUST
  be caught by `flutter analyze` with zero warnings.
- Bug fixes MUST add a regression test that fails before the fix and
  passes after. "Fixed by inspection" is not acceptable.
- The full test suite MUST pass locally and in CI before merge. Skipped
  or `@Disabled` tests MUST link to a tracking issue in the commit
  message.

**Rationale**: The system spans three runtimes; without enforced test
coverage at each layer, regressions silently cross the
backend → frontend / mobile boundary and surface in production.

### III. User Experience Consistency

The product MUST feel like one product across the React frontend and the
Flutter mobile app.

- Visual language: the frontend MUST use Tailwind utility classes from
  the shared design tokens (colors, spacing, typography). Mobile MUST
  derive its theme from the same token set; ad-hoc color or spacing
  literals are forbidden in both clients.
- Navigation: frontend routing MUST use React Router v6 primitives
  (`<Outlet>`, `useNavigate`); `useHistory` is forbidden. Mobile
  navigation MUST go through the project's existing router service —
  no direct `Navigator.push` in feature widgets.
- Forms and feedback: every async action (create / update / delete) MUST
  show a loading state, a success confirmation, and an actionable error
  message derived from `ApiResponse.error`. Silent failures are
  forbidden.
- Accessibility: all interactive elements MUST be keyboard reachable on
  web and screen-reader labeled on mobile. Color contrast MUST meet
  WCAG 2.1 AA for text and essential icons.
- Localization: user-facing strings MUST go through the project's i18n
  layer (frontend i18n catalog, Flutter `AppLocalizations`). Hard-coded
  Vietnamese or English strings in components are not allowed.
- Charts (Chart.js) MUST register their components once at module load
  and share the project's chart color palette so reports look identical
  to dashboards.

**Rationale**: Two clients written in different stacks drift fast.
Enforcing a single token set, a single error/loading contract, and
shared navigation primitives keeps the perceived product coherent.

### IV. Performance Requirements

The system MUST be fast enough to feel instant under realistic load.

- Backend API SLOs (measured at p95 on the production tier):
  - Read endpoints: ≤ 200 ms.
  - Write endpoints: ≤ 400 ms.
  - Gemini-backed endpoints (AI assist, summarization): ≤ 3 s end-to-end;
    they MUST be invoked asynchronously from the UI with a visible
    progress indicator.
- Database: every query that runs on a request path MUST be covered by
  an index that makes its `EXPLAIN ANALYZE` cost sub-linear in table
  size for filtered columns. N+1 queries are forbidden — use JPA fetch
  joins or DTO projections.
- Caching: read endpoints whose result is stable for ≥ 30 seconds MUST
  use `@Cacheable` against Redis with an explicit TTL; mutations MUST
  pair with `@CacheEvict` for the same key space. The Gemini API key
  MUST never be exposed beyond the backend; AI responses that are
  deterministic for a given input MUST be cached.
- Frontend: initial route bundle MUST stay ≤ 250 KB gzipped. Routes MUST
  be code-split with `React.lazy` / dynamic `import()`. Images MUST be
  served at the displayed resolution and lazy-loaded below the fold.
- Mobile: cold start to first interactive frame MUST be ≤ 2 s on a
  reference mid-range device. Lists MUST use `ListView.builder` (or
  equivalent virtualized widget) for any data set that can exceed 50
  items.
- Performance regressions: any PR that increases p95 latency on a
  benchmarked endpoint by > 10 %, or the frontend bundle by > 5 %, MUST
  be justified in the PR description or rejected.

**Rationale**: This is a productivity tool — perceived slowness directly
undermines its value. Setting explicit, measurable budgets at every tier
turns "fast" from an aspiration into a reviewable property.

## Quality & Operational Standards

- **Stack lock**: the canonical stack is Spring Boot 3.5.0 on Java 17+,
  React 18 + Vite 5, Flutter 3.x, PostgreSQL 16, Redis 7. Adding a new
  runtime, framework, or major library requires an amendment to this
  constitution (see Governance).
- **Dependencies**: a new backend dependency MUST be added only when no
  existing dependency covers the need. New Maven, npm, or pub
  dependencies MUST be pinned to a specific version and noted in the PR
  description with a one-line rationale.
- **Database changes**: schema changes MUST go through Flyway or
  Liquibase (whichever is already wired). Ad-hoc `ALTER TABLE`
  statements applied directly to environments are forbidden.
- **Security**: JWT MUST use the jjwt 0.12.x builder API (`Jwts.builder()`);
  deprecated APIs are forbidden. Auth-bearing endpoints MUST be
  protected either by `@PreAuthorize` or by URL-pattern rules in
  `SecurityFilterChain` (see Principle I) — not by ad-hoc checks inside
  handlers. Sensitive logs (tokens, passwords, PII) MUST be redacted.
- **Containers**: local development MUST work via `docker-compose up -d`
  using `.env` derived from `.env.example`. Any new service required for
  local development MUST be added to `docker-compose.yml` with sensible
  defaults.
- **Documentation**: any change to public API contracts, environment
  variables, or developer onboarding steps MUST be reflected in `README.md`
  or `/docs/` in the same PR.

## Development Workflow & Quality Gates

- **Branching**: feature work happens on branches created via
  `/speckit-git-feature`. Direct commits to `main` are forbidden except
  for documentation-only fixes from a maintainer.
- **PR checklist (enforced by reviewer)**:
  1. Lint, type-check, and test suites pass for every affected tier
     (backend, frontend, mobile) — not just the tier that changed.
  2. New behavior is covered by tests at the appropriate layer (Principle II).
  3. UI changes include before/after screenshots or a recording for both
     the React app and, if affected, the Flutter app.
  4. Performance-sensitive paths include a measurement (latency, bundle
     size, query plan) in the PR description.
  5. The spec-kit artifacts for the feature (`spec.md`, `plan.md`,
     `tasks.md`) are updated and committed alongside code.
- **Constitution Check gate** (used by `/speckit-plan`): every plan MUST
  evaluate the four principles above. Violations MUST be either resolved
  before Phase 0 or recorded in the plan's Complexity Tracking section
  with an explicit justification and rollback strategy.
- **Release**: a release MUST run the full test suite, perform a smoke
  test against a staging environment using the production
  `docker-compose.prod.yml` profile, and tag the commit. Hotfixes follow
  the same gates with the smoke test scope reduced to the affected area.

## Governance

This constitution is the project's highest-authority development
document. It supersedes individual preferences and any conflicting
guidance in `CLAUDE.md`, README files, or comments in code. `CLAUDE.md`
remains the day-to-day runtime guidance file for Claude Code and MUST
remain consistent with this document; on conflict, this document wins.

**Amendment procedure**:

1. Propose the change in a PR that edits `.specify/memory/constitution.md`.
2. The PR description MUST state which principle or section is changing,
   the rationale, and any migration steps for code that is now
   non-compliant.
3. The PR MUST be reviewed and approved by at least one maintainer
   other than the proposer.
4. Merge updates this file and triggers a follow-up sweep of
   `.specify/templates/*.md` and `CLAUDE.md` for alignment.

**Versioning policy** (semantic):

- **MAJOR**: backward-incompatible removal or redefinition of a
  principle or governance rule.
- **MINOR**: a new principle, section, or materially expanded guidance.
- **PATCH**: clarifications, wording, typo fixes, or non-semantic
  refinements.

**Compliance review**: every PR review MUST verify compliance with the
four core principles. Repeated, unjustified violations are grounds for
reverting the offending change. Quarterly, the maintainers MUST audit a
random sample of merged PRs against this constitution and file
follow-up tasks for any drift discovered.

**Version**: 1.1.0 | **Ratified**: 2026-05-27 | **Last Amended**: 2026-05-27
