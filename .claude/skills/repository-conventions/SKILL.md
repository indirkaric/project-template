---
name: repository-conventions
description: Use when creating or modifying the repository layer (I<Domain>Repository interface, its impl, or a read-path projection) in this repo.
---

Full detail for the `repository/` layer referenced by `CLAUDE.md`'s
package layout. Read `CLAUDE.md` first for the placeholders and the
overall map.

## Repository layer

- `modules/<domain>/repository/I<Domain>Repository` is a hand-written
  interface with domain-named methods (`findByEntityId`,
  `findPagedList(customerId, status, pageable)`, `existsByEntityId`) — it
  does NOT extend `JpaRepository` directly, so callers never see Spring
  Data's generic surface.
- `repository/impl/<Domain>Repository` implements it, wrapping the actual
  Spring Data JPA repository (or raw `EntityManager`/Criteria API if a
  query needs it).
- List/detail reads go through `repository/projection/` projections, not
  the full entity graph, to avoid over-fetching.
- `Check.isNotNull`/`isNotEmpty` on every public method parameter in the
  impl, same as every other layer.
