---
name: entity-and-liquibase-conventions
description: Use when creating or modifying a JPA entity, its Liquibase changelog, its audit fields, or the datasource configuration in this repo.
---

## Entities

- `entity/<Name>.java` extends `entity/common/ExternalEntity`: internal `id`, public `entity_id`,
  and the audit columns. Use `AuditEntity` only for rows with no public id, such as join tables.
- Lombok `@Data`, `@EqualsAndHashCode(callSuper = true, exclude = {<collections>})`,
  `@ToString(callSuper = true, exclude = {<collections>})`,
  `@EntityListeners(BaseEntityListener.class)`, `@Table(name = "...", schema = "{{schema}}")`.
- Never expose the internal `id` over the API. Lookups and responses use `entityId`.
- `BaseEntityListener` fills `createdBy`/`createdUtc`/`updatedBy`/`updatedUtc` from
  `AuditContext`. Don't set them by hand.

## Liquibase

- Root `liquibase/changelog.xml` includes `0.0.1-SNAPSHOT/changelog.xml`, which includes one
  formatted-SQL file per change (`create_<table>_table.sql`,
  `add_column_<col>_to_<table>_table.sql`). Each file starts with `--liquibase formatted sql` and
  `--changeset {{service-name}}:<id>` and ends with a `--rollback` line.
- Tables live in schema `{{schema}}`: `id int8 GENERATED ALWAYS AS IDENTITY`,
  `entity_id varchar NOT NULL` with a unique index, then the four audit columns.
- Additive only. Never edit a changeset that has run; never `ddl-auto: update`.

## Datasource

`database.readwrite` / `database.readonly` in `application.yaml`, not `spring.datasource`.
`DataSourceRouting` uses the read-write pool only inside a read-write transaction.
`TransactionDefinitionInterceptor` detects `@Transactional` on the method **or** the class.
Read services must therefore be `@Transactional(readOnly = true)`: that annotation is the routing
decision.
