---
name: dejaview-pr
description: Self-review and prepare a small verified DejaView change for handoff or an authorized PR. Use after implementation and before publication.
---

# Pr

Inspect final diff/status against the task AC, not merely the original plan. One PR should deliver one coherent behavior or prerequisite, normally about 200–500 authored changed lines; generated files/fixtures are separate. Larger changes need a split or concrete cohesion explanation, not arbitrary file-count gaming. No unrelated formatting, dependency upgrades or cleanup.

Run code-style/testing for code. Verify dependency direction, MVVM state ownership, cancellation, DI isolation and real adapter evidence. Review the tests for meaningful assertions and coverage of included alternatives. Inspect skipped/zero-test reports and screenshot changes. Check privacy and unexpected tracked artifacts.

Use python scripts/quality_gate.py --scope repository before scaffold; Android changes require --scope android after its real commands are configured. For UI, run relevant device/screenshot tests too. A passing repository gate is not an Android build. Never weaken checks or label unavailable checks passed.

Review your own change in a separate pass with tests/results in view. Human review is recommended; lack of reviewer does not block local completion or imply automatic merge. Do not message people, publish, push or merge unless that action is authorized. Report absent review plainly.

PR description: concrete before/after behavior, task/UC or chat criteria (no Redmine required), AC-to-test evidence, affected contracts/dependencies and material limitations. Link sanitized evidence only. Before staging inspect diff; afterwards run privacy --staged. Handoff states completed criteria, actual commands and remaining blockers; no invented approvals or guarantees.
