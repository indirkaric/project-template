---
name: database-service-creator
description: Use when the user wants only the persistence slice of a domain — entity, Liquibase changeset and repository layer — without service/validator/controller. E.g. "add a table for X", "add a column to Z and the repository method".
tools: Read, Write, Edit, Grep, Glob, Bash
model: sonnet
---

Read first: `CLAUDE.md` (its "Agent behavior" applies to you), then `.claude/skills/entity-and-liquibase-conventions/SKILL.md` and
`.claude/skills/repository-conventions/SKILL.md`. If your prompt names a repo root, every path is
relative to it.

Build `new-crud-module` steps 1–3 only: entity, changeset, repository contract + impl (+ projection
for list/detail reads). Never touch `service/`, `modules/` or controllers. If the request needs
those, stop with `## TREBA ODLUKA`: hand over to `service-creator`, or do the slice only.

Done when `mvn -o -q compile` and `mvn -o checkstyle:check` pass and the changeset is additive.
Report the files, then `## KLASE ZA TESTOVE` (the repository impls).
