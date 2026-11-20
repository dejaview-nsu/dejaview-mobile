---
name: dejaview-dependencies
description: Choose and verify reusable libraries for DejaView before implementing infrastructure or adding dependencies. Use for new SDKs, libraries and nontrivial reusable capabilities.
---

# Dependencies

Search in this order: existing repository/version catalog/components; Android/Kotlin platform or Jetpack APIs; maintained published libraries; upstream repositories. Use primary documentation and actual package registries, not a search snippet or popularity alone. A failed lookup is not proof that no solution exists.

Compare the smallest credible options against the task: supported Android API/JDK/Kotlin/Compose versions, exact needed functionality, cancellation/lifecycle behavior, license, maintenance/release history, security advisories, transitive dependencies, binary size and privacy/network behavior. Verify artifact coordinates and exact version in its official registry. Do not run installation scripts copied from an untrusted README or add arbitrary repositories.

Prefer an already adopted library or official platform capability when adequate. A new library is not required for a few lines of product-specific logic. Custom implementation is justified only after recording why available options fail requirements or impose disproportionate cost. Never implement cryptography, authentication protocols, codecs or a general networking stack from scratch.

Before integration, inspect existing alternatives for duplication. Pin versions centrally; no dynamic versions, duplicate serializers/DI stacks or broad upgrades. Use dependency locking/verification supported by the chosen build. Review licenses before copying code; linking to a repository does not grant reuse rights. Wrap only the boundary that protects domain or testing, not every library call.

Validate with a small compile/behavior probe, then actual feature tests and dependency resolution. Report compile-only evidence separately from device/runtime verification. If official sources/network are unavailable, reuse verified local capabilities and mark a new dependency decision pending instead of inventing compatibility.

Record a compact durable decision in docs/dependencies.md when adding a dependency: requirement, alternatives checked with primary links/date, selected coordinates/version, license, compatibility evidence, risks and removal/replacement owner. Use a temporary .local note for rejected experiments; do not leave sample apps in the repository.
