---
name: new-crud-module
description: Ordered checklist for scaffolding a brand-new, persistence-backed domain module (entity through controller) in this repo's layered style, per CLAUDE.md. Use when starting a module from zero and you want the checklist injected into the current conversation instead of delegating to a separate subagent.
---

Follow `CLAUDE.md` at the repo root for the package map and its "Agent
behavior" section (language to respond in, editing/git policy). For the
exact naming/annotation rules at each step, pull in
`entity-and-liquibase-conventions`, `repository-conventions`,
`service-layer-conventions`, `controller-conventions`,
`exception-handling-conventions`, and `testing-conventions` as you reach
each step — they'll trigger automatically, or invoke them directly.

This checklist is the ordering only — don't skip steps or do them out of
order, since later steps depend on earlier ones existing. This is for a
persistence-backed module; if the module is actually a pure
orchestration/proxy with no local entity, use `new-proxy-module` instead.

Steps 1–6 are root-level and shared; only steps 7–9 go under
`modules/<domain>/`. Every new package gets a `package-info.java`.

1. `entity/<Entity>.java`
2. Liquibase changeset adding the table, in the current versioned
   subdirectory of `src/main/resources/liquibase/`, included from
   `changelog.xml`
3. `repository/I<Domain>Repository` + `repository/impl/<Domain>Repository`
   (+ `repository/projection/` if the module needs list/detail reads)
4. `service/validator/I<Verb><Domain>ServiceValidator` + impl
5. `service/<domain>/I<Verb><Domain>Service` + impl (one per operation:
   create/modify/delete/read)
6. `mapper/I<Domain>Mapper` and `dto/<Domain>DTO`
7. `modules/<domain>/model/` (request/response/list records)
8. `modules/<domain>/service/I<Verb><Domain>ResponseCreatorService` + impl
9. `modules/<domain>/<Domain>Controller`
10. If this service uses the optional Kafka domain-event pattern: reuse
    the existing `DomainEventListener`/`DomainChangeEventListener` for the
    new event type — don't add a second one. Skip entirely if this
    service doesn't need async eventing.
11. Tests for every class added in steps 3-9, then `mvn -o verify`
    (tests + JaCoCo gate) and `mvn -o checkstyle:check`.

This is the same ordering `service-creator` follows when delegated to as a
subagent. Use this skill instead of that agent when you want to do the
scaffolding yourself, in the main conversation, with the checklist as a
guide rather than handing the whole task off.
