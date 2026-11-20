---
name: dejaview-feature
description: Implement a DejaView UC or Compose screen with MVVM state, real/demo adapters and regression tests. Use for product behavior changes.
---

# Feature

Read the role card, coverage matrix and relevant UC; distinguish the reduced sprint scope from the full PDF MVP. Use pipeline, code-style and testing. For UI, use design to resolve the actual frame before layout; do not assume a reference screen already exists.

Define one immutable UiState and user actions, including unknown/loading/empty/error distinctions. Separate Content from route/VM wiring. ViewModel depends on domain contracts; data adapters own DTO mapping. Use cases contain meaningful business rules rather than mandatory forwarding boilerplate.

Create deterministic Fake scenarios with synthetic data and a real adapter selected explicitly by DI. Production never silently switches to Fake. Navigation carries small IDs. Side effects have defined delivery/acknowledgement semantics across collector recreation; test pending actions and Back/cancel. Request IDs or equivalent ordering prevent stale responses and duplicate completion.

Integrate through existing contracts: only search shell records history, personal movie changes reach both list/card, and session invalidation is shared. Implement included alternative flows, not merely a happy screenshot. Verify main/error/cancel/restoration behavior and UI semantics. The result names completed AC, tests and intentionally deferred alternatives; partial UC completion is not the entire UC.
