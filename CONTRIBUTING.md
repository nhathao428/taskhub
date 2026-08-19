# Contributing to TaskHub

## Project layout

| Directory | What it is |
| --- | --- |
| `backend/` | Spring Boot 3.5 API, Java 17, PostgreSQL + Flyway + Redis |
| `frontend/` | Vite single-page app, Tailwind |
| `mobile/` | Mobile client |
| `ml/` | Machine learning components |
| `docs/` | Design and reference documentation |

## Running it locally

```bash
cp .env.example .env    # then fill in the values
./start.sh              # start.ps1 on Windows
```

The backend falls back to H2 when PostgreSQL is not running, so you can work
on API code without Docker. Integration tests that need a real database use
Testcontainers and skip themselves automatically when Docker is unavailable.

## Running the tests

```bash
cd backend && mvn test
cd frontend && npm ci && npm run lint && npm run build
```

CI runs exactly these commands. If they pass locally they will pass there.

## Standards for a change

- **A behaviour change needs a test.** For anything touching authentication or
  authorisation this is required, not preferred.
- **Schema changes go through Flyway.** Add a new versioned migration; never
  edit a migration that has already been applied.
- **No secrets in the repository.** `.env.example` holds placeholder values
  only. If you need a new setting, add it there with an obviously fake value.
- **Keep the API documented.** The project exposes Swagger UI through
  springdoc; new endpoints should carry the annotations that make them show up.

## Commit messages

Conventional-commit style:

```
feat: add task assignment endpoint
fix: reject expired refresh tokens
test: cover tenant isolation on task listing
```

## Reporting a vulnerability

See [SECURITY.md](SECURITY.md). Do not open a public issue.
