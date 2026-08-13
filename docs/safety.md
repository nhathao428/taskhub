# Safety & Guardrails — TaskHub

Loops amplify judgment — good and bad. Minimum bar for any loop that touches TaskHub code or
external systems.

## Path Denylist

The loop must **never** auto-edit these without explicit human approval — escalate with context:

```
.env
.env.*
.evn
**/application.yml
**/application*.yaml
**/application*.properties          # DB / Redis / Gemini config & secrets
backend/src/main/resources/db/migration/V*.sql   # Flyway schema migrations
**/security/**
**/config/SecurityConfig.*
**/*Jwt*                            # JwtUtil / JwtFilter / token logic
**/*secret*
**/*_key*
docker-compose*.yml
render.yaml
Caddyfile*
```

Encode in every implementer/fix skill:
> Do not modify files matching the denylist. Escalate to a human with full context.

## Secrets

- The Gemini API key lives in `.env` (gitignored), injected via the backend only. **Never** echo,
  commit, or expose it to frontend/mobile. A leaked key was scrubbed once (commit `7c62603`) — do
  not regress.
- Loops never read or write production credentials.

## Auto-Merge Policy

**Default: no auto-merge.** TaskHub is a coursework/portfolio repo with a single maintainer.

| Allowed (L2, after human enables) | Never |
|-----------------------------------|-------|
| Typos in comments/docs            | Behavior changes |
| Lint auto-fix in test files only  | Dependency major/minor bumps |
| Import ordering                   | Lockfile changes without review |
| `.gitattributes` EOL normalization (one-time) | Any denylist path |

## MCP Connector Least Privilege

| Connector | Read | Write |
|-----------|------|-------|
| GitHub | issues, PRs, checks | comment, label (not merge) |
| Database (Postgres) | — | no production write from loops |
| Gemini | — | backend-only, never from a loop |

Use a separate bot token with minimal scopes when a loop reaches L2+.

## Human Gates (required)

1. Anything touching auth/JWT, DB migrations, secrets, or deploy config.
2. Multi-file refactors or architectural changes.
3. More than `max-fix-attempts` (2) on the same item → stop and escalate.
4. Verifier returns `ESCALATE_HUMAN` (e.g. tests can't run) → stop.

## Kill Switch

Set `loop-pause-all` in `STATE.md` High Priority (or disable the scheduled workflow). Resume only
after a human clears the flag. See `loop-budget.md`.
