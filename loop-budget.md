# Loop Budget — TaskHub

Token caps and kill switch for the maintenance loops. The loop checks this before acting and
appends to `loop-run-log.md` when a limit is hit.

## Daily limits

| Loop | Max runs/day | Max tokens/day | Max sub-agent spawns/run |
|------|--------------|----------------|--------------------------|
| Daily Triage | 2 | 100k | 0 (L1) / 2 (L2) |
| CI Sweeper | 24 | 300k | 0 (L1) / 2 (L2) |
| Dependency Sweeper | 1 | 200k | 0 (L1) / 2 (L2) |

## On budget exceed

1. Pause the scheduler (disable the GitHub Actions schedule or local automation).
2. Append an event to `loop-run-log.md`.
3. Notify the human via `STATE.md` → High Priority.

## Kill switch

- Flag: write `loop-pause-all` under **High Priority** in `STATE.md`, or disable the scheduled workflow.
- While paused, no loop may edit code or spawn sub-agents.
- Resume only after a human clears the flag in `STATE.md`.
