---
name: dejaview-module
description: Create or modify DejaView Gradle modules, source sets, build variants and DI. Use for scaffold and build configuration.
---

# Module

Read the owner task and docs/planning/contracts.md. Inspect wrapper/settings/catalog/convention plugins before editing. Dev 1 owns root wiring; coordinate the smallest necessary migration without waiting for redundant approval.

At scaffold, establish MVVM presentation/domain/data/di packages, compatible pinned toolchain, demo/prod variants and compileable interface entrypoints. Do not create empty per-layer Gradle modules or boilerplate use cases. Domain must remain independent of Android adapters; dependency graph is acyclic. App wires feature implementations; feature-to-feature implementation dependencies are forbidden.

Use dejaview-dependencies for toolchain/library selection. Register verified quality tasks in config/quality-gate.json as described in docs/testing.md. Add real formatter, lint/static analysis, unit tests, coverage and dependency-boundary checks. Compile both variants and validate unique Hilt bindings. Production must not depend on core/testing or demo source sets.

Discover actual Gradle tasks rather than guessing variant names. A missing SDK or wrapper cannot be replaced by a script that reports success. For the initial scaffold, include an emulator smoke and prove that a representative violating dependency/test fails the gate. Add only needed manifest permissions/exported components. Keep build outputs ignored and retain the trusted Gradle wrapper through a narrow reviewed artifact policy, not a general binary allowlist.
