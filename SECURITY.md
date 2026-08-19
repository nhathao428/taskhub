# Security Policy

TaskHub handles authentication tokens and business data for small companies,
so a vulnerability here has real consequences for whoever runs it.

## Reporting a vulnerability

**Do not open a public issue.**

Use GitHub private reporting: go to the
[Security tab](https://github.com/nhathao428/taskhub/security) and click
**Report a vulnerability**. Alternatively, email **nhathao152k5@gmail.com**
with `taskhub security` in the subject.

### What to include

- Affected component: backend, frontend, or deployment configuration
- Steps to reproduce, with the smallest request that shows the problem
- What an attacker gains

### What to expect

This is a student project maintained alongside university. Realistic
expectation: acknowledgement within a few days, a fix or an explanation within
two weeks.

## Areas of particular concern

- **JWT handling** - token signing, expiry, and refresh logic
- **Authorisation** - whether one tenant can read another tenant's tasks
- **Flyway migrations** - anything that drops or exposes data on upgrade
- **Redis cache** - data leaking between users through a shared cache key
- **`.env.example`** - if a real secret ever appears there, that is a bug

## Out of scope

- Vulnerabilities in Spring Boot, PostgreSQL, or Redis themselves; report
  those upstream
- Findings that require access to the deployment environment you already
  control
- Missing hardening on a local development setup started with `start.sh`
