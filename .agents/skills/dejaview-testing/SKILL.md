---
name: dejaview-testing
description: Design and execute automated DejaView regression tests, coverage and CI gates. Use for behavior changes, bug fixes and test infrastructure.
---

# Testing

Read docs/testing.md and the task AC. Build a mapping from each criterion/risk to a named automated test and level. Prefer deterministic fakes at domain boundaries and real parsers/mappers/database adapters where their behavior matters; mocks must not merely repeat implementation details.

Test observable state and outcomes: success, empty, invalid input, error, retry, cancel, stale results, duplicate actions and restoration where applicable. Use controlled dispatchers/time, no arbitrary sleeps or real internet in unit tests. For StateFlow with WhileSubscribed establish a collector; assert stable states rather than every incidental emission.

For bugs, reproduce the issue before the fix and retain the regression. For new rules/state transitions, write tests alongside or before implementation and demonstrate they fail under a relevant defect where feasible. Do not require artificial tests for low-impact documentation edits, generated code or getters.

Run unit/mapper/contract tests, architecture checks, formatter/static analysis, demo/prod compilation and relevant Compose/instrumentation tests. Add navigation/accessibility and screenshot regressions for changed screens. Inspect screenshot diffs before accepting new baselines. API fixtures are synthetic and grounded in the selected schema; live smoke is a separate controlled check.

Use the project coverage policy, report covered and missed branches, and cover each changed business rule/error path. Coverage percentage alone is insufficient. Never exclude hard code, lower a threshold or replace assertions with snapshots to make CI green. Meaningful mutation checks are useful for critical pure logic, but do not add tooling without a dependency decision.

Inspect test counts, skipped tests and reports. Missing SDK/emulator/credentials is NOT RUN, not PASS. Automate device flows wherever reproducible; leave a focused device/visual check only for behavior the environment cannot validate. Attach exact remaining criteria rather than demanding a full manual regression every time.
