---
name: dejaview-contract
description: Change shared DejaView interfaces, routes and events with compatible migration of all consumers. Use for cross-feature contracts.
---

# Contract

Read contracts.md and owners. Search definition, all implementations/Fakes, DI bindings, previews/tests and call sites. Specify old-to-new behavior, null/unknown semantics, success/error/cancel and event scope. Avoid duplicate local models to bypass shared ownership.

Make the smallest justified interface change. Update interface and Fake together, migrate all consumers and real bindings, then docs. If temporary compatibility is needed, name the adapter owner and removal task; do not leave two sources of truth. Routes carry IDs, not secrets, URI or large DTOs.

Preserve single search-history writer/request deduplication, shared session state and consistent list/card state. Late responses cannot cross account scopes. Test consumer scenarios and compile affected demo/prod variants, not only the interface module.

Human review by an affected consumer is desirable, not a mandatory gate for this mobile project. Absent review requires explicit self-review/test evidence, not fabricated approval or waiting indefinitely. If consumers do not yet exist, provide a compileable contract/Fake/example and mark integration unverified. Update owner cards when responsibilities change.
