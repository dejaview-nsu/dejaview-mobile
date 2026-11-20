---
name: dejaview-code-style
description: Write or review DejaView Kotlin and Compose code using MVVM, Clean Architecture and SOLID. Use for production code and structural refactors.
---

# Code Style

Read docs/engineering.md for the enforced project conventions and examples of boundaries. Apply the existing formatter configuration; use official Kotlin conventions where the repository is silent.

## Structure
Presentation owns Compose, ViewModel and UiState; domain owns business rules and consumer-facing interfaces; data owns transport/storage DTOs and adapters; DI wires implementations. Dependencies point toward domain. Feature implementations do not import one another. Android, Retrofit, Room and Compose types stay out of domain. Coroutines/Flow are allowed in domain contracts.

Use MVVM: stateless Content(state, onAction), a route collecting state with lifecycle, and a ViewModel exposing immutable StateFlow. ViewModel orchestrates domain operations and UI state; it does not own Activity/Context, DAO, Retrofit or navigation controller. Keep purely visual ephemeral state in UI. Persist only small restoration inputs, never credentials or media blobs.

SOLID means focused responsibilities, replaceable adapters, behaviorally equivalent fakes, narrow consumer contracts and constructor injection. Do not create BaseViewModel, generic BaseRepository, service locators, a UseCase per trivial method or interfaces for every class. Add an abstraction when a real rule, boundary or substitution requires it. Prefer composition; avoid inheritance for code reuse.

## Kotlin and runtime
Prefer val, private/internal visibility and immutable snapshots. Use descriptive domain names and sealed states for mutually exclusive outcomes. Avoid boolean parameter puzzles, nullable success/error pairs, unchecked casts and non-null assertions in production. Comments explain constraints or reasons; remove commented-out code and obsolete TODOs.

Use structured concurrency and injectable dispatchers/clocks for deterministic tests. Never GlobalScope, blocking I/O on Main or swallowing CancellationException in catch/runCatching. Shared mutations have explicit ordering; stale responses cannot restore a cleared session or replace a newer search. Close streams, cancel calls and release recorder/player resources in every terminal path.

UI strings use resources; tokens/components use core/ui. Keep side effects out of recomposition and avoid global mutable event buses. Validate at trust boundaries and map expected failures explicitly; do not catch every exception and return empty success. Logging excludes personal data and secrets.

## Review
Check dependency direction, responsibilities, public API size, cancellation, resource ownership and state restoration. Run formatter/lint/static analysis and relevant behavior tests. Split files by responsibility, not arbitrary line count. No unrelated reformatting or speculative optimization. Complexity warnings require simplification or a narrow documented exception, never a blanket suppression.
