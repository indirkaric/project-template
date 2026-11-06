---
name: entity-and-liquibase-conventions
description: Use when creating or modifying a JPA entity, its Liquibase changelog, or its audit-field listener in this repo. Covers entity annotations, schema changes, and how createdBy/updatedBy get populated.
---

Full detail for the `entity/` layer referenced by `CLAUDE.md`'s package
layout. Read `CLAUDE.md` first for the placeholders and the overall map.

## Entities

- `entity/<Name>.java`, Lombok `@Data`, `@EqualsAndHashCode(callSuper = true,
  exclude = {"<collectionFields>"})`, `@ToString(callSuper = true, exclude =
  {"<collectionFields>"})`, `@EntityListeners(BaseEntityListener.class)`,
  explicit `@Table(name = "...", schema = "{{schema}}")`.
- `BaseEntityListener`'s `@PrePersist`/`@PreUpdate` set `createdBy`/
  `updatedBy`/`updatedUtc` by reading the current user from
  `IScopeContext` (see the `inter-service-propagation` skill) — entity
  listeners aren't Spring beans, so they fetch it via a static
  `BeanUtility.getBean(...)` lookup, not constructor injection.
- Schema changes go exclusively through Liquibase changelogs (per service's
  `src/main/resources/liquibase/`) — never `ddl-auto: update`.
- Liquibase changesets are additive only: never edit a changeset that has
  already shipped, always add a new one for the next change.
