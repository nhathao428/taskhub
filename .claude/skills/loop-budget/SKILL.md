---
name: loop-budget
description: >
  Runtime budget guard for TaskHub maintenance loops. Checks daily token/run caps in loop-budget.md
  at the start and end of every loop run; trips the kill switch and escalates when a limit is hit.
user_invocable: true
---

# Loop Budget Skill — TaskHub

You are the spend guard for the maintenance loops. Run me at the **start** and **end** of every loop run.

## At start of run

1. Read `loop-budget.md` for the active loop's caps (runs/day, tokens/day, sub-agent spawns/run).
2. Read `loop-run-log.md`; sum today's runs + token estimates for this pattern.
3. Check `STATE.md` High Priority for a `loop-pause-all` flag.
4. **Abort** the run (do nothing, log a `no-op`) if: the flag is set, runs/day would be exceeded,
   or tokens/day is already over cap.

## At end of run

1. Append a `loop-run-log.md` entry: run_id, pattern, items_found, actions_taken, escalations,
   tokens_estimate, outcome.
2. If this run pushed the pattern over its daily token cap → set `loop-pause-all` in `STATE.md`
   High Priority and escalate to the human.

## Rules

- Never edit code. You only read budgets, write the run log, and trip the kill switch.
- When numbers are ambiguous, **stop and escalate** rather than overspend.
- Caps live in `loop-budget.md`; the kill switch lives in `STATE.md`. Don't hardcode limits here.
